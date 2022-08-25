ARG APP_INSIGHTS_AGENT_VERSION=3.3.1
ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/rd-professional-api.jar /opt/app/

EXPOSE 8090

CMD [ "rd-professional-api.jar" ]
