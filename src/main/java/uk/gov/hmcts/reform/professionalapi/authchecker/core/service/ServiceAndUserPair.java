package uk.gov.hmcts.reform.professionalapi.authchecker.core.service;

import lombok.Data;

import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.Service;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.User;

@Data
public class ServiceAndUserPair {
    private final Service service;
    private final User user;
}
