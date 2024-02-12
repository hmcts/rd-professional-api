package uk.gov.hmcts.reform.professionalapi.controller.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder(builderMethodName = "aUserIdentifierRequest")
public class ProfessionalUserIdentifierRequest {

    @NotNull
    private final String existingIdamId;

    @NotNull
    private final String newIdamId;


    @JsonCreator
    public ProfessionalUserIdentifierRequest(String existingIdamId, String newIdamId) {
        this.existingIdamId = existingIdamId;
        this.newIdamId = newIdamId;
    }


}

