#!/bin/sh

ROOT_DIR=/usr/share/nginx/html

# Replace env vars in JavaScript files
echo "Replacing env constants in JS"
for file in $ROOT_DIR/js/app.*.js* $ROOT_DIR/index.html $ROOT_DIR/precache-manifest*.js;
do
  echo "Processing $file ...";
  sed -i 's|CONTAINER_APP_ENV_DNS_SUFFIX|'${CONTAINER_APP_ENV_DNS_SUFFIX}'|g' $file
done

echo "Starting Nginx"
nginx -g 'daemon off;'