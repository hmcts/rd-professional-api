package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_INVALID_STATUS_PASSED;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator.isInputOrganisationStatusValid;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.PENDING;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationCreationRequestValidatorTest {

    @Mock //must be mocked as it is in interface
    private RequestValidator validator1;
    @Mock //must be mocked as it is in interface
    private RequestValidator validator2;

    private OrganisationCreationRequestValidator organisationCreationRequestValidator;
    private UserCreationRequest userCreationRequest;
    private OrganisationCreationRequest organisationCreationRequest;
    private Exception myException;

    @Before
    public void setup() {
        organisationCreationRequestValidator = new OrganisationCreationRequestValidator(asList(validator1, validator2));
        userCreationRequest = UserCreationRequest.aUserCreationRequest().firstName("fName").lastName("lName")
                .email("test@email.com").build();
        organisationCreationRequest = new OrganisationCreationRequest("Company", "PENDING", "SraId",
                "true", "12345678", "www.company.com", userCreationRequest,
                new HashSet<>(), null);
    }

    @Test
    public void test_CallsAllValidators() {
        organisationCreationRequestValidator.validate(organisationCreationRequest);

        verify(validator1, times(1)).validate(organisationCreationRequest);
        verify(validator2, times(1)).validate(organisationCreationRequest);

        assertThat(OrganisationCreationRequestValidator.contains(OrganisationStatus.PENDING.name())).isEqualTo(true);
        assertThat(OrganisationCreationRequestValidator.contains("pend")).isEqualTo(false);
    }

    @Test(expected = EmptyResultDataAccessException.class) //null value should throw empty exception
    public void test_validateOrganisationIdentifierNull() {
        organisationCreationRequestValidator.validateOrganisationIdentifier(null);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    //value less than 7 char in length should throw empty exception
    public void test_validateOrganisationIdentifierTooShort() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("AB");
    }

    @Test(expected = EmptyResultDataAccessException.class) //incorrect format should throw empty exception
    public void test_validateOrganisationIdentifierWrongFormat() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("@@@@@@@");
    }

    @Test(expected = Test.None.class) //valid value should not throw exception
    public void test_validateOrganisationIdentifierNoException() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("Q90SB9S");
    }

    @Test
    public void test_contains() {
        assertThat(OrganisationCreationRequestValidator.contains("quhajdsajsh")).isFalse();
        assertThat(OrganisationCreationRequestValidator.contains("PENDING")).isTrue();
    }

    @Test(expected = InvalidRequest.class) //Pending value should not throw empty exception
    public void test_isOrganisationActive_Pending() {
        Organisation myOrgg = new Organisation();
        myOrgg.setStatus(OrganisationStatus.PENDING);
        organisationCreationRequestValidator.isOrganisationActive(myOrgg);
    }

    @Test(expected = Test.None.class) //Active value should not throw empty exception
    public void test_isOrganisationActive_Active() {
        Organisation organisation = new Organisation();
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisationCreationRequestValidator.isOrganisationActive(organisation);
    }

    @Test(expected = EmptyResultDataAccessException.class) //null value should throw empty exception
    public void test_isOrganisationActive_Null() {
        organisationCreationRequestValidator.isOrganisationActive(null);
    }

    @Test(expected = InvalidRequest.class) //empty value should throw empty exception
    public void test_isOrganisationActive_Empty() {
        organisationCreationRequestValidator.isOrganisationActive(new Organisation());
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateOrganisationRequest() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("");
        OrganisationCreationRequest orgReq = new OrganisationCreationRequest("", "", "",
                "true", "", "", null, paymentAccounts,
                null);
        organisationCreationRequestValidator.validateOrganisationRequest(orgReq);
    }

    @Test //empty value should throw invalid request
    public void test_requestValues() {
        Assertions.assertThatThrownBy(() -> organisationCreationRequestValidator.requestValues(""))
                .isExactlyInstanceOf(InvalidRequest.class);
    }

    @Test(expected = InvalidRequest.class) // if the fields are null or empty it should throw invalid request
    public void test_requestContactInformation() {
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("", null, null,
                null, null, null, null, null);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        organisationCreationRequestValidator.requestContactInformation(contactList);
    }

    @Test(expected = InvalidRequest.class)
    public void test_requestContactInformationDxAddwithDxNumerHasSpecialChars() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 1*2$3&4@",
                "DxExchange");
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("A", "A", "A", "A",
                "A", "A", "A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        organisationCreationRequestValidator.requestContactInformation(contactList);
    }

    @Test(expected = InvalidRequest.class)
    public void test_requestContactInformationDxAddwithEmpty() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 1234567890", null);
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("A", "A", "A", "A",
                "A", "A", "A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        organisationCreationRequestValidator.requestContactInformation(contactList);

        DxAddressCreationRequest dxRequest1 = new DxAddressCreationRequest(null, "DxExchange");
        List<DxAddressCreationRequest> dxList1 = new ArrayList<>();
        dxList1.add(dxRequest1);
        ContactInformationCreationRequest contactInfoCreateRequest1
                = new ContactInformationCreationRequest("A", "A", "A", "A",
                "A", "A", "A", dxList1);
        List<ContactInformationCreationRequest> contactList1 = new ArrayList<>();
        contactList1.add(contactInfoCreateRequest1);

        organisationCreationRequestValidator.requestContactInformation(contactList1);
    }

    @Test(expected = InvalidRequest.class)
    public void test_requestContactInformationDxAddwithInvalidLength() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 12345678900000",
                "DxExchange1234567890");
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("A", "A", "A",
                "A", "A", "A", "A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        organisationCreationRequestValidator.requestContactInformation(contactList);

        DxAddressCreationRequest dxRequest1 = new DxAddressCreationRequest("DX 1234567890",
                "DxExchangeDxExchange123");
        List<DxAddressCreationRequest> dxList1 = new ArrayList<>();
        dxList1.add(dxRequest1);
        ContactInformationCreationRequest contactInfoCreateRequest1
                = new ContactInformationCreationRequest("A", "A", "A",
                "A", "A", "A", "A", dxList1);
        List<ContactInformationCreationRequest> contactList1 = new ArrayList<>();
        contactList1.add(contactInfoCreateRequest1);

        organisationCreationRequestValidator.requestContactInformation(contactList1);
    }

    @Test(expected = Test.None.class)
    public void test_requestContactInformationDxAddwithvalid() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 1234567890",
                "DxExchange1234567890");
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest
                = new ContactInformationCreationRequest("A", "A", "A",
                "A", "A", "A", "A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        organisationCreationRequestValidator.requestContactInformation(contactList);
    }

    @Test(expected = Test.None.class)
    public void test_should_validate_valid_email_and_should_not_throw_exception() {

        String[] validEmails = new String[]{
            "shreedhar.lomte@hmcts.net",
            "shreedhar@yahoo.com",
            "ad'il@wahoo.com",
            "Email.100@yahoo.com",
            "email111@email.com",
            "email.100@email.com.au",
            "email@gmail.com.com",
            "email_231_a@email.com",
            "email_100@yahoo-test.ABC.CoM",
            "email-100@yahoo.com",
            "email-100@email.net",
            "email+100@gmail.com",
            "emAil-100@yahoo-test.com",
            "e.mAil-100@yahoo-test.com"};

        for (String email : validEmails) {
            OrganisationCreationRequestValidator.validateEmail(email);
        }

    }

    @Test(expected = InvalidRequest.class)
    public void test_should_validate_valid_email_and_should_throw_exception() {
        String[] validEmails = new String[]{"あいうえお@example.com",
            "emAil@1.com",
            "email@111",
            "email@.com.my",
            "email123@gmail.",
            "email123@.com",
            "email123@.com.com",
            ".email@email.com",
            "email()*@gmAil.com",
            "eEmail()*@gmail.com",
            "email@%*.com",
            "email..2002@gmail.com",
            "email.@gmail.com",
            "email@email@gmail.com",
            "email@gmail.com.",
            "email..2002@gmail.com@",
            "-email.23@email.com",
            "$email.3@email.com",
            "!email@email.com",
            "+@Adil61371@gmail.com",
            "_email.23@email.com",
            "email.23@-email.com"};

        for (String email : validEmails) {
            OrganisationCreationRequestValidator.validateEmail(email);
        }
    }

    @Test(expected = Test.None.class)
    public void test_should_validate_mandatory_user_fields_and_not_throw_exception() {

        NewUserCreationRequest request = new NewUserCreationRequest("fanme", "lastname",
                "sl@hmcts.net", new ArrayList<String>(), false);

        OrganisationCreationRequestValidator.validateNewUserCreationRequestForMandatoryFields(request);
    }

    @Test(expected = InvalidRequest.class)
    public void test_should_validate_mandatory_user_fields_and_throw_exception() {
        NewUserCreationRequest request = new NewUserCreationRequest(null, null,
                "al@hmcts.net", new ArrayList<String>(), false);
        OrganisationCreationRequestValidator.validateNewUserCreationRequestForMandatoryFields(request);
    }

    @Test(expected = InvalidRequest.class)
    public void test_should_validate_company_no_length_and_throw_if_length_more_than_8() {
        OrganisationCreationRequest orgReq = new OrganisationCreationRequest("", "", "",
                "true", "123456789", "", null, new HashSet<>(),
                null);
        organisationCreationRequestValidator.validateCompanyNumber(orgReq);
    }

    @Test
    public void test_should_validate_company_no_length_and_not_throw_if_length_is_8() {
        OrganisationCreationRequest orgReq = new OrganisationCreationRequest("", "", "",
                "true", "12345678", "", null, new HashSet<>(),
                null);
        organisationCreationRequestValidator.validateCompanyNumber(orgReq);
    }

    @Test(expected = Test.None.class)
    public void test_isInputOrganisationStatusValid_with_valid_status() {
        isInputOrganisationStatusValid(ACTIVE.name(), ACTIVE.name());
    }

    @Test(expected = Test.None.class)
    public void test_isInputOrganisationStatusValid_with_lowercase_status() {
        isInputOrganisationStatusValid(ACTIVE.name().toLowerCase(), ACTIVE.name() + "," + PENDING.name());
    }

    @Test
    public void test_isInputOrganisationStatusValid_with_invalid_status() {
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid(OrganisationStatus.PENDING.name(), ACTIVE.name())));
    }

    @Test
    public void test_isInputOrganisationStatusValid_with_invalid_status_from_multiple_list() {
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid(OrganisationStatus.BLOCKED.name(),
                        ACTIVE.name() + "," + PENDING.name())));
    }

    @Test
    public void test_isInputOrganisationStatusValid_with_blank_status() {
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid(null, ACTIVE.name())));
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid("", ACTIVE.name())));
        verifyResourceNotFoundExceptionThrown(catchThrowable(() ->
                isInputOrganisationStatusValid(" ", ACTIVE.name())));
    }

    public void verifyResourceNotFoundExceptionThrown(Throwable raisedException) {
        assertThat(raisedException).isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessageStartingWith(ERROR_MESSAGE_INVALID_STATUS_PASSED);
    }
}