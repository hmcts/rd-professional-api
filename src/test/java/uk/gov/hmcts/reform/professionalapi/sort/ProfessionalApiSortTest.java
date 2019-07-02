package uk.gov.hmcts.reform.professionalapi.sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class ProfessionalApiSortTest {

    private final Organisation organisationMock = mock(Organisation.class);

    @Test
    public void shouldSortProfessionalUserList() {

        ProfessionalUser user1 = new ProfessionalUser("firstName1",
                "lastName1",
                "emailAddress1@org.com",
                organisationMock);


        ProfessionalUser user2 = new ProfessionalUser("firstName2",
                "lastName2",
                "emailAddress2@org.com",
                organisationMock);

        user1.setCreated(LocalDateTime.now());

        user2.setCreated(LocalDateTime.now().plusMinutes(60));

        List<ProfessionalUser> users = new ArrayList<ProfessionalUser>();
        users.add(user2);
        users.add(user1);

        organisationMock.setUsers(users);

        when(organisationMock.getUsers())
                .thenReturn(users);

        List<ProfessionalUser> sortedUserList = ProfessionalApiSort.sortUserListByCreatedDate(organisationMock);
        ProfessionalUser sortedProfessionalUser = sortedUserList.get(0);

        assertThat(sortedProfessionalUser.getFirstName()).isEqualTo("firstName1");
        assertThat(sortedProfessionalUser.getLastName()).isEqualTo("lastName1");
        assertThat(sortedProfessionalUser.getEmailAddress()).isEqualTo("emailAddress1@org.com");
    }
}