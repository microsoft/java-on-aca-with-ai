# 03 - Build a Micronaut microservice using MySQL

__This guide is part of the [Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll build a [Micronaut](https://micronaut.io/) microserver that references guide [ACCESS A DATABASE WITH MICRONAUT DATA JDBC](https://guides.micronaut.io/latest/micronaut-data-jdbc-repository-maven-java.html). The service is bound to an [Azure Database For MySQL Flexible server](https://learn.microsoft.com/azure/mysql/flexible-server/overview), and it uses [Flyway](https://guides.micronaut.io/latest/micronaut-flyway-maven-java.html) to manage database schema migrations including initial data population. Furthermore, it utilizes OpenTelemetry java agent to automatically capture telemetry data and send to OpenTelemetry collector, see [this comment](https://github.com/micronaut-projects/micronaut-tracing/issues/388#issuecomment-1810802923) for more information.

---


---

⬅️ Previous guide: [02 - Build a reactive and native Quarkus microservice using PostgreSQL](../02-build-a-reactive-and-native-quarkus-microservice-using-postgresql/README.md)

➡️ Next guide: [04 - Build a NGINX Reverse Proxy](../04-build-a-nginx-reverse-proxy/README.md)
