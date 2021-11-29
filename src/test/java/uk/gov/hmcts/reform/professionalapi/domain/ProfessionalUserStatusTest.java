package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus.PENDING;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserStatusTest {

    @Test
    void test_shouldReturnTrueWhenPendingisPassed() {
        ProfessionalUserStatus status = PENDING;
        assertThat(status).isEqualTo(PENDING);
    }

    @Test
    void test_shouldReturnTrueWhenActiveisPassed() {
        ProfessionalUserStatus status = ACTIVE;
        assertThat(status).isEqualTo(ACTIVE);;
    }
}
