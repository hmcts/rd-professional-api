package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.junit.Assert.fail;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public class OrganisationStatusValidatorImplTest {

    OrganisationStatusValidatorImpl organisationStatusValidatorImpl = new OrganisationStatusValidatorImpl();

    Organisation dummyOrganisation = new Organisation(
            "dummyName",
            OrganisationStatus.ACTIVE,
            "sraId",
            "12345678",
            Boolean.FALSE,
            "dummySite.com");

    String orgId = dummyOrganisation.getOrganisationIdentifier();

    @Test
    public void testValidate() {
        try {
            organisationStatusValidatorImpl.validate(dummyOrganisation, OrganisationStatus.ACTIVE, orgId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = InvalidRequest.class)
    public void testThrowsExceptionWhenCurrentStatusActiveAndInputStatusPending() {
        organisationStatusValidatorImpl.validate(dummyOrganisation, OrganisationStatus.PENDING, orgId);
    }

    @Test(expected = InvalidRequest.class)
    public void testThrowsExceptionWhenCurrentStatusDeleted() {
        dummyOrganisation.setStatus(OrganisationStatus.DELETED);
        organisationStatusValidatorImpl.validate(dummyOrganisation, OrganisationStatus.ACTIVE, orgId);
    }
}