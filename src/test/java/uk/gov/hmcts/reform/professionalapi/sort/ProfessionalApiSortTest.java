package uk.gov.hmcts.reform.professionalapi.sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

public class ProfessionalApiSortTest {

    private final Organisation organisationMock = mock(Organisation.class);

    @Test
    public void shouldSortProfessionalUserListByOrgAdminRoleFirst() {

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

        PrdEnumId user2PrdEnumId = new PrdEnumId(4, "ADMIN_ROLE");
        PrdEnum user2PrdEnum = new PrdEnum(user2PrdEnumId, "organisation-admin", "organisation admin");
        List<UserAttribute> user2AttributeList = new ArrayList<>();
        UserAttribute user2Attribute =  new UserAttribute(user1, user2PrdEnum);
        user2AttributeList.add(user2Attribute);
        user2.setUserAttributes(user2AttributeList);

        List<ProfessionalUser> users = new ArrayList<ProfessionalUser>();
        users.add(user1);
        users.add(user2);

        organisationMock.setUsers(users);
        when(organisationMock.getUsers()).thenReturn(users);

        List<ProfessionalUser> sortedUserList = ProfessionalApiSort.sortUsers(organisationMock);
        ProfessionalUser sortedProfessionalUser = sortedUserList.get(0);

        assertThat(sortedProfessionalUser.getFirstName()).isEqualTo("firstName2");
        assertThat(sortedProfessionalUser.getLastName()).isEqualTo("lastName2");
        assertThat(sortedProfessionalUser.getEmailAddress()).isEqualTo("emailAddress2@org.com");
        assertThat(sortedProfessionalUser.getUserAttributes().get(0).getPrdEnum().getEnumName()).isEqualTo("organisation-admin");
    }
}