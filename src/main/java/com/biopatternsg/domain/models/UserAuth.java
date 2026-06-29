package com.biopatternsg.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserAuth {
    String access_token;
    String refresh_token;
    String token_type;
    String id_token;
    long expires_in;
}
