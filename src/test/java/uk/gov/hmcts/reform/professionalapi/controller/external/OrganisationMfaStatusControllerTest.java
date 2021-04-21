package uk.gov.hmcts.reform.professionalapi.controller.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationMfaStatusControllerTest {

    @InjectMocks
    private OrganisationMfaStatusController orgMfaStatusExternalController;

    @Mock
    private MfaStatusService mfaStatusServiceMock;
    private ResponseEntity<MfaStatusResponse> mfaStatusResponseEntity;

    @Test
    public void test_retrieveMfaStatusByUserId() {

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