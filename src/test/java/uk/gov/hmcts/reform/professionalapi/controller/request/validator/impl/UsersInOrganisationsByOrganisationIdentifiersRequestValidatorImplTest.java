package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImplTest {

    private final UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl validator =
            new UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl();

    @Test
    void shouldNotThrowExceptionWhenPageSizeAndSearchAfterAreValid() {
        Integer pageSize = 10;
        UUID searchAfterUser = UUID.randomUUID();
        UUID searchAfterOrg = UUID.randomUUID();
        assertDoesNotThrow(() -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsLessThanOne() {
        Integer pageSize = -1;
        UUID searchAfterUser = UUID.randomUUID();
        UUID searchAfterOrg = UUID.randomUUID();
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsNullAndSearchAfterIsValid() {
        Integer pageSize = null;
        UUID searchAfterUser = UUID.randomUUID();
        UUID searchAfterOrg = UUID.randomUUID();
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSearchAfterOrgIsInvalid() {
        Integer pageSize = 10;
        UUID searchAfterUser = UUID.randomUUID();
        UUID searchAfterOrg = null;
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSearchAfterOrgIsInvalid2() {
        Integer pageSize = 10;
        UUID searchAfterUser = UUID.randomUUID();
        UUID searchAfterOrg = UUID.fromString("00000000-0000-0000-0000-000000000000");
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSearchAfterUserIsInvalid() {
        Integer pageSize = 10;
        UUID searchAfterUser = null;
        UUID searchAfterOrg = UUID.randomUUID();
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }

    @Test
    void shouldThrowExceptionWhenPageSearchAfterUserIsInvalid2() {
        Integer pageSize = 10;
        UUID searchAfterUser = UUID.fromString("00000000-0000-0000-0000-000000000000");
        UUID searchAfterOrg = UUID.randomUUID();
        assertThrows(InvalidRequest.class, () -> validator.validate(pageSize, searchAfterOrg, searchAfterUser));
    }
}
