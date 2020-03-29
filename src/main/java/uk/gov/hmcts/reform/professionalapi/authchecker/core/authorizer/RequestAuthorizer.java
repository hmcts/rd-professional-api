package uk.gov.hmcts.reform.professionalapi.authchecker.core.authorizer;

import javax.servlet.http.HttpServletRequest;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.Subject;

public interface RequestAuthorizer<T extends Subject> {
    T authorise(HttpServletRequest request);
}
