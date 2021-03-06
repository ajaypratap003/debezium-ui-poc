package io.debezium.configserver;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.debezium.configserver.rest.ConnectorResource;
import io.debezium.configserver.util.Infrastructure;
import io.debezium.configserver.util.PostgresInfrastructureTestProfile;
import io.debezium.testing.testcontainers.ConnectorConfigurationTestingHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;

@QuarkusTest
@TestProfile(PostgresInfrastructureTestProfile.class)
public class ValidatePostgresConnectionIT {

    @Test
    public void testValidPostgresConnection() {
        ObjectNode config = ConnectorConfigurationTestingHelper.getConfig(
                Infrastructure.getPostgresConnectorConfiguration(1)
                    .with("database.hostname", "localhost")
                    .with("database.port", Infrastructure.getPostgresContainer().getMappedPort(5432))
        );

        given().when().contentType(ContentType.JSON).accept(ContentType.JSON).body(config.toString())
            .post(ConnectorResource.API_PREFIX + ConnectorResource.CONNECTION_VALIDATION_ENDPOINT, "postgres")
            .then().log().all()
            .statusCode(200)
            .assertThat().body("status", equalTo("VALID"))
                .body("genericValidationResults.size()", is(0))
                .body("propertyValidationResults.size()", is(0));
    }

    @Test
    public void testInvalidHostnamePostgresConnection() {
        ObjectNode config = ConnectorConfigurationTestingHelper.getConfig(
                Infrastructure.getPostgresConnectorConfiguration(1)
                    .with("database.hostname", "zzzzzzzzzz"));

        given().when().contentType(ContentType.JSON).accept(ContentType.JSON).body(config.toString())
                .post(ConnectorResource.API_PREFIX + ConnectorResource.CONNECTION_VALIDATION_ENDPOINT, "postgres")
                .then().log().all()
                .statusCode(200)
                .assertThat().body("status", equalTo("INVALID"))
                .body("genericValidationResults.size()", is(0))
                .body("propertyValidationResults.size()", is(1))
                .rootPath("propertyValidationResults[0]")
                    .body("property", equalTo("database.hostname"))
                    .body("message", startsWith("Unable to connect:"));
    }

    @Test
    public void testInvalidPostgresConnection() {
        given().when().contentType(ContentType.JSON).accept(ContentType.JSON).body("{\"connector.class\":\"io.debezium.connector.postgresql.PostgresConnector\"}")
                .post(ConnectorResource.API_PREFIX + ConnectorResource.CONNECTION_VALIDATION_ENDPOINT, "postgres")
            .then().log().all()
            .statusCode(200)
            .assertThat().body("status", equalTo("INVALID"))
                .body("genericValidationResults.size()", is(0))
                .body("propertyValidationResults.size()", is(4))
                .body("propertyValidationResults",
                    hasItems(
                        Map.of("property", "database.user", "message", "A value is required"),
                        Map.of("property", "database.dbname", "message", "A value is required"),
                        Map.of("property", "database.server.name", "message", "A value is required"),
                        Map.of("property", "database.hostname", "message", "A value is required")
                    ));
    }

}
