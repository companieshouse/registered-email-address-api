package uk.gov.companieshouse.registeredemailaddressapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisteredEmailAddressResponseData extends RegisteredEmailAddressDTO {
    @JsonProperty("etag")
    private String etag;
    @JsonProperty("kind")
    private String kind;
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