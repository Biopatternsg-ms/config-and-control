package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.NetworkConfig;

public interface NetworkRepository {

    NetworkConfig save(NetworkConfig networkConfig);
    NetworkConfig findById(String id);
    NetworkConfig findByName(String name);
    NetworkConfig findByNameExists(String id, String name);
}
