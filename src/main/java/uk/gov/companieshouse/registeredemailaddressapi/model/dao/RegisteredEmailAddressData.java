package uk.gov.companieshouse.registeredemailaddressapi.model.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class RegisteredEmailAddressData {
    @Field("registered_email_address")
    private String registeredEmailAddress;
    @Field("accept_appropriate_email_address_statement")
    private boolean acceptAppropriateEmailAddressStatement;
    @Field("etag")
    private String etag;
    @Field("kind")
    private String kind;

    public String getRegisteredEmailAddress() {
        return registeredEmailAddress;
    }

    public void setRegisteredEmailAddress(String registeredEmailAddress) {
        this.registeredEmailAddress = registeredEmailAddress;
    }

    public boolean isAcceptAppropriateEmailAddressStatement() {
        return acceptAppropriateEmailAddressStatement;
    }

    public void setAcceptAppropriateEmailAddressStatement(boolean acceptAppropriateEmailAddressStatement) {
        this.acceptAppropriateEmailAddressStatement = acceptAppropriateEmailAddressStatement;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

}