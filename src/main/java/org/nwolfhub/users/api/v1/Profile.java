package org.nwolfhub.users.api.v1;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.nwolfhub.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/profile")
public class Profile {
    private final UserService userService;
    private final Set<Jwt> updateRequests = new HashSet<>();

    public Profile(UserService userService) {
        this.userService = userService;
    }

    public ResponseEntity<String> fetchProfile(@AuthenticationPrincipal Jwt jwt) {

    }
}
