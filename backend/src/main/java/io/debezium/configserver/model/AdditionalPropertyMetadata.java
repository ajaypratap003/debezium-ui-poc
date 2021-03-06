package io.debezium.configserver.model;

import java.util.List;

public class AdditionalPropertyMetadata {

    public final boolean isMandatory;
    public final ConnectorProperty.Category category;
    public final List<String> allowedValues;

    public AdditionalPropertyMetadata(boolean isMandatory, ConnectorProperty.Category category) {
        this.isMandatory = isMandatory;
        this.category = category;
        this.allowedValues = null;
    }

    public AdditionalPropertyMetadata(boolean isMandatory, ConnectorProperty.Category category, List<String> allowedValues) {
        this.isMandatory = isMandatory;
        this.category = category;
        this.allowedValues = allowedValues;
    }
}
