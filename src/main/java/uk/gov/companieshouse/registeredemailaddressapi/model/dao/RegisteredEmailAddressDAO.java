package uk.gov.companieshouse.registeredemailaddressapi.model.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

@Document(collection = "registered_email_address")
public class RegisteredEmailAddressDAO {

    @Id
    private String id;
    @Field("registered_email_address")
    private String registered_email_address;
    @Field("created_at")
    private Date created_at;
    @Field("updated_at")
    private Date updated_at;
    @Field("etag")
    private String etag;
    @Field("kind")
    private String kind;
    @Field("links")
    private Map<String, String> links;
}
