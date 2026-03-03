package com.biopatternsg.domain.models.user_registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserCredentials {
    String type;
    String value;
    boolean temporary;
}
