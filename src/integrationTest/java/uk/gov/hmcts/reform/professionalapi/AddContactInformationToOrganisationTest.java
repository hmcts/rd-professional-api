package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createContactInformationCreationRequests;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.getContactInformationList;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;

public class AddContactInformationToOrganisationTest extends AuthorizationEnabledIntegrationTest {

    OrganisationCreationRequest organisationCreationRequest = null;
    Map<String, Object> orgResponse = null;
    String userId = null;
    List<ContactInformationCreationRequest> contactInformationCreationRequests = null;
    String orgId = null;

    @BeforeEach
    public void setUpOrganisationData() {

         organisationCreationRequest = organisationRequestWithAllFields()
                .build();
        orgResponse = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

         orgId = (String) orgResponse.get(ORG_IDENTIFIER);

         userId = updateOrgAndInviteUser(orgId, puiOrgManager);
         contactInformationCreationRequests = getContactInformationList();

    }

    @Test
    void add_contact_informations_to_organisation() {

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("201 CREATED");
    }

    @Test
    void add_contact_informations_to_organisation_returns_201_when_multiple_dxAddresses() {
        List<ContactInformationCreationRequest> contactInformationCreationRequests = createContactInformationCreationRequests();
        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("201 CREATED");
    }

    @Test
    void add_contact_informations_to_organisation_returns_201_when_contact_information_dxAddress_list_is_null() {

        contactInformationCreationRequests = Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("country")
                .county("county")
                .townCity("town-city")
                .uprn("uprn")
                .postCode("some-post-code").dxAddress(null)

                //.dxAddress(Arrays.asList(dxAddressCreationRequest()
                //       .dxNumber("DX 1234567890")
                //       .dxExchange("dxExchange").build()))
                .build());

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();

//        ContactInformationEntityResponse errorResponse = get400ErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        // assertThat(errorResponse.getErrorDescription()).contains("Empty contactInformation value");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("201 CREATED");
    }

    @Test
    void add_contact_informations_to_organisation_returns_201_when_contact_information_dxAddress_list_is_empty() {

        contactInformationCreationRequests = Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("country")
                .county("county")
                .townCity("town-city")
                .uprn("uprn")
                .postCode("some-post-code").dxAddress(new ArrayList<>())

                //.dxAddress(Arrays.asList(dxAddressCreationRequest()
                //       .dxNumber("DX 1234567890")
                //       .dxExchange("dxExchange").build()))
                .build());

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();

//        ContactInformationEntityResponse errorResponse = get400ErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        // assertThat(errorResponse.getErrorDescription()).contains("Empty contactInformation value");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("201 CREATED");
    }

    @Test
    void add_contact_informations_to_organisation_returns_403_when_forbidden_user_role() {

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiFinanceManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        ErrorResponse errorResponse = get404ErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        assertThat(errorResponse.getErrorDescription()).contains("Access is denied");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("403");
    }



    @Test
    void add_contact_informations_to_organisation_returns_401_when_unauthorised_user() {

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,null,null);

        assertThat(addContactsToOrgresponse).isNotNull();
        ErrorResponse errorResponse = get404ErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("401");
    }

    @Test
    void add_contact_informations_to_organisation_returns_404_when_empty_contact_information_list() {

        contactInformationCreationRequests = new ArrayList<>();
        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        ErrorResponse errorResponse = get404ErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        assertThat(errorResponse.getErrorMessage()).contains("Resource not found");
        assertThat(errorResponse.getErrorDescription()).contains("Request is empty");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("404");
    }

    @Test
    void add_contact_informations_to_organisation_returns_400_when_contact_information_addressLine1_is_missing() {

        contactInformationCreationRequests = Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("country")
                .county("county")
                .townCity("town-city")
                .uprn("uprn")
                .postCode("some-post-code")
                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                        .dxNumber("DX 1234567890")
                        .dxExchange("dxExchange").build()))
                .build());

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();

        ContactInformationEntityResponse errorResponse = get400ErrorResponse(addContactsToOrgresponse.get("response_body").toString());

       // assertThat(errorResponse.getErrorDescription()).contains("Empty contactInformation value");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("400");
    }

    @Test
    void add_contact_informations_to_organisation_returns_400_when_contact_information_addressLine1_DxNum_DxExchange_missing() {

        contactInformationCreationRequests = Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("country")
                .county("county")
                .townCity("town-city")
                .uprn("uprn")
                .postCode("some-post-code")
                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                        .dxNumber("this is an invalid dx number")
                        .dxExchange(null).build()))
                .build());

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();

        ContactInformationEntityResponse errorResponse = get400ErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        // assertThat(errorResponse.getErrorDescription()).contains("Empty contactInformation value");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("400");
    }

    @Test
    void add_contact_informations_to_organisation_returns_400_returns_bad_request_when_dx_num_invalid() {
        contactInformationCreationRequests = Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("country")
                .county("county")
                .townCity("town-city")
                .uprn("uprn")
                .postCode("some-post-code")
                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                        .dxNumber("this is an invalid dx number")
                        .dxExchange("dxExchange").build()))
                .build());

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();

        ContactInformationEntityResponse errorResponse = get400ErrorResponse(addContactsToOrgresponse.get("response_body").toString());

       // assertThat(errorResponse.getErrorDescription()).contains("DX Number (max=13) or DX Exchange (max=20) has invalid length");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("400");

    }

    private ErrorResponse get404ErrorResponse(String errorDetails){
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        ErrorResponse errorResponse=null;
        try {
            errorResponse =  mapper.readValue(errorDetails,ErrorResponse.class);
        } catch (JsonProcessingException e) {
            errorResponse = null;
        }
        return errorResponse;
    }

    private ContactInformationEntityResponse get400ErrorResponse(String errorDetails){
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        ContactInformationEntityResponse errorResponse=null;
        try {
            errorResponse =  mapper.readValue(errorDetails,ContactInformationEntityResponse.class);
        } catch (JsonProcessingException e) {
            errorResponse = null;
        }
        return errorResponse;
    }

}