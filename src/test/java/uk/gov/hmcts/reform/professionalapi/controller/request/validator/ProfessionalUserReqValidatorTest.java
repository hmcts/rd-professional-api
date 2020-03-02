package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import static java.util.Collections.singleton;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

public class ProfessionalUserReqValidatorTest {

    private ProfessionalUserReqValidator profUserReqValidator;
    private UserProfileUpdatedData userProfileUpdatedData;

    @Before
    public void setUp() {
        profUserReqValidator = new ProfessionalUserReqValidator();
        userProfileUpdatedData = new UserProfileUpdatedData();
    }

    @Test(expected = InvalidRequest.class)
    public void testValidateRequestAllNull() {
        profUserReqValidator
                .validateRequest(null, null, null);
    }

    @Test
    public void testValidateRequestNoneNull() {
        profUserReqValidator
                .validateRequest("ordId", "true", "");
    }

    @Test
    public void test_isValidEmailReturnsTrueForValidEmail() {
        String email = "email@test.com";
        Boolean result = profUserReqValidator.isValidEmail(email);
        assertTrue(result);
    }

    @Test
    public void test_isValidEmailReturnsFalseForInvalidEmail() {
        String email = "this-is-not-an-email";
        Boolean result = profUserReqValidator.isValidEmail(email);
        assertFalse(result);
    }

    @Test(expected = Test.None.class)
    public void test_validateUserStatus() {
        String status = IdamStatus.ACTIVE.name();
        profUserReqValidator.validateUserStatus(status);
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateUserStatusThrows400ForInvalidStatus() {
        String status = "this-is-not-a-status";
        profUserReqValidator.validateUserStatus(status);
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateStatusIsActiveThrows400ForInvalidStatus() {
        String status = "this-is-not-a-status";
        profUserReqValidator.validateStatusIsActive(status);
    }

    @Test
    public void test_validateStatusIsActiveDoesNotThrow400ForValidStatus() {
        String status = "ACTIVE";
        profUserReqValidator.validateStatusIsActive(status);
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateModifyRolesRequestThrows400WhenInvalidRequestWhenUserIdIsEmpty() {
        profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedData, "");
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateModifyRolesRequestThrows400ForInvalidAddRoleName() {
        userProfileUpdatedData.setRolesAdd((singleton(new RoleName(""))));

        profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedData, UUID.randomUUID().toString());
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateModifyRolesRequestThrows400ForInvalidDeleteRoleName() {
        userProfileUpdatedData.setRolesAdd((singleton(new RoleName("pui-user-manager"))));
        userProfileUpdatedData.setRolesDelete((singleton(new RoleName(""))));

        profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedData, UUID.randomUUID().toString());
    }

    @Test(expected = Test.None.class)
    public void test_validateModifyRolesRequestDoesNotThrow400WhenRequestIsValid() {
        profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedData, UUID.randomUUID().toString());
    }
}
