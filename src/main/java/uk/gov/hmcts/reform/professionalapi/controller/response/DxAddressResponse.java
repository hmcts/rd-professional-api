package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;


public class DxAddressResponse {

    @JsonProperty
    private  String dxNumber;
    @JsonProperty
    private  String dxExchange;

    public DxAddressResponse(DxAddress dxAddress) {
        getDxAddressResponse(dxAddress);
    }

    private void getDxAddressResponse(DxAddress dxAddress) {

        this.dxNumber = dxAddress.getDxNumber();
        this.dxExchange = dxAddress.getDxExchange();
    }
}
