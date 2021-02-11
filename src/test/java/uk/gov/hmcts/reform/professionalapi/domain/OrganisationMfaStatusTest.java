package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationMfaStatusTest {

    private OrganisationMfaStatus organisationMfaStatus;
    private Organisation organisation = new Organisation();

    @Before
    public void setUp() {
        organisationMfaStatus = new OrganisationMfaStatus();
        organisationMfaStatus.setMfaStatus(MFAStatus.EMAIL);
        organisationMfaStatus.setLastUpdated(LocalDateTime.now());
        organisationMfaStatus.setCreated(LocalDateTime.now());

        organisationMfaStatus.setOrganisation(organisation);
    }

    @Test
    public void test_creates_organisation_mfa_status_correctly() {
        assertThat(organisationMfaStatus.getOrganisationId()).isNull();
        assertThat(organisationMfaStatus.getMfaStatus()).isEqualTo(MFAStatus.EMAIL);
        assertThat(organisationMfaStatus.getLastUpdated()).isNotNull();
        assertThat(organisationMfaStatus.getOrganisation()).isNotNull();
    }


}