package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

@ExtendWith(MockitoExtension.class)
class OrganisationPbaResponseTest {

    private Organisation organisation;
    private final ArrayList<SuperUser> users = new ArrayList<>();

    @BeforeEach
    void setUp() {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname",
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