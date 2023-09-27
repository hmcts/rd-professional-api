package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder(builderMethodName = "abulkCustomerRequest")
public class BulkCustomerRequest {

    @JsonProperty(value = "bulkCustomerId")
    private String bulkCustomerId;

    @JsonProperty(value = "idamId")
    private String idamId;

    public BulkCustomerRequest(String bulkCustomerId, String idamId) {
        this.bulkCustomerId = bulkCustomerId;
        this.idamId = idamId;
    }
}
