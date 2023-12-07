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
    public static final String COMPANY_NUMBER_KEY = "company-number";

    // URIs
    public static final String TRANSACTION_URI_PATTERN = "/transactions/%s/registered-email-address";
    public static final String VALIDATION_STATUS_URI_SUFFIX = "/validation-status";

    public static final String TRANSACTIONS_PRIVATE_API_PREFIX = "/private/transactions/";

    public static final String TRANSACTIONS_PUBLIC_API_PREFIX = "/transactions/";

    // Filings
    public static final String FILING_KIND = "registered-email-address";

    //submissions
    public static final String REGISTERED_EMAIL_ADDRESS = "registered_email_address";

    public static final String ACCEPT_EMAIL_STATEMENT = "accept_appropriate_email_address_statement";

    public static final String COMPANY_NUMBER = "company_number";
    public static final String LINK_SELF = "self";
    public static final String LINK_VALIDATION = "validation_status";
    public static final String LINK_RESOURCE = "resource";

    // Utitity constants
    public static final String DATE_FORMATTER_PATTERN = "d MMMM yyyy";

}
