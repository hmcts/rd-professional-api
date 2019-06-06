package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(value = BlockJUnit4ClassRunner.class)
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
    public void validateTest() {
        try {
            organisationStatusValidatorImpl.validate(dummyOrganisation, OrganisationStatus.ACTIVE, orgId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = InvalidRequest.class)
    public void throwsExceptionWhenCurrentStatusActiveAndInputStatusPending() {
        organisationStatusValidatorImpl.validate(dummyOrganisation, OrganisationStatus.PENDING, orgId);
    }

    @Test(expected = InvalidRequest.class)
    public void throwsExceptionWhenCurrentStatusDeleted() {
        dummyOrganisation.setStatus(OrganisationStatus.DELETED);
        organisationStatusValidatorImpl.validate(dummyOrganisation, OrganisationStatus.ACTIVE, orgId);
    }
}