package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

@Getter
@Builder(builderMethodName = "aContactInformationCreationRequest")
public class ContactInformationCreationRequest {

    @NotNull@NotEmpty
    private final String addressLine1;

    private final String addressLine2;

    private final String addressLine3;

    private final String townCity;

    private final String county;

    private final String country;

    private final String postCode;

    private final List<DxAddressCreationRequest> dxAddress;


    @JsonCreator
    public ContactInformationCreationRequest(

            @JsonProperty("addressLine1") String addressLine1,
            @JsonProperty("addressLine2") String addressLine2,
            @JsonProperty("addressLine3") String addressLine3,
            @JsonProperty("townCity") String townCity,
            @JsonProperty("county") String county,
            @JsonProperty("country") String country,
            @JsonProperty("postCode") String postCode,
            @JsonProperty("dxAddress") List<DxAddressCreationRequest> dxAddress) {

        this.addressLine1 = PbaAccountUtil.removeEmptySpaces(addressLine1);
        this.addressLine2 = PbaAccountUtil.removeEmptySpaces(addressLine2);
        this.addressLine3 = PbaAccountUtil.removeEmptySpaces(addressLine3);
        this.townCity = PbaAccountUtil.removeEmptySpaces(townCity);
        this.county = PbaAccountUtil.removeEmptySpaces(county);
        this.country = PbaAccountUtil.removeEmptySpaces(country);
        this.postCode = PbaAccountUtil.removeEmptySpaces(postCode);
        this.dxAddress = dxAddress;
    }
}