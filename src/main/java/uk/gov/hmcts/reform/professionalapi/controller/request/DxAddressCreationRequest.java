package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Builder(builderMethodName = "dxAddressCreationRequest")
public class DxAddressCreationRequest {

    @NotNull
    @Pattern(regexp = "^(?:DX|NI) [0-9]{10}+$")
    @Size(max = 13)
    private final String dxNumber;
    @NotNull
    @Size(max = 20)
    private final String dxExchange;

    @JsonCreator
    public DxAddressCreationRequest(@JsonProperty("dxNumber") String dxNumber,
                                    @JsonProperty("dxExchange") String dxExchange) {

        this.dxNumber = dxNumber;
        this.dxExchange = dxExchange;
    }
}