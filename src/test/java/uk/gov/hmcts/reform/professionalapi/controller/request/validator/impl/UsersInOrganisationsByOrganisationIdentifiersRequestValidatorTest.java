package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.generateRandomUUID;

@ExtendWith(MockitoExtension.class)
class UsersInOrganisationsByOrganisationIdentifiersRequestValidatorTest {

    private final UsersInOrganisationsByOrganisationIdentifiersRequestValidator validator =
            new UsersInOrganisationsByOrganisationIdentifiersRequestValidator();

    @Test
    void shouldNotThrowExceptionWhenPageSizeAndSearchAfterAreValid() {
        Integer pageSize = 10;
        UUID searchAfterUser = generateRandomUUID();
        UUID searchAfterOrg = generateRandomUUID();
        assertDoesNotThrow(() -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsLessThanOne() {
        Integer pageSize = -1;
        UUID searchAfterUser = generateRandomUUID();
        UUID searchAfterOrg = generateRandomUUID();
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsNullAndSearchAfterIsValid() {
        Integer pageSize = null;
        UUID searchAfterUser = generateRandomUUID();
        UUID searchAfterOrg = generateRandomUUID();
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSearchAfterOrgIsInvalid() {
        Integer pageSize = 10;
        UUID searchAfterUser = generateRandomUUID();
        UUID searchAfterOrg = null;
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSearchAfterUserIsInvalid() {
        Integer pageSize = 10;
        UUID searchAfterUser = null;
        UUID searchAfterOrg = generateRandomUUID();
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }
}
