package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserProfileCreateResponse {

    private UUID id;
    private Integer idamStatusCode;

}

