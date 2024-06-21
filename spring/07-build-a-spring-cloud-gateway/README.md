# 07 - Build a Spring Cloud Gateway

__This guide is part of the [Build, Run and Monitor Intelligent Spring Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

A Spring Cloud gateway allows you to selectively expose your microservices and to route traffic to them and among them. In this section, we will create a Spring Cloud Gateway that will expose the microservices we created in the preceding two sections.

---

## Create a Spring Cloud Gateway

The application that we create in this guide is [available here](gateway/).

To create our gateway, we will invoke the Spring Initalizr service from the command line:

```bash
curl https://start.spring.io/starter.tgz \
    -d type=maven-project \
    -d dependencies=cloud-gateway-reactive,cloud-eureka,cloud-config-client \
    -d baseDir=gateway \
    -d name=gateway \
    -d bootVersion=3.2.5 \
    -d javaVersion=17 \
    | tar -xzvf -
```

> We use the `Cloud Gateway Reactive`, `Eureka Discovery Client` and the `Config Client` components.

## Configure the application

Open `src/main/resources/application.properties` and add the following configuration:

```properties
spring.application.name=gateway
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lowerCaseServiceId=true
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=*
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET
```

- The `spring.cloud.gateway.discovery.locator.enabled=true` part is to configure Spring Cloud Gateway to use the Spring Cloud Service Registry to discover the available microservices.
- The `spring.cloud.gateway.globalcors.corsConfiguration` part is to allow Cross-Origin Resource Sharing (CORS) requests to our gateway. This will be helpful in the next guide, when we will add a front-end application.

## Create the application on Azure Container Apps

As in [01 - Build a simple Java application](../01-build-a-simple-java-application/README.md), create a specific `gateway` application.

```bash
cd gateway
./mvnw clean package -DskipTests
az containerapp create \
    --name gateway \
    --resource-group $RESOURCE_GROUP \
    --environment $ENVIRONMENT \
    --artifact ./target/demo-0.0.1-SNAPSHOT.jar \
    --min-replicas 1 \
    --ingress external \
    --bind $CONFIG_SERVER_NAME $EUREKA_SERVER_NAME \
    --target-port 8080
cd ..
```

## Test the project in the cloud

- Go to your container app `city-service`
- Find the "Application Url" in the "Essentials" section

As the gateway is connected to the Spring Cloud Eureka Server, it should have automatically opened routes to the available microservices, with URL paths in the form of `/microservice-id/**`. For example:

- Test the `city-service` microservice endpoint: 
  ```
  curl https://gateway.victorioussky-9afe1793.canadacentral.azurecontainerapps.io/city-service/cities
  ```
- Test the `weather-service` microservice endpoint by doing: 
  ```
  curl https://gateway.victorioussky-9afe1793.canadacentral.azurecontainerapps.io/weather-service/weather/city?name=Paris%2C%20France
  ```

If you need to check your code, the final project is available in the ["gateway" folder](gateway/).

---

⬅️ Previous guide: [06 - Build a Spring Boot microservice using MySQL](../06-build-a-spring-boot-microservice-using-mysql/README.md)

➡️ Next guide: [08 - Putting it all together, a complete microservice stack](../08-putting-it-all-together-a-complete-microservice-stack/README.md)