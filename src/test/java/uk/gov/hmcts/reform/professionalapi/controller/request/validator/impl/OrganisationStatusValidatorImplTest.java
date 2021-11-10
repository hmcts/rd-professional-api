package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@ExtendWith(MockitoExtension.class)
class OrganisationStatusValidatorImplTest {

    private OrganisationStatusValidatorImpl organisationStatusValidatorImpl;
    private Organisation organisation;
    private String orgId;

    @BeforeEach
    void setUp() {
        organisationStatusValidatorImpl = new OrganisationStatusValidatorImpl();
        organisation = new Organisation("dummyName", OrganisationStatus.ACTIVE, "sraId",
                "12345678", Boolean.FALSE, "dummySite.com");
        orgId = organisation.getOrganisationIdentifier();
    }

    @Test
    void test_Validate() {
        try {
            organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.ACTIVE, orgId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void test_ThrowsExceptionWhenCurrentStatusActiveAndInputStatusPending() {
        assertThrows(InvalidRequest.class, () ->
                organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.PENDING, orgId));
    }

    @Test
    void test_ThrowsExceptionWhenCurrentStatusDeleted() {
        organisation.setStatus(OrganisationStatus.DELETED);
        assertThrows(InvalidRequest.class, () ->
                organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.ACTIVE, orgId));
    }
}