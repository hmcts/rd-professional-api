package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import javax.validation.constraints.NotEmpty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder(builderMethodName = "aDeleteMultipleAddressRequest")
public class DeleteMultipleAddressRequest {

    @NotEmpty
    @JsonProperty
    private Set<String> addressId;

    public DeleteMultipleAddressRequest(Set<String> addressId) {
        this.addressId = addressId;
    }
}
