package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public class OrganisationStatusTest {

    @Test
    public void shouldReturnTrueWhenPendingisPassed() {
        OrganisationStatus organisationStatus = OrganisationStatus.ACTIVE;
        assertThat(organisationStatus.isActive()).isEqualTo(true);
        assertThat(organisationStatus.isPending()).isEqualTo(false);
    }

    @Test
    public void shouldReturnTrueWhenActiveisPassed() {
        OrganisationStatus organisationStatus = OrganisationStatus.PENDING;
        assertThat(organisationStatus.isActive()).isEqualTo(false);
        assertThat(organisationStatus.isPending()).isEqualTo(true);
    }
}
