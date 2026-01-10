package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class PipelineRepositoryDB implements PanacheMongoRepository<PipelineCollection> {

    public PipelineCollection findByIdAndUser(String id, Long userId){

        return find("{'_id': ?1, 'userId': ?2}", new ObjectId(id), userId).firstResult();
    }

    public PipelineCollection findByNameAndUser(String name, Long userId){

        return find("{'name': ?1, 'userId': ?2}", name, userId).firstResult();
    }

    public List<PipelineCollection> findByNetworkIdAndUser(String networkId, Long userId){

        return find("{'networkId': ?1, 'userId': ?2}", networkId, userId).list();
    }
}
