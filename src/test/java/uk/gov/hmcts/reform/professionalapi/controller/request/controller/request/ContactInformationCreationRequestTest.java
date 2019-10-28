package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;

public class ContactInformationCreationRequestTest {

    private List<DxAddressCreationRequest> dxAddresses = new ArrayList<>();

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void has_mandatory_fields_specified_not_null() {

        ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
                null, null, null, null, null, null, null, null);

        Set<ConstraintViolation<ContactInformationCreationRequest>> violations = validator
                .validate(contactInformationCreationRequest);
        assertThat(violations.size()).isEqualTo(2);
    }

    @Test
    public void creates_contact_information_creation_request_correctly() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("some-address", "some-exchange");

        dxAddresses.add(dxAddressCreationRequest);

        ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
                "some-address1", "some-address2", "some-address3", "some-town-city", "some-county", "some-country",
                "some-post-code", dxAddresses);

        assertThat(contactInformationCreationRequest.getAddressLine1()).isEqualTo("some-address1");
        assertThat(contactInformationCreationRequest.getAddressLine2()).isEqualTo("some-address2");
        assertThat(contactInformationCreationRequest.getAddressLine3()).isEqualTo("some-address3");
        assertThat(contactInformationCreationRequest.getTownCity()).isEqualTo("some-town-city");
        assertThat(contactInformationCreationRequest.getCounty()).isEqualTo("some-county");
        assertThat(contactInformationCreationRequest.getCountry()).isEqualTo("some-country");
        assertThat(contactInformationCreationRequest.getPostCode()).isEqualTo("some-post-code");
        assertThat(contactInformationCreationRequest.getDxAddress()).isEqualTo(dxAddresses);
    }

    @Test
    public void creates_contact_information_creation_request_correctly_when_optional_values_are_null() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("some-address", "some-exchange");

        dxAddresses.add(dxAddressCreationRequest);

        ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
                "some-address1", null, null, null, null, null, null, null);

        assertThat(contactInformationCreationRequest.getAddressLine1()).isEqualTo("some-address1");
        assertThat(contactInformationCreationRequest.getAddressLine2()).isNull();
        assertThat(contactInformationCreationRequest.getAddressLine3()).isNull();
        assertThat(contactInformationCreationRequest.getTownCity()).isNull();
        assertThat(contactInformationCreationRequest.getCounty()).isNull();
        assertThat(contactInformationCreationRequest.getCountry()).isNull();
        assertThat(contactInformationCreationRequest.getPostCode()).isNull();
        assertThat(contactInformationCreationRequest.getDxAddress()).isNull();
    }

    @Test
    public void creates_contact_information_creation_request_correctly_without_dx_address() {

        ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
                "some-address1", "some-address2", "some-address3", "some-town-city", "some-county", "some-country",
                "some-post-code", null);

        assertThat(contactInformationCreationRequest.getAddressLine1()).isEqualTo("some-address1");
        assertThat(contactInformationCreationRequest.getAddressLine2()).isEqualTo("some-address2");
        assertThat(contactInformationCreationRequest.getAddressLine3()).isEqualTo("some-address3");
        assertThat(contactInformationCreationRequest.getTownCity()).isEqualTo("some-town-city");
        assertThat(contactInformationCreationRequest.getCounty()).isEqualTo("some-county");
        assertThat(contactInformationCreationRequest.getCountry()).isEqualTo("some-country");
        assertThat(contactInformationCreationRequest.getPostCode()).isEqualTo("some-post-code");
        assertThat(contactInformationCreationRequest.getDxAddress()).isNull();
    }

    @Test
    public void test_contactInformationCreationRequestBuilder() {
        String addressLine1 = "addressLine1";
        String addressLine2 = "addressLine2";
        String addressLine3 = "addressLine3";
        String townCity = "townCity";
        String county = "county";
        String country = "country";
        String postCode = "postC";
        List<DxAddressCreationRequest> dxAddress = new ArrayList<>();
        DxAddressCreationRequest dxAddressCreationRequestMock = mock(DxAddressCreationRequest.class);
        dxAddress.add(dxAddressCreationRequestMock);

        ContactInformationCreationRequest contactInformationCreationRequest = ContactInformationCreationRequest.aContactInformationCreationRequest()
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .addressLine3(addressLine3)
                .townCity(townCity)
                .county(county)
                .country(country)
                .postCode(postCode)
                .dxAddress(dxAddress)
                .build();

        assertThat(contactInformationCreationRequest.getAddressLine1()).isEqualTo(addressLine1);
        assertThat(contactInformationCreationRequest.getAddressLine2()).isEqualTo(addressLine2);
        assertThat(contactInformationCreationRequest.getAddressLine3()).isEqualTo(addressLine3);
        assertThat(contactInformationCreationRequest.getTownCity()).isEqualTo(townCity);
        assertThat(contactInformationCreationRequest.getCounty()).isEqualTo(county);
        assertThat(contactInformationCreationRequest.getCountry()).isEqualTo(country);
        assertThat(contactInformationCreationRequest.getPostCode()).isEqualTo(postCode);
        assertThat(contactInformationCreationRequest.getDxAddress()).isEqualTo(dxAddress);
    }
}
