package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.NetworkConfig;

public interface NetworkRepository {

    NetworkConfig save(NetworkConfig networkConfig);
    NetworkConfig update(NetworkConfig networkConfig);
    NetworkConfig findById(String id);
    NetworkConfig findByName(String name);
}
