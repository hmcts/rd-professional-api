package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class BulkCustomerDetailsProcessor  implements Processor {


    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {

        //TODO validations here
    }
}
