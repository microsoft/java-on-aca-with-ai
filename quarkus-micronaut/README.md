# Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI

This series of guides focus on building Java applications using Quarkus and Micronaut frameworks and running them on Azure Container Apps. It also covers how to use Azure OpenAI to build AI applications.

## [00 - Prerequisites and Setup](00-setup-your-environment/README.md)

Prerequisites and environment setup.

## [01 - Build a simple Java application](01-build-a-simple-java-application/README.md)

Build the simplest possible Java application using the [Quarkus maven plugin](https://quarkus.io/guides/quarkus-maven-plugin) and [Micronaut Launch](https://launch.micronaut.io/).

## [02 - Build a reactive and native Quarkus microservice using PostgreSQL](02-build-a-reactive-and-native-quarkus-microservice-using-postgresql/README.md)

Build a reactive and native [Quarkus](https://quarkus.io/) microservice that uses [Quarkus Reactive](https://quarkus.io/guides/quarkus-reactive-architecture) and [Quarkus Native](https://quarkus.io/guides/building-native-image). The service is bound to an [Azure Database for PostgreSQL Flexible Server](https://learn.microsoft.com/azure/postgresql/flexible-server/overview), and it uses [Liquibase](https://quarkus.io/guides/liquibase) to manage database schema migrations including initial data population.

## [03 - Build a Micronaut microservice using MySQL](03-build-a-micronaut-microservice-using-mysql/README.md)

Build a [Micronaut](https://micronaut.io/) microserver that uses [Micronaut Data JDBC](https://guides.micronaut.io/latest/micronaut-data-jdbc-repository-maven-java.html). The service is bound to an [Azure Database For MySQL Flexible server](https://learn.microsoft.com/azure/mysql/flexible-server/overview), and it uses [Flyway](https://guides.micronaut.io/latest/micronaut-flyway-maven-java.html) to manage database schema migrations including initial data population.

## [04 - Build a NGINX Reverse Proxy](04-build-a-nginx-reverse-proxy/README.md)

Build a [NGINX Reverse Proxy](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/) to route HTTP requests to internal services.

## [05 - Putting it all together, a complete microservice stack](05-putting-it-all-together-a-complete-microservice-stack/README.md)

Use a front-end to access graphically our complete microservice stack.

## [06 - Build a Quarkus AI application using Azure OpenAI](06-build-a-quarkus-ai-application-using-azure-openai/README.md)

Build an AI application that uses [Azure OpenAI Service](https://learn.microsoft.com/en-us/azure/ai-services/openai/) in order to analyze and forecast weather based on historical data.
