# 01 - Build a simple Java application

__This guide is part of the [Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll build a simple Java application and deploy it to Azure Container Apps. This will give us a starting point for adding advanced technologies in later sections.

We use Quarkus and Micronaut frameworks to build the simpliest possible Java application.

---

## Build a simple Quarkus application

The Quarkus application that we create in this guide is [quarkus-simple-application](quarkus-simple-application).

A typical way to create Quarkus applications is to use the [Quarkus maven plugin](https://quarkus.io/guides/quarkus-maven-plugin), [Quarkus CLI](https://quarkus.io/blog/quarkus-cli/) or [Quarkus - Start coding with code.quarkus.io](https://code.quarkus.io/). Feel free to explore more outside this training. For the purposes of this training, we will only use the [Quarkus maven plugin](https://quarkus.io/guides/quarkus-maven-plugin) to create a simple Quarkus application.

In an __empty__ directory execute the `mvn` command line below:

```bash
mvn io.quarkus.platform:quarkus-maven-plugin:3.11.3:create \
    -DprojectGroupId=com.example \
    -DprojectArtifactId=quarkus-simple-application \
    -Dextensions='rest' \
    -DjavaVersion="17"
```

The project includes `quarkus-rest` extension, use Java 17, uses `com.example` as group ID and `quarkus-simple-application` as artifact ID.

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
mvn clean package -f quarkus-simple-application/pom.xml
java -jar quarkus-simple-application/target/quarkus-app/quarkus-run.jar
```

Open another terminal and requesting the `/hello` endpoint should return the "Hello from Quarkus REST" message.

```bash
curl http://localhost:8080/hello --silent
```

Finally, press `Ctrl+C` to stop the application.

### Build and deploy the application on Azure Container Apps

This section shows how to build a native executable, build a Docker image, push it to the Azure Container Registry, and deploy it to the Azure Container Apps.

```bash
# Build and push quarkus-simple-application image to ACR
mvn clean package -Dnative -Dquarkus.native.container-build -f quarkus-simple-application/pom.xml

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
```

Invoke `/hello` endpoint exposed by the Azure Container Apps `quarkus-simple-application` and test if it works as expected:

```bash
APP_URL=https://$(az containerapp show \
    --name quarkus-simple-application \
    --resource-group $RESOURCE_GROUP_NAME \
    --query properties.configuration.ingress.fqdn \
    -o tsv)

# It should return "Hello from Quarkus REST"
curl $APP_URL/hello --silent
```

---

⬅️ Previous guide: [00 - Setup your environment](../00-setup-your-environment/README.md)

➡️ Next guide: [02 - Build a reactive and native Quarkus microservice using PostgreSQL](../02-build-a-reactive-and-native-quarkus-microservice-using-postgresql/README.md)
