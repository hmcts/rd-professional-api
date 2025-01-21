package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "aUpdateContactInformationRequest")
public class UpdateContactInformationRequest {

    private final String uprn;

    @NotNull @NotEmpty
    private final String addressLine1;

    private final String addressLine2;

    private final String addressLine3;

    private final String townCity;

    private final String county;

    private final String country;

    private final String postCode;

    private final String dxNumber;

    private final String dxExchange;


    @JsonCreator
    public UpdateContactInformationRequest(
            @JsonProperty("uprn") String uprn,
            @JsonProperty("addressLine1") String addressLine1,
            @JsonProperty("addressLine2") String addressLine2,
            @JsonProperty("addressLine3") String addressLine3,
            @JsonProperty("townCity") String townCity,
            @JsonProperty("county") String county,
            @JsonProperty("country") String country,
            @JsonProperty("postCode") String postCode,
            @JsonProperty("dxNumber") String dxNumber,
            @JsonProperty("dxNumber") String dxExchange) {

        this.uprn = uprn;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.townCity = townCity;
        this.county = county;
        this.country = country;
        this.postCode = postCode;
        this.dxNumber = dxNumber;
        this.dxExchange = dxExchange;
    }
}