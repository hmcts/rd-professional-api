package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;

@Getter
@NoArgsConstructor
public class ContactInformationResponse {

    @JsonProperty
    private String addressLine1;
    @JsonProperty
    private String addressLine2;
    @JsonProperty
    private String addressLine3;
    @JsonProperty
    private String townCity;
    @JsonProperty
    private String county;
    @JsonProperty
    private String country;
    @JsonProperty
    private String postCode;
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
        this.dxAddress = contactInfo.getDxAddresses()
                    .stream()
                    .map(dxAddress -> new DxAddressResponse(dxAddress))
                    .collect(toList());
    }

}
