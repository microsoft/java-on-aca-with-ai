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
