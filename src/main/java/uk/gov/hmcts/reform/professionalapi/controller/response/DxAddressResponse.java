package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

@Getter
public class DxAddressResponse {

    @JsonProperty
    private  String dxNumber;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
     private  String dxExchange;

    public DxAddressResponse(DxAddress dxAddress) {
        this.dxNumber = dxAddress.getDxNumber();

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(dxAddress.getDxExchange()))) {

            this.dxExchange = dxAddress.getDxExchange().trim();
        }

    }

}
