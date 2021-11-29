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
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.domain.MFAStatus.EMAIL;

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
        mfaStatusResponse.setMfa(EMAIL.toString());

        lenient().when(professionalUserRepository.findByUserIdentifier(any())).thenReturn(professionalUser);
        lenient().when(organisation.isOrganisationStatusActive()).thenReturn(true);
        lenient().when(professionalUser.getOrganisation()).thenReturn(organisation);
    }

    @Test
    void test_findMfaStatusByUserId() {
        when(organisation.getOrganisationMfaStatus()).thenReturn(orgMfaStatus);

        String uuid = UUID.randomUUID().toString();

        ResponseEntity<MfaStatusResponse> mfaStatusResponseEntity = mfaStatusService
                .findMfaStatusByUserId(uuid);

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

        String uuid = UUID.randomUUID().toString();

        assertThrows(ResourceNotFoundException.class, () ->
                mfaStatusService.findMfaStatusByUserId(uuid));
    }

    @Test
    void test_findMfaStatusByUserId_shouldReturn400_whenInactiveOrg() {
        when(organisation.isOrganisationStatusActive()).thenReturn(false);

        String uuid = UUID.randomUUID().toString();

        assertThrows(InvalidRequest.class, () ->
                mfaStatusService.findMfaStatusByUserId(uuid));
    }

    @Test
    void test_updateOrgMfaStatus() {
        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(EMAIL);
        OrganisationMfaStatus organisationMfaStatus = mock(OrganisationMfaStatus.class);
        when(organisation.getOrganisationMfaStatus()).thenReturn(organisationMfaStatus);
        when(organisationMfaStatus.getMfaStatus()).thenReturn(EMAIL);

        ResponseEntity<Object> response = mfaStatusService.updateOrgMfaStatus(mfaUpdateRequest, organisation);

        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertEquals(organisation.getOrganisationMfaStatus().getMfaStatus(), mfaUpdateRequest.getMfa());

        verify(organisation, times(2)).getOrganisationMfaStatus();
        verify(organisationMfaStatus, times(1)).setMfaStatus(EMAIL);
    }
}