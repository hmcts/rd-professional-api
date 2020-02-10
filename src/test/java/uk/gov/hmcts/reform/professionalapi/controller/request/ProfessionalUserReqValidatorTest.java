package uk.gov.hmcts.reform.professionalapi.controller.request;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.antlr.v4.runtime.misc.Array2DHashSet;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

public class ProfessionalUserReqValidatorTest {

    ProfessionalUserReqValidator profUserReqValidator = new ProfessionalUserReqValidator();
    UserProfileUpdatedData userProfileUpdatedDataMock = mock(UserProfileUpdatedData.class);

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

    @Test(expected = InvalidRequest.class)
    public void test_validateModifyRolesRequestThrows400WhenInvalidRequestWhenUserIdIsEmpty() {
        profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedDataMock, "");
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateModifyRolesRequestThrows400ForInvalidAddRoleName() {
        Set<RoleName> rolesAdd = new Array2DHashSet<>();
        rolesAdd.add(new RoleName(""));
        when(userProfileUpdatedDataMock.getRolesAdd()).thenReturn(rolesAdd);

        profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedDataMock, UUID.randomUUID().toString());
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateModifyRolesRequestThrows400ForInvalidDeleteRoleName() {
        Set<RoleName> rolesAdd = new Array2DHashSet<>();
        rolesAdd.add(new RoleName("pui-user-manager"));
        when(userProfileUpdatedDataMock.getRolesAdd()).thenReturn(rolesAdd);

        Set<RoleName> rolesDelete = new Array2DHashSet<>();
        rolesDelete.add(new RoleName(""));
        when(userProfileUpdatedDataMock.getRolesDelete()).thenReturn(rolesDelete);

        profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedDataMock, UUID.randomUUID().toString());
    }

    @Test(expected = InvalidRequest.class)
    public void should_throw_bad_request_when_user_already_exists() {
        ProfessionalUser professionalUser = new ProfessionalUser();
        ProfessionalUserReqValidator.checkUserAlreadyExist(professionalUser);
    }

    @Test(expected = Test.None.class)
    public void should_not_throw_bad_request_when_user_not_exists() {
        ProfessionalUserReqValidator.checkUserAlreadyExist(null);
    }
}
