#!/usr/bin/env sh

GRADLE_CLEAN=true
GRADLE_INSTALL=true

clean_old_docker_artifacts() {
    docker stop rd-professional-api
    docker stop rd-professional-db

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

  cd $(dirname "$0")/..

  ./gradlew clean assemble

  export SERVER_PORT="${SERVER_PORT:-8090}"

  chmod +x bin/*

  docker-compose up
}

execute_script
