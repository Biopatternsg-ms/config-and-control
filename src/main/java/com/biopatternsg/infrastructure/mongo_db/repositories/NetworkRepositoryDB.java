package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

public class NetworkRepositoryDB implements PanacheMongoRepository <NetworkCollection>{

    public NetworkCollection findByIdAndIdUser(String id, Long userId){

        return find("{'_id': ?1, 'userId': ?2}", id, userId).firstResult();
    }

    public NetworkCollection findByUserAndName(String name, Long userId){

        return find("{'name': ?1, 'userId': ?2}", name, userId).firstResult();
    }
}
