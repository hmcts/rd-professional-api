package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "aContactInformationCreationRequest")
public class ContactInformationCreationRequest {

    @NotNull
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

        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.townCity = townCity;
        this.county = county;
        this.country = country;
        this.postCode = postCode;
        this.dxAddress = dxAddress;
    }


    public String getAddressLine1() {
        return addressLine1;
    }


    public String getAddressLine2() {
        return addressLine2;
    }


    public String getAddressLine3() {
        return addressLine3;
    }


    public String getTownCity() {
        return townCity;
    }


    public String getCounty() {
        return county;
    }


    public String getCountry() {
        return country;
    }


    public String getPostCode() {
        return postCode;
    }


    public List<DxAddressCreationRequest> getDxAddress() {
        return dxAddress;
    }
}