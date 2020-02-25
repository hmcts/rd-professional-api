package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public class OrganisationStatusValidatorImplTest {

    private OrganisationStatusValidatorImpl organisationStatusValidatorImpl;
    private Organisation organisation;
    private String orgId;

    @Before
    public void setUp() {
        organisationStatusValidatorImpl = new OrganisationStatusValidatorImpl();
        organisation = new Organisation("dummyName", OrganisationStatus.ACTIVE, "sraId", "12345678", Boolean.FALSE, "dummySite.com");
        orgId = organisation.getOrganisationIdentifier();
    }

    @Test
    public void testValidate() {
        try {
            organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.ACTIVE, orgId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = InvalidRequest.class)
    public void testThrowsExceptionWhenCurrentStatusActiveAndInputStatusPending() {
        organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.PENDING, orgId);
    }

    @Test(expected = InvalidRequest.class)
    public void testThrowsExceptionWhenCurrentStatusDeleted() {
        organisation.setStatus(OrganisationStatus.DELETED);
        organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.ACTIVE, orgId);
    }
}