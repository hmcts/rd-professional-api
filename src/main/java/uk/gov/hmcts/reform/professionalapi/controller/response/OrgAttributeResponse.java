package uk.gov.hmcts.reform.professionalapi.controller.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.OrgAttribute;

@NoArgsConstructor
@Getter
public class OrgAttributeResponse {

    @JsonProperty
    private String key;
    @JsonProperty
    private String value;


    public OrgAttributeResponse(OrgAttribute orgAttributes) {
        this.key = orgAttributes.getKey();
        this.value = orgAttributes.getValue();
    }
}
