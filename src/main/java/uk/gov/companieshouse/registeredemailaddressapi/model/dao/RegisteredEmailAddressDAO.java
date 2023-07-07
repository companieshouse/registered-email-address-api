package uk.gov.companieshouse.registeredemailaddressapi.model.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "registered_email_address")
public class RegisteredEmailAddressDAO {

    @Id
    private String id;
    @Field("transaction_id")
    private String transactionId;
    @Field("data")
    private RegisteredEmailAddressData data;
    @Field("created_at")
    private LocalDateTime createdAt;
    @Field("updated_at")
    private LocalDateTime updatedAt;
    @Field("links")
    private Map<String, String> links;
    @Field("last_modified_by_user_id")
    private String lastModifiedByUserId;
    @Field("http_request_id")
    private String httpRequestId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public RegisteredEmailAddressData getData() {
        return data;
    }

    public void setData(RegisteredEmailAddressData data) {
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

    public String getLastModifiedByUserId() {
        return lastModifiedByUserId;
    }

    public void setLastModifiedByUserId(String lastModifiedByUserId) {
        this.lastModifiedByUserId = lastModifiedByUserId;
    }

    public String getHttpRequestId() {
        return httpRequestId;
    }

    public void setHttpRequestId(String httpRequestId) {
        this.httpRequestId = httpRequestId;
    }

}