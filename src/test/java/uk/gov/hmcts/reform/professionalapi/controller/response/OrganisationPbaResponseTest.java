package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;


public class OrganisationPbaResponseTest {

    private Organisation organisationMock = Mockito.mock(Organisation.class);

    @Test
    public void testGetOrganisationPbaResponse() throws Exception {

        OrganisationPbaResponse organisationPbaResponse = new OrganisationPbaResponse(organisationMock, true);

        OrganisationEntityResponse name;

        Field f = organisationPbaResponse.getClass().getDeclaredField("organisationEntityResponse");
        f.setAccessible(true);
        name = (OrganisationEntityResponse) f.get(organisationPbaResponse);

        assertNotNull(name);
        assertTrue(name instanceof OrganisationEntityResponse);

        verify(organisationMock,
                times(1)).getCompanyUrl();




    }

}