package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.RequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationCreationRequestValidatorTest {

    @Mock
    RequestValidator validator1;

    @Mock
    RequestValidator validator2;

    @Mock
    OrganisationCreationRequest request;

    public List<String> getEnumList() {
        ArrayList<String> enumStringList = new ArrayList<>();
        enumStringList.add("Probate");
        enumStringList.add("BULKSCAN");
        enumStringList.add("Civil Money Claims");
        return enumStringList;
    }

    public List<Map<String,String>> createJurisdictions() {

        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        Map<String,String> jid1 = new HashMap<String,String>();
        jid1.put("id", "Probate");
        Map<String,String> jid2 = new HashMap<String,String>();
        jid2.put("id", "BULKSCAN");
        maps.add(jid1);
        maps.add(jid2);

        return maps;
    }

    @Test
    public void testCallsAllValidators() {

        OrganisationCreationRequestValidator organisationCreationRequestValidator =
                new OrganisationCreationRequestValidator(asList(validator1, validator2));

        organisationCreationRequestValidator.validate(request);

        verify(validator1, times(1)).validate(request);
        verify(validator2, times(1)).validate(request);

        assertThat(OrganisationCreationRequestValidator.contains(OrganisationStatus.PENDING.name())).isEqualTo(true);
        assertThat(OrganisationCreationRequestValidator.contains("pend")).isEqualTo(false);
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
    public void should_throw_exception_when_jurisdictions_has_empty_jurisdiction_id() {

        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        Map<String,String> jid1 = new HashMap<String,String>();
        Map<String,String> jid2 = new HashMap<String,String>();
        jid2.put("id", "BULKSCAN");
        maps.add(jid1);
        maps.add(jid2);

        assertThatThrownBy(() -> OrganisationCreationRequestValidator.validateJurisdictions(maps, getEnumList()))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Jurisdiction id should have at least 1 element");

    }

    @Test
    public void should_throw_exception_when_jurisdictions_has_invalid_jurisdiction_id_key() {

        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        Map<String,String> jid1 = new HashMap<String,String>();
        jid1.put("if", "PROBATE");
        Map<String,String> jid2 = new HashMap<String,String>();
        jid2.put("id", "BULKSCAN");
        maps.add(jid1);
        maps.add(jid2);

        assertThatThrownBy(() -> OrganisationCreationRequestValidator.validateJurisdictions(maps, getEnumList()))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Jurisdictions key value should be 'id'");

    }

    @Test
    public void should_throw_exception_when_jurisdictions_id_has_null() {

        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        Map<String,String> jid1 = new HashMap<String,String>();
        jid1.put("id", "");
        Map<String,String> jid2 = new HashMap<String,String>();
        jid2.put("id", "BULKSCAN");
        maps.add(jid1);
        maps.add(jid2);

        assertThatThrownBy(() -> OrganisationCreationRequestValidator.validateJurisdictions(maps, getEnumList()))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Jurisdictions value should not be blank or null");

    }

    @Test
    public void should_throw_exception_when_jurisdictions_id_has_invalid_value() {

        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        Map<String,String> jid1 = new HashMap<String,String>();
        jid1.put("id", "BULKSCAN");
        Map<String,String> jid2 = new HashMap<String,String>();
        jid2.put("id", "id2");
        maps.add(jid1);
        maps.add(jid2);

        assertThatThrownBy(() -> OrganisationCreationRequestValidator.validateJurisdictions(maps, getEnumList()))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Jurisdiction id not valid : id2");

    }

}