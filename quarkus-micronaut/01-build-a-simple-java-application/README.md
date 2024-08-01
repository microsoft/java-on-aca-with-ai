# 01 - Build a simple Java application

__This guide is part of the [Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll build a simple Java application and deploy it to Azure Container Apps. This will give us a starting point for adding advanced technologies in later sections.

We use Quarkus and Micronaut frameworks to build the simpliest possible Java application.

---

## Build a simple Quarkus application

The Quarkus application that we create in this guide is [quarkus-simple-application](quarkus-simple-application).

A typical way to create Quarkus applications is to use the [Quarkus maven plugin](https://quarkus.io/guides/quarkus-maven-plugin), [Quarkus CLI](https://quarkus.io/blog/quarkus-cli/) or [Quarkus - Start coding with code.quarkus.io](https://code.quarkus.io/). Feel free to explore more outside this training. For the purposes of this training, we will only use the [Quarkus maven plugin](https://quarkus.io/guides/quarkus-maven-plugin) to create a simple Quarkus application.

Execute the `mvn` command line below:

```bash
mvn io.quarkus.platform:quarkus-maven-plugin:3.11.3:create \
    -DprojectGroupId=com.example \
    -DprojectArtifactId=quarkus-simple-application \
    -Dextensions='rest' \
    -DjavaVersion="17"
```

The project includes `quarkus-rest` extension, uses Java 17, uses `com.example` as group ID and `quarkus-simple-application` as artifact ID.

### The Greeting REST resource

During the project creation, the `quarkus-simple-application/src/main/java/com/example/GreetingResource.java` file has been created with the following content:

```java
package com.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }
}
```

It’s a very simple REST endpoint, returning "Hello from Quarkus REST" to requests on "/hello".

### Test the project locally

Run the project:

```bash
cd ${BASE_DIR}/01-build-a-simple-java-application
mvn clean package -f quarkus-simple-application/pom.xml
java -jar quarkus-simple-application/target/quarkus-app/quarkus-run.jar
```

Open another terminal and requesting the `/hello` endpoint should return the "Hello from Quarkus REST" message.

```bash
curl http://localhost:8080/hello --silent
```

Switch back to the terminal where the Quarkus application is running, and press `Ctrl+C` to stop the application.

Build and run the Quarkus application as a native executable:

```bash
mvn clean package -Dnative -DskipTests -Dquarkus.native.container-build -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:23.1.4.0-Final-java21-amd64 -f quarkus-simple-application/pom.xml

./quarkus-simple-application/target/quarkus-simple-application-1.0.0-SNAPSHOT-runner
```

> If you run the native executable in MacOS, you may receive similar error message "zsh: exec format error: ./quarkus-simple-application/target/quarkus-simple-application-1.0.0-SNAPSHOT-runner". This is because the Quarkus builder image creates Linux ELF64 executables that is incompatible with MacOS. If you really want to build a MacOS compatible native exectutable, you have to configure GraalVM locally by following [this guide](https://quarkus.io/guides/building-native-image#configuring-graalvm), and run `mvn clean package -Dnative -DskipTests -f quarkus-simple-application/pom.xml`.
> 
> Please notice that you still need to use the Quarkus builder image to build the native executable, whic is required to build the container image later for Azure Container Apps.

Test the application using the sampe approach as before, and press `Ctrl+C` to stop the application.

### Build and deploy Java application on Azure Container Apps

This section shows how to build a Java application (Quarkus native executable), build a Docker image, push it to the Azure Container Registry, and deploy it to the Azure Container Apps.

```bash
# Build and push quarkus-simple-application image to ACR
docker buildx build --platform linux/amd64 -f quarkus-simple-application/src/main/docker/Dockerfile.native -t quarkus-simple-application ./quarkus-simple-application
docker tag quarkus-simple-application ${ACR_LOGIN_SERVER}/quarkus-simple-application
docker login $ACR_LOGIN_SERVER \
    -u $ACR_USER_NAME \
    -p $ACR_PASSWORD
docker push ${ACR_LOGIN_SERVER}/quarkus-simple-application

# Deploy quarkus-simple-application to Azure Container Apps
az containerapp create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name quarkus-simple-application \
    --image ${ACR_LOGIN_SERVER}/quarkus-simple-application \
    --environment $ACA_ENV \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USER_NAME \
    --registry-password $ACR_PASSWORD \
    --target-port 8080 \
    --ingress 'external' \
    --min-replicas 1
cd ${BASE_DIR}
```

Alternatively, there is an existing Docker image stored in the GitHub Container Registry, you can deploy it to the Azure Container Apps directly to save the time that is required to build Quarkus native executable and Docker image:

```bash
# Deploy quarkus-simple-application with the existing image ghcr.io/microsoft/quarkus-simple-application-v1 to Azure Container Apps
az containerapp create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name quarkus-simple-application \
    --image ghcr.io/microsoft/quarkus-simple-application-v1 \
    --environment $ACA_ENV \
    --target-port 8080 \
    --ingress 'external' \
    --min-replicas 1
cd ${BASE_DIR}
```

Fetch the URL of the Azure Container Apps `quarkus-simple-application`:

```bash
APP_URL=https://$(az containerapp show \
    --name quarkus-simple-application \
    --resource-group $RESOURCE_GROUP_NAME \
    --query properties.configuration.ingress.fqdn \
    -o tsv)
```

Wait for a while until the container is up and running, then invoke `/hello` endpoint and test if it works as expected:

```bash
# It should return "Hello from Quarkus REST"
curl $APP_URL/hello --silent
```

## Build a simple Micronaut application

The Micronaut application that we create in this guide is [micronaut-simple-application](micronaut-simple-application).

A typical way to create Micronaut applications is to use the [Micronaut Command Line Interface](https://docs.micronaut.io/latest/guide/#cli) or [Micronaut Launch](https://launch.micronaut.io/). Feel free to explore more outside this training. For the purposes of this training, we will only use the [Micronaut Launch](https://launch.micronaut.io/) to create a simple Micronaut application.

Execute the following commands:

```bash
curl --location \
    --request GET 'https://launch.micronaut.io/create/default/com.example.micronaut-simple-application?lang=JAVA&build=MAVEN&test=JUNIT&javaVersion=JDK_17' \
    --output micronaut-simple-application.zip \
    && unzip micronaut-simple-application.zip \
    && rm -rf micronaut-simple-application.zip
```

The project uses Java 17, uses Maven as build tool, uses `com.example` as group ID and `micronaut-simple-application` as artifact ID.

### Add a Hello REST controller

After the project is created, run the following command to create a Hello REST controller located in `micronaut-simple-application/src/main/java/com/example/HelloController.java`:

```bash
cat << EOF > micronaut-simple-application/src/main/java/com/example/HelloController.java
package example.micronaut;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/hello") 
public class HelloController {
    @Get 
    @Produces(MediaType.TEXT_PLAIN) 
    public String hello() {
        return "Hello from Micronaut REST"; 
    }
}
EOF
```

It’s a very simple REST endpoint, returning "Hello from Micronaut REST" to requests on "/hello".

### Test the project locally

Run the project:

```bash
cd ${BASE_DIR}/01-build-a-simple-java-application
mvn clean package -f micronaut-simple-application/pom.xml
java -jar micronaut-simple-application/target/micronaut-simple-application-0.1.jar
```

Open another terminal and requesting the `/hello` endpoint should return the "Hello from Micronaut REST" message.

```bash
curl http://localhost:8080/hello --silent
```

Finally, press `Ctrl+C` to stop the application.

### Build and deploy Java application on Azure Container Apps using jar artifact

This section shows how to deploy `micronaut-simple-application` to the Azure Container Apps with a jar artifact.

```bash
az containerapp create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name micronaut-simple-application \
    --artifact micronaut-simple-application/target/micronaut-simple-application-0.1.jar \
    --environment $ACA_ENV \
    --target-port 8080 \
    --ingress 'external' \
    --min-replicas 1
cd ${BASE_DIR}
```

Alternatively, there is an existing Docker image stored in the GitHub Container Registry, you can deploy it to the Azure Container Apps directly:

```bash
# Deploy micronaut-simple-application with the existing image ghcr.io/microsoft/micronaut-simple-application-v1 to Azure Container Apps
az containerapp create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name micronaut-simple-application \
    --image ghcr.io/microsoft/micronaut-simple-application-v1 \
    --environment $ACA_ENV \
    --target-port 8080 \
    --ingress 'external' \
    --min-replicas 1
cd ${BASE_DIR}
```

Fetch the URL of the Azure Container Apps `micronaut-simple-application`:

```bash
APP_URL=https://$(az containerapp show \
    --name micronaut-simple-application \
    --resource-group $RESOURCE_GROUP_NAME \
    --query properties.configuration.ingress.fqdn \
    -o tsv)
```

Wait for a while until the container is up and running, then invoke `/hello` endpoint and test if it works as expected:

```bash
# It should return "Hello from Micronaut REST"
curl $APP_URL/hello --silent
```

## Conclusion

Congratulations, you have deployed your first Java application to Azure Container Apps!

---

⬅️ Previous guide: [00 - Setup your environment](../00-setup-your-environment/README.md)

➡️ Next guide: [02 - Build a reactive and native Quarkus microservice using PostgreSQL](../02-build-a-reactive-and-native-quarkus-microservice-using-postgresql/README.md)
