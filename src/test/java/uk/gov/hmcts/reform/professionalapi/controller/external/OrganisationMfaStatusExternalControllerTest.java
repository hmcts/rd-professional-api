package uk.gov.hmcts.reform.professionalapi.controller.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationMfaStatusExternalControllerTest {

    @InjectMocks
    private OrganisationMfaStatusExternalController orgMfaStatusExternalController;

    @Mock
    private MfaStatusService mfaStatusServicemock;
    private MfaStatusResponse mfaStatusResponse;
    private OrganisationMfaStatus organisationMfaStatus;


    @Before
    public void setUp() {
        organisationMfaStatus = new OrganisationMfaStatus();
        mfaStatusResponse = new MfaStatusResponse(organisationMfaStatus);
    }

    @Test
    public void test_retrieveMfaStatusByUserId() {

        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        String id = UUID.randomUUID().toString();

        when(mfaStatusServicemock.findMfaStatusByUserId(id)).thenReturn(mfaStatusResponse);

        ResponseEntity<?> actual = orgMfaStatusExternalController.retrieveMfaStatusByUserId(id);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(mfaStatusServicemock, times(1)).findMfaStatusByUserId(id);
    }

}