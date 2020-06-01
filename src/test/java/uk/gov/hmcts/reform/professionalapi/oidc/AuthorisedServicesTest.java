package uk.gov.hmcts.reform.professionalapi.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application.yaml")
public class AuthorisedServicesTest {

    @Value("${services}")
    protected String authorisedServices;

    @Test
    public void authorisedServicesList() {
        assertThat(authorisedServices).contains("rd_professional_api");
        assertThat(authorisedServices).contains("rd_user_profile_api");
        assertThat(authorisedServices).contains("xui_webapp");
        assertThat(authorisedServices).contains("finrem_payment_service");
        assertThat(authorisedServices).contains("fpl_case_service");
        assertThat(authorisedServices).contains("iac");
        assertThat(authorisedServices).contains("aac-manage-case-assignment");
    }
}
