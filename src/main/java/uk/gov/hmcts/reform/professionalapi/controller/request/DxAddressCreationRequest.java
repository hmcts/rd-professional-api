package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Builder(builderMethodName = "dxAddressCreationRequest")
public class DxAddressCreationRequest {

    @NotNull
    @Schema(name = "dxNumber", example = "string")
    @Pattern(regexp = "^(?:DX|NI) \\d{10}+$")
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