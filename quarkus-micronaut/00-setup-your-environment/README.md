# 00 - Setup your environment

__This guide is part of the [Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll set up everything you need to expeditiously complete the lab.

---

## Prerequisites

This training lab requires the following to be installed on your machine:

* [JDK 17](https://docs.microsoft.com/java/openjdk/download#openjdk-17)

  > The environment variable `JAVA_HOME` should be set to the path of the JDK installation. The directory specified by this path should have `bin`, `jre`, and `lib` among its subdirectories. Further, ensure your `PATH` variable contains the directory `${JAVA_HOME}/bin`. To test, type `which javac` into bash shell ensure the resulting path points to a file inside `${JAVA_HOME}/bin`.

* A text editor or an IDE. If you do not already have an IDE for Java development, we recommend using [Visual Studio Code](https://code.visualstudio.com) with the [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack). You can then use Visual Studio Code or an IDE of your choice.

* [Docker](https://docs.docker.com/get-docker/)

* 💡 In some sections, you will access the UI of the Microservice applications in a web browser. Use the [Microsoft Edge](https://microsoft.com/edge), Google Chrome, or Firefox for these sections.

* The Bash shell. While Azure CLI should behave identically on all environments, shell semantics vary. Therefore, only bash can be used with the commands in this training. To complete this training on Windows, use [Git Bash that accompanies the Windows distribution of Git](https://git-scm.com/download/win) or the [Windows Subsystem for Linux](https://learn.microsoft.com/windows/wsl/install).

* [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest) version 2.61.0 or later. You can check the version of your current Azure CLI installation by running:

  ```bash
  az --version
  ```

  Upgrade to the latest version if needed:

  ```bash
  az upgrade
  ```

  > 💡 If you try the command above and you see the error `bash: az: command not found`, run the following command: `alias az='az.cmd'` and try again.

* 🚧 Install or update the `Azure Application Insights` and `Azure Container Apps` extensions.

  ```bash
  az extension add -n application-insights --upgrade --allow-preview true
  az extension add --name containerapp --upgrade --allow-preview true
  ```

You need an Azure subscription to complete this lab. If you do not have an Azure subscription, create a [free account](https://azure.microsoft.com/free/ai-services/?azure-portal=true) before you begin.

You need the access granted to Azure OpenAI in your subscription. If you don't have access, please request access by following the instructions in [How do I get access to Azure OpenAI?](https://learn.microsoft.com/azure/ai-services/openai/overview#how-do-i-get-access-to-azure-openai).

Sign in to your Azure account by running:

```bash
az login
```

Set the default subscription to be used in this lab:

```bash
az account set --subscription "<Your Subscription ID>"
```

Register the `Microsoft.App` and `Microsoft.OperationalInsights` namespaces if they're not already registered in your Azure subscription.

```bash
az provider register --namespace Microsoft.App
az provider register --namespace Microsoft.OperationalInsights
``` 

## Creating Azure Resources

To save time, we provide bash commands for creating all the Azure resources you will need for this lab other than the Azure Container Apps itself.

### Define environment variables

Define the following environment variables in your bash shell, they will be used throughout the lab:

```bash
let "randomIdentifier=$RANDOM*$RANDOM"
LOCATION=eastus
RESOURCE_GROUP_NAME=aca-lab-rg-$randomIdentifier
POSTGRESQL_SERVER_NAME=postgres$randomIdentifier
MYSQL_SERVER_NAME=mysql$randomIdentifier
DB_NAME=demodb
DB_ADMIN=demouser
DB_ADMIN_PWD='super$ecr3t'$RANDOM$RANDO
REGISTRY_NAME=acr$randomIdentifier
ACA_ENV=acaenv$randomIdentifier
APP_INSIGHTS=appinsights$randomIdentifier
AZURE_OPENAI_NAME=azure-openai$randomIdentifier
AZURE_OPENAI_MODEL_NAME=gpt-35-turbo-16k
AZURE_OPENAI_MODEL_VERSION=0613
```

### Set up databases

Create a resource group and deploy an Azure Database for PostgreSQL Flexible Server and an Azure Database for MySQL Flexible Server in it.

```bash
az group create \
    --name $RESOURCE_GROUP_NAME \
    --location $LOCATION

az postgres flexible-server create \
    --name $POSTGRESQL_SERVER_NAME \
    --resource-group $RESOURCE_GROUP_NAME \
    --admin-user $DB_ADMIN \
    --admin-password $DB_ADMIN_PWD \
    --database-name $DB_NAME \
    --public-access 0.0.0.0 \
    --yes

az mysql flexible-server create \
    --name $MYSQL_SERVER_NAME \
    --resource-group $RESOURCE_GROUP_NAME \
    --admin-user $DB_ADMIN \
    --admin-password $DB_ADMIN_PWD \
    --database-name $DB_NAME \
    --public-access 0.0.0.0 \
    --yes
```

These databases will be used by the microservices later.

### Create an Azure Container Registry

Create an Azure Container Registry and get the connection details. 

```bash
az acr create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $REGISTRY_NAME \
    --sku Basic \
    --admin-enabled
ACR_LOGIN_SERVER=$(az acr show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $REGISTRY_NAME \
    --query 'loginServer' \
    --output tsv)
ACR_USER_NAME=$(az acr credential show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $REGISTRY_NAME \
    --query 'username' \
    --output tsv)
ACR_PASSWORD=$(az acr credential show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $REGISTRY_NAME \
    --query 'passwords[0].value' \
    --output tsv)
```

You will build application images and push them to this registry.

### Create an Azure Container Apps Environment

Create an Azure Container Apps environment.

```bash
az containerapp env create \
    --resource-group $RESOURCE_GROUP_NAME \
    --location $LOCATION \
    --name $ACA_ENV
```

The environment creates a secure boundary around a group of your container apps. You will deploy your microservices to this environment and they can able to communicate with each other.

### Collect and read OpenTelemetry data in Azure Container Apps 

OpenTelemetry agents live within your container app environment. You configure agent settings through the Azure CLI.

The managed OpenTelemetry agent accepts the following destinations:

* Azure Monitor Application Insights
* Datadog
* Any OTLP endpoint (For example: New Relic or Honeycomb)

In this lab, you use Azure Monitor Application Insights as the destination.

First, create an Azure Application Insights resource to receive OpenTelemetry data.

```bash
logAnalyticsWorkspace=$(az monitor log-analytics workspace list \
    -g $RESOURCE_GROUP_NAME \
    --query "[0].name" -o tsv)

az monitor app-insights component create \
    --app $APP_INSIGHTS \
    -g $RESOURCE_GROUP_NAME \
    -l $LOCATION \
    --workspace $logAnalyticsWorkspace
```

Next, enable OpenTelemetry for the Azure Container Apps environment and configure it to send data to the Azure Application Insights resource.

```bash
appInsightsConn=$(az monitor app-insights component show \
    --app $APP_INSIGHTS \
    -g $RESOURCE_GROUP_NAME \
    --query 'connectionString' -o tsv)

az containerapp env telemetry app-insights set \
  --name $ACA_ENV \
  --resource-group $RESOURCE_GROUP_NAME \
  --connection-string $appInsightsConn \
  --enable-open-telemetry-logs true \
  --enable-open-telemetry-traces true
```

### Create an Azure OpenAI service

Run the following command to create an Azure OpenAI resource:

```bash
az cognitiveservices account create \
    --name $AZURE_OPENAI_NAME \
    --resource-group $RESOURCE_GROUP_NAME \
    --location $LOCATION \
    --kind OpenAI \
    --custom-domain $AZURE_OPENAI_NAME \
    --sku s0
```

Set the default rule of the Azure OpenAI service to allow network access by default.

```bash
resourceId=$(az cognitiveservices account show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $AZURE_OPENAI_NAME \
    --query id --output tsv | tr -d '\r')

# Note: If you are using Git Bash, you may need to set environment variable MSYS_NO_PATHCONV=1 to avoid path conversion issue, see https://github.com/Azure/azure-cli/blob/main/doc/use_cli_with_git_bash.md#auto-translation-of-resource-ids for more details.
MSYS_NO_PATHCONV=1 az resource update \
    --ids ${resourceId} \
    --set properties.networkAcls="{'defaultAction':'Allow', 'ipRules':[],'virtualNetworkRules':[]}"
```

Retrieve the API key for the Azure OpenAI resource:

```bash
AZURE_OPENAI_KEY=$(az cognitiveservices account keys list \
    --name $AZURE_OPENAI_NAME \
    --resource-group $RESOURCE_GROUP_NAME \
    --query key1 \
    --output tsv)
```

Deploy a deployment of `gpt-35-turbo-16k` model in your Azure OpenAI resource:

```bash
az cognitiveservices account deployment create \
    --name $AZURE_OPENAI_NAME \
    --resource-group $RESOURCE_GROUP_NAME \
    --deployment-name $AZURE_OPENAI_MODEL_NAME \
    --model-name $AZURE_OPENAI_MODEL_NAME \
    --model-version $AZURE_OPENAI_MODEL_VERSION \
    --model-format OpenAI \
    --sku Standard \
    --capacity 10
```

---

➡️ Next guide: [01 - Build a simple Java application](../01-build-a-simple-java-application/README.md)
