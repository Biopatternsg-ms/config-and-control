package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.NetworkConfig;

import java.util.List;

public interface NetworkRepository {

    NetworkConfig save(NetworkConfig networkConfig);
    NetworkConfig findById(String id);
    NetworkConfig findByName(String name);
    NetworkConfig findByNameExists(String id, String name);
    List<NetworkConfig> findByIdUser();
}
