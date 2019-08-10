package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.RequestValidator;
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

    @Mock
    UserCreationRequest userCreationRequest;

    Organisation myOrg;
    Exception myExceptionalException;


    @Before
    public void setup() {
        organisationCreationRequestValidator =
                new OrganisationCreationRequestValidator(asList(validator1, validator2));
    }

    public List<String> getEnumList() {
        ArrayList<String> enumStringList = new ArrayList<>();
        enumStringList.add("Probate");
        enumStringList.add("BULKSCAN");
        enumStringList.add("Civil Money Claims");
        return enumStringList;
    }

    public List<Jurisdiction> createJurisdictions() {

        List<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();
        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("Probate");
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId("BULKSCAN");
        jurisdictions.add(jurisdiction1);
        jurisdictions.add(jurisdiction2);
        return jurisdictions;
    }

    @Test
    public void testCallsAllValidators() {

        when(orgCreateRequest.getSuperUser()).thenReturn(userCreationRequest);

        when(userCreationRequest.getEmail()).thenReturn("some@gmail.com");
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

    @Test (expected = InvalidRequest.class) //Pending value should not throw empty exception
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

    @Test(expected = InvalidRequest.class) //empty value should throw empty exception
    public void isOrganisationActive_Empty_Test() {
        organisationCreationRequestValidator.isOrganisationActive(org);
    }


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

    @Test(expected = Test.None.class)
    public void should_validate_jurisdictions_successfully() {

        OrganisationCreationRequestValidator.validateJurisdictions(createJurisdictions(), getEnumList());
    }

    @Test
    public void should_throw_exception_when_jurisdictions_are_empty() {

        assertThatThrownBy(() -> OrganisationCreationRequestValidator.validateJurisdictions(new ArrayList<>(), getEnumList()))
                .isInstanceOf(InvalidRequest.class)
            .hasMessage("Jurisdictions not present");
    }


    @Test
    public void should_throw_exception_when_jurisdictions_id_has_null() {

        List<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();
        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("");
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction1.setId("BULKSCAN");
        jurisdictions.add(jurisdiction1);
        jurisdictions.add(jurisdiction2);

        assertThatThrownBy(() -> OrganisationCreationRequestValidator.validateJurisdictions(jurisdictions, getEnumList()))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Jurisdiction value should not be blank or null");

    }

    @Test
    public void should_throw_exception_when_jurisdictions_id_has_invalid_value() {

        List<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();
        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("id2");
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId("BULKSCAN");
        jurisdictions.add(jurisdiction1);
        jurisdictions.add(jurisdiction2);

        assertThatThrownBy(() -> OrganisationCreationRequestValidator.validateJurisdictions(jurisdictions, getEnumList()))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Jurisdiction id not valid : id2");

    }
}