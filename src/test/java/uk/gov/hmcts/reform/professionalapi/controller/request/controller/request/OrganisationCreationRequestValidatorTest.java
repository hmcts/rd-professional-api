package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import uk.gov.hmcts.reform.professionalapi.controller.request.*;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationCreationRequestValidatorTest {

    @Mock
    RequestValidator validator1;

    @Mock
    RequestValidator validator2;

    @Mock
    OrganisationCreationRequest orgCreateRequest;

    @Mock
    OrganisationCreationRequestValidator organisationCreationRequestValidator;

    @Mock
    Organisation org;

    Organisation myOrg;
    Exception myExceptionalException;


    @Before
    public void setup() {
        organisationCreationRequestValidator =
                new OrganisationCreationRequestValidator(asList(validator1, validator2));
    }

    @Test
    public void testCallsAllValidators() {

        organisationCreationRequestValidator.validate(orgCreateRequest);

        verify(validator1, times(1)).validate(orgCreateRequest);
        verify(validator2, times(1)).validate(orgCreateRequest);

        assertThat(OrganisationCreationRequestValidator.contains(OrganisationStatus.PENDING.name())).isEqualTo(true);
        assertThat(OrganisationCreationRequestValidator.contains("pend")).isEqualTo(false);
    }

    @Test(expected = EmptyResultDataAccessException.class) //null value should throw empty exception
    public void validateOrganisationIdentifierNullTest() {
        organisationCreationRequestValidator.validateOrganisationIdentifier(null);
    }

    @Test(expected = EmptyResultDataAccessException.class) //value less than 7 char in length should throw empty exception
    public void validateOrganisationIdentifierTooShortTest() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("AB");
    }

    @Test(expected = EmptyResultDataAccessException.class) //incorrect format should throw empty exception
    public void validateOrganisationIdentifierWrongFormatTest() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("@@@@@@@");
    }

    @Test //valid value should not throw exception
    public void validateOrganisationIdentifierNoExcepTest() {
        myExceptionalException = null;
        try {
            organisationCreationRequestValidator.validateOrganisationIdentifier("Q90SB9S");
        } catch (Exception e) {
            myExceptionalException = e;
        }

        assertThat(myExceptionalException).isEqualTo(null);
    }

    @Test
    public void containsTest() {
        assertThat(OrganisationCreationRequestValidator.contains("quhajdsajsh")).isFalse();
        assertThat(OrganisationCreationRequestValidator.contains("PENDING")).isTrue();
    }

    @Test (expected = EmptyResultDataAccessException.class) //Pending value should not throw empty exception
    public void isOrganisationActive_Pending_Test() {
        Organisation myOrgg = new Organisation();
        myOrgg.setStatus(OrganisationStatus.PENDING);
        organisationCreationRequestValidator.isOrganisationActive(myOrgg);
    }

    @Test //Active value should not throw empty exception
    public void isOrganisationActive_Active_Test() {
        Organisation myOrgg = new Organisation();
        myOrgg.setStatus(OrganisationStatus.ACTIVE);
        myExceptionalException = null;

        try {
            organisationCreationRequestValidator.isOrganisationActive(myOrgg);
        } catch (Exception e) {
            myExceptionalException = e;
        }

        assertThat(myExceptionalException).isEqualTo(null);
    }

    @Test(expected = EmptyResultDataAccessException.class) //null value should throw empty exception
    public void isOrganisationActive_Null_Test() {
        organisationCreationRequestValidator.isOrganisationActive(null);
    }

    @Test(expected = EmptyResultDataAccessException.class) //empty value should throw empty exception
    public void isOrganisationActive_Empty_Test() {
        organisationCreationRequestValidator.isOrganisationActive(org);
    }

    /*@Test(expected = InvalidRequest.class)
    public void requestValuesTest() {
        organisationCreationRequestValidator.requestValues("");
    }*/

    @Test(expected = InvalidRequest.class)
    public void validateOrganisationRequestTest() {
        List<String> list = new ArrayList<>();
        list.add("");
        OrganisationCreationRequest orgReq = new OrganisationCreationRequest("","","", "true", "","",null, list,null);
        organisationCreationRequestValidator.validateOrganisationRequest(orgReq);
    }

    @Test //empty value should throw invalid request
    public void requestValuesTest() {
        Assertions.assertThatThrownBy(() -> organisationCreationRequestValidator.requestValues(""))
                .isExactlyInstanceOf(InvalidRequest.class);
    }

    @Test(expected = InvalidRequest.class) // if the fields are null or empty it should throw invalid request
    public void requestContactInformationTest() {
        ContactInformationCreationRequest contactInfoCreateRequest = new ContactInformationCreationRequest("",null,null,null, null,null,null,null);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        organisationCreationRequestValidator.requestContactInformation(contactList);
    }

    @Test(expected = InvalidRequest.class) //invalid dx number should throw invalid request
    public void requestContactInformationDxAddTest() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 1234591", "DxExchange");
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest = new ContactInformationCreationRequest("A","A","A","A", "A","A","A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        organisationCreationRequestValidator.requestContactInformation(contactList);
    }
}