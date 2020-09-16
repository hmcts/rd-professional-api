package uk.gov.hmcts.reform.professionalapi.service;

import java.util.Map;

public interface FeatureToggleService {

    boolean isFlagEnabled(String serviceName, String flagName);

    Map<String, String> getLaunchDarklyMap();
}
