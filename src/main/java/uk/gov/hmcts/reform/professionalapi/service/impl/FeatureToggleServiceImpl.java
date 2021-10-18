package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.service.FeatureToggleService;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_MFA_LD_FLAG;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.RD_PROFESSIONAL_MULTI_PBA_LD_FLAG;

@Service
public class FeatureToggleServiceImpl implements FeatureToggleService {

    @Autowired
    private final LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    private final String userName;

    private Map<String, String> launchDarklyMap;

    @Autowired
    public FeatureToggleServiceImpl(LDClient ldClient, @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.userName = userName;
    }

    /**
     * add controller.method name, flag name  in map to apply ld flag on api like below
     * launchDarklyMap.put("OrganisationExternalController.retrieveOrganisationsByStatusWithAddressDetailsOptional",
     * "prd-aac-get-org-by-status");
     */
    @PostConstruct
    public void mapServiceToFlag() {
        launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("OrganisationMfaStatusController.retrieveMfaStatusByUserId",
                PRD_MFA_LD_FLAG);
        launchDarklyMap.put("OrganisationInternalController.updateOrgMfaStatus",
                PRD_MFA_LD_FLAG);
        launchDarklyMap.put("OrganisationExternalController.deletePaymentAccountsOfOrganisation",
                RD_PROFESSIONAL_MULTI_PBA_LD_FLAG);
        launchDarklyMap.put("OrganisationInternalController.retrieveOrgByPbaStatus",
                RD_PROFESSIONAL_MULTI_PBA_LD_FLAG);
        launchDarklyMap.put("OrganisationExternalController.addPaymentAccountsToOrganisation",
                RD_PROFESSIONAL_MULTI_PBA_LD_FLAG);
        launchDarklyMap.put("OrganisationInternalController.updateAnOrganisationsRegisteredPbas",
                RD_PROFESSIONAL_MULTI_PBA_LD_FLAG);
    }

    @Override
    public boolean isFlagEnabled(String serviceName, String flagName) {
        LDUser user = new LDUser.Builder(userName)
            .firstName(userName)
            .custom("environment", environment)
            .build();

        return ldClient.boolVariation(flagName, user, false);
    }

    @Override
    public Map<String, String> getLaunchDarklyMap() {
        return launchDarklyMap;
    }
}




