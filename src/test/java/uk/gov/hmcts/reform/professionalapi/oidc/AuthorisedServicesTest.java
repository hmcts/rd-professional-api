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
        assertThat(authorisedServices)
                .contains("rd_professional_api")
                .contains("rd_user_profile_api")
                .contains("xui_webapp")
                .contains("finrem_payment_service")
                .contains("fpl_case_service")
                .contains("iac")
                .contains("aac-manage-case-assignment");
    }
}
