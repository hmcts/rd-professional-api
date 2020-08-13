package uk.gov.hmcts.reform.professionalapi.controller.request.processor;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.StringUtils.repeat;
import static org.apache.commons.lang.StringUtils.rightPad;
import static org.apache.http.client.utils.URLEncodedUtils.parse;

import com.microsoft.applicationinsights.extensibility.TelemetryProcessor;
import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

@Component
@Slf4j
public class TelemetryRequestProcessor implements TelemetryProcessor {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Override
    public boolean process(Telemetry telemetry) {

        try {
            if (isNull(telemetry)) {
                return true;
            }

            if (telemetry instanceof RequestTelemetry) {
                RequestTelemetry requestTelemetry = (RequestTelemetry) telemetry;

                //with curl url encode @ replaced by %40
                requestTelemetry.setUrl(requestTelemetry.getUrlString().replace("%40", "@"));

                List<NameValuePair> params = parse(new URI(requestTelemetry.getUrlString()),
                    Charset.forName("UTF-8"));

                Set<String> values = params.stream().filter(mp -> mp.getValue().contains("@")
                    && mp.getValue().contains(".")).map(NameValuePair::getValue).collect(toSet());

                String pathVariable = getName(requestTelemetry.getUrlString());
                pathVariable = pathVariable.substring(0, pathVariable.indexOf("?") == -1 ? pathVariable.length()
                    : pathVariable.indexOf("?"));

                if (pathVariable.contains(".") && (pathVariable.contains("@"))) {
                    values.add(pathVariable);
                }

                //anonymize  string values
                for (String val : values) {
                    String encodeVal = "";
                    if(val.indexOf("@") >=6) {
                        String id = val.substring(2, (val.indexOf("@")-2));
                        encodeVal = val.replace(id , repeat("*", id.length()));
                    } else {
                        String id = val.substring(0, (val.indexOf("@")));
                        String anonymizedId = id.length() > 3 ?  rightPad(id.substring(0, 2), id.length(), "*") :
                            rightPad(id.substring(0, 1), id.length(), "*");
                        encodeVal = val.replace(id, anonymizedId);
                    }
                    requestTelemetry.setUrl(requestTelemetry.getUrlString().replace(val, encodeVal));
                }
            }
        } catch (Exception exception) {
            log.error("{}::RequestTelemetry error malformed uri", loggingComponentName);
            throw new InvalidRequest("RequestTelemetry error malformed uri");
        }
        return true;
    }
}
