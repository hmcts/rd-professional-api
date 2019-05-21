package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;


public class FindUsersByOrganisationTest extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ProfessionalUserRepository professionalUserRepository;

    @Autowired
    private ContactInformationRepository contactInformationRepository;

    @Autowired
    private DxAddressRepository dxAddressRepository;

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

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

    public String createOrganisationRequest() {
        uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get("organisationIdentifier");
    }

    @Test
    public void can_retrieve_users_with_showDeleted_true_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier , "True");
        validateUsers(response);
    }

    @Test
    public void can_retrieve_users_with_showDeleted_false_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier , "False");
        validateUsers(response);
    }

    @Test
    public void can_retrieve_users_with_showDeleted_null_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier , null);
        validateUsers(response);

    }

    @Test
    public void retrieve_users_with_invalid_organisationIdentifier_should_return_status_400() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation("123" , "False");
        assertThat(response.get("http_status")).isEqualTo("400");

    }

    @Test
    public void retrieve_users_with_non_existing_organisationIdentifier_should_return_status_404() {
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(UUID.randomUUID().toString() , "False");
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    private void validateUsers(Map<String, Object> response){
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isGreaterThan(0);

        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("userIdentifier")).isNull();
        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(professionalUsersResponse.get("status")).isEqualTo(ProfessionalUserStatus.PENDING.name());
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(0);
    }


}
