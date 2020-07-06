package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public class OrganisationStatusTest {

    @Test
    public void shouldReturnTrueWhenPendingisPassed() {
        OrganisationStatus organisationStatus = OrganisationStatus.ACTIVE;

        assertThat(organisationStatus.isActive()).isTrue();
        assertThat(organisationStatus.isPending()).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenActiveisPassed() {
        OrganisationStatus organisationStatus = OrganisationStatus.PENDING;
        
        assertThat(organisationStatus.isActive()).isFalse();
        assertThat(organisationStatus.isPending()).isTrue();
    }
}
