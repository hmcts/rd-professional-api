package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrganisationInfoTest {

    private final String organisationIdentifier = "organisationIdentifier";
    private final OrganisationStatus status = OrganisationStatus.ACTIVE;
    private final LocalDateTime lastUpdated = LocalDateTime.now();
    private final List<String> organisationProfileIds = List.of("orgProfileId1", "orgProfileId2");

    @Test
    void test_OrganisationInfo() {
        OrganisationInfo organisationInfo = new OrganisationInfo(organisationIdentifier, status, lastUpdated,
                organisationProfileIds);

        assertThat(organisationInfo.getOrganisationIdentifier()).isEqualTo(organisationIdentifier);
        assertThat(organisationInfo.getStatus()).isEqualTo(status);
        assertThat(organisationInfo.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(organisationInfo.getOrganisationProfileIds()).hasSize(2);
        assertThat(organisationInfo.getOrganisationProfileIds().get(0)).isEqualTo("orgProfileId1");
        assertThat(organisationInfo.getOrganisationProfileIds().get(1)).isEqualTo("orgProfileId2");
    }

    @Test
    void test_OrganisationInfoNoArgs() {
        OrganisationInfo organisationInfo = new OrganisationInfo();
        organisationInfo.setOrganisationIdentifier(organisationIdentifier);
        organisationInfo.setStatus(status);
        organisationInfo.setLastUpdated(lastUpdated);
        organisationInfo.setOrganisationProfileIds(organisationProfileIds);

        assertThat(organisationInfo.getOrganisationIdentifier()).isEqualTo(organisationIdentifier);
        assertThat(organisationInfo.getStatus()).isEqualTo(status);
        assertThat(organisationInfo.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(organisationInfo.getOrganisationProfileIds()).hasSize(2);
        assertThat(organisationInfo.getOrganisationProfileIds().get(0)).isEqualTo("orgProfileId1");
        assertThat(organisationInfo.getOrganisationProfileIds().get(1)).isEqualTo("orgProfileId2");
    }
}
