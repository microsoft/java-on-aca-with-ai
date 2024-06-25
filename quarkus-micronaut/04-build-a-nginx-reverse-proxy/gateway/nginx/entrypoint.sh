#!/usr/bin/env sh
set -eu

# Remove starting http:// from the OTEL_EXPORTER_OTLP_ENDPOINT, see https://github.com/grpc/grpc/issues/19954#issuecomment-676273319
export OTEL_EXPORTER_OTLP_ENDPOINT=$(echo $OTEL_EXPORTER_OTLP_ENDPOINT | sed 's/http:\/\///g')

# Replace the environment variables in the nginx.conf.template with the actual values
envsubst '${OTEL_EXPORTER_OTLP_ENDPOINT} ${PORT} ${CITY_SERVICE_URL} ${WEATHER_SERVICE_URL}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

exec "$@"
