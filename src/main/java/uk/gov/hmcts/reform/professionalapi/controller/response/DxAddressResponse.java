package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

@Getter
public class DxAddressResponse {

    @JsonProperty
    private final String dxNumber;
    @JsonProperty
    private final String dxExchange;

    public DxAddressResponse(DxAddress dxAddress) {
        this.dxNumber = dxAddress.getDxNumber();
        this.dxExchange = dxAddress.getDxExchange();
    }

}
