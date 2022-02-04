package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ADDRESS_LIST_IS_EMPTY;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_REQUEST_IS_EMPTY;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ORG_ADDRESS;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ORG_IDS_DOES_NOT_MATCH;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithMultipleAddressAllFields;


public class DeleteContactInformationIntegrationTest extends AuthorizationEnabledIntegrationTest {

    OrganisationCreationRequest organisationCreationRequest = null;
    Map<String, Object> orgResponse = null;
    String userId = null;
    String orgId = null;
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    ContactInformationRepository contactInformationRepository;

    public void setUpSingleContactAddOrganisationData() {
        organisationCreationRequest = organisationRequestWithAllFields()
                .build();
        commonProcessForORg();
    }

    public String setUpMultipleContactAddOrganisationData() {
        organisationCreationRequest = organisationRequestWithMultipleAddressAllFields()
                .build();
        return commonProcessForORg();
    }

    private String commonProcessForORg() {
        orgResponse = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        orgId = (String) orgResponse.get(ORG_IDENTIFIER);
        userId = updateOrgAndInviteUser(orgId, puiOrgManager);
        return orgId;
    }


    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_empty_array() {
        setUpSingleContactAddOrganisationData();
        String json = "[]";
        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(addressResponse);
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ERROR_MSG_ADDRESS_LIST_IS_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ADDRESS_LIST_IS_EMPTY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_empty_single_object() {
        setUpSingleContactAddOrganisationData();
        String json = "[\n"
                + "    {}\n"
                + "]";
        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(addressResponse);
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ERROR_MSG_REQUEST_IS_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_empty_multiple_object() {
        setUpSingleContactAddOrganisationData();
        String json = "[\n"
                + "    {\n"
                + "\n"
                + "    },\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "]";
        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        ErrorResponse errorResponse = convertJsonToErrorResponseObj(addressResponse);
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ERROR_MSG_REQUEST_IS_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_all_three_contactInformation_from_org_address() {
        String orgId = setUpMultipleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();

        String addressId01 = "\"" + "addressId" + "\"" + ":" + "\"" + contacts.get(0).getId().toString() + "\"";
        String addressId02 = "\"" + "addressId" + "\"" + ":" + "\"" + contacts.get(1).getId().toString() + "\"";
        String addressId03 = "\"" + "addressId" + "\"" + ":" + "\"" + contacts.get(2).getId().toString() + "\"";

        var addressIdList = contacts.stream()
                .map(ci -> ci.getId().toString()).collect(Collectors.toList());

        String json = "[\n"
                + "    {\n"
                +       addressId01 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId02 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId03 + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        Organisation organisationAfterDeletion = organisationRepository.findByOrganisationIdentifier(orgId);
        assertThat(organisationAfterDeletion.getContactInformation().size()).isEqualTo(3);
        assertThat(organisationAfterDeletion.getContactInformation().stream()
                .allMatch(ci -> addressIdList.contains(ci.getId().toString()))).isTrue();
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_ADDRESS);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_one_contactInformation_from_org_address() {
        String orgId = setUpMultipleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();
        String addressIdString = contacts.get(0).getId().toString();
        String addressId = "\"" + "addressId" + "\"" + ":" + "\"" + addressIdString + "\"";

        String json = "[\n"
                + "    {\n"
                +       addressId + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        Organisation organisationAfterDeletion = organisationRepository.findByOrganisationIdentifier(orgId);
        assertThat(organisationAfterDeletion.getContactInformation().size()).isEqualTo(2);
        assertThat(organisationAfterDeletion.getContactInformation().stream()
                .noneMatch(ci -> addressIdString.equals(ci.getId().toString()))).isTrue();
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "204 NO_CONTENT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_two_contactInformation_from_org_address() {
        String orgId = setUpMultipleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();

        String addressId01 = "\"" + "addressId" + "\"" + ":" + "\"" + contacts.get(0).getId().toString() + "\"";
        String addressId02 = "\"" + "addressId" + "\"" + ":" + "\"" + contacts.get(1).getId().toString() + "\"";

        var addressIdList = contacts.stream()
                .limit(2)
                .map(ci -> ci.getId().toString()).collect(Collectors.toList());

        String json = "[\n"
                + "    {\n"
                +       addressId01 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId02 + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        Organisation organisationAfterDeletion = organisationRepository.findByOrganisationIdentifier(orgId);
        assertThat(organisationAfterDeletion.getContactInformation().size()).isEqualTo(1);
        assertThat(organisationAfterDeletion.getContactInformation().stream()
                .noneMatch(ci -> addressIdList.contains(ci.getId().toString()))).isTrue();
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "204 NO_CONTENT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_single_non_exist_address_from_multiAdd_org() {
        setUpMultipleContactAddOrganisationData();
        String addressId = "\"" + "addressId" + "\"" + ":" + "\"" + "1234" + "\"";

        String json = "[\n"
                + "    {\n"
                +       addressId + "\n"
                + "    }\n"
                + "]";
        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "404");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_IDS_DOES_NOT_MATCH);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_one_contactInformation_from_singleAdd_org() {
        setUpSingleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();
        String addressIdString = contacts.get(0).getId().toString();
        String addressId = "\"" + "addressId" + "\"" + ":" + "\"" + addressIdString + "\"";

        String json = "[\n"
                + "    {\n"
                +       addressId + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        assertThat(contacts.size()).isEqualTo(1);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_ADDRESS);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_single_non_exist_address_from_singleAdd_org() {
        setUpSingleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();
        String addressId = "\"" + "addressId" + "\"" + ":" + "\"" + "1234" + "\"";

        String json = "[\n"
                + "    {\n"
                +       addressId + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        assertThat(contacts.size()).isEqualTo(1);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "404");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_IDS_DOES_NOT_MATCH);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_single_non_exist_null_address_from_singleAdd_org() {
        setUpSingleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();
        String addressId = "\"" + "addressId" + "\"" + ":" + "\"" + "null" + "\"";

        String json = "[\n"
                + "    {\n"
                +       addressId + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        assertThat(contacts.size()).isEqualTo(1);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "404");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_IDS_DOES_NOT_MATCH);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_multiple_non_exist_address_from_singleAdd_org() {
        setUpSingleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();

        String addressId01 = "\"" + "addressId" + "\"" + ":" + "\"" + "1234" + "\"";
        String addressId02 = "\"" + "addressId" + "\"" + ":" + "\"" + "2345" + "\"";
        String addressId03 = "\"" + "addressId" + "\"" + ":" + "\"" + "3456" + "\"";

        String json = "[\n"
                + "    {\n"
                +       addressId01 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId02 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId03 + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "404");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_IDS_DOES_NOT_MATCH);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_multiple_invalid_data_01() {
        setUpSingleContactAddOrganisationData();
        String addressId01 = "\"" + "addressId" + "\"" + ":" + "\"" + "" + "\"";
        String addressId02 = "\"" + "addressId" + "\"" + ":" + "\"" + "" + "\"";
        String addressId03 = "\"" + "addressId" + "\"" + ":" + "\"" + "" + "\"";

        String json = "[\n"
                + "    {\n"
                +       addressId01 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId02 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId03 + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_multiple_invalid_data_02() {
        setUpSingleContactAddOrganisationData();
        String addressId01 = "\"" + "addressId" + "\"" + ":" + "\"" + null + "\"";
        String addressId02 = "\"" + "addressId" + "\"" + ":" + "\"" + null + "\"";
        String addressId03 = "\"" + "addressId" + "\"" + ":" + "\"" + "" + "\"";

        String json = "[\n"
                + "    {\n"
                +       addressId01 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId02 + "\n"
                + "    },\n"
                + "    {\n"
                +       addressId03 + "\n"
                + "    }\n"
                + "]";

        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }































    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_one_empty_single_object() {
        setUpSingleContactAddOrganisationData();
        String json = "[\n"
                + "    {\n"
                + "        \n"
                + "    },\n"
                + "    {\n"
                + "        \"addressId\": \"2aacfee6-b76a-42d1-92cb-6c690adc733f\"\n"
                + "    }\n"
                + "]";
        var requestArrayList = convertJsonRequestToRequestObj(json);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        ErrorResponse errorResponse = convertJsonToErrorResponseObj(addressResponse);
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ERROR_MSG_REQUEST_IS_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_malformed_request() {
        setUpSingleContactAddOrganisationData();
        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                null,
                                puiOrgManager,
                                userId);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(MALFORMED_JSON.getErrorMessage());
    }


    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_null_address_request() {
        setUpSingleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest(null);
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));
        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_empty_address_request() {
        setUpSingleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest("");
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_one_empty_address_request() {
        setUpSingleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest("");
        var deleteMultipleAddressRequest02 = new DeleteMultipleAddressRequest("test");
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest, deleteMultipleAddressRequest02));

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }

    private List<DeleteMultipleAddressRequest> convertJsonRequestToRequestObj(String request) {
        List<DeleteMultipleAddressRequest> deleteRequest = null;
        try {
            deleteRequest = Arrays.asList(mapper.readValue(request, DeleteMultipleAddressRequest[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return deleteRequest;
    }

    private ErrorResponse convertJsonToErrorResponseObj(Map<String, Object> pbaResponse) {
        String reason = (String) pbaResponse.get("response_body");

        ErrorResponse errorResponse = null;
        try {
            errorResponse = mapper.readValue(reason, ErrorResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return errorResponse;
    }


}
