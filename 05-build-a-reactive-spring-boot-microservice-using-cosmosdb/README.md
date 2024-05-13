# 05 - Build a reactive Spring Boot microservice using Cosmos DB

__This guide is part of the [Build, Run and Monitor Intelligent Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll build an application that uses a [Cosmos DB database](https://docs.microsoft.com/en-us/azure/cosmos-db) in order to access a globally-distributed database with optimum performance.

We'll use the reactive programming paradigm to build our microservice in this section, leveraging the [Spring reactive stack](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html). In contrast, we'll build a more traditional data-driven microservice in the next section.

---

## Prepare the Azure Cosmos DB database

From section 00, you should already have a Cosmos DB account named `javalab-cosmos-<unique string>`. [Open Azure Portal](https://portal.azure.com) and navigate to your Cosmos DB account.

- Click on the `Data Explorer` menu item
  - Expand the container named `java-on-aca-cosmosdb`.
  - In that container, expand the container named `City`.
  - Click on `Items` and use the `New Item` button to create some sample items using the contents below:

    ```json
    {
        "name": "Paris, France"
    }
    ```

    ```json
    {
        "name": "London, UK"
    }
    ```

![Data explorer](media/01-data-explorer.png)

## Create a Spring WebFlux microservice

The microservice that we create in this guide is [available here](city-service/).

To create our microservice, we will invoke the Spring Initalizr service from the command line:

```bash
curl https://start.spring.io/starter.tgz \
    -d type=maven-project \
    -d dependencies=webflux,cloud-eureka,cloud-config-client \
    -d baseDir=city-service \
    -d name=city-service \
    -d bootVersion=3.2.5 \
    -d javaVersion=17 \
    | tar -xzvf -
```

> We use the `Spring WebFlux`, `Eureka Discovery Client` and the `Config Client` Spring Boot starters.

## Add the Azure Cosmos DB API

In the application's `pom.xml` file, add the Azure Cosmos DB dependency just after the `spring-cloud-starter-netflix-eureka-client` dependency:

```xml
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-cosmos</artifactId>
            <version>4.59.0</version>
        </dependency>
```

## Add Spring reactive code to get the data from the database

Next to the `CityServiceApplication` class, create a `City` domain object:

```java
package com.example.demo;

class City {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

Then, in the same location, create a new `CityController.java` file that
contains the code that will be used to query the database.

> The CityController class will get its Cosmos DB configuration from the Azure Container Apps Service Connector that we will configure later.

```java
package com.example.demo;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;
import java.util.List;

@RestController
public class CityController {

    @Value("${azure.cosmos.uri}")
    private String cosmosDbUrl;

    @Value("${azure.cosmos.key}")
    private String cosmosDbKey;

    @Value("${azure.cosmos.database}")
    private String cosmosDbDatabase;

    private CosmosAsyncContainer container;

    @PostConstruct
    public void init() {
        container = new CosmosClientBuilder()
                .endpoint(cosmosDbUrl)
                .key(cosmosDbKey)
                .buildAsyncClient()
                .getDatabase(cosmosDbDatabase)
                .getContainer("City");
    }

    @GetMapping("/cities")
    public Flux<List<City>> getCities() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        return container.queryItems("SELECT TOP 20 * FROM City c", options, City.class)
                .byPage()
                .map(FeedResponse::getResults);
    }
}
```

## Create the application on Azure Container Apps

As in [01 - Build a simple Java application](../01-build-a-simple-java-application/README.md), create a specific `city-service` application in your Azure Container Apps:

```bash
cd city-service
./mvnw clean package -DskipTests
az containerapp create \
    --name city-service \
    --resource-group $RESOURCE_GROUP \
    --environment $ENVIRONMENT \
    --artifact ./target/demo-0.0.1-SNAPSHOT.jar \
    --min-replicas 1 \
    --ingress external \
    --bind $CONFIG_SERVER_NAME $EUREKA_SERVER_NAME \
    --target-port 8080
cd ..
```

## Connect the Azure Cosmos DB database to the application

Azure Container Apps can automatically connect the Cosmos DB database we created to our microservice.

- Go to [the Azure portal](https://portal.azure.com) and look for your container app `city-service`
- Select `Service Connector (preview)` from the left table of contents
- Click on `+ Create`
- Choose `Cosmos DB` as the Service type
- Give your Connection a name, for example "cosmos_city"
- Select the `NoSQL` API type
- Select the Cosmos DB account and Database we created in the initial 00 setup step
- Verify that the Client type is `SpringBoot`
- Click the `Next: Authentication` button

![Connect to Cosmos DB database 1 of 4](media/02-service-connector-cosmos.png)

- Select `Connection string` for the authentication type
- Expand the `Advanced` tag below to verify the property names injected into the connected app (Optional)
- Click the `Next: Networking` button

![Connect to Cosmos DB database 2 of 4](media/03-service-connector-cosmos.png)

- Leave `Configure firewall rules to enable access to target service` selected
- Click the `Next: Review + Create` button

![Connect to Cosmos DB database 3 of 4](media/04-service-connector-cosmos.png)

- Once validation passes, click the `Create` button to create the Service Connector

![Connect to Cosmos DB database 4 of 4](media/05-service-connector-cosmos.png)

## Test the project in the cloud

- Go to your container app `city-service`
- Find the "Application Url" in the "Essentials" section

You can now use `curl` to test the `/cities` endpoint, and it should give you the list of cities you created. For example, if you only created `Paris, France` and `London, UK` as is shown in this guide, you should get:

```json
[[{"name":"Paris, France"},{"name":"London, UK"}]]
```

If you need to check your code, the final project is available in the ["city-service" folder](city-service/).

Finally, let's update the `city-service` to make it accept internal traffic only instead of public accessible.

```bash
az containerapp ingress update \
    --name city-service \
    --resource-group $RESOURCE_GROUP \
    --type internal
```

---

⬅️ Previous guide: [04 - Build a Spring Boot microservice using Spring Cloud features](../04-build-a-spring-boot-microservice-using-spring-cloud-features/README.md)

➡️ Next guide: [06 - Build a Spring Boot microservice using MySQL](../06-build-a-spring-boot-microservice-using-mysql/README.md)
