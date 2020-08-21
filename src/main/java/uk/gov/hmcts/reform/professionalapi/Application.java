package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@Slf4j
@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication
@EnableCaching
@EnableFeignClients(basePackages = {
        "uk.gov.hmcts.reform.professionalapi" },
        basePackageClasses = { IdamApi.class, ServiceAuthorisationApi.class }
        )
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application  {
    public static void main(final String[] args) {
        log.error("Getting System environment variables ");
        log.error("variables :  " + System.getenv());
        System.getenv().entrySet().iterator().forEachRemaining(entry -> {
            log.error(entry.getKey() + "     " + entry.getValue());
        });
        log.error("End getting env veriables");
        System.out.println("Getting System environment variables ");
        System.out.println("variables :  " + System.getenv());
        System.getenv().entrySet().iterator().forEachRemaining(entry -> {
            System.out.println(entry.getKey() + "     " + entry.getValue());
        });
        System.out.println("End getting env veriables");
        SpringApplication.run(Application.class, args);
    }
}