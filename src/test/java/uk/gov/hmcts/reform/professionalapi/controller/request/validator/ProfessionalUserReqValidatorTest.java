package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import java.util.UUID;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserReqValidatorTest {

    private ProfessionalUserReqValidator profUserReqValidator;
    private UserProfileUpdatedData userProfileUpdatedData;

    @BeforeEach
    void setUp() {
        profUserReqValidator = new ProfessionalUserReqValidator();
        userProfileUpdatedData = new UserProfileUpdatedData();
    }

    @Test
    void test_ValidateRequestAllNull() {
        assertThrows(InvalidRequest.class, () ->
                profUserReqValidator.validateRequest(null, null, null));
    }

    @Test
    void test_ValidateRequestNoneNull() {
        profUserReqValidator
                .validateRequest("ordId", "true", "");
    }

    @Test
    void test_validateUserStatus() {
        String status = IdamStatus.ACTIVE.name();
        assertDoesNotThrow(() ->
                profUserReqValidator.validateUserStatus(status));
    }

    @Test
    void test_validateUserStatusThrows400ForInvalidStatus() {
        String status = "this-is-not-a-status";
        assertThrows(InvalidRequest.class, () ->
                profUserReqValidator.validateUserStatus(status));
    }

    @Test
    void test_validateStatusIsActiveThrows400ForInvalidStatus() {
        String status = "this-is-not-a-status";
        assertThrows(InvalidRequest.class, () ->
                profUserReqValidator.validateStatusIsActive(status));
    }

    @Test
    void test_validateStatusIsActiveDoesNotThrow400ForValidStatus() {
        String status = "ACTIVE";
        profUserReqValidator.validateStatusIsActive(status);
    }

    @Test
    void test_validateUuidThrows400ForInvalidUuid() {
        String id = "123456";
        assertThrows(InvalidRequest.class, () -> profUserReqValidator.validateUuid(id));
    }

    @Test
    void test_validateUuidDoesNotThrow400ForValidUuid() {
        String id = UUID.randomUUID().toString();
        assertTrue(profUserReqValidator.validateUuid(id));
    }

    @Test
    void test_validateUuidDoesNotThrow400ForNullId() {
        assertTrue(profUserReqValidator.validateUuid(null));
    }

    @Test
    void test_validateUuidDoesNotThrow400ForEmptyId() {
        assertTrue(profUserReqValidator.validateUuid(""));
    }

    @Test
    void test_validateOrganisationMatchDoesThrow400ForMismatchOrgId() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        assertThrows(AccessDeniedException.class, () -> profUserReqValidator.validateOrganisationMatch(
                "orgid", professionalUser));
    }

    @Test
    void test_validateOrganisationMatchDoesThrow400ForNullUserId() {
        assertThrows(InvalidRequest.class, () -> profUserReqValidator.validateOrganisationMatch(
                "orgid", null));
    }

    @Test
    void test_validateOrganisationMatchDoesThrow400ForNullOrgId() {
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", null);

        assertThrows(AccessDeniedException.class, () -> profUserReqValidator.validateOrganisationMatch(
                "orgid", professionalUser));
    }

    @Test
    void test_validateOrganisationMatchDoesNotThrow403ForMatchedOrgId() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        organisation.setOrganisationIdentifier("orgid");
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        profUserReqValidator.validateOrganisationMatch("orgid", professionalUser);
        assertTrue(true);
    }

    @Test
    void test_validateModifyRolesRequestThrows400WhenInvalidRequestWhenUserIdIsEmpty() {
        assertThrows(InvalidRequest.class, () ->
                profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedData, ""));
    }

    @Test
    void test_validateModifyRolesRequestThrows400ForInvalidAddRoleName() {
        userProfileUpdatedData.setRolesAdd((singleton(new RoleName(""))));

        String uuid = UUID.randomUUID().toString();

        assertThrows(InvalidRequest.class, () ->
                profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedData, uuid));
    }

    @Test
    void test_validateModifyRolesRequestThrows400ForInvalidDeleteRoleName() {
        userProfileUpdatedData.setRolesAdd((singleton(new RoleName("pui-user-manager"))));
        userProfileUpdatedData.setRolesDelete((singleton(new RoleName(""))));

        String uuid = UUID.randomUUID().toString();

        assertThrows(InvalidRequest.class, () ->
                profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedData, uuid));
    }

    @Test
    void test_validateModifyRolesRequestDoesNotThrow400WhenRequestIsValid() {
        assertDoesNotThrow(() ->
                profUserReqValidator.validateModifyRolesRequest(userProfileUpdatedData, UUID.randomUUID().toString()));
    }
}
