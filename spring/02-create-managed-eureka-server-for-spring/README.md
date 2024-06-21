# 02 - Create Managed Eureka Server for Spring

__This guide is part of the [Build, Run and Monitor Intelligent Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

A key feature of cloud-native application is *service discovery* - the ability to provide a common place to find and identify individual services. In this section, we'll create a [Spring Cloud Eureka Server](https://spring.io/projects/spring-cloud-netflix) to enable this functionality.

---

## Create a Managed Eureka Server

Define the environment variables. Please note that those environment variables defined in [01 - Build a simple Java application](../01-build-a-simple-java-application/README.md) will also be used.

```bash
EUREKA_SERVER_NAME="eurekaserver01"
```

Create the Managed Eureka Server.

```bash
az containerapp env java-component spring-cloud-eureka create \
  --environment $ENVIRONMENT \
  --resource-group $RESOURCE_GROUP \
  --name $EUREKA_SERVER_NAME
```

## Review

We have enabled Azure Container Apps to create a Managed Eureka Server for Spring.

In the next section, we will create a Managed Config Server for Spring, to enable to ability of *externalized configuration*.

---

⬅️ Previous guide: [01 - Build a simple Java application](../01-build-a-simple-java-application/README.md)

➡️ Next guide: [03 - Create and configure Managed Config Server for Spring](../03-create-and-configure-managed-config-server-for-spring/README.md)
