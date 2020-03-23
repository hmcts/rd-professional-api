package uk.gov.hmcts.reform.professionalapi.authchecker.core;

import javax.servlet.http.HttpServletRequest;

public interface RequestAuthorizer<T extends Subject> {
    T authorise(HttpServletRequest request);
}
