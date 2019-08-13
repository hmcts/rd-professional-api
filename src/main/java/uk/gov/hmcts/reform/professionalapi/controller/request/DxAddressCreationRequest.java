package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

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

        this.dxNumber = PbaAccountUtil.removeEmptySpaces(dxNumber);
        this.dxExchange = PbaAccountUtil.removeEmptySpaces(dxExchange);
    }
}