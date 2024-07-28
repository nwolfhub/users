package org.nwolfhub.users.rate;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final Map<CombinedUserName, Bucket> buckets = new ConcurrentHashMap<>();
    public Bucket getBucket(Jwt jwt, String section) {
        return getBucket(jwt.getSubject(), jwt.getClaims().containsKey("GROUP_admin")?"admin":"user", section);
    }
    public Bucket getBucket(String name, String group, String section) {
        return buckets.computeIfAbsent(new CombinedUserName(name, group, section), this::newBucket);
    }

    private Bucket newBucket(CombinedUserName userName) {
        log.info("Creating bucket {}", userName.getName());
        if(buckets.size()>10000) {
            log.info("Issued bucket cleanup");
            List<CombinedUserName> forRemoval = new ArrayList<>();
            buckets.forEach((k, v) -> {
                if(v.getAvailableTokens()==resolveSection(userName.getSection())) forRemoval.add(k);
            });
            log.info("{} buckets were marked for removal", forRemoval.size());
            forRemoval.forEach(buckets::remove);
        }
        int limit = userName.getGroup().equalsIgnoreCase("admin")?9999:resolveSection(userName.getGroup());;
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(limit)
                        .refillIntervally(limit, Duration.of(1, ChronoUnit.MINUTES))
                        .build())
                .build();
    }
    private int resolveSection(String section) {
        switch (section) {
            case "users" -> {
                return 30;
            }
            case "user" -> {
                return 70;
            }
            case "auth" -> {
                return 5;
            }
        }
        return 0;
    }

    private class CombinedUserName {
        public String name;
        public String group;
        public String section;

        public CombinedUserName() {}

        public CombinedUserName(String name, String group, String section) {
            this.name = name;
            this.group = group;
            this.section = section;
        }

        public String getName() {
            return name;
        }

        public CombinedUserName setName(String name) {
            this.name = name;
            return this;
        }

        public String getGroup() {
            return group;
        }

        public CombinedUserName setGroup(String group) {
            this.group = group;
            return this;
        }

        public String getSection() {
            return section;
        }

        public CombinedUserName setSection(String section) {
            this.section = section;
            return this;
        }
    }
}
