package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus.PENDING;

import org.junit.Test;

public class ProfessionalUserStatusTest {

    @Test
    public void test_shouldReturnTrueWhenPendingisPassed() {
        ProfessionalUserStatus status = PENDING;
        assertThat(status).isEqualTo(PENDING);
    }

    @Test
    public void test_shouldReturnTrueWhenActiveisPassed() {
        ProfessionalUserStatus status = ACTIVE;
        assertThat(status).isEqualTo(ACTIVE);;
    }
}
