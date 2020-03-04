package uk.gov.hmcts.reform.professionalapi.controller;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.response.LegacyPbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.LegacyPbaAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;

public class LegacyPbaControllerTest {

    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final UserAttributeRepository userAttributeRepository = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);
    private final UserAttributeServiceImpl userAttributeService = mock(UserAttributeServiceImpl.class);
    private final UserProfileFeignClient userProfileFeignClient = mock(UserProfileFeignClient.class);

    private ProfessionalUserServiceImpl professionalUserServiceImpl;
    private LegacyPbaAccountServiceImpl legacyPbaAccountServiceImpl;
    private LegacyPbaController legacyPbaController;

    private ProfessionalUser professionalUser;
    private Organisation organisation;
    private String email = randomAlphabetic(5) + "@test.com";

    @Before
    public void setUp() {
        professionalUserServiceImpl = new ProfessionalUserServiceImpl(
                organisationRepository, professionalUserRepository, userAttributeRepository,
                prdEnumRepository, userAttributeService, userProfileFeignClient);

        legacyPbaAccountServiceImpl = new LegacyPbaAccountServiceImpl();

        legacyPbaController = new LegacyPbaController(legacyPbaAccountServiceImpl, professionalUserServiceImpl);
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id", "companyN", false, "www.org.com");
        professionalUser = new ProfessionalUser("some-fname", "some-lname", email, organisation);

        organisationRepository.save(organisation);
        professionalUserRepository.save(professionalUser);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_retrieveLegacyPbaAccountsByEmail() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        when(professionalUserRepository.findByEmailAddress(email)).thenReturn(professionalUser);

        ResponseEntity<?> actual = legacyPbaController.retrievePbaAccountsByEmail(email);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(professionalUserRepository, times(1)).findByEmailAddress(email);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_retrieveLegacyPbaAccountsByEmail_throw404WhenUserIsNull() {
        legacyPbaController.retrievePbaAccountsByEmail(any(String.class));
    }

    @Test
    public void test_retrieveLegacyPbaAccountsByEmail_whenPbaIsNullReturnsEmptyPbaList() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        when(professionalUserRepository.findByEmailAddress(email)).thenReturn(professionalUser);

        ResponseEntity<?> actual = legacyPbaController.retrievePbaAccountsByEmail(email);

        assertThat(actual).isNotNull();
        assertThat(((LegacyPbaResponse) actual.getBody()).getPayment_accounts()).isEmpty();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(professionalUserRepository, times(1)).findByEmailAddress(email);
    }
}
