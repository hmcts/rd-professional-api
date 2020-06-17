#!/usr/bin/env sh

GRADLE_CLEAN=true
GRADLE_INSTALL=true

# Test S2S key - not used in any HMCTS key vaults or services
export S2S_SECRET=SZDUA3L7N32PE2IS
export S2S_MICROSERVICE=rd_professional_api

build_s2s_image() {
    git clone https://github.com/hmcts/s2s-test-tool.git
    cd s2s-test-tool
    git checkout allow-all-microservices
    ./gradlew build
    docker build -t hmcts/service-token-provider .
    cd .. && rm -rf s2s-test-tool
}

build_service_auth_app() {
    git clone https://github.com/hmcts/service-auth-provider-app.git
    cd service-auth-provider-app
    ./gradlew build
    docker build -t hmcts/service-token-provider .
    cd .. && rm -rf service-auth-provider-app
}

clean_old_docker_artifacts() {
    docker stop rd-professional-api
    docker stop rd-professional-db
    docker stop service-token-provider

    docker rm rd-professional-api
    docker rm rd-professional-db
    docker rm service-token-provider

    docker rmi hmcts/rd-professional-api
    docker rmi hmcts/rd-professional-db
    docker rmi hmcts/service-token-provider

    docker volume rm rd-professional-api_rd-professional-db-volume
}

execute_script() {

  clean_old_docker_artifacts

    #build_s2s_image

    #build_service_auth_app
    docker-compose down -v
    docker system prune
    ./gradlew clean assemble

    export SERVER_PORT="${SERVER_PORT:-8090}"

    pwd

    chmod +x bin/*

    docker-compose up
}

execute_script
