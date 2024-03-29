package uk.gov.companieshouse.registeredemailaddressapi.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyEmailValidation;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyStatusValidation;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyTypeValidation;
import uk.gov.companieshouse.registeredemailaddressapi.service.PrivateDataRetrievalService;

@Configuration
public class ReaServiceEligibilityConfig {

    @Value("${allowed.company.statuses}")
    private Set<String> allowedCompanyStatuses;

    @Value("${allowed.company.types}")
    private Set<String> allowedCompanyTypes;

    private final PrivateDataRetrievalService privateDataRetrievalService;

    @Autowired
    public ReaServiceEligibilityConfig(PrivateDataRetrievalService privateDataRetrievalService) {
        this.privateDataRetrievalService = privateDataRetrievalService;
    }

    @Bean
    @Qualifier("rea-update-eligibility-rules")
    List<EligibilityRule<CompanyProfileApi>> reaUpdateEligibilityRules() {
        var listOfRules = new ArrayList<EligibilityRule<CompanyProfileApi>>();

        var companyStatusValidation = new CompanyStatusValidation(allowedCompanyStatuses);
        var companyTypeValidationForWebFiling = new CompanyTypeValidation(allowedCompanyTypes);
        var companyEmailValidation = new CompanyEmailValidation(privateDataRetrievalService);

        /* Check 1: Company Status */
        listOfRules.add(companyStatusValidation);

        /* Check 2: Company Type */
        listOfRules.add(companyTypeValidationForWebFiling);

        /* Check 3: Company Email */
        listOfRules.add(companyEmailValidation);

        return listOfRules;
    }
}
