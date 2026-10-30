// Ingests N assets into Margo (each is validated and published to Kafka).
// Usage: node scripts/load-assets.mjs [count] [margoUrl]
// Every 10th asset is invalid so the dashboard has failures to show.
const count = Number(process.argv[2] ?? 10000);
const base = process.argv[3] ?? "http://localhost:8081";
const concurrency = 50;

let next = 0;
let approved = 0;
let rejected = 0;
let failed = 0;

async function worker() {
  while (next < count) {
    const i = next++;
    const body = {
      id: `load-${i}`,
      title: i % 10 === 0 ? "" : `Load Asset ${i}`,
      durationSeconds: 30,
      hlsPath: `/hls/load-${i}/index.m3u8`,
    };
    try {
      const res = await fetch(`${base}/assets`, {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify(body),
      });
      const asset = await res.json();
      if (asset.status === "APPROVED") approved++;
      else rejected++;
    } catch {
      failed++;
    }
  }
}

const start = Date.now();
await Promise.all(Array.from({ length: concurrency }, worker));
const secs = (Date.now() - start) / 1000;
console.log(`sent ${count} in ${secs.toFixed(1)}s (${Math.round(count / secs)}/s)`);
console.log(`approved=${approved} rejected=${rejected} failed=${failed}`);
