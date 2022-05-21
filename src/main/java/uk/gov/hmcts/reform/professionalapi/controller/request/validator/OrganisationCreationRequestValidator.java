package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidContactInformations;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationValidationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.EMAIL_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_EMPTY_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_INVALID_STATUS_PASSED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.removeAllSpaces;

@Component
@Slf4j
public class OrganisationCreationRequestValidator {

    private final List<RequestValidator> validators;

    private static String loggingComponentName;


    public OrganisationCreationRequestValidator(List<RequestValidator> validators) {
        this.validators = validators;
    }

    public static void validateEmail(String email) {
        if (email != null && !email.matches(EMAIL_REGEX)) {
            throw new InvalidRequest("Email format invalid for email: " + email);
        }
    }

    public static void validateNewUserCreationRequestForMandatoryFields(NewUserCreationRequest request) {
        if (StringUtils.isBlank(request.getFirstName()) || StringUtils.isBlank(request.getLastName())
                || StringUtils.isBlank(request.getEmail())) {
            throw new InvalidRequest("Mandatory fields are blank or null");
        }
        validateEmail(request.getEmail());
    }

    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        validators.forEach(v -> v.validate(organisationCreationRequest));
        validateOrganisationRequest(organisationCreationRequest);
        validateEmail(organisationCreationRequest.getSuperUser().getEmail());

    }

    public List<ContactInformationValidationResponse> validate(
            List<ContactInformationCreationRequest> contactInformationCreationRequests) {

        Optional<List<ContactInformationCreationRequest>> infoList =
                Optional.ofNullable(contactInformationCreationRequests);
        if (infoList.isPresent() && infoList.get().isEmpty()) {
            throw new InvalidRequest("Request is empty");
        }

        return validateConstraintValidation(contactInformationCreationRequests);

    }


    private List<ContactInformationValidationResponse> validateConstraintValidation(
            List<ContactInformationCreationRequest> contactInformationCreationRequests) {
        var contactInformationValidationResponses = new ArrayList<ContactInformationValidationResponse>();

        contactInformationCreationRequests.forEach(contactInfo ->
            validateContactInformation(contactInfo, contactInformationValidationResponses));
        return contactInformationValidationResponses;
    }

    public void validateContactInformations(
            List<ContactInformationCreationRequest> contactInformationCreationRequests) {

        var contactInfoValidations =
                validate(contactInformationCreationRequests);

        List<ContactInformationValidationResponse> result = null;
        if (contactInfoValidations != null && !contactInfoValidations.isEmpty()) {
            result = contactInfoValidations.stream()
                    .filter(contactInfoValidation -> !contactInfoValidation.isValidAddress())
                    .collect(Collectors.toList());
        }
        if (result != null && !result.isEmpty()) {
            throw new InvalidContactInformations("Invalid Contact informations", contactInfoValidations);
        }
    }

    public static boolean contains(String status) {
        for (OrganisationStatus type : OrganisationStatus.values()) {
            if (type.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    public void validateOrganisationIdentifier(String inputOrganisationIdentifier) {
        if (null == inputOrganisationIdentifier || LENGTH_OF_ORGANISATION_IDENTIFIER != inputOrganisationIdentifier
                .length() || !inputOrganisationIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)) {
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
        //validating Company Number
        if (organisationCreationRequest.getCompanyNumber().length() > 8) {
            throw new InvalidRequest("Company number must not be greater than 8 characters long");
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

    private void requestPaymentAccount(Set<String> paymentAccounts) {
        if (paymentAccounts != null) {
            paymentAccounts
                    .forEach(paymentAccount -> {
                        if (isEmptyValue(paymentAccount)) {
                            throw new InvalidRequest("Empty paymentAccount value" + paymentAccount);
                        }
                    });
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

            contactInformations
                    .forEach(contactInformation -> {
                        if (isEmptyValue(contactInformation.getAddressLine1())
                                || isEmptyValue(contactInformation.getAddressLine2())
                                || isEmptyValue(contactInformation.getAddressLine3())
                                || isEmptyValue(contactInformation.getCountry())
                                || isEmptyValue(contactInformation.getPostCode())
                                || isEmptyValue(contactInformation.getTownCity())) {

                            throw new InvalidRequest(ERROR_MESSAGE_EMPTY_CONTACT_INFORMATION);
                        }
                        if (null != contactInformation.getDxAddress()) {
                            contactInformation.getDxAddress().forEach(this::isDxAddressValid);
                        }
                        if (null != contactInformation.getUprn() && contactInformation.getUprn().length() > 14) {
                            throw new InvalidRequest("Uprn must not be greater than 14 characters long");
                        }
                    });

        }
    }

    public void validateContactInformation(
            ContactInformationCreationRequest contactInformation,
            List<ContactInformationValidationResponse> contactInformationValidationResponses) {

        try {
            Optional<ContactInformationCreationRequest> contactInfoOptional =
                    Optional.ofNullable(contactInformation);
            if (!contactInfoOptional.isPresent()) {
                throw new InvalidRequest(ERROR_MESSAGE_EMPTY_CONTACT_INFORMATION);
            } else if (isEmptyValue(contactInformation.getAddressLine1())
                    || isEmptyValue(contactInformation.getAddressLine2())
                    || isEmptyValue(contactInformation.getAddressLine3())
                    || isEmptyValue(contactInformation.getCountry())
                    || isEmptyValue(contactInformation.getPostCode())
                    || isEmptyValue(contactInformation.getTownCity())) {

                throw new InvalidRequest(ERROR_MESSAGE_EMPTY_CONTACT_INFORMATION);
            } else if (StringUtils.isBlank(contactInformation.getAddressLine1())) {
                throw new InvalidRequest("AddressLine1 cannot be empty");
            } else {
                List<DxAddressCreationRequest> dxAddressList = contactInformation.getDxAddress();
                if (dxAddressList != null && dxAddressList.isEmpty()) {
                    throw new InvalidRequest("DX Number or DX Exchange cannot be empty");
                } else if (dxAddressList != null && !dxAddressList.isEmpty()) {
                    dxAddressList.forEach(this::isDxAddressValid);
                }
                ContactInformationValidationResponse contactInfoBuilder = new ContactInformationValidationResponse();
                contactInfoBuilder.setUprn(contactInformation.getUprn());
                contactInfoBuilder.setValidAddress(true);
                contactInformationValidationResponses.add(contactInfoBuilder);
            }
        } catch (InvalidRequest invalidRequest) {

            var contactInfoBuilder = new ContactInformationValidationResponse();
            contactInfoBuilder.setUprn(contactInformation.getUprn());
            contactInfoBuilder.setValidAddress(false);
            contactInfoBuilder.setErrorDescription(invalidRequest.getMessage());
            contactInformationValidationResponses.add(contactInfoBuilder);

        }

    }

    public boolean isEmptyValue(String value) {
        return value != null && value.trim().isEmpty();
    }

    private void isDxAddressValid(DxAddressCreationRequest dxAddress) {
        if (StringUtils.isBlank(dxAddress.getDxNumber()) || StringUtils.isBlank(dxAddress.getDxExchange())) {
            throw new InvalidRequest("DX Number or DX Exchange cannot be empty");
        } else if (dxAddress.getDxNumber().length() >= 14 || dxAddress.getDxExchange().length() >= 41) {
            throw new InvalidRequest("DX Number (max=13) or DX Exchange (max=40) has invalid length");
        } else if (!dxAddress.getDxNumber().matches("^[a-zA-Z0-9 ]*$")) {
            throw new InvalidRequest("Invalid Dx Number entered: " + dxAddress.getDxNumber() + ", it can only contain "
                    .concat("numbers, letters and spaces"));
        }
    }

    public static void isInputOrganisationStatusValid(String organisationStatus, String allowedStatus) {
        var validStatusList = asList(allowedStatus.split(","));
        var orgStatus = removeAllSpaces(organisationStatus);
        if (isBlank(orgStatus) || !validStatusList.contains(orgStatus.toUpperCase())) {
            log.error(loggingComponentName + ERROR_MESSAGE_INVALID_STATUS_PASSED);
            throw new ResourceNotFoundException(ERROR_MESSAGE_INVALID_STATUS_PASSED);
        }
    }

    @Value("${loggingComponentName}")
    public static void setLoggingComponentName(String loggingComponentName) {
        OrganisationCreationRequestValidator.loggingComponentName = loggingComponentName;
    }



}

