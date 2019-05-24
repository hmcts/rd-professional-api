package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class CreateNewUserWithRolesTest extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;
    @Autowired
    private ProfessionalUserRepository professionalUserRepository;
    @Autowired
    private ContactInformationRepository contactInformationRepository;
    @Autowired
    private DxAddressRepository dxAddressRepository;
    @Autowired
    PaymentAccountRepository paymentAccountRepository;
    @Autowired
    PaymentAccountServiceImpl paymentAccountService;
    @Autowired
    ProfessionalUserServiceImpl professionalUserService;

    private ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Before
    public void setUp() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
        dxAddressRepository.deleteAll();
        contactInformationRepository.deleteAll();
        professionalUserRepository.deleteAll();
        paymentAccountRepository.deleteAll();
        organisationRepository.deleteAll();
    }

    @Test
    public void post_request_adds_new_user_to_an_organisation() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("some@email.com")
                .status("PENDING")
                .roles(userRoles)
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse);

        String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");

        assertThat(newUserResponse).isNotNull();
        assertEquals(newUserResponse.get("userIdentifier"), userIdentifierResponse);
    }

    @Test
    public void returns_404_when_organisation_identifier_not_found() {

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation("invalid");

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");
    }


}
