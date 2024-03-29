package uk.gov.hmcts.reform.professionalapi.controller.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class OrganisationMfaStatusControllerTest {

    @InjectMocks
    private OrganisationMfaStatusController orgMfaStatusExternalController;

    @Mock
    private MfaStatusService mfaStatusServiceMock;
    private ResponseEntity<MfaStatusResponse> mfaStatusResponseEntity;

    @Test
    void test_retrieveMfaStatusByUserId() {
        mfaStatusResponseEntity = ResponseEntity.status(HttpStatus.OK).body(new MfaStatusResponse());
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        String id = UUID.randomUUID().toString();

        when(mfaStatusServiceMock.findMfaStatusByUserId(id)).thenReturn(mfaStatusResponseEntity);

        ResponseEntity<?> actual = orgMfaStatusExternalController.retrieveMfaStatusByUserId(id);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(mfaStatusServiceMock, times(1)).findMfaStatusByUserId(id);
    }

}