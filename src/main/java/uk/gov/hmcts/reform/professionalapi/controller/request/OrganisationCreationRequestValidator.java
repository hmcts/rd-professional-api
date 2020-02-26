package uk.gov.hmcts.reform.professionalapi.controller.request;

import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;

import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@Component
@Slf4j
public class OrganisationCreationRequestValidator {

    private final List<RequestValidator> validators;

    private static String emailRegex = "^[A-Za-z0-9]+[\\w!#$%&’.*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@[A-Za-z0-9]+(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    public static final String CHARACTERS = " characters";

    public static final String THIRTEEN_OR_LESS = "must be 13 characters or less, you have entered ";

    public OrganisationCreationRequestValidator(List<RequestValidator> validators) {
        this.validators = validators;
    }

    public static void validateEmail(String email) {
        if (email != null && !email.matches(emailRegex)) {
            throw new InvalidRequest("Email format invalid for email: " + email);
        }
    }

    public static void validateNewUserCreationRequestForMandatoryFields(NewUserCreationRequest request) {
        if (StringUtils.isBlank(request.getFirstName()) || StringUtils.isBlank(request.getLastName()) || StringUtils.isBlank(request.getEmail())) {
            throw new InvalidRequest("Mandatory fields are blank or null");
        }
        validateEmail(request.getEmail());
    }

    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        validators.forEach(v -> v.validate(organisationCreationRequest));
        validateOrganisationRequest(organisationCreationRequest);
        validateEmail(organisationCreationRequest.getSuperUser().getEmail());

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
        if (StringUtils.isBlank(inputOrganisationIdentifier)) {
            throw new InvalidRequest("The given organisationIdentifier cannot be null or empty");
        } else if (!inputOrganisationIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)) {
            throw new InvalidRequest("The given organisationIdentifier must be 7 Alphanumeric Characters");
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
                        isDxAddressValid(dxAddress);
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

    private void isDxAddressValid(DxAddressCreationRequest dxAddress) {
        if (StringUtils.isEmpty(dxAddress.getDxNumber()) || StringUtils.isEmpty(dxAddress.getDxExchange())) {
            throw new InvalidRequest("DX Number or DX Exchange cannot be empty");
        } else if (dxAddress.getDxNumber().length() >= 14 || dxAddress.getDxExchange().length() >= 21) {
            throw new InvalidRequest("DX Number (max=13) or DX Exchange (max=20) has invalid length");
        } else if (!dxAddress.getDxNumber().matches("^[a-zA-Z0-9 ]*$")) {
            throw new InvalidRequest("Invalid Dx Number entered: " + dxAddress.getDxNumber() + ", it can only contain numbers, letters and spaces");
        }
    }


    public static void validateJurisdictions(List<Jurisdiction> jurisdictions, List<String> enumList) {

        if (CollectionUtils.isEmpty(jurisdictions)) {
            throw new InvalidRequest("Jurisdictions not present");
        } else {
            jurisdictions.forEach(jurisdiction -> {
                if (StringUtils.isBlank(jurisdiction.getId())) {
                    throw new InvalidRequest("Jurisdiction value should not be blank or null");
                } else if (!enumList.contains(jurisdiction.getId())) {
                    throw new InvalidRequest("Jurisdiction id not valid : " + jurisdiction.getId());
                }
            });
        }
    }
}
