package uk.gov.companieshouse.registeredemailaddressapi.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Map;


public class RegisteredEmailAddressDTO {

    @JsonProperty("id")
    private String id;
    @NotBlank(message = "registered_email_address must not be blank")
    @Pattern(regexp = "^.+@.+\\..+$",
            message ="registered_email_address must have a valid email format" )
    @JsonProperty("registered_email_address")
    private String registeredEmailAddress;
    @JsonProperty("etag")
    private String etag;
    @JsonProperty("kind")
    private String kind;
    @JsonProperty("links")
    private Map<String, String> links;
    @JsonIgnore
    public boolean isForUpdate() {
        return StringUtils.isNotBlank(registeredEmailAddress);
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegisteredEmailAddress() {
        return registeredEmailAddress;
    }

    public void setRegisteredEmailAddress(String registeredEmailAddress) {
        this.registeredEmailAddress = registeredEmailAddress;
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

