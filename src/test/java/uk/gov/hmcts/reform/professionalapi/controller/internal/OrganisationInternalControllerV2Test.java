package uk.gov.hmcts.reform.professionalapi.controller.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
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
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationSraUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ISO_DATE_TIME_FORMATTER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NAME;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_STATUS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class OrganisationInternalControllerV2Test {
    private OrganisationsDetailResponseV2 organisationsDetailResponse;
    private OrganisationEntityResponseV2 organisationEntityResponse;
    private OrganisationService organisationServiceMock;
    private Organisation organisation;
    private OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;

    private ProfessionalUserService professionalUserServiceMock;

    private OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest;

    private UserCreationRequest userCreationRequest;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;

    private NewUserCreationRequest newUserCreationRequest;

    private ProfessionalUser professionalUser;

    private PrdEnumServiceImpl prdEnumServiceMock;

    private UpdateOrganisationRequestValidator updateOrganisationRequestValidatorMock;

    private UserProfileFeignClient userProfileFeignClient;

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);

    private List<PrdEnum> prdEnumList;

    private OrganisationSraUpdateRequest organisationSraUpdateRequest;

    @InjectMocks
    private OrganisationInternalControllerV2 organisationInternalController;

    @BeforeEach
    void setUp() throws Exception {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");

        userCreationRequest = new UserCreationRequest("some-fname", "some-lname",
                "some@email.com");
        professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        updateOrganisationRequestValidatorMock = mock(UpdateOrganisationRequestValidator.class);
        organisationIdentifierValidatorImpl = mock(OrganisationIdentifierValidatorImpl.class);
        organisationsDetailResponse =
                new OrganisationsDetailResponseV2(singletonList(organisation),
                        false, false, true,true);
        organisationEntityResponse =
                new OrganisationEntityResponseV2(organisation,
                        false, true, true,true);
        organisationsDetailResponse =
                new OrganisationsDetailResponseV2(singletonList(organisation),
                        false, false, true,true);
        organisationEntityResponse =
                new OrganisationEntityResponseV2(organisation,
                        false, true, true,true);

        organisationServiceMock = mock(OrganisationService.class);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        HashSet<UserAccessType> userAccessTypes = new HashSet<>();
        userAccessTypes.add(new UserAccessType("jurisdictionId", "organisationProfileId", "accessTypeId", false));
        newUserCreationRequest = new NewUserCreationRequest("some-name", "some-last-name",
                "some@email.com", userRoles, false, userAccessTypes);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        userProfileFeignClient = mock(UserProfileFeignClient.class);
        organisation.setOrganisationIdentifier("AK57L4T");
        organisationSraUpdateRequest = new OrganisationSraUpdateRequest("sraId");
        organisationOtherOrgsCreationRequest = new OrganisationOtherOrgsCreationRequest("test", "PENDING",
                "Status message",
                "sra-id", "false", "number02", "company-url",
                userCreationRequest, null,
                null,"Doctor",null);

        organisationEntityResponse = new OrganisationEntityResponseV2(organisation,
                false, false, true,true);

        userRoles.add("pui-user-manager");

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_RetrieveOrganisations() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveAllOrganisationsForV2Api(null, null))
                .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null,
                null, null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .retrieveAllOrganisationsForV2Api(null, null);
    }

    @Test
    void test_RetrieveOrganisationsWithSince() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        String since = "2019-08-16T15:00:41";
        LocalDateTime formattedSince = LocalDateTime.parse(since, ISO_DATE_TIME_FORMATTER);

        when(organisationServiceMock.retrieveAllOrganisationsForV2Api(formattedSince, null))
                .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, since,
                null, null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .retrieveAllOrganisationsForV2Api(formattedSince, null);
    }

    @Test
    void test_RetrieveOrganisationByIdWithStatusNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisationForV2Api(any(String.class), any(boolean.class)))
                .thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationInternalController
                .retrieveOrganisations(organisation.getOrganisationIdentifier(), null, null, 1, null);

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
                .getOrganisationIdentifier(), null, "PENDING", null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .retrieveOrganisationForV2Api(organisation.getOrganisationIdentifier(), true);
    }

    @Test
    void test_RetrieveOrganisationByStatusWithIdNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.findByOrganisationStatusForV2Api(any(), any(), any()))
                .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController
                .retrieveOrganisations(null,  null, "PENDING", null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .findByOrganisationStatusForV2Api(null, OrganisationStatus.PENDING.name(), null);
    }

    @Test
    void test_RetrieveOrganisationWithPageNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 1, Sort.by(order));

        when(organisationServiceMock.retrieveAllOrganisationsForV2Api(null, pageable))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, null, null, 1);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .retrieveAllOrganisationsForV2Api(null, pageable);
    }

    @Test
    void test_RetrieveOrganisationWithSizeNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(order));

        when(organisationServiceMock.retrieveAllOrganisationsForV2Api(null, pageable))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController
                .retrieveOrganisations(null, null, null, 1, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .retrieveAllOrganisationsForV2Api(null, pageable);
    }

    @Test
    void test_RetrieveOrganisationByStatusWithPagination() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_STATUS).ignoreCase();
        Sort.Order name = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(order).and(Sort.by(name)));

        when(organisationServiceMock.findByOrganisationStatusForV2Api(any(), any(), any()))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, "PENDING", 1, 20);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .findByOrganisationStatusForV2Api(null, OrganisationStatus.PENDING.name(), pageable);
    }

    @Test
    void testUpdateOrganisation()throws JsonProcessingException {
        OrgAttributeRequest org = new OrgAttributeRequest();
        org.setKey("test-key");
        org.setValue("test-value");

        List<OrgAttributeRequest> orgAttributes = new ArrayList<>();
        orgAttributes.add(org);


        organisationOtherOrgsCreationRequest.setStatusMessage("Company in review");
        organisationOtherOrgsCreationRequest.setStatus("ACTIVE");
        organisationOtherOrgsCreationRequest.setOrgType("testOrgType");

        SuperUser superUser = new SuperUser();
        organisation.setUsers(Collections.singletonList(superUser));

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileCreationResponse);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserById(any())).thenReturn(professionalUser);
        when(userProfileFeignClient.createUserProfile(any(UserProfileCreationRequest.class)))
                .thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                        .status(200).build());


        String orgId = "AK57L4T";
        ResponseEntity<Object> response = organisationInternalController
                .updatesOrganisation(organisationOtherOrgsCreationRequest, orgId, null);

        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(professionalUserServiceMock, times(1)).findProfessionalUserById(any());
    }

    @Test
    void testUpdateOrgSraId() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        String updatedSraId = randomAlphabetic(7);
        organisation.setSraId(updatedSraId);
        organisationSraUpdateRequest.setSraId(updatedSraId);

        assertThat(organisationSraUpdateRequest.getSraId()).isNotEmpty();

        when(organisationServiceMock.updateOrganisationSra(organisationSraUpdateRequest,
            organisation.getOrganisationIdentifier())).thenReturn(new OrganisationsDetailResponseV2(
                List.of(organisation),true, false, false,
            true));

        ResponseEntity<OrganisationsDetailResponseV2> response = organisationInternalController
            .updateOrganisationSra(organisationSraUpdateRequest,organisation.getOrganisationIdentifier());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationIdentifierValidatorImpl, times(1)).validateOrganisationExistsAndActive(
            organisation.getOrganisationIdentifier());
        verify(organisationServiceMock, times(1))
            .updateOrganisationSra(organisationSraUpdateRequest, organisation.getOrganisationIdentifier());

    }


}
