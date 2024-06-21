# Build, Run and Monitor Intelligent Java Apps on Azure Container Apps and Azure OpenAI

## What you should expect

This is not the official documentation but an opinionated workshop.

It is a hands-on training, and it will use the command line extensively. The idea is to get coding very quickly and play with the platform, from a simple demo to far more complex examples.

After completing all the guides, you should have a fairly good understanding of everything that Java on Azure Container Apps offers.

## [00 - Prerequisites and Setup](00-setup-your-environment/README.md)

Prerequisites and environment setup.

## [01 - Build a simple Java application](01-build-a-simple-java-application/README.md)

Build the simplest possible Java application using the Spring Initializr.

## [02 - Create Managed Eureka Server for Spring](02-create-managed-eureka-server-for-spring/README.md)

Create a [Spring Cloud Eureka Server](https://spring.io/projects/spring-cloud-netflix), that will be entirely managed and supported by Azure Container Apps, to be used by Spring Boot microservices.

## [03 - Create and configure Managed Config Server for Spring](03-create-and-configure-managed-config-server-for-spring/README.md)

Create a managed [Spring Cloud Config Server](https://cloud.spring.io/spring-cloud-config) and configure it to access Git repository.

## [04 - Build a Spring Boot microservice using Spring Cloud features](04-build-a-spring-boot-microservice-using-spring-cloud-features/README.md)

Build a Spring Boot microservice that is cloud-enabled: it uses a [Spring Cloud Eureka Server](https://spring.io/projects/spring-cloud-netflix) and a [Spring Cloud Config Server](https://cloud.spring.io/spring-cloud-config) which are both managed and supported by Azure Container Apps.

## [05 - Build a reactive Spring Boot microservice using Cosmos DB](05-build-a-reactive-spring-boot-microservice-using-cosmosdb/README.md)

Build a reactive Spring Boot microservice, that uses the [Spring reactive stack](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html) and is bound to a [Cosmos DB database](https://docs.microsoft.com/en-us/azure/cosmos-db/) in order to access a globally-distributed database with optimum performance.

## [06 - Build a Spring Boot microservice using MySQL](06-build-a-spring-boot-microservice-using-mysql/README.md)

Build a classical Spring Boot application that uses JPA to access a [MySQL database managed by Azure](https://docs.microsoft.com/en-us/azure/mysql/).

## [07 - Build a Spring Cloud Gateway](07-build-a-spring-cloud-gateway/README.md)

Build a [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) to route HTTP requests to the correct Spring Boot microservices.

## [08 - Putting it all together, a complete microservice stack](08-putting-it-all-together-a-complete-microservice-stack/README.md)

Use a front-end to access graphically our complete microservice stack.

## [09 - Build a Spring AI application using Azure OpenAI](09-build-a-spring-ai-application-using-azure-openai/README.md)

Build an AI application that uses [Azure OpenAI Service](https://learn.microsoft.com/en-us/azure/ai-services/openai/) in order to analyze and forecast weather based on historical data.

---

## Trademarks

This project may contain trademarks or logos for projects, products, or services. Authorized use of Microsoft 
trademarks or logos is subject to and must follow 
[Microsoft's Trademark & Brand Guidelines](https://www.microsoft.com/en-us/legal/intellectualproperty/trademarks/usage/general).
Use of Microsoft trademarks or logos in modified versions of this project must not cause confusion or imply Microsoft sponsorship.
Any use of third-party trademarks or logos are subject to those third-party's policies.
