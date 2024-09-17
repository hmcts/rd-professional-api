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
public class OrganisationNameUpdateRequest {

    @JsonProperty(value = "names")
    private List<OrganisationNameUpdateData> organisationNameUpdateDataList;

    public OrganisationNameUpdateRequest() {

    }

    @Getter
    @Setter
    public static class OrganisationNameUpdateData {

        @Valid
        @NotNull(message = " Name is required.")
        private String name;
        @Valid
        @NotNull(message = " organisation Id is required.")
        private String organisationId;

        @JsonCreator
        public OrganisationNameUpdateData(
            @JsonProperty("name") String name,
            @JsonProperty("organisationId") String organisationId) {
            this.name = name;
            this.organisationId = organisationId;
        }
    }
}