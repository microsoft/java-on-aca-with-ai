# 03 - Build a Micronaut microservice using MySQL

__This guide is part of the [Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll build a [Micronaut](https://micronaut.io/) microserver that references guide [ACCESS A DATABASE WITH MICRONAUT DATA JDBC](https://guides.micronaut.io/latest/micronaut-data-jdbc-repository-maven-java.html). The service is bound to an [Azure Database For MySQL Flexible server](https://learn.microsoft.com/azure/mysql/flexible-server/overview), and it uses [Flyway](https://guides.micronaut.io/latest/micronaut-flyway-maven-java.html) to manage database schema migrations including initial data population. Furthermore, it utilizes OpenTelemetry java agent to automatically capture telemetry data and send to OpenTelemetry collector, see [OpenTelemetry Java Agent](https://opentelemetry.io/docs/zero-code/java/agent/) for more information.

---

## Create a Micronaut application

The Micronaut application that we create in this guide is [weather-service](weather-service).

### Initialize the database

The `weather-service/src/main/resources/db/migration/V1__schema.sql` file contains the Flyway migration script that creates the `weather` table and populates it with two records in the database:

```sql
DROP TABLE IF EXISTS weather;

CREATE TABLE weather (
   city         VARCHAR(255) NOT NULL UNIQUE PRIMARY KEY,
   description  VARCHAR(255) NOT NULL,
   icon         VARCHAR(255) NOT NULL
);

INSERT INTO weather (city, description, icon) VALUES ('Paris, France', 'Very cloudy!', 'weather-fog');
INSERT INTO weather (city, description, icon) VALUES ('London, UK', 'Quite cloudy', 'weather-pouring');
```

### Micronaut code to get the data from the database

The `weather-service/src/main/java/com/example/domain/Weather.java` is a Micronaut data entity that represents the `weather` table in the database:

```java
package com.example.domain;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.validation.constraints.NotNull;

@Serdeable
@MappedEntity
public class Weather {

    @Id
    private String city;

    @NotNull
    private String description;

    @NotNull
    private String icon;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "city=" + city +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
```

The `weather-service/src/main/java/com/example/WeatherRepository.java` is a repository that extends `io.micronaut.data.repository.CrudRepository` to interact with the database:

```java
package com.example;

import com.example.domain.Weather;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface WeatherRepository extends CrudRepository<Weather, String> {
}
```

And the `weather-service/src/main/java/com/example/WeatherController.java` implements a REST endpoint `/weather/city` that returns the weather for a specific city:

```java
package com.example;

import com.example.domain.Weather;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import jakarta.validation.constraints.NotBlank;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/weather")
public class WeatherController {

    private static Logger logger = LoggerFactory.getLogger(WeatherController.class);

    protected final WeatherRepository weatherRepository;

    public WeatherController(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    @Get("/city")
    public Optional<Weather> show(@QueryValue("name") @NotBlank String cityName) {
        logger.info("Getting weather for city: " + cityName);
        return weatherRepository.findById(cityName);
    }

}
```

### Dependencies and configuration

The `weather-service/pom.xml` file includes dependencies for connecting to the MySQL database with Micronaut Data JDBC and Flyway:

```xml
    <dependency>
      <groupId>io.micronaut.data</groupId>
      <artifactId>micronaut-data-jdbc</artifactId>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>io.micronaut.flyway</groupId>
      <artifactId>micronaut-flyway</artifactId>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>io.micronaut.sql</groupId>
      <artifactId>micronaut-jdbc-hikari</artifactId>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-mysql</artifactId>
      <scope>runtime</scope>
    </dependency>
```

The `weather-service/src/main/resources/application.properties` file contains the configuration for the Flyway, Micronaut Data JDBC, and OpenTelemetry:

```properties
flyway.datasources.default.enabled=true
datasources.default.db-type=mysql
datasources.default.dialect=MYSQL
datasources.default.driver-class-name=com.mysql.cj.jdbc.Driver

# OpenTelemetry configurations
otel.traces.exporter=otlp
```

For database connection configuration, we will create secrets and reference them in environment variables when deploying the application on Azure Container Apps later.

### Automatic telemetry data capture with OpenTelemetry Java agent

The `weather-service/Dockerfile-otel-agent` Dockerfile builds the Docker image that includes the OpenTelemetry Java agent:

```Dockerfile
FROM openjdk:17

WORKDIR /app

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

COPY target/weather-service-0.1.jar /app/weather-service.jar

EXPOSE 8080

CMD ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "weather-service.jar"]
```

The output Docker image can automatically capture telemetry data and send it to the OpenTelemetry collector.

## Build and deploy the application on Azure Container Apps

Similar to [Build and deploy Java application on Azure Container Apps](../01-build-a-simple-java-application/README.md#build-and-deploy-java-application-on-azure-container-apps), create a specific `weather-service` application in your Azure Container Apps.

```bash
# Build and push weather-service image to ACR
cd ${BASE_DIR}/03-build-a-micronaut-microservice-using-mysql
mvn clean package -DskipTests -f weather-service/pom.xml

docker buildx build --platform linux/amd64 -f weather-service/Dockerfile-otel-agent -t weather-service ./weather-service
docker tag weather-service ${ACR_LOGIN_SERVER}/weather-service
docker login $ACR_LOGIN_SERVER \
    -u $ACR_USER_NAME \
    -p $ACR_PASSWORD
docker push ${ACR_LOGIN_SERVER}/weather-service

# Deploy weather-service to Azure Container Apps
export DATASOURCES_DEFAULT_URL=jdbc:mysql://$MYSQL_SERVER_NAME.mysql.database.azure.com:3306/$DB_NAME
export DATASOURCES_DEFAULT_USERNAME=$DB_ADMIN
export DATASOURCES_DEFAULT_PASSWORD=$DB_ADMIN_PWD
az containerapp create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name weather-service \
    --image ${ACR_LOGIN_SERVER}/weather-service \
    --environment $ACA_ENV \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USER_NAME \
    --registry-password $ACR_PASSWORD \
    --target-port 8080 \
    --secrets \
        datasourceurl=${DATASOURCES_DEFAULT_URL} \
        datasourceusername=${DATASOURCES_DEFAULT_USERNAME} \
        datasourcepassword=${DATASOURCES_DEFAULT_PASSWORD} \
    --env-vars \
        DATASOURCES_DEFAULT_URL=secretref:datasourceurl \
        DATASOURCES_DEFAULT_USERNAME=secretref:datasourceusername \
        DATASOURCES_DEFAULT_PASSWORD=secretref:datasourcepassword \
    --ingress 'external' \
    --min-replicas 1
cd ${BASE_DIR}
```

## Test the project in the cloud

Invoke `/weather/city` endpoint exposed by the Azure Container Apps `weather-service` and test if it works as expected:

```bash
APP_URL=https://$(az containerapp show \
    --name weather-service \
    --resource-group $RESOURCE_GROUP_NAME \
    --query properties.configuration.ingress.fqdn \
    -o tsv)

# You should see the weather for London, UK returned: {"city":"London, UK","description":"Quite cloudy","icon":"weather-pouring"}
curl $APP_URL/weather/city?name=London%2C%20UK --silent

# You should see the weather for Paris, France returned: {"city":"Paris, France","description":"Very cloudy!","icon":"weather-fog"}
curl $APP_URL/weather/city?name=Paris%2C%20France --silent
```

Finally, let's update the `weather-service` to make it accept internal traffic only instead of public accessible.

```bash
az containerapp ingress update \
    --name weather-service \
    --resource-group $RESOURCE_GROUP_NAME \
    --type internal
```

---

⬅️ Previous guide: [02 - Build a reactive and native Quarkus microservice using PostgreSQL](../02-build-a-reactive-and-native-quarkus-microservice-using-postgresql/README.md)

➡️ Next guide: [04 - Build a NGINX Reverse Proxy](../04-build-a-nginx-reverse-proxy/README.md)
