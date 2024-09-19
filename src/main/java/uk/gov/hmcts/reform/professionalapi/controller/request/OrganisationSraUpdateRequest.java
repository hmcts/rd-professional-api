package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Getter
@Setter
public class OrganisationSraUpdateRequest {


    @JsonProperty(value = "sarIds")
    private List<OrganisationSraUpdateData> organisationSraUpdateDataList;

    public OrganisationSraUpdateRequest() {

    }

    @Getter
    @Setter
    public static class OrganisationSraUpdateData {

        @Valid
        @NotNull(message = "SRA Id is required.")
        private  String sraId;

        @Valid
        @NotNull(message = " organisation Id is required.")
        private String organisationId;

        @JsonCreator
        public OrganisationSraUpdateData(
            @JsonProperty("organisationId") String organisationId,
            @JsonProperty("sraId") String sraId) {
            this.organisationId = organisationId;
            this.sraId = sraId;
        }
    }
}