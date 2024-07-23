package org.nwolfhub.users.rate;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BucketProvider {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
}
