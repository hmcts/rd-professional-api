package uk.gov.hmcts.reform.professionalapi.controller.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponseV2;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NAME;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_STATUS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class OrganisationInternalControllerV2Test {
    private OrganisationResponse organisationResponse;
    private OrganisationsDetailResponseV2 organisationsDetailResponse;
    private OrganisationEntityResponseV2 organisationEntityResponse;
    private OrganisationService organisationServiceMock;
    private Organisation organisation;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);

    @InjectMocks
    private OrganisationInternalControllerV2 organisationInternalController;

    @BeforeEach
    void setUp() throws Exception {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        organisationResponse = new OrganisationResponse(organisation);
        organisationsDetailResponse =
                new OrganisationsDetailResponseV2(singletonList(organisation),
                        false, false, true,true);
        organisationEntityResponse =
                new OrganisationEntityResponseV2(organisation,
                        false, true, true,true);
        organisationResponse = new OrganisationResponse(organisation);
        organisationsDetailResponse =
                new OrganisationsDetailResponseV2(singletonList(organisation),
                        false, false, true,true);
        organisationEntityResponse =
                new OrganisationEntityResponseV2(organisation,
                        false, true, true,true);

        organisationServiceMock = mock(OrganisationService.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        organisation.setOrganisationIdentifier("AK57L4T");

        organisationResponse = new OrganisationResponse(organisation);

        organisationEntityResponse = new OrganisationEntityResponseV2(organisation,
                false, false, true,true);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_RetrieveOrganisations() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveAllOrganisationsForV2Api(null))
                .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null,
                null, null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .retrieveAllOrganisationsForV2Api(null);
    }

    @Test
    void test_RetrieveOrganisationByIdWithStatusNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisationForV2Api(any(String.class), any(boolean.class)))
                .thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationInternalController
                .retrieveOrganisations(organisation.getOrganisationIdentifier(), null, 1, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(1);

        verify(organisationServiceMock, times(1))
                .retrieveOrganisationForV2Api(organisation.getOrganisationIdentifier(), true);
    }

    @Test
    void test_RetrieveOrganisationByIdWithStatusNotNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisationForV2Api(any(String.class), any(boolean.class)))
                .thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(organisation
                .getOrganisationIdentifier(), "PENDING", null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .retrieveOrganisationForV2Api(organisation.getOrganisationIdentifier(), true);
    }

    @Test
    void test_RetrieveOrganisationByStatusWithIdNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.findByOrganisationStatusForV2Api(any(), any()))
                .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController
                .retrieveOrganisations(null, "PENDING", null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .findByOrganisationStatusForV2Api(OrganisationStatus.PENDING.name(), null);
    }

    @Test
    void test_RetrieveOrganisationWithPageNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 1, Sort.by(order));

        when(organisationServiceMock.retrieveAllOrganisationsForV2Api(pageable))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, null, 1);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .retrieveAllOrganisationsForV2Api(pageable);
    }

    @Test
    void test_RetrieveOrganisationWithSizeNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(order));

        when(organisationServiceMock.retrieveAllOrganisationsForV2Api(pageable))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController
                .retrieveOrganisations(null, null, 1, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .retrieveAllOrganisationsForV2Api(pageable);
    }

    @Test
    void test_RetrieveOrganisationByStatusWithPagination() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_STATUS).ignoreCase();
        Sort.Order name = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(order).and(Sort.by(name)));

        when(organisationServiceMock.findByOrganisationStatusForV2Api(any(), any()))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, "PENDING", 1, 20);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .findByOrganisationStatusForV2Api(OrganisationStatus.PENDING.name(), pageable);
    }

}