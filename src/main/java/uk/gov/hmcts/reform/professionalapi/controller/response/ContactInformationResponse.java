package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;

@Getter
public class ContactInformationResponse {

    @JsonProperty
    private final String addressLine1;
    @JsonProperty
    private final String addressLine2;
    @JsonProperty
    private final String addressLine3;
    @JsonProperty
    private final String townCity;
    @JsonProperty
    private final String county;
    @JsonProperty
    private final String country;
    @JsonProperty
    private final String postCode;
    @JsonProperty
    private  List<DxAddressResponse> dxAddress;

    public ContactInformationResponse(ContactInformation contactInfo) {
        this.addressLine1 = contactInfo.getAddressLine1();
        this.addressLine2 = contactInfo.getAddressLine2();
        this.addressLine3 = contactInfo.getAddressLine3();
        this.townCity = contactInfo.getTownCity();
        this.county = contactInfo.getCounty();
        this.country = contactInfo.getCountry();
        this.postCode = contactInfo.getPostCode();
        if (contactInfo.getDxAddresses().size() > 0) {
            this.dxAddress = contactInfo.getDxAddresses()
                    .stream()
                    .map(dxAddres -> new DxAddressResponse(dxAddres))
                    .collect(toList());
        }
    }

}
