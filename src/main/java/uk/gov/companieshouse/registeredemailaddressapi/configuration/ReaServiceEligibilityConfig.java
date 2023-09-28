package uk.gov.companieshouse.registeredemailaddressapi.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyStatusValidation;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyTypeValidation;

@Configuration
public class ReaServiceEligibilityConfig {

    @Value("${allowed.company.statuses}")
    private Set<String> allowedCompanyStatuses;

    @Value("${allowed.company.types}")
    private Set<String> allowedCompanyTypes;

    @Bean
    @Qualifier("rea-update-eligibility-rules")
    List<EligibilityRule<CompanyProfileApi>> reaUpdateEligibilityRules() {
        var listOfRules = new ArrayList<EligibilityRule<CompanyProfileApi>>();

        var companyStatusValidation = new CompanyStatusValidation(allowedCompanyStatuses);
        var companyTypeValidationForWebFiling = new CompanyTypeValidation(allowedCompanyTypes);

        /* Check 1: Company Status */
        listOfRules.add(companyStatusValidation);

        /* Check 2: Company Type */
        listOfRules.add(companyTypeValidationForWebFiling);

        return listOfRules;
    }
}
