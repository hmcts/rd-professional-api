package uk.gov.hmcts.reform.professionalapi.controller.constants;

public class ProfessionalApiConstants {

    private ProfessionalApiConstants() {
    }


    public static final String EMAIL_REGEX = "^[A-Za-z0-9]+[\\w!#$%&'’.*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@"
            + "[A-Za-z0-9]+(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    public static final int LENGTH_OF_UUID = 36;
    public static final int LENGTH_OF_ORGANISATION_IDENTIFIER = 7;
    public static final String ORG_ID_VALIDATION_ERROR_MESSAGE
            = "The given organisationIdentifier must be 7 Alphanumeric Characters";
    public static final String ORGANISATION_IDENTIFIER_FORMAT_REGEX = "^[A-Z0-9]{7}$";
    public static final String NO_ORG_FOUND_FOR_GIVEN_ID
            = "No Organisation was found with the given organisationIdentifier ";
    public static final String ORG_NOT_ACTIVE_NO_USERS_RETURNED
            = "Organisation is not Active hence not returning any users";
    public static final String ERROR_MESSAGE_403_FORBIDDEN = "403 Forbidden";
    public static final int ZERO_INDEX = 0;
    public static final int ONE = 1;
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String PUI_USER_MANAGER = "pui-user-manager";
    public static final String ACTIVE = "Active";
    public static final String USER_EMAIL = "UserEmail";
    public static final String REFERER = "Referer";
    
    public static final String ERROR_MESSAGE_INVALID_STATUS_PASSED =
            "Invalid status param provided, only Active status is allowed";
    public static final String FIRST_NAME = "firstName";
    public static final String EMPTY = "";
    public static final String ERROR_MESSAGE_UP_FAILED = "Error while invoking UP";
    public static final String ERROR_MESSAGE_USER_MUST_BE_ACTIVE = "User status must be Active to perform this "
            .concat("operation");

    public static final int ERROR_CODE_400 = 400;
    public static final int ERROR_CODE_500 = 500;
    public static final int STATUS_CODE_204 = 204;
    public static final int USER_COUNT = 1;
    public static final String ERROR_MESSAGE_400_ADMIN_NOT_PENDING = "The organisation admin is not in Pending state";
    public static final String ERROR_MESSAGE_400_ORG_MORE_THAN_ONE_USER = "The organisation has more than one user"
            .concat("registered with HMCTS");
    public static final String ERROR_MESSAGE_INTERNAL_SERVER = "Internal Server Error";
    public static final String ERR_MESG_500_ADMIN_NOTFOUNDUP = "The Organisation admin details could not be retrieved";
    public static final String DELETION_SUCCESS_MSG = "The organisation has deleted successfully";
    public static final String PRD_AAC_SYSTEM = "prd-aac-system";

    public static final String ORG_NOT_ACTIVE = "The requested Organisation is not 'Active'";
    public static final String INVALID_MFA_VALUE = "The MFA status value provided is not valid. "
            + "Please provide a valid value for the MFA preference of the organisation and try again.";
    public static final String NO_USER_FOUND = "The requested user does not exist";
    public static final String EMPTY_USER_ID = "User Id cannot be empty";

    public static final String ERROR_MSG_NO_ORGANISATION_FOUND = "No organisation belongs to given email";
    public static final String ERROR_MSG_NO_PBA_FOUND = "No PBAs associated with given email";

}
