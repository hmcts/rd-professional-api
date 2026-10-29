# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.8
ARG PLATFORM=""
FROM hmctsprod.azurecr.io/base/java${PLATFORM}:25-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/rd-professional-api.jar /opt/app/

EXPOSE 8090

CMD [ "rd-professional-api.jar" ]
