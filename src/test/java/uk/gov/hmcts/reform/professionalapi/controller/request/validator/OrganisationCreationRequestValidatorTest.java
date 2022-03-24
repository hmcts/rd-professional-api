package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidContactInformations;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_INVALID_STATUS_PASSED;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.isInputOrganisationStatusValid;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.PENDING;

@ExtendWith(MockitoExtension.class)
class OrganisationCreationRequestValidatorTest {

    @Mock //must be mocked as it is in interface
    private RequestValidator validator1;
    @Mock //must be mocked as it is in interface
    private RequestValidator validator2;

    private OrganisationCreationRequestValidator organisationCreationRequestValidator;
    private UserCreationRequest userCreationRequest;
    private OrganisationCreationRequest organisationCreationRequest;
    private Exception myException;

    @BeforeEach
    void setup() {
        organisationCreationRequestValidator = new OrganisationCreationRequestValidator(asList(validator1, validator2));
        userCreationRequest = UserCreationRequest.aUserCreationRequest().firstName("fName").lastName("lName")
                .email("test@email.com").build();
        organisationCreationRequest = new OrganisationCreationRequest("Company", "PENDING", "SraId",
                "true", null, "12345678", "www.company.com", userCreationRequest,
                new HashSet<>(), null,null,null);
    }

    @Test
    void test_CallsAllValidators() {
        organisationCreationRequestValidator.validate(organisationCreationRequest);

        verify(validator1, times(1)).validate(organisationCreationRequest);
        verify(validator2, times(1)).validate(organisationCreationRequest);

        assertThat(OrganisationCreationRequestValidator.contains(OrganisationStatus.PENDING.name())).isTrue();
        assertThat(OrganisationCreationRequestValidator.contains("pend")).isFalse();
    }

