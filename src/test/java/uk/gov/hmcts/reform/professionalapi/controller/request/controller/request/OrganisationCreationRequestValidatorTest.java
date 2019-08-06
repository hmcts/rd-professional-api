package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.Jurisdiction;
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