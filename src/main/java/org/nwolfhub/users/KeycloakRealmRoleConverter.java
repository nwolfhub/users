package org.nwolfhub.users;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        final Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        List<GrantedAuthority> listOfGranted = new ArrayList<>(((List<String>) realmAccess.get("roles")).stream()
                .map(roleName -> "ROLE_" + roleName) // prefix to map to a Spring Security "role"
                .map(SimpleGrantedAuthority::new)
                .toList());
        listOfGranted.addAll(((List<String>) jwt.getClaims().get("groups")).stream()
                .map(name -> "GROUP_" + name)
                .map(SimpleGrantedAuthority::new)
                .toList());
        return listOfGranted;
    }
}