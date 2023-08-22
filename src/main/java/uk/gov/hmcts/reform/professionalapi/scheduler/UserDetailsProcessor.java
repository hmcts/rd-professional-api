package uk.gov.hmcts.reform.professionalapi.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;

import java.util.List;

import static java.util.Collections.singletonList;


@Component
@Slf4j
public class UserDetailsProcessor extends JsrValidationBaseProcessor<UserDetails> {


    @Autowired
    JsrValidatorInitializer<UserDetails> flagDetailsJsrValidatorInitializer;

    @Value("${logging-component-name}")
    String logComponentName;

    @Override
    public void process(Exchange exchange) {
        List<UserDetails> userDetailsList;

        userDetailsList = singletonList((UserDetails) exchange.getIn().getBody());

        log.info(" {} Categories Records count before Validation {}::", logComponentName, userDetailsList.size());


        log.info(" {} Categories Records count after Validation {}::", logComponentName, userDetailsList.size());

        exchange.getMessage().setBody(userDetailsList);
    }

}
