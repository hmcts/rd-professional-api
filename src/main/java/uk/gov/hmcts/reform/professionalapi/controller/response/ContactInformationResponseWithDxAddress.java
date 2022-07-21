package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;

import java.util.List;

@Getter
public class ContactInformationResponseWithDxAddress extends ContactInformationResponse {

    @JsonProperty
    private final List<DxAddressResponse> dxAddress;

    public ContactInformationResponseWithDxAddress(ContactInformation contactInfo) {

        this.addressId = contactInfo.getId();
        this.uprn = contactInfo.getUprn();
        this.created = contactInfo.getCreated();

        this.addressLine1 = contactInfo.getAddressLine1();
        this.addressLine2 = contactInfo.getAddressLine2();
        this.addressLine3 = contactInfo.getAddressLine3();
        this.townCity = contactInfo.getTownCity();
        this.county = contactInfo.getCounty();
        this.country = contactInfo.getCountry();
        this.postCode = contactInfo.getPostCode();
        this.dxAddress = contactInfo.getDxAddresses()
                .stream()
                .map(DxAddressResponse::new)
                .toList();
    }

}
