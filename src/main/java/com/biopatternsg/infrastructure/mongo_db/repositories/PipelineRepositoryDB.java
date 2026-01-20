package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class PipelineRepositoryDB implements PanacheMongoRepository<PipelineCollection> {

    public PipelineCollection findByIdAndUser(String id, List<String> networkIdList){

        return find("{'_id': :id, 'networkId': {'$in': :networkIdList}}",
                Parameters.with("id", new ObjectId(id)).and("networkIdList", networkIdList))
                .firstResult();
    }

    public PipelineCollection findByName(String name, List<String> networkIdList){

        return find("{'name': :name, 'networkId': {'$in': :networkIdList}}",
                Parameters.with("name", name).and("networkIdList", networkIdList))
                .firstResult();
    }

    public PipelineCollection findByNameIfExists(String id, String name, List<String> networkIdList){

        return find("{'_id': {'$ne': :id}, 'name': :name, 'networkId': {'$in': :networkIdList}}",
                Parameters.with("id", new ObjectId(id)).and("networkIdList", networkIdList).and("name", name))
                .firstResult();
    }

    public List<PipelineCollection> findByNetwork(String networkId){

        return find("{'networkId': :networkId",
                Parameters.with("networkId", networkId))
                .list();
    }

    public List<PipelineCollection> findByNetworkList(List<String> networkIdList){

        return find("{'networkId': {'$in': :networkIdList}",
                Parameters.with("networkId", networkIdList))
                .list();
    }
}
