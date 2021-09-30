#!/usr/bin/env sh

GRADLE_CLEAN=true
GRADLE_INSTALL=true

clean_old_docker_artifacts() {
    docker stop rd-professional-api
    docker stop rd-professional-db

    docker rm rd-professional-api
    docker rm rd-professional-db

    docker rmi hmcts/rd-professional-api
    docker rmi hmcts/rd-professional-db

    docker volume rm rd-professional-api_rd-professional-db-volume
}

execute_script() {

    clean_old_docker_artifacts

    ./gradlew clean assemble

    if [ -f ~/.bash_functions ]; then
        . ~/.bash_functions
        get_az_keyvault_secrets 'rd'
    fi

    export SERVER_PORT="${SERVER_PORT:-8090}"

    pwd

    chmod +x bin/*

    docker-compose up
}

execute_script
