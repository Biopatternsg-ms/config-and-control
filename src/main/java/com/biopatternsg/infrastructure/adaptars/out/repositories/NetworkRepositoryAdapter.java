package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.infrastructure.adaptars.mappers.NetworkMapper;
import com.biopatternsg.infrastructure.mongo_db.repositories.NetworkRepositoryDB;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class NetworkRepositoryAdapter implements NetworkRepository {

    @Inject
    private SessionUtil sessionUtil;
    @Inject
    private NetworkRepositoryDB networkRepositoryDB;

    @Override
    public NetworkConfig save(NetworkConfig networkConfig) {

        var networkCollection = NetworkMapper.toNetworkCollection(networkConfig, sessionUtil.getUserId());
        networkCollection.persistOrUpdate();
        return NetworkMapper.toNetworkConfig(networkCollection);
    }

    @Override
    public NetworkConfig findById(String id) {

        var networkCollection = networkRepositoryDB.findByIdAndIdUser(id, sessionUtil.getUserId());
        return NetworkMapper.toNetworkConfig(networkCollection);
    }

    @Override
    public NetworkConfig findByName(String name) {

        var networkCollection = networkRepositoryDB.findByUserAndName(name, sessionUtil.getUserId());
        return NetworkMapper.toNetworkConfig(networkCollection);
    }

    @Override
    public NetworkConfig findByNameExists(String id, String name) {

        var networkCollection = networkRepositoryDB.findByUserAndNameExists(id, name, sessionUtil.getUserId());
        return NetworkMapper.toNetworkConfig(networkCollection);
    }

    @Override
    public List<NetworkConfig> findByIdUser() {

        var networkCollectionList = networkRepositoryDB.findByUser(sessionUtil.getUserId());
        return NetworkMapper.toNetworkConfigList(networkCollectionList);
    }
}
