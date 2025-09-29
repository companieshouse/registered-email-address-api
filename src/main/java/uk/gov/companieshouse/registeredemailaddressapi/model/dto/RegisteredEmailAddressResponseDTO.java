package uk.gov.companieshouse.registeredemailaddressapi.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
public class RegisteredEmailAddressResponseDTO {
    @JsonProperty("id")
    private String id;
    @JsonProperty("data")
    private RegisteredEmailAddressResponseData data;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty("links")
    private Map<String, String> links;

    @JsonIgnore
    public boolean isForUpdate() {
        return StringUtils.isNotBlank(data.getRegisteredEmailAddress());
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RegisteredEmailAddressResponseData getData() {
        return data;
    }

    public void setData(RegisteredEmailAddressResponseData data) {
        this.data = data;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}