package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdatePbaRequest {

    @JsonProperty("pbaNumbers")
    List<PbaUpdateRequest> pbaRequestList;

}
