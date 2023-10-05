package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_CREATE_EXTERNAL_V2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_CREATE_INTERNAL_V2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_MFA_LD_FLAG;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_RETRIEVE_EXTERNAL_V2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_RETRIEVE_INTERNAL_V2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_RETRIEVE_PBA_EXTERNAL_V2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_RETRIEVE_PBA_INTERNAL_V2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_UPDATE_INTERNAL_V2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.RD_PROFESSIONAL_MULTIPLE_ADDRESS;
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
        launchDarklyMap.put("OrganisationExternalController.addContactInformationsToOrganisation",
                RD_PROFESSIONAL_MULTIPLE_ADDRESS);
        launchDarklyMap.put("OrganisationExternalController.deleteMultipleAddressesOfOrganisation",
                RD_PROFESSIONAL_MULTIPLE_ADDRESS);
        launchDarklyMap.put("OrganisationInternalControllerV2.retrieveOrganisations",
            PRD_RETRIEVE_INTERNAL_V2);
        launchDarklyMap.put("OrganisationInternalControllerV2.createOrganisation",
            PRD_CREATE_INTERNAL_V2);
        launchDarklyMap.put("OrganisationInternalControllerV2.updatesOrganisation",
            PRD_UPDATE_INTERNAL_V2);
        launchDarklyMap.put("OrganisationInternalControllerV2.retrievePaymentAccountBySuperUserEmail",
            PRD_RETRIEVE_PBA_INTERNAL_V2);
        launchDarklyMap.put("OrganisationExternalControllerV2.retrievePaymentAccountByEmail",
            PRD_RETRIEVE_PBA_EXTERNAL_V2);
        launchDarklyMap.put("OrganisationExternalControllerV2.retrieveOrganisationUsingOrgIdentifier",
            PRD_RETRIEVE_EXTERNAL_V2);
        launchDarklyMap.put("OrganisationExternalControllerV2.createOrganisationUsingExternalController",
            PRD_CREATE_EXTERNAL_V2);
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




