package uk.gov.hmcts.reform.professionalapi.controller.constants;

public class ProfessionalApiGeneratorConstants {

    private ProfessionalApiGeneratorConstants() {
    }


    public static final int LENGTH_OF_UUID = 36;
    public static final int LENGTH_OF_ORGANISATION_IDENTIFIER = 7;
    public static final String ORG_ID_VALIDATION_ERROR_MESSAGE = "The given organisationIdentifier must be 7 Alphanumeric Characters";
    public static final String ORGANISATION_IDENTIFIER_FORMAT_REGEX = "^[A-Z0-9]{7}$";
    public static final String NO_ORG_FOUND_FOR_GIVEN_ID = "No Organisation was found with the given organisationIdentifier ";
    public static final String ORG_NOT_ACTIVE_NO_USERS_RETURNED = "Organisation is not Active hence not returning any users";
    public static final String ERROR_MESSAGE_403_FORBIDDEN = "403 Forbidden";
    public static final String ERROR_MESSAGE_INVALID_STATUS_PASSED = "Please check status param passed as this is invalid status.";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String FIRST_NAME = "firstName";
    public static final String EMPTY = "";
    public static final String ACTIVE = "Active";
}
