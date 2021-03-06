import { Connector } from "@debezium/ui-models";
import { Services } from "@debezium/ui-services";
import {
  Button,
  DataList,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Flex,
  FlexItem,
  Title
} from "@patternfly/react-core";
import { CubesIcon } from "@patternfly/react-icons";
import React from "react";
import { useHistory } from "react-router-dom";
import { PageLoader } from "src/app/components";
import { AppLayoutContext } from 'src/app/Layout/AppLayoutContext';
import { ApiError, fetch_retry } from "src/app/shared";
import { WithLoader } from "src/app/shared/WithLoader";
import { ConnectorListItem } from "./ConnectorListItem";
import "./ConnectorsPage.css";

/**
 * Sorts the connectors by name.
 * @param connectors
 */
function getSortedConnectors(connectors: Connector[]) {
  const sortedConns: Connector[] = connectors.sort((thisConn, thatConn) => {
    return thisConn.name.localeCompare(thatConn.name);
  });

  return sortedConns;
}

export const ConnectorsPage: React.FunctionComponent = (props) => {
  const appLayoutContext = React.useContext(AppLayoutContext);
  const [connectors, setConnectors] = React.useState<Connector[]>([] as Connector[]);

  const [loading, setLoading] = React.useState(true);
  const [apiError, setApiError] = React.useState<boolean>(false);
  const [errorMsg, setErrorMsg] = React.useState<Error>(new Error());
  const history = useHistory();

  const createConnector = () => {
    const connectorNames = connectors.map( (conn) => {
      return conn.name;
    });
    history.push({
      pathname: "/app/create-connector",
      state: { value: appLayoutContext.clusterId, connectorNames },
    });
  };

  const getConnectorsList = () =>{
    const connectorService = Services.getConnectorService();
    fetch_retry(connectorService.getConnectors, connectorService, [
      appLayoutContext.clusterId,
    ])
      .then((cConnectors: Connector[]) => {
        setLoading(false);
        setConnectors([...cConnectors]);
      })
      .catch((err: React.SetStateAction<Error>) => {
        setApiError(true);
        setErrorMsg(err);
      });
  }

  React.useEffect(() => {
    const getConnectorsInterval = setInterval(() => getConnectorsList(), 10000);
    return () => clearInterval(getConnectorsInterval);;
  },[]);

  return (
    <WithLoader
      error={apiError}
      loading={loading}
      loaderChildren={<PageLoader />}
      errorChildren={<ApiError error={errorMsg} />}
    >
      {() => (
        <>
          {connectors.length > 0 ? (
            <>
              <Flex className="connectors-page_toolbarFlex">
                <FlexItem>
                  <Title headingLevel={"h1"}>Connectors</Title>
                </FlexItem>
                <FlexItem>
                  <Button
                    variant="primary"
                    onClick={createConnector}
                    className="connectors-page_toolbarCreateButton"
                  >
                    Create a connector
                  </Button>
                </FlexItem>
              </Flex>
              <DataList aria-label={"connector list"} className="connectors-page_dataList">
                {getSortedConnectors(connectors).map((conn, index) => {
                  return (
                    <ConnectorListItem
                      key={index}
                      name={conn.name}
                      type={conn.connectorType}
                      status={conn.connectorStatus}
                      taskStates={conn.taskStates}
                    />
                  );
                })}
              </DataList>
            </>
          ) : (
            <EmptyState variant={EmptyStateVariant.large}>
              <EmptyStateIcon icon={CubesIcon} />
              <Title headingLevel="h4" size="lg">
                No connectors
              </Title>
              <EmptyStateBody>
                Please click 'Create a connector' to create a new connector.
              </EmptyStateBody>
              <Button
                onClick={createConnector}
                variant="primary"
                className="connectors-page_createButton"
              >
                Create a connector
              </Button>
            </EmptyState>
          )}
        </>
      )}
    </WithLoader>
  );
};
