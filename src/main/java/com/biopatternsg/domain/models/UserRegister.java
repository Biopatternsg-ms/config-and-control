package com.biopatternsg.domain.models;

import com.biopatternsg.domain.models.user_registration.UserCredentials;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRegister {
    String username;
    String email;
    String firstName;
    String lastName;
    boolean enabled;
    boolean emailVerified;
    List<String> realmRoles;
    List<UserCredentials> credentials;
    List<String> requiredActions;
}
