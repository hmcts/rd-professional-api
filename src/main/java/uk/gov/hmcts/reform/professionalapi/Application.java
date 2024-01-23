package uk.gov.hmcts.reform.professionalapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.professionalapi", "uk.gov.hmcts.reform.idam"})
@EnableCaching
@ImportAutoConfiguration({
        FeignAutoConfiguration.class
})
@EnableFeignClients(basePackages = {
        "uk.gov.hmcts.reform.professionalapi" },
        basePackageClasses = { IdamApi.class, ServiceAuthorisationApi.class })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application  {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
