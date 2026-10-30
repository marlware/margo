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

## Load test

`node scripts/load-assets.mjs [count]` - ingests 10,000 assets by default (every 10th is invalid). Needs Margo and Kafka running.

## Flink

The compose file also runs a Flink 2.3 cluster (UI on :8083). Kafka has two listeners: `localhost:9092` for apps on this machine and `kafka:19092` for containers. The job itself lives in `edith/flink`.
