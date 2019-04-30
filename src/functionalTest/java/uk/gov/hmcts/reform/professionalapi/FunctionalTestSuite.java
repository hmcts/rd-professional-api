package uk.gov.hmcts.reform.professionalapi;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient;
import uk.gov.hmcts.reform.professionalapi.client.S2sClient;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-functional.yaml")
public abstract class FunctionalTestSuite {

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${targetInstance}")
    protected String professionalApiUrl;

    protected ProfessionalApiClient professionalApiClient;

    @Before
    public void setUp() {
        String s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();

        professionalApiClient = new ProfessionalApiClient(
                                                          professionalApiUrl,
                                                          s2sToken);
    }

    @After
    public void tearDown() {
    }
}
