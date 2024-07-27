package org.nwolfhub.users.rate;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BucketProvider {
    private static final Logger log = LoggerFactory.getLogger(BucketProvider.class);
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    public Bucket getBucket(String bucketName) {
        return buckets.computeIfAbsent(bucketName, this::newBucket);
    }

    private Bucket newBucket(String name) {
        log.info("Creating bucket {}", name);
        if(buckets.size()>10000) {
            log.info("Issued bucket cleanup");
            List<String> forRemoval = new ArrayList<>();
            buckets.forEach((k, v) -> {
                if(v.getAvailableTokens()==resolveSection(k)) forRemoval.add(k);
            });
            log.info("{} buckets were marked for removal", forRemoval.size());
            forRemoval.forEach(buckets::remove);
        }
        int limit = resolveGroup(name).equalsIgnoreCase("admin")?9999:resolveSection(name);;
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(limit)
                        .refillIntervally(limit, Duration.of(1, ChronoUnit.MINUTES))
                        .build())
                .build();
    }
    private int resolveSection(String name) {
        String sectionName = name.split("SEC")[1].split("USR")[1];
        switch (sectionName) {
            case "users" -> {
                return 30;
            }
            case "user" -> {
                return 40;
            }
            case "auth" -> {
                return 5;
            }
        }
        return 0;
    }

    private String resolveGroup(String name) {
        return name.split("GRP")[1].split("SEC")[0];
    }
}
