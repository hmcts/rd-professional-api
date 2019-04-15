package uk.gov.hmcts.reform.professionalapi.utils;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ResponseUtils {

    private ResponseUtils() {}

    public static List<String> pbaNumbersFrom(Map<String, Object> createOrganisationResponse) {

        return ((List<String>) createOrganisationResponse.get("pbaAccounts"));

    }
}
