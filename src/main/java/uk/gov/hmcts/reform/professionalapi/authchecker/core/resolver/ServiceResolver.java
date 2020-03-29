package uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver;

import uk.gov.hmcts.reform.professionalapi.authchecker.servicetoken.ServiceTokenParser;

public class ServiceResolver implements SubjectResolver<Service> {

    private final ServiceTokenParser serviceTokenParser;

    public ServiceResolver(ServiceTokenParser serviceTokenParser) {
        this.serviceTokenParser = serviceTokenParser;
    }

    @Override
    public Service getTokenDetails(String bearerToken) {
        String subject = serviceTokenParser.parse(bearerToken);
        return new Service(subject);
    }
}
