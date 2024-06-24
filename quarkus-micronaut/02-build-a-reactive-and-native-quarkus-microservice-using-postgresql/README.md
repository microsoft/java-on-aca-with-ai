# 02 - Build a reactive and native Quarkus microservice using PostgreSQL

__This guide is part of the [Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll build a reactive and native [Quarkus](https://quarkus.io/) microservice that references guides [GETTING STARTED WITH REACTIVE](https://quarkus.io/guides/getting-started-reactive) and [SIMPLIFIED HIBERNATE REACTIVE WITH PANACHE](https://quarkus.io/guides/hibernate-reactive-panache). The service is bound to an [Azure Database for PostgreSQL Flexible Server](https://learn.microsoft.com/azure/postgresql/flexible-server/overview), and it uses [Liquibase](https://quarkus.io/guides/liquibase) to manage database schema migrations including initial data population. Furthermore, it utilizes OpenTelemetry to instrument the application and send distributed tracing to OpenTelemetry collector, see [USING OPENTELEMETRY](https://quarkus.io/guides/opentelemetry) for more information.

---

## Create a Quarkus application

The Quarkus application that we create in this guide is [city-service](city-service).

### Initialize the database

The `city-service/src/main/resources/db/changeLog.xml` file contains the Liquibase changelog that creates the `city` table and populates it with two cities in the database:

```xml
<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
        http://www.liquibase.org/xml/ns/dbchangelog-ext https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd">
    <changeSet id="1" author="city-service">
        <createTable tableName="city">
            <column name="id" type="bigint">
                <constraints primaryKey="true"/>
            </column>
            <column name="name" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
    
    <changeSet id="2" author="city-service">
        <insert tableName="city">
            <column name="id" value="1" valueNumeric="true"/>
            <column name="name" value="Paris, France" />
        </insert>
    </changeSet>
    
    <changeSet id="3" author="city-service">
        <insert tableName="city">
            <column name="id" value="2" valueNumeric="true"/>
            <column name="name" value="London, UK" />
        </insert>
    </changeSet>
</databaseChangeLog>
```

### Quarkus reactive code to get the data from the database

The `city-service/src/main/java/com/example/City.java` is a Quarkus reactive Panache entity that represents the `city` table in the database:

```java
package com.example;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import jakarta.persistence.*;

@Entity
public class City extends PanacheEntity {

    @Column(unique = true)
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

And the `city-service/src/main/java/com/example/CityResource.java` implements a REST endpoint `cities` that returns all cities from the database in a reactive way using `io.smallrye.mutiny.Uni` class:

```java
package com.example;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/")
public class CityResource {

    @GET
    @Path("cities")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<City>> getCities() {
        Log.info("Getting all cities");
        return City.listAll();
    }
}
```

### Dependencies and configuration

The `city-service/pom.xml` file includes the `quarkus-opentelemetry` and `opentelemetry-jdbc` extensions to instrument the application with OpenTelemetry:

```xml
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-opentelemetry</artifactId>
    </dependency>
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-jdbc</artifactId>
    </dependency>
```

It also includes dependencies for connecting to the PostgreSQL database with Hibernate Reactive Panache and Liquibase:

```xml
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-hibernate-reactive-panache</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-hibernate-orm</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
    <!-- Hibernate Reactive uses the reactive-pg-client with PostgreSQL under the hood -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-reactive-pg-client</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-liquibase</artifactId>
    </dependency>
```

The `city-service/src/main/resources/application.properties` file contains the configuration for the Liquibase and OpenTelemetry:

```properties
quarkus.liquibase.migrate-at-start=true

# OpenTelemetry configurations
quarkus.otel.exporter.otlp.traces.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT}
quarkus.datasource.jdbc.telemetry=true
```

For database connection configuration, we will create secrets and reference them in environment variables later.

## Build and deploy the application on Azure Container Apps

Similar to [Build and deploy Quarkus application on Azure Container Apps](../01-build-a-simple-java-application/README.md#build-and-deploy-quarkus-application-on-azure-container-apps), create a specific `city-service` application in your Azure Container Apps.

```bash
# Build and push city-service image to ACR
mvn clean package -DskipTests -Dnative -Dquarkus.native.container-build -f city-service/pom.xml

docker buildx build --platform linux/amd64 -f city-service/src/main/docker/Dockerfile.native -t city-service ./city-service
docker tag city-service ${ACR_LOGIN_SERVER}/city-service
docker login $ACR_LOGIN_SERVER \
    -u $ACR_USER_NAME \
    -p $ACR_PASSWORD
docker push ${ACR_LOGIN_SERVER}/city-service

# Deploy city-service to Azure Container Apps
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://${POSTGRESQL_SERVER_NAME}.postgres.database.azure.com:5432/${DB_NAME}?sslmode=require
export QUARKUS_DATASOURCE_REACTIVE_URL=postgresql://${POSTGRESQL_SERVER_NAME}.postgres.database.azure.com:5432/${DB_NAME}?sslmode=require
export QUARKUS_DATASOURCE_USERNAME=${DB_ADMIN}
export QUARKUS_DATASOURCE_PASSWORD=${DB_ADMIN_PWD}
az containerapp create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name city-service \
    --image ${ACR_LOGIN_SERVER}/city-service \
    --environment $ACA_ENV \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USER_NAME \
    --registry-password $ACR_PASSWORD \
    --target-port 8080 \
    --secrets \
        jdbcurl=${QUARKUS_DATASOURCE_JDBC_URL} \
        reactiveurl=${QUARKUS_DATASOURCE_REACTIVE_URL} \
        dbusername=${QUARKUS_DATASOURCE_USERNAME} \
        dbpassword=${QUARKUS_DATASOURCE_PASSWORD} \
    --env-vars \
        QUARKUS_DATASOURCE_JDBC_URL=secretref:jdbcurl \
        QUARKUS_DATASOURCE_REACTIVE_URL=secretref:reactiveurl \
        QUARKUS_DATASOURCE_USERNAME=secretref:dbusername \
        QUARKUS_DATASOURCE_PASSWORD=secretref:dbpassword \
    --ingress 'external' \
    --min-replicas 1
```

## Test the project in the cloud

Invoke `/cities` endpoint exposed by the Azure Container Apps `city-service` and test if it works as expected:

```bash
APP_URL=https://$(az containerapp show \
    --name city-service \
    --resource-group $RESOURCE_GROUP_NAME \
    --query properties.configuration.ingress.fqdn \
    -o tsv)

# You should see the list of cities returned: [{"id":1,"name":"Paris, France"},{"id":2,"name":"London, UK"}]
curl $APP_URL/cities --silent
```

Finally, let's update the `city-service` to make it accept internal traffic only instead of public accessible.

```bash
az containerapp ingress update \
    --name city-service \
    --resource-group $RESOURCE_GROUP_NAME \
    --type internal
```

---

⬅️ Previous guide: [01 - Build a simple Java application](../01-build-a-simple-java-application/README.md)

➡️ Next guide: [03 - Build a Micronaut microservice using MySQL](../03-build-a-micronaut-microservice-using-mysql/README.md)
