package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeleteMultipleAddressRequest {

    @JsonProperty(value = "addressId")
    private Set<String> addressId;


}
