package org.nwolfhub.users.api.v1;

import io.github.bucket4j.Bucket;
import org.nwolfhub.users.JsonBuilder;
import org.nwolfhub.users.UserService;
import org.nwolfhub.users.db.model.User;
import org.nwolfhub.users.db.repository.UserRepository;
import org.nwolfhub.users.rate.BucketProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class Auth {
    private final UserRepository userRepository;
    private final UserService userService;
    private final BucketProvider bucketProvider;

    public Auth(UserRepository userRepository, UserService userService, BucketProvider bucketProvider) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.bucketProvider = bucketProvider;
    }
    private final Logger logger = LoggerFactory.getLogger(Auth.class);

    @GetMapping("/postLogin")
    public ResponseEntity<String> postLogin(@AuthenticationPrincipal Jwt jwt) {
        Bucket bucket = bucketProvider.getBucket(jwt, "auth");
        if(bucket.tryConsume(1)) {
            Optional<User> optionalUser = userRepository.findById(jwt.getSubject());
            if (optionalUser.isPresent()) {
                return ResponseEntity.ok(JsonBuilder.ok);
            }
            logger.info("New user is found. Completing postLogin");
            new Thread(() -> {
                User user = userService.obtainUserInfoFromKeycloak(jwt);
                userRepository.save(user);
            }).start();
            return ResponseEntity.accepted().body(JsonBuilder.ok);
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(JsonBuilder.tooManyRequests);
    }
}
