package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrganisationStatusTest {

    @Test
    void test_shouldReturnTrueWhenPendingisPassed() {
        OrganisationStatus organisationStatus = OrganisationStatus.ACTIVE;

        assertThat(organisationStatus.isActive()).isTrue();
        assertThat(organisationStatus.isPending()).isFalse();
    }

    @Test
    void test_shouldReturnTrueWhenActiveisPassed() {
        OrganisationStatus organisationStatus = OrganisationStatus.PENDING;
        
        assertThat(organisationStatus.isActive()).isFalse();
        assertThat(organisationStatus.isPending()).isTrue();
    }

    @Test
    void test_shouldReturnTrueWhenReviewisPassed() {
        OrganisationStatus organisationStatus = OrganisationStatus.REVIEW;

        assertThat(organisationStatus.isActive()).isFalse();
        assertThat(organisationStatus.isReview()).isTrue();
    }
}
