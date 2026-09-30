package uk.gov.hmcts.reform.professionalapi.util;

import lombok.Getter;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

@Getter
public class TestApplicationServer
        implements ApplicationListener<WebServerInitializedEvent> {

    private volatile int serverPort;

    public String getBaseUrl() {
        return "http://127.0.0.1:" + serverPort;
    }

    public String url(String path) {
        return getBaseUrl()
                + (path.startsWith("/") ? path : "/" + path);
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.serverPort = event.getWebServer().getPort();
    }

}