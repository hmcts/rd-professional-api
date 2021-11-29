package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MfaStatusResponseTest {

    @Test
    void test_getMfaStatusResponse() {
        OrganisationMfaStatus orgMfaStatus = new OrganisationMfaStatus();
        MfaStatusResponse mfaStatusResponse = new MfaStatusResponse();
        mfaStatusResponse.setMfa(orgMfaStatus.getMfaStatus().toString());

        assertThat(mfaStatusResponse.getMfa()).isEqualTo(orgMfaStatus.getMfaStatus().toString());
    }

}