    @Test
    void test_validateOrganisationIdentifierNull() {
        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationCreationRequestValidator.validateOrganisationIdentifier(null));
    }

    @Test
    void test_validateOrganisationIdentifierTooShort() {
        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationCreationRequestValidator.validateOrganisationIdentifier("AB"));
    }

    @Test
    void test_validateOrganisationIdentifierWrongFormat() {
        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationCreationRequestValidator.validateOrganisationIdentifier("@@@@@@@"));
    }

    @Test
    void test_validateOrganisationIdentifierNoException() {
        assertDoesNotThrow(() ->
                organisationCreationRequestValidator.validateOrganisationIdentifier("Q90SB9S"));
    }

    @Test
    void test_contains() {
        assertThat(OrganisationCreationRequestValidator.contains("quhajdsajsh")).isFalse();
        assertThat(OrganisationCreationRequestValidator.contains("PENDING")).isTrue();
    }

    @Test
    void test_isOrganisationActive_Pending() {
        Organisation myOrgg = new Organisation();
        myOrgg.setStatus(OrganisationStatus.PENDING);
        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.isOrganisationActive(myOrgg));
    }

    @Test
    void test_isOrganisationActive_Active() {
        Organisation organisation = new Organisation();
        organisation.setStatus(OrganisationStatus.ACTIVE);
        assertDoesNotThrow(() ->
                organisationCreationRequestValidator.isOrganisationActive(organisation));
    }

    @Test
    void test_isOrganisationActive_Null() {
        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationCreationRequestValidator.isOrganisationActive(null));
    }

    @Test
    void test_isOrganisationActive_Empty() {
        Organisation organisation = new Organisation();
        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.isOrganisationActive(organisation));
    }

    @Test
    void test_validateOrganisationRequest() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("");
        OrganisationCreationRequest orgReq = new OrganisationCreationRequest("", "", "",
                "true", null,"", "", null, paymentAccounts,
                null,null,null);
        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.validateOrganisationRequest(orgReq));
    }

    @Test
    void test_requestValues() {
        Assertions.assertThatThrownBy(() -> organisationCreationRequestValidator.requestValues(""))
                .isExactlyInstanceOf(InvalidRequest.class);
    }

    @Test
    void test_requestContactInformation() {
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest(null,"", null, null,
                null, null, null, null, null);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.requestContactInformation(contactList));
    }

    @Test
    void test_requestContactInformationDxAddwithDxNumerHasSpecialChars() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 1*2$3&4@",
                "DxExchange");
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("A","A", "A", "A", "A",
                "A", "A", "A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.requestContactInformation(contactList));
    }

    @Test
    void test_requestContactInformationDxAddwithEmpty() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 1234567890", null);
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("A","A", "A", "A", "A",
                "A", "A", "A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.requestContactInformation(contactList));

        DxAddressCreationRequest dxRequest1 = new DxAddressCreationRequest(null, "DxExchange");
        List<DxAddressCreationRequest> dxList1 = new ArrayList<>();
        dxList1.add(dxRequest1);
        ContactInformationCreationRequest contactInfoCreateRequest1
                = new ContactInformationCreationRequest("A","A", "A", "A", "A",
                "A", "A", "A", dxList1);
        List<ContactInformationCreationRequest> contactList1 = new ArrayList<>();
        contactList1.add(contactInfoCreateRequest1);

        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.requestContactInformation(contactList1));
    }

    @Test
    void test_requestContactInformationDxAddwithInvalidLength() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 12345678900000",
                "DxExchange1234567890");
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("A","A", "A", "A",
                "A", "A", "A", "A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.requestContactInformation(contactList));

        DxAddressCreationRequest dxRequest1 = new DxAddressCreationRequest("DX 1234567890",
                "DxExchangeDxExchange123");
        List<DxAddressCreationRequest> dxList1 = new ArrayList<>();
        dxList1.add(dxRequest1);
        ContactInformationCreationRequest contactInfoCreateRequest1
                = new ContactInformationCreationRequest("A","A", "A", "A",
                "A", "A", "A", "A", dxList1);
        List<ContactInformationCreationRequest> contactList1 = new ArrayList<>();
        contactList1.add(contactInfoCreateRequest1);

        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.requestContactInformation(contactList1));
    }

    @Test
    void test_requestContactInformationDxAddwithvalid() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 1234567890",
                "DxExchange1234567890");
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("A","A", "A", "A",
                "A", "A", "A", "A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        assertDoesNotThrow(() ->
                organisationCreationRequestValidator.requestContactInformation(contactList));
    }

    @Test
    void test_should_validate_valid_email_and_should_not_throw_exception() {

        String[] validEmails = new String[]{
            "test@test.com", "test1@test.com",
            "tes't@test.com", "Email.100@test.com", "email111@email.com", "email.100@email.com.au",
            "email@etst.com.com", "email_231_a@email.com", "email_100@test-test.ABC.CoM", "email-100@test.com",
            "email-100@test.net", "email+100@test.com", "emAil-100@test-test.com", "e.mAil-100@test-test.com"};

        for (String email : validEmails) {
            assertDoesNotThrow(() ->
                    OrganisationCreationRequestValidator.validateEmail(email));
        }

    }

    @Test
    void test_should_validate_valid_email_and_should_throw_exception() {
        String[] validEmails = new String[]{
            "あいうえお@example.com", "emAil@1.com", "email@111",
            "email@.com.my", "email123@test.", "email123@.com", "email123@.com.com", ".email@email.com",
            "email()*@test.com", "eEmail()*@test.com", "email@%*.com", "email@email@test.com",
            "email@test.com.", "email..2002@test.com@", "-email.23@email.com", "$email.3@email.com",
            "!email@email.com", "+@test61371@test.com", "_email.23@email.com", "email.23@-email.com"};

        for (String email : validEmails) {
            assertThrows(InvalidRequest.class, () ->
                    OrganisationCreationRequestValidator.validateEmail(email));
        }
    }

    @Test
    void test_should_validate_mandatory_user_fields_and_not_throw_exception() {

        NewUserCreationRequest request = new NewUserCreationRequest("fanme", "lastname",
                "sl@test.com", new ArrayList<String>(), false);

        assertDoesNotThrow(() ->
                OrganisationCreationRequestValidator.validateNewUserCreationRequestForMandatoryFields(request));
    }

    @Test
    void test_should_validate_mandatory_user_fields_and_throw_exception() {
        NewUserCreationRequest request = new NewUserCreationRequest(null, null,
                "al@test.com", new ArrayList<String>(), false);
        assertThrows(InvalidRequest.class, () ->
                OrganisationCreationRequestValidator.validateNewUserCreationRequestForMandatoryFields(request));
    }

    @Test
    void test_should_validate_company_no_length_and_throw_if_length_more_than_8() {
        OrganisationCreationRequest orgReq = new OrganisationCreationRequest("", "", null,"",
                "true", "123456789", "", null, new HashSet<>(),
                null,null,null);
        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.validateCompanyNumber(orgReq));
    }

    @Test
    void test_should_validate_uprn_and_throw_if_length_more_than_14() {
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("777777777777777","abc", "abc", null,
                null, null, null, null, null);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        assertThrows(InvalidRequest.class, () ->
                organisationCreationRequestValidator.requestContactInformation(contactList));
    }

    @Test
    void test_should_validate_uprn_and_not_throw_if_length_is_14() {
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("77777777777777",null, null, null,
                null, null, null, null, null);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        assertDoesNotThrow(() ->
                organisationCreationRequestValidator.requestContactInformation(contactList));
    }

    @Test
    void test_should_validate_company_no_length_and_not_throw_if_length_is_8() {
        OrganisationCreationRequest orgReq = new OrganisationCreationRequest("", "", null, "",
                "true", "12345678", "", null, new HashSet<>(),
                null,null,null);
        organisationCreationRequestValidator.validateCompanyNumber(orgReq);
    }

    @Test
    void test_isInputOrganisationStatusValid_with_valid_status() {
        assertDoesNotThrow(() ->
                isInputOrganisationStatusValid(ACTIVE.name(), ACTIVE.name()));
    }

    @Test
    void test_isInputOrganisationStatusValid_with_lowercase_status() {
        assertDoesNotThrow(() ->
                isInputOrganisationStatusValid(ACTIVE.name().toLowerCase(), ACTIVE.name() + "," + PENDING.name()));
    }

    @Test
    void test_isInputOrganisationStatusValid_with_invalid_status() {
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid(OrganisationStatus.PENDING.name(), ACTIVE.name())));
    }

    @Test
    void test_isInputOrganisationStatusValid_with_invalid_status_from_multiple_list() {
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid(OrganisationStatus.BLOCKED.name(),
                        ACTIVE.name() + "," + PENDING.name())));
    }

    @Test
    void test_isInputOrganisationStatusValid_with_blank_status() {
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid(null, ACTIVE.name())));
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid("", ACTIVE.name())));
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid(" ", ACTIVE.name())));
    }

    @Test
    void test_add_contacts_informations_to_orgs_validation_address_line_is_missing() {
        List<ContactInformationCreationRequest> contactInformationCreationRequests =
                Arrays.asList(aContactInformationCreationRequest()
                        //.addressLine1("")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .postCode("some-post-code")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("this is an invalid dx number")
                                .dxExchange(null).build()))
                        .build());


        Throwable thrown = catchThrowable(() -> {
            organisationCreationRequestValidator
                    .validateContactInformations(contactInformationCreationRequests);
        });


        assertThat(thrown)
                .isInstanceOf(InvalidContactInformations.class)
                .hasMessageContaining("Invalid Contact informations");


    }

    @Test
    void test_add_contacts_informations_to_orgs_valid_dx_address() {
        List<ContactInformationCreationRequest> contactInformationCreationRequests =
                Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .postCode("some-post-code")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build());

        organisationCreationRequestValidator
                .validateContactInformations(contactInformationCreationRequests);

        assertTrue(true);

    }

    @Test
    void test_add_contacts_informations_to_orgs_null_dx_address() {
        List<ContactInformationCreationRequest> contactInformationCreationRequests =
                Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .postCode("some-post-code")
                        .dxAddress(null)
                        .build());

        organisationCreationRequestValidator
                .validateContactInformations(contactInformationCreationRequests);

        assertTrue(true);

    }


    void verifyResourceNotFoundExceptionThrown(Throwable raisedException) {
        assertThat(raisedException).isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessageStartingWith(ERROR_MESSAGE_INVALID_STATUS_PASSED);
    }
}