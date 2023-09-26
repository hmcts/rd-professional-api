package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.domain.CommonCsvField;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CommonCsvFieldProcessor implements Processor {


    /**
     * Processes the message exchange and set the row id for the record(s).
     * @param exchange the message exchange
     */
    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        // setting initial value as 2 because first row contains headers.
        if (exchange.getIn().getBody() instanceof List) {
            AtomicInteger counter = new AtomicInteger(2);
            List<CommonCsvField> body = (List<CommonCsvField>) exchange.getIn().getBody();
            body.forEach(i -> i.setRowId((long) counter.getAndIncrement())
            );
            exchange.getMessage().setBody(body);
        } else {
            CommonCsvField body = (CommonCsvField) exchange.getIn().getBody();
            // setting row id to 2 because there is only one record
            body.setRowId(2L);
            exchange.getMessage().setBody(body);
        }
    }
}
