package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;

import static org.assertj.core.api.Assertions.assertThat;


public class MfaStatusResponseTest {

    @Test
    public void test_getMfaStatusResponse() {
        OrganisationMfaStatus orgMfaStatus = new OrganisationMfaStatus();
        MfaStatusResponse mfaStatusResponse = new MfaStatusResponse(orgMfaStatus);

        assertThat(mfaStatusResponse.getMfa()).isEqualTo(orgMfaStatus.getMfaStatus().toString());
    }

}