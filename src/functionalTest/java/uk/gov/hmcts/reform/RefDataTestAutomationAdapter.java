package uk.gov.hmcts.reform;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import java.util.UUID;

public class RefDataTestAutomationAdapter extends DefaultTestAutomationAdapter {

    public static final String EMAIL_TEMPLATE = "freg-test-user-%s@prdfunctestuser.com";
    public static final String ORG_NAME_TEMPLATE = "prd_test-org_%s";

    private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this);

    @Override
    public void doLoadTestData() {
        //loader.importDefinitions();
    }

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        switch (key.toString()) {
            case ("generateUUID"):
                return UUID.randomUUID();
            case ("generateEmailId"):
                return generateRandomEmail();
            case ("generateCompanyNo"):
                return generateComapnyNo();
            case ("generateOrgName"):
                return generateOrgName();
            default:
                return super.calculateCustomValue(scenarioContext, key);
        }
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10));
    }

    public static String generateComapnyNo() {
        return randomAlphanumeric(8);
    }

    public static String generateOrgName() {
        return String.format(ORG_NAME_TEMPLATE, randomAlphanumeric(8));
    }
}
