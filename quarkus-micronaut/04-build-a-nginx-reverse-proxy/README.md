# 04 - Build a NGINX Reverse Proxy

__This guide is part of the [Build, Run and Monitor Intelligent Quarkus and Micronaut Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

Build a [NGINX Reverse Proxy](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/) to route HTTP requests to internal services running in the same Azure Container Apps environment. Furthermore, it utilizes native OpenTelemetry module to capture telemetry data and send to OpenTelemetry collector, see [NGINX Native OpenTelemetryModule](https://github.com/nginxinc/nginx-otel) for more information.

---

## Create a NGINX Reverse Proxy

The NGINX reverse proxy that we create in this guide is [gateway](gateway).

### NGINX configuration

The `gateway/nginx/nginx.conf.template` file contains the NGINX configuration for OpenTelemetry and reverse proxies that route requests to the city-service and weather-service:

```nginx
# Load the OpenTelemetry module
load_module modules/ngx_otel_module.so;

events {}

http {

    # Turn tracing on for http traffic
    otel_trace on;

    # If you are at the start of the request, this context
    # will be created by the library and consumed by downstream
    # services. Required if you want traces to be connected to each
    # other across services ("distributed tracing")
    otel_trace_context inject;

    # This is how the NGINX server will appear in your trace viewer
    otel_service_name "gateway";

    otel_exporter {
        endpoint ${OTEL_EXPORTER_OTLP_ENDPOINT};
    }

    server {
        # Server configuration
        listen ${PORT};
        # Allow CORS
        add_header 'Access-Control-Allow-Origin' '*';
        
        # Route requests /city-service to the city-service defined in the environment variable CITY_SERVICE_URL
        location /city-service {
            rewrite ^/city-service/(.*)$ /$1 break;

            proxy_pass ${CITY_SERVICE_URL};
            proxy_http_version 1.1;
        }

        # Route requests /weather-service to the weather-service defined in the environment variable WEATHER_SERVICE_URL
        location /weather-service {
            rewrite ^/weather-service/(.*)$ /$1 break;

            proxy_pass ${WEATHER_SERVICE_URL};
            proxy_http_version 1.1;
        }
    }
}
```

### NGINX entrypoint script

The `gateway/nginx/entrypoint.sh` script replaces environment variables in the NGINX configuration file at runtime and starts the NGINX server:

```bash
#!/usr/bin/env sh
set -eu

# Remove starting http:// from the OTEL_EXPORTER_OTLP_ENDPOINT, see https://github.com/grpc/grpc/issues/19954#issuecomment-676273319
export OTEL_EXPORTER_OTLP_ENDPOINT=$(echo $OTEL_EXPORTER_OTLP_ENDPOINT | sed 's/http:\/\///g')

# Replace the environment variables in the nginx.conf.template with the actual values
envsubst '${OTEL_EXPORTER_OTLP_ENDPOINT} ${PORT} ${CITY_SERVICE_URL} ${WEATHER_SERVICE_URL}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

exec "$@"
```

### NGINX Dockerfile

The `gateway/Dockerfile` Dockerfile uses `nginx:$NGINX_VERSION-otel` as the base image, which includes the OpenTelemetry module. The `nginx.conf.template` file is copied to the NGINX configuration directory, and the `entrypoint.sh` script is copied to the root directory.

```Dockerfile
ARG NGINX_VERSION=1.26
FROM nginx:$NGINX_VERSION-otel

COPY nginx/nginx.conf.template /etc/nginx/nginx.conf.template
COPY nginx/entrypoint.sh /

ENV OTEL_EXPORTER_OTLP_ENDPOINT=localhost:4317
ENV PORT=8080
ENV CITY_SERVICE_URL=http://city-service:8080
ENV WEATHER_SERVICE_URL=http://weather-service:8080
ENTRYPOINT ["/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
```

## Build and deploy the application on Azure Container Apps

Similar to [Build and deploy Java application on Azure Container Apps](../01-build-a-simple-java-application/README.md#build-and-deploy-java-application-on-azure-container-apps), create a specific `gateway` application in your Azure Container Apps.

```bash
# Build and push gateway image to ACR
cd ${BASE_DIR}/04-build-a-nginx-reverse-proxy

docker buildx build --platform linux/amd64 -f gateway/Dockerfile -t gateway ./gateway
docker tag gateway ${ACR_LOGIN_SERVER}/gateway
docker login $ACR_LOGIN_SERVER \
    -u $ACR_USER_NAME \
    -p $ACR_PASSWORD
docker push ${ACR_LOGIN_SERVER}/gateway

# Deploy gateway to Azure Container Apps
az containerapp create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name gateway \
    --image ${ACR_LOGIN_SERVER}/gateway \
    --environment $ACA_ENV \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USER_NAME \
    --registry-password $ACR_PASSWORD \
    --target-port 8080 \
    --env-vars \
        CITY_SERVICE_URL=http://city-service \
        WEATHER_SERVICE_URL=http://weather-service \
    --ingress 'external' \
    --min-replicas 1
cd ${BASE_DIR}
```

Alternatively, there is an existing Docker image stored in the GitHub Container Registry, you can deploy it to the Azure Container Apps directly:

```bash
# Deploy gateway with the existing image ghcr.io/microsoft/java-on-aca-with-ai-gateway to Azure Container Apps
az containerapp create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name gateway \
    --image ghcr.io/microsoft/java-on-aca-with-ai-gateway \
    --environment $ACA_ENV \
    --target-port 8080 \
    --env-vars \
        CITY_SERVICE_URL=http://city-service \
        WEATHER_SERVICE_URL=http://weather-service \
    --ingress 'external' \
    --min-replicas 1
cd ${BASE_DIR}
```

## Test the project in the cloud

Fetch the URL of the Azure Container Apps `gateway`:

```bash
APP_URL=https://$(az containerapp show \
    --name gateway \
    --resource-group $RESOURCE_GROUP_NAME \
    --query properties.configuration.ingress.fqdn \
    -o tsv)
```

Invoke `/city-service/cities` and `/weather-service/weather/city` endpoints and test if they work as expected:

```bash
# You should see the list of cities returned: [{"id":1,"name":"Paris, France"},{"id":2,"name":"London, UK"}]
curl -L "$APP_URL/city-service/cities" --silent

# You should see the weather for London, UK returned: {"city":"London, UK","description":"Quite cloudy","icon":"weather-pouring"}
curl -L "$APP_URL/weather-service/weather/city?name=London%2C%20UK" --silent

# You should see the weather for Paris, France returned: {"city":"Paris, France","description":"Very cloudy!","icon":"weather-fog"}
curl -L "$APP_URL/weather-service/weather/city?name=Paris%2C%20France" --silent
```

---

⬅️ Previous guide: [03 - Build a Micronaut microservice using MySQL](../03-build-a-micronaut-microservice-using-mysql/README.md)

➡️ Next guide: [05 - Putting it all together, a complete microservice stack](../05-putting-it-all-together-a-complete-microservice-stack/README.md)