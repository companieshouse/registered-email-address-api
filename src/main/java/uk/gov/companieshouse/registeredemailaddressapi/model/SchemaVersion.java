package uk.gov.companieshouse.registeredemailaddressapi.model;

/**
* Starting at version 3.1 a new top-level 'schema_version' field with an initial value is to be included
*/
public enum SchemaVersion {
    VERSION_3_1("3.1");

    private final String version;

    SchemaVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

}