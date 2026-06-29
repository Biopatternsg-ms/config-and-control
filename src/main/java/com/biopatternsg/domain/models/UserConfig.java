package com.biopatternsg.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserConfig {

    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String enabled;
    private String emailVerified;
    private int createdAt;
}
