package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.domain.MFAStatus.EMAIL;

@ExtendWith(MockitoExtension.class)
class OrganisationMfaStatusTest {

    private OrganisationMfaStatus organisationMfaStatus;
    private Organisation organisation = new Organisation();

    @BeforeEach
    void setUp() {
        organisationMfaStatus = new OrganisationMfaStatus();
        organisationMfaStatus.setLastUpdated(LocalDateTime.now());
        organisationMfaStatus.setCreated(LocalDateTime.now());
        organisationMfaStatus.setOrganisation(organisation);
    }

    @Test
    void test_creates_organisation_default_mfa_status_correctly() {
        assertThat(organisationMfaStatus.getOrganisationId()).isNull();
        assertThat(organisationMfaStatus.getMfaStatus()).isEqualTo(EMAIL);
        assertThat(organisationMfaStatus.getLastUpdated()).isNotNull();
        assertThat(organisationMfaStatus.getCreated()).isNotNull();
        assertThat(organisationMfaStatus.getOrganisation()).isNotNull();
    }


}