package uk.gov.hmcts.reform.professionalapi.controller.constants;

import java.time.format.DateTimeFormatter;

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

    public static final String ALPHA_NUMERIC_WITH_SPECIAL_CHAR_REGEX = "[^A-Za-z0-9-]";

    public static final String NO_ORG_FOUND_FOR_GIVEN_ID
            = "No Organisation was found with the given organisationIdentifier ";

    public static final String NO_CONTACT_FOUND_FOR_GIVEN_ORG
        = "No Contact Information was found with the given organisationIdentifier ";

    public static final String ORG_NOT_ACTIVE_NO_USERS_RETURNED
            = "Organisation is not Active hence not returning any users";
    public static final String ERROR_MESSAGE_403_FORBIDDEN = "403 Forbidden";
    public static final int ZERO_INDEX = 0;
    public static final int ONE = 1;
    public static final String COMMA = ",";
    public static final String REG_EXP_COMMA_DILIMETER = ",(?!\\\\s)";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String PUI_USER_MANAGER = "pui-user-manager";
    public static final String ACTIVE = "Active";
    public static final String USER_EMAIL = "UserEmail";

    public static final String ERROR_MESSAGE_INVALID_STATUS_PASSED =
            "Invalid status param provided, only Active status is allowed";
    public static final String FIRST_NAME = "firstName";
    public static final String ORG_NAME = "name";

    public static final String ORG_STATUS = "status";
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final Integer DEFAULT_PAGE = 1;
    public static final String EMPTY = "";
    public static final String ERROR_MESSAGE_UP_FAILED = "Error while invoking UP";
    public static final String ERROR_MESSAGE_USER_MUST_BE_ACTIVE = "User status must be Active to perform this "
            .concat("operation");

    public static final String ERROR_MESSAGE_EMPTY_CONTACT_INFORMATION =
            "Empty contactInformation value";
    public static final String LOG_ERROR_BODY_START =
            "{}:: {}";
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
    public static final String ERROR_MSG_EMAIL_FOUND = "email address ";
    public static final String ERROR_MSG_NO_PBA_FOUND = "No PBAs associated with given email";
    public static final String PBA_STATUS_MESSAGE_ACCEPTED = "Edited by Admin";
    public static final String PBA_STATUS_MESSAGE_AUTO_ACCEPTED = "Auto approved by Admin";

    public static final String ERROR_MSG_PARTIAL_SUCCESS = "Some of the PBAs successfully added to organisation";
    public static final String ADD_PBA_REQUEST_EMPTY = "PBA request is empty";

    public static final String BAD_REQUEST_STR = "Bad Request - ";
    public static final String EXCEPTION_MSG_NO_VALID_ORG_STATUS_PASSED = BAD_REQUEST_STR
            + "Invalid status(es) passed : %s";

    public static final String ERROR_MSG_PARTIAL_SUCCESS_UPDATE = "Some of the PBAs updated successfully";
    public static final String ERROR_MSG_PBA_NOT_PENDING = "PBA is not in Pending status";
    public static final String ERROR_MSG_PBA_NOT_IN_ORG = "PBA is not associated with the Organisation";
    public static final String ERROR_MSG_PBA_MISSING = "Mandatory field PBA number is missing";
    public static final String ERROR_MSG_STATUS_MISSING = "Mandatory field Status missing";
    public static final String ERROR_MSG_STATUS_INVALID = "Value for Status field is invalid";
    public static final String ERROR_MSG_PBA_INVALID_FORMAT =
            "PBA numbers must start with PBA/pba and be followed by 7 alphanumeric characters";
    public static final String ERROR_MSG_PBAS_ENTERED_ARE_INVALID = ". The following PBAs entered are invalid: ";

    public static final String LOG_TWO_ARG_PLACEHOLDER = "{}:: {}";

    public static final String RD_PROFESSIONAL_MULTI_PBA_LD_FLAG = "rd-professional-multi-pba";

    public static final String RD_PROFESSIONAL_BULK_CUSTOMER_LD_FLAG = "rd-professional-bulk-customer-details";

    public static final String PRD_MFA_LD_FLAG = "prd-mfa-flag";
    public static final String RD_PROFESSIONAL_MULTIPLE_ADDRESS = "rd-professional-multiple-address";

    public static final String PRD_RETRIEVE_INTERNAL_V2 = "prd-retrieve-organisation-internal-v2";

    public static final String PRD_CREATE_INTERNAL_V2 = "prd-create-organisation-internal-v2";

    public static final String PRD_UPDATE_INTERNAL_V2 = "prd-update-organisation-internal-v2";

    public static final String PRD_RETRIEVE_PBA_INTERNAL_V2 = "prd-retrieve-organisation-pba-internal-v2";

    public static final String PRD_RETRIEVE_PBA_EXTERNAL_V2 = "prd-retrieve-organisation-pba-external-v2";

    public static final String PRD_RETRIEVE_EXTERNAL_V2 = "prd-retrieve-organisation-external-v2";

    public static final String PRD_CREATE_EXTERNAL_V2 = "prd-create-organisation-external-v2";

    public static final String ERROR_MSG_ORG_ADDRESS = "Organisation should have at least one address";
    public static final String ERROR_MSG_ORG_NOT_EXIST = "Organisation does not exist";
    public static final String ERROR_MSG_ORG_IDS_DOES_NOT_MATCH = "ids not found or not belonging to org";
    public static final String ERROR_MSG_REQUEST_IS_EMPTY = "Request is empty";
    public static final String ERROR_MSG_ADDRESS_LIST_IS_EMPTY = "Address list is empty";

    public static final String GET_ORG_BY_ID_NOTES_1 = "**IDAM Roles to access API**";
    public static final String GET_ORG_BY_ID_NOTES_2 = ":<br> pui-organisation-manager,<br> pui-finance-manager,";
    public static final String GET_ORG_BY_ID_NOTES_3 = "<br> pui-case-manager,<br> pui-caa,<br> pui-user-manager";

    public static final String DEL_ORG_PBA_NOTES_1 = "Bad Request Error: One of the below reasons: <br>";
    public static final String DEL_ORG_PBA_NOTES_2 = "- Organisation is not ACTIVE.<br>";
    public static final String DEL_ORG_PBA_NOTES_3 = "- No payment accounts passed to be deleted in the request body.";
    public static final String DEL_ORG_PBA_NOTES_4 =
                                    "<br>- Passed payment account numbers are in an invalid format.<br>";
    public static final String DEL_ORG_PBA_NOTES_5 = "-The payment accounts are not associated with users organisation";

    public static final String GET_PBA_EMAIL_NOTES_1 =
                                    "**IDAM Roles to access API** : <br> pui-finance-manager,<br>pui-";
    public static final String GET_PBA_EMAIL_NOTES_2 =
                                    "user-manager,<br> pui-organisation-manager,<br> pui-case-manager";

    public static final String GET_ORG_BY_STATUS_NOTES_1 =
                                    "**IDAM Roles to access API** : <br> pui-organisation-manager";
    public static final String GET_ORG_BY_STATUS_NOTES_2 =
                                    ",<br>pui-finance-manager,<br> pui-case-manager,pui-caa,<br>";
    public static final String GET_ORG_BY_STATUS_NOTES_3 = "pui-user-manager,citizen,caseworker";

    public static final String DELETE_ORG_ADD_400_MESSAGE_1 = "Bad Request Error: One of the below reasons: <br>";
    public static final String DELETE_ORG_ADD_400_MESSAGE_2 = "- Request is malformed.<br>";
    public static final String DELETE_ORG_ADD_400_MESSAGE_3 = "- Organisation id is missing.<br>";
    public static final String DELETE_ORG_ADD_400_MESSAGE_4 = "- Organisation should have at least one address.";

    public static final String DELETE_ORG_ADD_404_MESSAGE_1 = "NOT FOUND Error: One of the below reasons: <br>";
    public static final String DELETE_ORG_ADD_404_MESSAGE_2 = "- Organisation does not exist.<br>";
    public static final String DELETE_ORG_ADD_404_MESSAGE_3 = "- Request is empty.<br>";
    public static final String DELETE_ORG_ADD_404_MESSAGE_4 = "- id1, id2 does not exist<br>";
    public static final String DELETE_ORG_ADD_404_MESSAGE_5 = "OR<br>";
    public static final String DELETE_ORG_ADD_404_MESSAGE_6 = "id1, id2 does not belong to given org.";

    public static final String FIND_BY_PBA_STATUS_1 = "select o from Organisation o join fetch payment_account p \n";
    public static final String FIND_BY_PBA_STATUS_2 = "on p.organisationId = o.id \n";
    public static final String FIND_BY_PBA_STATUS_3 = "where p.pbaStatus = :pbaStatus \n";
    public static final String FIND_BY_PBA_STATUS_4 = "order by p.created asc";

    public static final String GET_USERS_BY_ORG_1 = "**IDAM Roles to access API** : \n pui-finance-manager,";
    public static final String GET_USERS_BY_ORG_2 = "pui-user-manager,\n pui-organisation-manager,";
    public static final String GET_USERS_BY_ORG_3 = "\n pui-case-manager,\n pui-caa";

    public static final String GET_USER_STATUS_EMAIL_1 = "**IDAM Roles to access API** : \n pui-finance-manager";
    public static final String GET_USER_STATUS_EMAIL_2 = ",\n pui-user-manager,\n pui-organisation-manager,";
    public static final String GET_USER_STATUS_EMAIL_3 = "\n pui-case-manager,\n caseworker-publiclaw-courtadmin";
    public static final String ORGANISATION_MISMATCH = "Organisation doesn't match for the given userIdentifier";

    public static final String ORG_TYPE_REGEX = "^[(a-zA-Z0-9 )\\p{L}\\p{N}'’-]{1,256}$";

    public static final String ORG_TYPE_INVALID = "Org Type is invalid - can only contain Alphabetic,"
            + " empty space, ', - characters and must be less than 256 characters";

    public static final String INVALID_MANDATORY_PARAMETER = "001 missing/invalid parameter";
    public static final String INVALID_PAGE_INFORMATION = "002 missing/invalid page information";
    public static final String INVALID_SINCE_TIMESTAMP = "003 Field 'since' is in a invalid format. Expected format: ";
    public static final String SINCE_TIMESTAMP_FORMAT = "yyyy-MM-ddTHH:mm:ss";
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final String PROFESSIONAL_USER_404_MESSAGE = "Professional User not found:";
}
