package uk.gov.hmcts.reform.professionalapi.service.impl;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class MfaStatusServiceImplTest {

    @InjectMocks
    private MfaStatusServiceImpl mfaStatusService;

    @Mock
    private ProfessionalUserRepository professionalUserRepository;
    @Mock
    private ProfessionalUser professionalUser;
    @Mock
    private Organisation organisation;

    @Test
    public void test_findMfaStatusByUserId() {
        when(professionalUserRepository.findByUserIdentifier(any())).thenReturn(professionalUser);
        when(organisation.isOrganisationStatusActive()).thenReturn(true);
        when(professionalUser.getOrganisation()).thenReturn(organisation);
        when(organisation.getOrganisationMfaStatus()).thenReturn(new OrganisationMfaStatus());

        MfaStatusResponse mfaStatusResponse = mfaStatusService.findMfaStatusByUserId(UUID.randomUUID().toString());

        assertThat(mfaStatusResponse).isNotNull();
        verify(professionalUserRepository, times(1)).findByUserIdentifier(any());
    }

    @Test(expected = InvalidRequest.class)
    public void test_findMfaStatusByUserId_shouldReturn400_whenEmptyUserID() {
        // Unnecessary mock.
        //when(professionalUserRepository.findByUserIdentifier(any())).thenReturn(professionalUser)

        mfaStatusService.findMfaStatusByUserId(StringUtils.EMPTY);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_findMfaStatusByUserId_shouldReturn404_whenUserNotFound() {
        when(professionalUserRepository.findByUserIdentifier(any())).thenReturn(null);

        mfaStatusService.findMfaStatusByUserId(UUID.randomUUID().toString());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_findMfaStatusByUserId_shouldReturn404_whenInactiveOrg() {
        when(professionalUserRepository.findByUserIdentifier(any())).thenReturn(professionalUser);
        when(organisation.isOrganisationStatusActive()).thenReturn(false);
        when(professionalUser.getOrganisation()).thenReturn(organisation);

        mfaStatusService.findMfaStatusByUserId(UUID.randomUUID().toString());
    }
}