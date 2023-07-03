package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrganisationPbaResponseTest {

    private ProfessionalUser professionalUser;
    private Organisation organisation;
    private final ArrayList<SuperUser> users = new ArrayList<>();

    @BeforeEach
    void setUp() {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, null,"www.org.com");
        professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);

        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
    }

    @Test
    void test_GetOrganisationPbaResponse() throws Exception {
        OrganisationPbaResponse sut = new OrganisationPbaResponse(organisation, true, false, true);
        assertThat(sut.getOrganisationEntityResponse()).isNotNull();
    }
}