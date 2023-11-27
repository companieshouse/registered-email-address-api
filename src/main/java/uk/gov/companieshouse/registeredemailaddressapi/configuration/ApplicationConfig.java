package uk.gov.companieshouse.registeredemailaddressapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.registeredemailaddressapi.service.MockEmailRetrieverImpl;
import uk.gov.companieshouse.registeredemailaddressapi.service.OracleQueryApiDataRetrievalServiceImpl;
import uk.gov.companieshouse.registeredemailaddressapi.service.PrivateEmailDataRetrievalService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Configuration
public class ApplicationConfig {

    @Value("$env.name")
    private String envName;

    @Bean
    public Supplier<LocalDateTime> dateTimeNow() {
        return LocalDateTime::now;
    }

    @Bean
    public Supplier<LocalDate> dateNow() {
        return LocalDate::now;
    }

    @Bean
    @ConditionalOnExpression("'${env.name}'.equals('stagsbox') || '${env.name}'.equals('livesbox')")
    public PrivateEmailDataRetrievalService getMockEmailRetriever() {
        return new MockEmailRetrieverImpl() ;
    }

    @Bean
    @ConditionalOnExpression("!'${env.name}'.equals('stagsbox') || !'${env.name}'.equals('livesbox')")
    public PrivateEmailDataRetrievalService getOracleQueryApiEmailRetriever() {
        return new OracleQueryApiDataRetrievalServiceImpl() ;
    }

//    @Bean
//    public PrivateEmailDataRetrievalService getOracleQueryApiEmailRetriever() {
////        return env.equalsIgnoreCase("sbox") ? new MockEmailRetrieverImpl() : new OracleQueryApiDataRetrievalServiceImpl() ;
//        return new OracleQueryApiDataRetrievalServiceImpl();
//    }
}
