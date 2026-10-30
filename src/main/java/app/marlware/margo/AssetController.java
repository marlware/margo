package app.marlware.margo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.json.JsonMapper;

@RestController
public class AssetController {

    private static final String TOPIC = "asset-updates";

    public record AssetRequest(String id, String title, Integer durationSeconds, String hlsPath) {}

    public record Asset(String id, String title, Integer durationSeconds, String hlsPath,
                        String status, List<String> errors) {}

    private final Map<String, Asset> assets = new ConcurrentHashMap<>();
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json = JsonMapper.builder().build();

    public AssetController(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
        // Seed one valid and one invalid asset so the flow works out of the box.
        store(validate(new AssetRequest("demo", "Demo video", 30, "/hls/demo/index.m3u8")));
        store(validate(new AssetRequest("broken", "", 0, "/hls/broken/video.mp4")));
    }

    // Seeded assets exist before Kafka is usable, so publish them once the app is up.
    @EventListener(ApplicationReadyEvent.class)
    public void publishSeeds() {
        assets.values().forEach(this::publish);
    }

    @PostMapping("/assets")
    public Asset ingest(@RequestBody AssetRequest req) {
        Asset asset = validate(req);
        if (store(asset)) publish(asset);
        return asset;
    }

    @GetMapping("/assets")
    public Collection<Asset> list() {
        return assets.values();
    }

    @GetMapping("/assets/{id}")
    public Asset get(@PathVariable String id) {
        Asset a = assets.get(id);
        if (a == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return a;
    }

    private Asset validate(AssetRequest req) {
        List<String> errors = new ArrayList<>();
        if (req.id() == null || req.id().isBlank()) errors.add("id is required");
        if (req.title() == null || req.title().isBlank()) errors.add("title is required");
        if (req.durationSeconds() == null || req.durationSeconds() <= 0) errors.add("durationSeconds must be > 0");
        if (req.hlsPath() == null || !req.hlsPath().endsWith(".m3u8")) errors.add("hlsPath must end in .m3u8");

        String status = errors.isEmpty() ? "APPROVED" : "REJECTED";
        return new Asset(req.id(), req.title(), req.durationSeconds(), req.hlsPath(), status, errors);
    }

    private boolean store(Asset asset) {
        if (asset.id() == null || asset.id().isBlank()) return false;
        assets.put(asset.id(), asset);
        return true;
    }

    private void publish(Asset asset) {
        kafka.send(TOPIC, asset.id(), json.writeValueAsString(asset));
    }
}
