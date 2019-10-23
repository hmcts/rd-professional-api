package uk.gov.hmcts.reform.professionalapi.controller.request;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;

public class ProfessionalUserReqValidatorTest {

    ProfessionalUserReqValidator profUserReqValidator = new ProfessionalUserReqValidator();
    ModifyUserProfileData modifyUserProfileDataMock = mock(ModifyUserProfileData.class);

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

    @Test(expected = InvalidRequest.class)
    public void test_validateModifyRolesRequestThrows400WhenInvalidRequest() {
        profUserReqValidator.validateModifyRolesRequest(modifyUserProfileDataMock, "");
    }
}
