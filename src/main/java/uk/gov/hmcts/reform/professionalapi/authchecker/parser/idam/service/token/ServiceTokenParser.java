package uk.gov.hmcts.reform.professionalapi.authchecker.parser.idam.service.token;

public interface ServiceTokenParser {
    String parse(String jwt) throws ServiceTokenParsingException;
}

