package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(value = BlockJUnit4ClassRunner.class)
public class OrganisationIdentifierValidatorImplTest {

    OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl = new OrganisationIdentifierValidatorImpl();

    @Test
    public void testValidate() {
        Organisation dummyOrganisation = new Organisation(
                "dummyName",
                OrganisationStatus.ACTIVE,
                "sraId",
                "12345678",
                Boolean.FALSE,
                "dummySite.com");

        organisationIdentifierValidatorImpl.validate(dummyOrganisation, dummyOrganisation.getStatus(), dummyOrganisation.getOrganisationIdentifier());
        try {
            organisationIdentifierValidatorImpl.validate(dummyOrganisation, OrganisationStatus.ACTIVE, dummyOrganisation.getOrganisationIdentifier());
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldCheckOrganisationDoesNotExist() {
        organisationIdentifierValidatorImpl.validate(null, null, null);
    }
}