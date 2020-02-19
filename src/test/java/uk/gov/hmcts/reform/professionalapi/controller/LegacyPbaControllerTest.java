package uk.gov.hmcts.reform.professionalapi.controller;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.LegacyPbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.service.impl.LegacyPbaAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;

public class LegacyPbaControllerTest {

    private LegacyPbaAccountServiceImpl legacyPbaAccountServiceImplMock;
    private ProfessionalUserServiceImpl professionalUserServiceImplMock;
    private ProfessionalUser professionalUserMock;
    private Organisation organisationMock;

    @InjectMocks
    private LegacyPbaController legacyPbaController;

    @Before
    public void setUp() {
        professionalUserServiceImplMock = mock(ProfessionalUserServiceImpl.class);
        professionalUserMock = mock(ProfessionalUser.class);
        legacyPbaAccountServiceImplMock = mock(LegacyPbaAccountServiceImpl.class);
        organisationMock = mock(Organisation.class);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_retrieveLegacyPbaAccountsByEmail() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        String email = randomAlphabetic(5) + "@test.com";
        List<String> pbaAccounts = new ArrayList<>();
        pbaAccounts.add("PBA1234567");

        when(professionalUserServiceImplMock.findProfessionalUserByEmailAddress(email)).thenReturn(professionalUserMock);
        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(legacyPbaAccountServiceImplMock.findLegacyPbaAccountByUserEmail(professionalUserMock)).thenReturn(pbaAccounts);

        ResponseEntity<?> actual = legacyPbaController.retrievePbaAccountsByEmail(email);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(professionalUserServiceImplMock,
                times(1))
                .findProfessionalUserByEmailAddress(email);

    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_retrieveLegacyPbaAccountsByEmail_throw404WhenUserIsNull() {
        when(professionalUserServiceImplMock.findProfessionalUserByEmailAddress(any(String.class))).thenReturn(null);

        ResponseEntity<?> actual = legacyPbaController.retrievePbaAccountsByEmail(any(String.class));
    }

    @Test
    public void test_retrieveLegacyPbaAccountsByEmail_whenPbaIsNullReturnsEmptyPbaList() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        String email = randomAlphabetic(5) + "@test.com";

        when(professionalUserServiceImplMock.findProfessionalUserByEmailAddress(email)).thenReturn(professionalUserMock);
        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(legacyPbaAccountServiceImplMock.findLegacyPbaAccountByUserEmail(professionalUserMock)).thenReturn(null);

        ResponseEntity<?> actual = legacyPbaController.retrievePbaAccountsByEmail(email);

        assertThat(actual).isNotNull();
        assertThat(((LegacyPbaResponse)actual.getBody()).getPayment_accounts()).isEmpty();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(professionalUserServiceImplMock,
                times(1))
                .findProfessionalUserByEmailAddress(email);
    }

}
