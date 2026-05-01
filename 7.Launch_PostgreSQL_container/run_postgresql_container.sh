podman run --name db-container \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=db \
  -p 5432:5432 \
  -d postgres:15-alpine