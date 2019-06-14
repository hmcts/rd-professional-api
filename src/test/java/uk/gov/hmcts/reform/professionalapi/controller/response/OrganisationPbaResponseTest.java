package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.mockito.Mockito.when;

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
    }

}