# Margo

Media ingest service. Validates simulated video assets and marks them APPROVED or REJECTED.

- `POST /assets` - ingest an asset
- `GET /assets`, `GET /assets/{id}` - read assets

Runs on port 8081: `./mvnw spring-boot:run`
