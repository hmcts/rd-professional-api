package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Builder(builderMethodName = "dxAddressCreationRequest")
public class DxAddressCreationRequest {

    @NotNull
    @Pattern(regexp = "^(?:DX|NI) [0-9]{10}+$")
    private final String dxNumber;
    @NotNull
    private final String dxExchange;

    @JsonCreator
    public DxAddressCreationRequest(@JsonProperty("dxNumber") String dxNumber,
                                    @JsonProperty("dxExchange") String dxExchange) {

        this.dxNumber = dxNumber;
        this.dxExchange = dxExchange;
    }
}