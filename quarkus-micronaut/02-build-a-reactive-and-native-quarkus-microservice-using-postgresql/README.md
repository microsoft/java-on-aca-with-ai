# 02 - Build a reactive and native Quarkus microservice using PostgreSQL

__This guide is part of the [Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll build a reactive and native [Quarkus](https://quarkus.io/) microservice that references guides [GETTING STARTED WITH REACTIVE](https://quarkus.io/guides/getting-started-reactive) and [SIMPLIFIED HIBERNATE REACTIVE WITH PANACHE](https://quarkus.io/guides/hibernate-reactive-panache). The service is bound to an [Azure Database for PostgreSQL Flexible Server](https://learn.microsoft.com/azure/postgresql/flexible-server/overview), and it uses [Liquibase](https://quarkus.io/guides/liquibase) to manage database schema migrations including initial data population. Furthermore, it utilizes OpenTelemetry to instrument the application and send distributed tracing to OpenTelemetry collector, see [USING OPENTELEMETRY](https://quarkus.io/guides/opentelemetry) for more information.

---


---

⬅️ Previous guide: [01 - Build a simple Java application](../01-build-a-simple-java-application/README.md)

➡️ Next guide: [03 - Build a Micronaut microservice using MySQL](../03-build-a-micronaut-microservice-using-mysql/README.md)
