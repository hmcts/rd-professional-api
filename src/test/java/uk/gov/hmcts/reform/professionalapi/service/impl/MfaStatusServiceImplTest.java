package uk.gov.hmcts.reform.professionalapi.service.impl;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MfaStatusServiceImplTest {

    @InjectMocks
    private MfaStatusServiceImpl mfaStatusService;

    @Mock
    private ProfessionalUserRepository professionalUserRepository;
    @Mock
    private ProfessionalUser professionalUser;
    @Mock
    private Organisation organisation;
    @Mock
    private OrganisationRepository organisationRepository;

    private MfaStatusResponse mfaStatusResponse;
    private OrganisationMfaStatus orgMfaStatus;


    @BeforeEach
    void setUp() {
        orgMfaStatus = new OrganisationMfaStatus();
        mfaStatusResponse = new MfaStatusResponse();
        mfaStatusResponse.setMfa(MFAStatus.EMAIL.toString());

        lenient().when(professionalUserRepository.findByUserIdentifier(any())).thenReturn(professionalUser);
        lenient().when(organisation.isOrganisationStatusActive()).thenReturn(true);
        lenient().when(professionalUser.getOrganisation()).thenReturn(organisation);
    }

    @Test
    void test_findMfaStatusByUserId() {
        when(organisation.getOrganisationMfaStatus()).thenReturn(orgMfaStatus);

        ResponseEntity<MfaStatusResponse> mfaStatusResponseEntity = mfaStatusService
                .findMfaStatusByUserId(UUID.randomUUID().toString());

        assertThat(mfaStatusResponseEntity).isNotNull();
        assertThat(mfaStatusResponseEntity.getBody()).isNotNull();
        assertThat(mfaStatusResponseEntity.getBody().getMfa()).isEqualTo(mfaStatusResponse.getMfa());
        verify(professionalUserRepository, times(1)).findByUserIdentifier(any());
        verify(organisation, times(1)).isOrganisationStatusActive();
        verify(professionalUser, times(1)).getOrganisation();
        verify(organisation, times(1)).getOrganisationMfaStatus();
    }

    @Test
    void test_findMfaStatusByUserId_shouldReturn400_whenEmptyUserID() {
        assertThrows(InvalidRequest.class, () ->
                mfaStatusService.findMfaStatusByUserId(StringUtils.EMPTY));
    }

    @Test
    void test_findMfaStatusByUserId_shouldReturn404_whenUserNotFound() {
        when(professionalUserRepository.findByUserIdentifier(any())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () ->
                mfaStatusService.findMfaStatusByUserId(UUID.randomUUID().toString()));
    }

    @Test
    void test_findMfaStatusByUserId_shouldReturn400_whenInactiveOrg() {
        when(organisation.isOrganisationStatusActive()).thenReturn(false);

        assertThrows(InvalidRequest.class, () ->
                mfaStatusService.findMfaStatusByUserId(UUID.randomUUID().toString()));
    }

    @Test
    void test_updateOrgMfaStatus() {
        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(MFAStatus.EMAIL);
        when(organisation.getOrganisationMfaStatus()).thenReturn(new OrganisationMfaStatus());

        mfaStatusService.updateOrgMfaStatus(mfaUpdateRequest, organisation);
        verify(organisation, times(1)).getOrganisationMfaStatus();
        assertEquals(organisation.getOrganisationMfaStatus().getMfaStatus(),mfaUpdateRequest.getMfa());
    }
}