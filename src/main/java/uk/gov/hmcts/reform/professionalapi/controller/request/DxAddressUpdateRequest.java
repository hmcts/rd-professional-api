package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "dxAddressUpdateRequest")
public class DxAddressUpdateRequest {

    private final String dxAddressId;

    @NotNull
    @Schema(name = "dxNumber", example = "string")
    @Pattern(regexp = "^(?:DX|NI) \\d{10}+$")
    private final String dxNumber;
    @NotNull
    private final String dxExchange;

    @JsonCreator
    public DxAddressUpdateRequest(@JsonProperty("dxAddressId") String dxAddressId,
                                  @JsonProperty("dxNumber") String dxNumber,
                                  @JsonProperty("dxExchange") String dxExchange) {

        this.dxAddressId = dxAddressId;
        this.dxNumber = dxNumber;
        this.dxExchange = dxExchange;
    }
}