package uk.gov.hmcts.reform.professionalapi.controller.constants;

public interface ProfessionalApiGeneratorConstants {

    int LENGTH_OF_UUID = 36;
    int LENGTH_OF_ORGANISATION_IDENTIFIER = 7;
    String ORG_ID_VALIDATION_ERROR_MESSAGE = "The given organisationIdentifier must be 7 Alphanumeric Characters";
    String ORGANISATION_IDENTIFIER_FORMAT_REGEX = "^[A-Z0-9]{7}$";
    String NO_ORG_FOUND_FOR_GIVEN_ID = "No Organisation was found with the given organisationIdentifier ";
    String ORG_NOT_ACTIVE_NO_USERS_RETURNED = "Organisation is not Active hence not returning any users";
    String ERROR_MESSAGE_403_FORBIDDEN = "403 Forbidden";

}
