# rd-professional-api

Professional Reference Data API

## Purpose

Provides professional reference data to client applications.  Implemented as a Java/SpringBoot application.

### Prerequisites

To run the project you will need to have the following installed:

* Java 8
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

While not essential, it is highly recommended to use the pre-push git hook included in this repository to ensure that all tests are passing. This can be done by running the following command:
`$ git config core.hooksPath .githooks`

### Environment Vars

If running locally for development or testing you will need to set the following environment variables

* export POSTGRES_USERNAME=dbrefdata
* export POSTGRES_PASSWORD=dbrefdata
* export S2S_SECRET=AAAAAAAAAAAAAAAC

### Running the application

To run the API quickly use the docker helper script as follows:

```
./bin/run-in-docker.sh
```

or

```
docker-compose up
```

application will listen on 8090 when started using the above methods.


Alternatively, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```

If required, to run with a low memory consumption, the following can be used:

```
./gradlew --no-daemon assemble && java -Xmx384m -jar build/libs/rd-professional-api.jar
```

### Using the application

To understand if the application is working, you can call it's health endpoint:

```
curl http://localhost:8090/health
```

If the API is running, you should see this response:

```
{"status":"UP"}
```

### DB Initialisation˙

The application uses a Postgres database which can be run through a docker container on its own if required.

this

The application should automatically apply any database migrations using flyway.

### Running integration tests:


You can run the *integration tests* as follows:

```
./gradlew integration
```

### Running functional tests:

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *functional tests* as follows:

```
./gradlew functional
```

If you want to run a specific scenario use this command:

```
./gradlew functional --tests <TestClassName> --info -Dscenario=<Scenario>
```

### Running smoke tests:

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *smoke tests* as follows:

```
./gradlew smoke
```

### Running mutation tests tests:

If you have some time to spare, you can run the *mutation tests* as follows:

```
./gradlew pitest
 ```
If you are using windows machine to run PI test , use following property in gradle.build under pitest section.
```
 useClasspathFile = true
```


As the project grows, these tests will take longer and longer to execute but are useful indicators of the quality of the test suite.

More information about mutation testing can be found here:
http://pitest.org/ 



### Testing in Postman

To test in Postman the easiest way is to start this service using the ./bin/run-in-docker.sh script.  The in postman paste the following script:

```
pm.sendRequest('http://127.0.0.1:8089/token', function (err, res) {
    if (err) {
        console.log(err);
    } else {
        pm.environment.set("token", res.text());
    }
});
```
into the pre-script window.  Also add a header as follows:

```
ServiceAuthorization: Bearer {{token}}
```

### Testing AKS Deployment in Preview

Every Pull-Request (P.R.) raised against master branch will automatically trigger an AKS build and deployment in Preview.

Pre-requisites: 
A1. Install Helm CLI tool on local machine AND verify that it is installed
```
$helm version
```    
A2. Install Kubectl CLI tool on local machine AND verify that it is installed
```
$kubectl version
```
    
A3. Log-in to the azure group (you will need to two-factor authenticate into your hmcts mail account first)
```
az aks get-credentials --resource-group cnp-aks-rg --name cnp-aks-cluster --subscription 1c4f0704-a29e-403d-b719-b90c34ef14c9 --overwrite
```

B1. Checking helm deployment, this will show all reference data application deployments
```
$helm ls --namespace rd
```

B2. Checking an individual deployment
```
$helm status [NAME (FROM STEP B1)] e.g. rd-professional-api-pr-189
```

B3. Deleting a deployment, this may be necessary if running Jenkins again
```
$helm del [name (FROM STEP B1)] e.g. rd-professional-api-pr-212 --purge
```

C1. Checking the kubernetes pods
```
$kubectl -n rd get pods
```

C2. Check the AKS logs
```
$kubectl -n rd logs [NAME (FROM STEP C1)] e.g. rd-professional-api-pr-212-java-786bcbbd79-gvcn9
```
