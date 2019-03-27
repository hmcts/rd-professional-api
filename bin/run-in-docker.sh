#!/usr/bin/env sh

print_help() {
  echo "Script to run docker container for System Reference Data API

  Usage:

  ./run-in-docker.sh [OPTIONS]

  Options:
    --clean, -c                   Clean and install current state of source code
    --install, -i                 Install current state of source code
    --param PARAM=, -p PARAM=     Parse script parameter
    --help, -h                    Print this help block

  Available parameters:

  SERVER_PORT                     HTTP port number the API will listen on (default: 8090)
  "
}

GRADLE_CLEAN=true
GRADLE_INSTALL=true

build_s2s_image() {
    git clone git@github.com:hmcts/s2s-test-tool.git
    cd s2s-test-tool
    ../gradlew build
    docker build -t hmcts/service-token-provider .
    cd .. && rm -rf s2s-test-tool
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

  build_s2s_image

  cd $(dirname "$0")/..

  if [ ${GRADLE_CLEAN} = true ]
  then
    echo "Clearing previous build.."
    ./gradlew clean
  fi

  if [ ${GRADLE_INSTALL} = true ]
  then
    echo "Assembling distribution.."
    ./gradlew assemble
  fi

  echo "Assigning environment variables.."
  export SERVER_PORT="${SERVER_PORT:-8090}"

  chmod +x bin/*

  echo "Bringing up docker containers.."
  docker-compose up
}

while true ; do
  case "$1" in
    -h|--help) print_help ; shift ; break ;;
    -c|--clean) GRADLE_CLEAN=true ; GRADLE_INSTALL=true ; shift ;;
    -i|--install) GRADLE_INSTALL=true ; shift ;;
    -p|--param)
      case "$2" in
        SERVER_PORT=*) SERVER_PORT="${2#*=}" ; shift 2 ;;
        *) shift 2 ;;
      esac ;;
    *) execute_script ; break ;;
  esac
done
