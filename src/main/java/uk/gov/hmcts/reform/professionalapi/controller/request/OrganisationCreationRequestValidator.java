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

    public final static String emailRegex = "^[A-Za-z0-9\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public OrganisationCreationRequestValidator(List<RequestValidator> validators) {
        this.validators = validators;
    }

    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        validators.forEach(v -> v.validate(organisationCreationRequest));
        validateOrganisationRequest(organisationCreationRequest);
        validateEmail(organisationCreationRequest.getSuperUser().getEmail());

    }

    public static void validateEmail(String email) {
        if (email != null && !email.matches(emailRegex)) {
            throw new InvalidRequest("Email format invalid");
        }
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
            throw new EmptyResultDataAccessException(1);
        }
    }

    public void isOrganisationActive(Organisation organisation) {

        if (organisation == null) {
            throw new EmptyResultDataAccessException("Organisation not found", 1);
        } else if (!organisation.isOrganisationStatusActive()) {
            throw new InvalidRequest("Organisation is not active. Cannot add new users");
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
        requestValues(request.getName(), request.getSraId(), request.getCompanyNumber(), request.getCompanyUrl());
        requestSuperUserValidateAccount(request.getSuperUser());
        requestPaymentAccount(request.getPaymentAccount());
        requestContactInformation(request.getContactInformation());
    }

    private void requestSuperUserValidateAccount(UserCreationRequest superUser) {

        if (superUser == null || isEmptyValue(superUser.getFirstName())
                || isEmptyValue(superUser.getEmail()) || isEmptyValue(superUser.getLastName())) {

            throw new InvalidRequest("UserCreationRequest is not valid");
        }

    }

    private void requestPaymentAccount(List<String> paymentAccounts) {

        if (paymentAccounts != null) {

            for (String paymentAccount : paymentAccounts) {

                if (isEmptyValue(paymentAccount)) {

                    throw new InvalidRequest("Empty paymentAccount value" + paymentAccount);
                }

            }
        }

    }

    public void requestValues(String... values) {

        for (String value : values) {

            if (isEmptyValue(value)) {
                throw new InvalidRequest("Empty input value" + value);
            }
        }
    }

    public void requestContactInformation(List<ContactInformationCreationRequest> contactInformations) {

        if (null != contactInformations) {

            for (ContactInformationCreationRequest contactInformation : contactInformations) {

                if (isEmptyValue(contactInformation.getAddressLine1()) || isEmptyValue(contactInformation.getAddressLine2())
                        || isEmptyValue(contactInformation.getAddressLine3()) || isEmptyValue(contactInformation.getCountry())
                        || isEmptyValue(contactInformation.getPostCode()) || isEmptyValue(contactInformation.getTownCity())) {

                    throw new InvalidRequest("Empty contactInformation value");
                }
                if (null != contactInformation.getDxAddress()) {

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

        boolean isEmpty = false;
        if (value != null && value.trim().isEmpty()) {
            isEmpty = true;
        }
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
