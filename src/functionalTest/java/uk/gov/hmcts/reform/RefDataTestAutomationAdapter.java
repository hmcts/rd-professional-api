package uk.gov.hmcts.reform;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

import lombok.SneakyThrows;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

public class RefDataTestAutomationAdapter extends DefaultTestAutomationAdapter {

    public static final String EMAIL_TEMPLATE = "freg-test-user-%s@prdfunctestuser.com";
    public static final String ORG_NAME_TEMPLATE = "prd_test-org_%s";
    public static final String EXT_USER_EMAIL_ID = "EXT_USER_EMAIL_ID";

    private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this);

    @Override
    public void doLoadTestData() {
    }

    @SneakyThrows
    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        switch (key.toString()) {
            case ("generateUUID"):
                return UUID.randomUUID();
            case ("generateEmailId"):
                return generateRandomEmail();
            case ("generateCompanyNo"):
                return generateCompanyNo();
            case ("generateOrgName"):
                return generateOrgName();
            case ("generateExternalUserEmailId"):
                return generateExternalUserEmailId();
            default:
                return super.calculateCustomValue(scenarioContext, key);
        }
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10));
    }

    public static String generateCompanyNo() {
        return randomAlphanumeric(8);
    }

    public static String generateOrgName() {
        return String.format(ORG_NAME_TEMPLATE, randomAlphanumeric(8));
    }


    public static String generateExternalUserEmailId() {
        String email = generateRandomEmail();
        getModifiableEnvironmentMap().put(EXT_USER_EMAIL_ID, email);
        return email;
    }

    public static Map<String, String> getModifiableEnvironmentMap() {
        try {
            Map<String,String> unmodifiableEnv = System.getenv();
            Class<?> cl = unmodifiableEnv.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String,String> modifiableEnv = (Map<String,String>) field.get(unmodifiableEnv);
            return modifiableEnv;
        } catch (Exception e) {
            throw new RuntimeException("Unable to access writable environment variable map.");
        }
    }
}
