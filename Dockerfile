ARG APP_INSIGHTS_AGENT_VERSION=2.3.1
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

# Mandatory!
ENV APP rd-professional-api.jar
ENV APPLICATION_TOTAL_MEMORY 512M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 48

# Optional
ENV JAVA_OPTS ""

COPY lib/applicationinsights-agent-2.3.1.jar lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

EXPOSE 8090

CMD [ "rd-professional-api.jar" ]
