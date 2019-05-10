package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DxAddressCreationRequest {

    @NotNull
    private final String dxNumber;
    @NotNull
    private final String dxExchange;

    @JsonIgnore
    private Boolean isDxRequestValid = false;

    @Builder(builderMethodName = "dxAddressCreationRequest")
    @JsonCreator
    public DxAddressCreationRequest(
            @JsonProperty("dxNumber") String dxNumber, @JsonProperty("dxExchange") String dxExchange) {

        this.dxNumber = dxNumber;
        this.dxExchange = dxExchange;
        this.isDxRequestValid = isDxRequestValid;
    }

    public String getDxNumber() {
        return dxNumber;
    }

    public String getDxExchange() {
        return dxExchange;
    }

    public void setIsDxRequestValid(Boolean isDxRequestValid) {
        this.isDxRequestValid = isDxRequestValid;
    }

    public Boolean getIsDxRequestValid() {
        return isDxRequestValid;
    }
}