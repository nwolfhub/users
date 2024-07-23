package org.nwolfhub.users;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.nwolfhub.users.db.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuer;

    public User obtainUserInfoFromKeycloak(Jwt jwt) {
        OkHttpClient client = new OkHttpClient();

        try {
            Response response = client.newCall(new Request.Builder()
                    .url(issuer + "/protocol/openid-connect/userinfo")
                    .get()
                    .addHeader("Authorization", "Bearer " + jwt.getTokenValue())
                    .build()).execute();
            if(response.isSuccessful()) {
                User user = new User();
                JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();
                user.setUsername(object.get("preferred_username").getAsString());
                if(object.has("given_name")) user.setName(object.get("given_name").getAsString());
                if(object.has("family_name")) user.setFamilyName(object.get("family_name").getAsString());
                if(object.has("name")) user.setName(object.get("name").getAsString());
                if(object.has("email")) user.setEmail(object.get("email").getAsString());
                if(object.has("groups")) {
                    List<String> groups = new ArrayList<>();
                    object.get("groups").getAsJsonArray().forEach(group -> groups.add(group.getAsString()));
                    user.setGroups(groups);
                }
                user.setId(jwt.getSubject());
                return user;
            } else throw new IOException("Failed to contact keycloak: " + (response.body()!=null?response.body().string():response.code()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
