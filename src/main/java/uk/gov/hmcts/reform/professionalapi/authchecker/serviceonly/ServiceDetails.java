package uk.gov.hmcts.reform.professionalapi.authchecker.serviceonly;

import java.util.Collections;

public class ServiceDetails extends org.springframework.security.core.userdetails.User {

    public ServiceDetails(String servicename) {
        super(servicename, "N/A", Collections.emptyList());
    }

}
