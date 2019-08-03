package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JurisdictionUserCreationRequest {

    String id;
    List<Map<String,String>> jurisdictions;

    @JsonCreator
    public JurisdictionUserCreationRequest(@JsonProperty(value = "id") String id,
                                      @JsonProperty(value = "jurisdictions") List<Map<String,String>> jurisdictions) {

        this.id = id;
        this.jurisdictions = jurisdictions;
    }

}
