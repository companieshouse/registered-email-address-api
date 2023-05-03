package uk.gov.companieshouse.registeredemailaddressapi.utils;

public class Constants {

    private Constants() {
    }

    // Request header names
    public static final String ERIC_REQUEST_ID_KEY = "X-Request-Id";
    public static final String ERIC_IDENTITY = "ERIC-identity";

    // Request attribute names
    public static final String TRANSACTION_KEY = "transaction";

    // URI path attributes
    public static final String TRANSACTION_ID_KEY = "transaction_id";

    // URIs
    public static final String TRANSACTION_URI_PATTERN = "/transactions/%s/registered-email-address/%s";
    public static final String VALIDATION_STATUS_URI_SUFFIX = "/validation-status";

    public static final String TRANSACTIONS_PRIVATE_API_PREFIX = "/private/transactions/";

    public static final String TRANSACTIONS_PUBLIC_API_PREFIX = "/transactions/";


    // Filings
    public static final String FILING_KIND_OVERSEAS_ENTITY = "overseas-entity";
    public static final String LINK_SELF = "self";
    public static final String LINK_RESOURCE = "resource";


}
