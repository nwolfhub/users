package org.nwolfhub.users.api;

import org.nwolfhub.users.JsonBuilder;
import org.nwolfhub.users.UserService;
import org.nwolfhub.users.db.model.User;
import org.nwolfhub.users.db.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class Auth {
    private final UserRepository userRepository;
    private final UserService userService;

    public Auth(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }
    private final Logger logger = LoggerFactory.getLogger(Auth.class);

    @GetMapping("")
    public ResponseEntity<String> postLogin(@AuthenticationPrincipal Jwt jwt) {
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
}
