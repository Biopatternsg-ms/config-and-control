package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import com.biopatternsg.infrastructure.adaptars.mappers.NetworkMapper;
import com.biopatternsg.infrastructure.mongo_db.repositories.NetworkRepositoryDB;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

        var networkObject = networkRepositoryDB.findByIdAndIdUser(id, sessionUtil.getUserId());
        if(networkObject != null){
            return NetworkMapper.toNetworkConfig(networkObject);
        }

        return null;
    }

    @Override
    public NetworkConfig findByName(String name) {

        var networkObject = networkRepositoryDB.findByUserAndName(name, sessionUtil.getUserId());
        if(networkObject != null){
            return NetworkMapper.toNetworkConfig(networkObject);
        }

        return null;
    }

    private NetworkCollection saveObject(NetworkConfig networkConfig){

        NetworkCollection object = new NetworkCollection();

        object.setName(networkConfig.getName());
        object.setDescription(networkConfig.getDescription());
        object.setUserId(sessionUtil.getUserId());

        object.persist();

        return object;
    }
}
