package uk.gov.companieshouse.registeredemailaddressapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;


public class RegisteredEmailAddressDTO extends RegisteredEmailAddress {

    @JsonProperty("id")
    private String id;
    @JsonProperty("etag")
    private String etag;
    @JsonProperty("kind")
    private String kind;
    @JsonProperty("links")
    private Map<String, String> links;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }
}

