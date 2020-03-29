package uk.gov.hmcts.reform.professionalapi.authchecker.core.service;

import java.util.Collections;

public class ServiceDetails extends org.springframework.security.core.userdetails.User {

    public ServiceDetails(String serviceName) {
        super(serviceName, "N/A", Collections.emptyList());
    }

}
