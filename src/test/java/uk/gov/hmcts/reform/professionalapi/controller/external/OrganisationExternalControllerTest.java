package uk.gov.hmcts.reform.professionalapi.controller.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;

public class OrganisationExternalControllerTest {

    @InjectMocks
    private OrganisationExternalController organisationExternalController;

    private OrganisationResponse organisationResponse;
    private OrganisationEntityResponse organisationEntityResponse;
    private OrganisationService organisationServiceMock;
    private PrdEnumServiceImpl prdEnumServiceMock;
    private OrganisationCreationRequest organisationCreationRequest;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    private PrdEnumRepository prdEnumRepository;
    private UserCreationRequest userCreationRequest;
    private Organisation organisation;

    private final PrdEnumId prdEnumId1 = new PrdEnumId(10, "JURISD_ID");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(13, "JURISD_ID");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(3, "PRD_ROLE");

    private final PrdEnum anEnum1 = new PrdEnum(prdEnumId1, "PROBATE", "JURISD_ID");
    private final PrdEnum anEnum2 = new PrdEnum(prdEnumId2, "BULKSCAN", "JURISD_ID");
    private final PrdEnum anEnum3 = new PrdEnum(prdEnumId3, "pui-case-manager", "PRD_ROLE");

    private List<PrdEnum> prdEnumList;
    private List<String> jurisdEnumIds;
    private List<Jurisdiction> jurisdictions;

    @Before
    public void setUp() throws Exception {
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        organisationServiceMock = mock(OrganisationService.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        prdEnumRepository = mock(PrdEnumRepository.class);

        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id", "companyN", false, "www.org.com");
        organisationResponse = new OrganisationResponse(organisation);
        SuperUser superUser = new SuperUser("some-fname", "some-lname", "some-email-address", organisation);
        organisationEntityResponse = new OrganisationEntityResponse(organisation, false);

        prdEnumList = new ArrayList<>();
        prdEnumList.add(anEnum1);
        prdEnumList.add(anEnum2);
        prdEnumList.add(anEnum3);

        jurisdEnumIds = new ArrayList<>();
        jurisdEnumIds.add("Probate");
        jurisdEnumIds.add("Bulk Scanning");
        jurisdictions = new ArrayList<>();

        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("Probate");
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId("Bulk Scanning");
        jurisdictions.add(jurisdiction1);
        jurisdictions.add(jurisdiction2);

        userCreationRequest = new UserCreationRequest("some-fname", "some-lname", "some@email.com", jurisdictions);
        organisationCreationRequest = new OrganisationCreationRequest("test", "PENDING", "sra-id", "false", "number02", "company-url", userCreationRequest, null, null);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateOrganisation() {
        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequest)).thenReturn(organisationResponse);
        when(prdEnumServiceMock.getPrdEnumByEnumType(any())).thenReturn(jurisdEnumIds);
        when(prdEnumRepository.findAll()).thenReturn(prdEnumList);

        ResponseEntity<?> actual = organisationExternalController.createOrganisationUsingExternalController(organisationCreationRequest);

        verify(organisationCreationRequestValidatorMock, times(1)).validate(any(OrganisationCreationRequest.class));
        verify(organisationServiceMock, times(1)).createOrganisationFrom(organisationCreationRequest);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void testRetrieveOrganisationByIdentifier() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        String id = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.retrieveOrganisation(id)).thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationExternalController.retrieveOrganisationUsingOrgIdentifier(id);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1)).retrieveOrganisation(eq(id));
    }
}