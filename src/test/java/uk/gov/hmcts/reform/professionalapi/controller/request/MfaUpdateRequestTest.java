package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MfaUpdateRequestTest {

    @Test
    void testMfaUpdateRequest() {

        MFAStatus statusPhone = MFAStatus.PHONE;
        MFAStatus statusNone = MFAStatus.NONE;
        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(statusPhone);
        MfaUpdateRequest mfaUpdateRequest2 = new MfaUpdateRequest(statusPhone);
        mfaUpdateRequest2.setMfa(statusNone);

        assertThat(mfaUpdateRequest.getMfa()).isEqualTo(statusPhone);
        assertThat(mfaUpdateRequest2.getMfa()).isEqualTo(statusNone);
    }

}