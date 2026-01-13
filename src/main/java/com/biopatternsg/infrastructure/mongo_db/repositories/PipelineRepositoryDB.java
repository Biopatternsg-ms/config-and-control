package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class PipelineRepositoryDB implements PanacheMongoRepository<PipelineCollection> {

    public PipelineCollection findByIdAndUser(String id, Long userId){

        return find("{'_id': :id, 'userId': :userId}",
                Parameters.with("id", new ObjectId(id)).and("userId", userId))
                .firstResult();
    }

    public PipelineCollection findByNameAndUser(String name, Long userId){

        return find("{'name': :name, 'userId': :userId}",
                Parameters.with("name", name).and("userId", userId))
                .firstResult();
    }

    public PipelineCollection findByNameAndUserExists(String id, String name, Long userId){

        return find("{'_id': {'$ne': :id}, 'name': :name, 'userId': :userId}",
                Parameters.with("id", new ObjectId(id)).and("userId", userId).and("name", name))
                .firstResult();
    }

    public List<PipelineCollection> findByNetworkIdAndUser(String networkId, Long userId){

        return find("{'networkId': :networkId, 'userId': :userId}",
                Parameters.with("networkId", networkId).and("userId", userId))
                .list();
    }
}
