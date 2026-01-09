package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import com.biopatternsg.infrastructure.mongo_db.mappers.NetworkMapper;
import com.biopatternsg.infrastructure.mongo_db.repositories.NetworkRepositoryDB;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class NetworkRepositoryAdapter implements NetworkRepository {

    @Inject
    SessionUtil sessionUtil;
    private NetworkRepositoryDB networkRepositoryDB;

    @Override
    public NetworkConfig save(NetworkConfig networkConfig) {

        var getNetworkConfig = findByName(networkConfig.getName());
        if(getNetworkConfig != null){
            return getNetworkConfig;
        }

        var networkObject = saveObject(networkConfig);
        return NetworkMapper.toNetworkConfig(networkObject);
    }

    @Override
    public NetworkConfig update(NetworkConfig networkConfig) {

        var networkObject = networkRepositoryDB.findByIdAndIdUser(networkConfig.getId(), sessionUtil.getUserId());
        if(networkObject == null){
            return null;
        }

        networkObject.setName(networkConfig.getName());
        networkObject.setDescription(networkConfig.getDescription());
        networkObject.update();

        return NetworkMapper.toNetworkConfig(networkObject);
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
