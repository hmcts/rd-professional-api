package uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver;

import javax.servlet.http.HttpServletRequest;

public interface RequestAuthorizer<T extends Subject> {
    T authorise(HttpServletRequest request);
}
