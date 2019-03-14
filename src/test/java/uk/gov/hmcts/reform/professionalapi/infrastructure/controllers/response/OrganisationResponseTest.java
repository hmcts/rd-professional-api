package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response;

import static java.util.UUID.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;

public class OrganisationResponseTest {

    @Test
    public void builds_response_correctly() {

        UUID orgId = randomUUID();
        UUID userId = randomUUID();

        Organisation organisation = new Organisation(orgId, "some-name", "some-status");
        organisation.addProfessionalUser(new ProfessionalUser(userId, null, null, null, null, null));

        OrganisationResponse organisationResponse = new OrganisationResponse(organisation);

        assertThat(organisationResponse)
                .extracting("name", "id").asList()
                .containsExactlyInAnyOrder(orgId.toString(), "some-name");

        assertThat(organisationResponse)
                .extracting("userIds").first()
                .isEqualTo(Collections.singletonList(userId.toString()));

        assertThat(organisationResponse)
                .extracting("userIds").size()
                .isEqualTo(1);
    }
}