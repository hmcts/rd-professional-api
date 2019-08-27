package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;


public class OrganisationPbaResponseTest {

    private Organisation organisationMock = Mockito.mock(Organisation.class);

    private final SuperUser professionalUserMock = Mockito.mock(SuperUser.class);

    @Test
    public void testGetOrganisationPbaResponse() throws Exception {

        ArrayList<SuperUser> users = new ArrayList<>();
        users.add(professionalUserMock);

        when(organisationMock.getUsers())
                .thenReturn(users);

        OrganisationPbaResponse sut = new OrganisationPbaResponse(organisationMock, true);

        OrganisationEntityResponse name;

        assertThat(sut.getOrganisationEntityResponse()).isNotNull();

    }

}