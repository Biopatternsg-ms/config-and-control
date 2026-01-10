package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

@ApplicationScoped
public class NetworkRepositoryDB implements PanacheMongoRepository <NetworkCollection>{

    public NetworkCollection findByIdAndIdUser(String id, Long userId){

        return find("{'_id': :id, 'userId': :userId}",
                Parameters.with("id", new ObjectId(id))
                        .and("userId", userId))
                .firstResult();
    }

    public NetworkCollection findByUserAndName(String name, Long userId){

        return find("{'name': :name, 'userId': :userId}", Parameters.with("name", name).and("userId", userId))
                .firstResult();
    }
}
