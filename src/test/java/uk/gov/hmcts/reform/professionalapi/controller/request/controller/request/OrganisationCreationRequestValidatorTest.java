package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

import java.util.ArrayList;
import java.util.List;

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


    @Before
    public void Setup() {
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
    public void validateOrganisationIdentifierTest() {
        organisationCreationRequestValidator.validateOrganisationIdentifier(null);
    }

    @Test(expected = EmptyResultDataAccessException.class) //null value should throw empty exception
    public void isOrganisationActive_Null_Test() {
        organisationCreationRequestValidator.isOrganisationActive(null);
    }

    @Test(expected = EmptyResultDataAccessException.class) //empty value should throw empty exception
    public void isOrganisationActiveTest() {
        organisationCreationRequestValidator.isOrganisationActive(org);
    }

//    @Test(expected = InvalidRequest.class)
//    public void requestValuesTest() {
//        organisationCreationRequestValidator.requestValues("");
//    }

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
    public void requestContactInformationDXAddTest() {
        DxAddressCreationRequest dxRequest = new DxAddressCreationRequest("DX 1234591", "DxExchange");
        List<DxAddressCreationRequest> dxList = new ArrayList<>();
        dxList.add(dxRequest);
        ContactInformationCreationRequest contactInfoCreateRequest = new ContactInformationCreationRequest("A","A","A","A", "A","A","A", dxList);
        List<ContactInformationCreationRequest> contactList = new ArrayList<>();
        contactList.add(contactInfoCreateRequest);

        organisationCreationRequestValidator.requestContactInformation(contactList);
    }
}