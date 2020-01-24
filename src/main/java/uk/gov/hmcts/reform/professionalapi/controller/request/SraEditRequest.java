package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "anSraEditRequest")
public class SraEditRequest {

    private String sraId;
    private String sraRegulated;

    @JsonCreator
    public SraEditRequest(@JsonProperty("sraId") String sraId, String sraRegulated) {
        this.sraId = sraId;
        this.sraRegulated = sraRegulated;
    }
}