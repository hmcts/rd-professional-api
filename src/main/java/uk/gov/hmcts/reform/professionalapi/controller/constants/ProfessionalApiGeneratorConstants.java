package uk.gov.hmcts.reform.professionalapi.controller.constants;

public class ProfessionalApiGeneratorConstants {

    private ProfessionalApiGeneratorConstants() {
    }

    // There are various regex for same email format. We need to take up a task to make all email regex same in both
    // PRD and UP

    public static final String EMAILREGEX = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+)"
            + "*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
    public static String EMAIL_REGEX = "^[A-Za-z0-9]+[\\w!#$%&’.*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@"
            .concat("[A-Za-z0-9]+(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
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
    public static final String FIRST_NAME = "firstName";
    public static final String EMPTY = "";
    public static final String ERROR_MESSAGE_UP_FAILED = "Error while invoking UP";
    public static final String ERROR_MESSAGE_USER_MUST_BE_ACTIVE = "User status must be Active to perform this "
            .concat("operation");
}
