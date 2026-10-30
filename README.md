# Margo

The front desk for videos

## What's going on here

Margo is one of three small projects that together pretend to be a video streaming service, kind of like a baby version of what sits behind Netflix or YouTube. Everything is simulated, so the "videos" and "ads" are little test patterns, but the way the pieces talk to each other is the real deal.

- **Margo** (this one) checks new videos before anyone can watch them
- **[Agnes](https://github.com/marlware/agnes)** is the part viewers actually talk to, since it streams the videos and shows ads
- **[Edith](https://github.com/marlware/edith)** picks which ad to show and counts how many times each one was seen

## So what does Margo do

Think of Margo as a bouncer for the video library. When someone adds a video, Margo looks over its details to see if it has a name, a real length, and a proper playlist file. If everything checks out, the video gets stamped APPROVED, and if not, it is REJECTED along with a list of what was wrong.

Margo then announces the result on a message board (Kafka) so that Agnes hears about it right away. That way Agnes only ever streams videos that passed the check.

Two videos come pre-loaded so there is something to look at from the start: a good one called `demo` and a broken one called `broken`.

## The dashboard

The dashboard is a small web page that shows every video, whether it passed, and why any of them failed. It refreshes itself every few seconds, so you can watch new videos show up while you run the stress test below.

## Trying it out

You will need Java, Node, and Docker. Here is the order to do things in:

- Start the shared stuff (the message board, Redis, and a Flink cluster that Edith uses) with `docker compose up -d`
- Start Margo with `./mvnw spring-boot:run`, which listens on port 8081
- Start the dashboard with `cd dashboard && npm install && npm run dev`, then open http://localhost:5173
- Add a video of your own, and leave out the title to watch it get rejected on the dashboard:

```
curl -X POST localhost:8081/assets -H "content-type: application/json" \
  -d '{"id":"mine","title":"My Video","durationSeconds":20,"hlsPath":"/hls/mine/index.m3u8"}'
```

- To run the whole pretend service, also start Edith and then Agnes, since their READMEs have the steps

## Stress test

If you want to see Margo handle a crowd, run `node scripts/load-assets.mjs`. It sends 10,000 videos through Margo, and every tenth one is deliberately broken. You can follow along on the dashboard or check that Agnes heard about all of them.

## Good to know

- Margo keeps its videos in memory, so restarting it forgets anything you added (the two starter videos come back)
- The compose file also runs Redis and a Flink cluster for Edith's sake, and the Flink dashboard is at http://localhost:8083
- Kafka listens on two addresses, `localhost:9092` for programs on your computer and `kafka:19092` for the containers
- The endpoints, if you want them, are `POST /assets`, `GET /assets`, and `GET /assets/{id}`
