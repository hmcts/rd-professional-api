package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;


public class OrganisationPbaResponseTest {

    private Organisation organisationMock = Mockito.mock(Organisation.class);
    private final ProfessionalUser professionalUserMock = Mockito.mock(ProfessionalUser.class);

    @Test
    public void testGetOrganisationPbaResponse() throws Exception {

        ArrayList<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUserMock);

        when(organisationMock.getUsers())
                .thenReturn(users);

        OrganisationPbaResponse organisationPbaResponse = new OrganisationPbaResponse(organisationMock, true);

        OrganisationEntityResponse name;

        Field f = organisationPbaResponse.getClass().getDeclaredField("organisationEntityResponse");
        f.setAccessible(true);
        name = (OrganisationEntityResponse) f.get(organisationPbaResponse);

        assertNotNull(name);
        assertTrue(name instanceof OrganisationEntityResponse);

        Mockito.verify(organisationMock,
                Mockito.times(1)).getCompanyUrl();




    }

}