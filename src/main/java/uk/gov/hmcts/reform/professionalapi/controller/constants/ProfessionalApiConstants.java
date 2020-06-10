package uk.gov.hmcts.reform.professionalapi.controller.constants;

public class ProfessionalApiConstants {

    private ProfessionalApiConstants() {
    }

    public static final int LENGTH_OF_UUID = 36;
    public static final int LENGTH_OF_ORGANISATION_IDENTIFIER = 7;
    public static final String ORG_ID_VALIDATION_ERROR_MESSAGE = "The given organisationIdentifier must be 7 Alphanumeric Characters";
    public static final String ORGANISATION_IDENTIFIER_FORMAT_REGEX = "^[A-Z0-9]{7}$";
    public static final String NO_ORG_FOUND_FOR_GIVEN_ID = "No Organisation was found with the given organisationIdentifier ";
    public static final String ORG_NOT_ACTIVE_NO_USERS_RETURNED = "Organisation is not Active hence not returning any users";
    public static final String ERROR_MESSAGE_403_FORBIDDEN = "403 Forbidden";
    public static final int ERROR_CODE_400 = 400;
    public static final int ERROR_CODE_500 = 500;
    public static final int STATUS_CODE_204 = 204;
    public static final int USER_COUNT = 1;
    public static final int ZERO_INDEX = 0;
    public static final int ONE = 1;
    public static final String ERROR_MESSAGE_400_ADMIN_NOT_PENDING = "The organisation admin is not in Pending state";
    public static final String ERROR_MESSAGE_400_ORG_MORE_THAN_ONE_USER = "The organisation has more than one user registered with HMCTS";
    public static final String ERROR_MESSAGE_INTERNAL_SERVER = "Internal Server Error";
    public static final String ERROR_MESSAGE_500_ADMIN_NOT_FOUND_UP = "The Organisation admin details could not be retrieved";
    public static final String DELETION_SUCCESS_MSG = "The organisation has deleted successfully";
    public static final String  ERROR_MESSAGE_INVALID_STATUS_PASSED = "Please check status param passed as this is invalid status.";
}
