# Margo

Media ingest service. Validates simulated video assets and marks them APPROVED or REJECTED.

- `POST /assets` - ingest an asset
- `GET /assets`, `GET /assets/{id}` - read assets

Runs on port 8081: `./mvnw spring-boot:run`

## Kafka

Every ingested asset is published to the `asset-updates` topic (Agnes consumes it).
Start Kafka first: `docker compose up -d`

## Redis

Redis is in the same compose file (used by Edith for targeting data).

## Dashboard

React + TypeScript view of asset status and validation failures.

- `cd dashboard && npm install && npm run dev` - http://localhost:5173
- Proxies `/assets` to Margo on :8081
