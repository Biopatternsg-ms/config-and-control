package com.biopatternsg.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class NetworkConfig {

    private String id;
    private Long userId;
    private String name;
    private String description;
}
