package uk.gov.hmcts.reform.professionalapi.controller.request;

import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.ORGANISATION_IDENTIFIER_FORMAT_REGEX;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;

@Component
@Slf4j
public class OrganisationCreationRequestValidator {

    private final List<RequestValidator> validators;

    @Autowired
    OrganisationRepository organisationRepository;

    public OrganisationCreationRequestValidator(List<RequestValidator> validators) {
        this.validators = validators;
    }

    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        validators.forEach(v -> v.validate(organisationCreationRequest));
        validateOrganisationRequest(organisationCreationRequest);
    }

    public static boolean contains(String status) {
        for (OrganisationStatus type : OrganisationStatus.values()) {
            if (type.name().equals(status.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public void validateOrganisationIdentifier(String inputOrganisationIdentifier) {

        if (null == inputOrganisationIdentifier || LENGTH_OF_ORGANISATION_IDENTIFIER != inputOrganisationIdentifier.length() || !inputOrganisationIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)) {
            String errorMessage = "Invalid organisationIdentifier provided organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new EmptyResultDataAccessException(1);
        }
    }

    public void isOrganisationActive(Organisation organisation) {

        if (organisation == null) {
            log.error("Organisation not found");
            throw new EmptyResultDataAccessException("Organisation not found", 1);
        } else if (!organisation.isOrganisationStatusActive()) {
            log.error("Organisation is not active. Cannot add new users");
            throw new EmptyResultDataAccessException("Organisation is not active. Cannot add new users", 1);
        }
    }

    public void validateCompanyNumber(OrganisationCreationRequest organisationCreationRequest) {
        log.info("validating Company Number");
        if (organisationCreationRequest.getCompanyNumber().length() != 8) {
            throw new InvalidRequest("Company number must be 8 characters long");
        }

        if (organisationRepository.findByCompanyNumber(organisationCreationRequest.getCompanyNumber()) != null) {
            throw new DuplicateKeyException("The company number provided already belongs to a created Organisation");
        }
    }

    public void validateOrganisationRequest(OrganisationCreationRequest request) {

        log.info("Inside validateOrganisationRequest::");
        requestValues(request.getName(), request.getSraId(), request.getCompanyNumber(), request.getCompanyUrl());
        log.info("after org req ::");
        requestPaymentAccount(request.getPaymentAccount());
        log.info("after payment req ::");
        requestContactInformation(request.getContactInformation());

    }

    private void requestPaymentAccount(List<String> paymentAccounts) {

        log.info("Inside requestPaymentAccount::" + paymentAccounts);

        if (paymentAccounts != null) {

            for (String paymentAccount : paymentAccounts) {

                log.info("Inside requestPaymentAccount::for::");

                if (isEmptyValue(paymentAccount)) {

                    throw new InvalidRequest("Empty paymentAccount value" + paymentAccount);
                }

            }
        }

    }

    public void requestValues(String... values) {

        log.info("Inside requestValues::" + values);

        for (String value : values) {

            if (isEmptyValue(value)) {
                log.error("::error::" + value);
                throw new InvalidRequest("Empty input value" + value);
            }
        }
    }

    public void requestContactInformation(List<ContactInformationCreationRequest> contactInformations) {

        log.info("Inside requestContactInformation::" + contactInformations);
        if (null != contactInformations) {

            for (ContactInformationCreationRequest contactInformation : contactInformations) {

                log.info("Inside requestContactInformation::for:");
                if (isEmptyValue(contactInformation.getAddressLine1()) || isEmptyValue(contactInformation.getAddressLine2())
                        || isEmptyValue(contactInformation.getAddressLine3()) || isEmptyValue(contactInformation.getCountry())
                        || isEmptyValue(contactInformation.getPostCode()) || isEmptyValue(contactInformation.getTownCity())) {

                    log.info("throwing exception");
                    throw new InvalidRequest("Empty contactInformation value");
                }
                if (null != contactInformation.getDxAddress()) {

                    log.info("Inside requestDxAddress::for:");
                    for (DxAddressCreationRequest dxAddress : contactInformation.getDxAddress()) {

                        if (isEmptyValue(dxAddress.getDxNumber()) || !isDxNumberValid(dxAddress.getDxNumber()) || isEmptyValue(dxAddress.getDxExchange())) {
                            throw new InvalidRequest("Invalid dxAddress value: " + dxAddress.getDxExchange() + ", DxNumber: " + dxAddress.getDxNumber());
                        }
                    }
                }
            }
        }
    }

    public boolean isEmptyValue(String value) {

        log.info("Inside isEmptyValue::" + value);
        boolean isEmpty = false;
        if (value != null && value.trim().isEmpty()) {
            isEmpty = true;
        }
        log.info("Inside isEmptyValue end::" + isEmpty);
        return isEmpty;
    }

    private Boolean isDxNumberValid(String dxNumber) {

        Boolean numberIsValid = true;

        if (dxNumber != null) {

            String regex = "^(?:DX|NI) [0-9]{10}+$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(dxNumber);
            numberIsValid = matcher.matches();
        }

        return numberIsValid;

    }

}
