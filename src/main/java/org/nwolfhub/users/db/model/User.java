package org.nwolfhub.users.db.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
public class User {
    @Id
    public String id;
    @Column(nullable = false)
    public String username;
    @Column(nullable = false)
    public String email;
    public String givenName;
    public String familyName;
    @Column(length = 512)
    public String name;
    @Convert(converter = StringToListConverter.class)
    public List<String> groups;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public User setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public User setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getGroups() {
        return groups;
    }

    public User setGroups(List<String> groups) {
        this.groups = groups;
        return this;
    }

    @Converter
    public class StringToListConverter implements AttributeConverter<List<String>, String> {

        @Override
        public String convertToDatabaseColumn(List<String> list) {
            if(list == null) return "";
            return String.join(",", list);
        }

        @Override
        public List<String> convertToEntityAttribute(String joined) {
            if(joined == null) return new ArrayList<>();
            return new ArrayList<>(Arrays.asList(joined.split(",")));
        }
    }
}
