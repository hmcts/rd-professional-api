package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrgAttributeRequest {

    private String key;
    private String value;

}
