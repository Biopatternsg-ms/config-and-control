package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.infrastructure.dtos.FindPipelineRequest;
import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class PipelineRepositoryDB implements PanacheMongoRepository<PipelineCollection> {

    public PipelineCollection findByIdAndUser(String id, List<String> networkIdList){

        return find("{'_id': :id, 'networkId': {'$in': :networkList}}",
                Parameters.with("id", new ObjectId(id)).and("networkList", networkIdList))
                .firstResult();
    }

    public PipelineCollection findByName(String name, List<String> networkIdList){

        return find("{'name': :name, 'networkId': {'$in': :networkIdList}}",
                Parameters.with("name", name).and("networkIdList", networkIdList))
                .firstResult();
    }

    public PipelineCollection findByNameIfExists(String id, String name, List<String> networkIdList){

        return find("{'_id': {'$ne': :id}, 'name': :name, 'networkId': {'$in': :networkList}}",
                Parameters.with("id", new ObjectId(id)).and("networkList", networkIdList).and("name", name))
                .firstResult();
    }

    public List<PipelineCollection> findByUserAndFilters(FindPipelineRequest pipelineRequest, List<String> networkIdList){

        Document query = new Document();
        query.append("networkId", new Document("$in", networkIdList));

        if(pipelineRequest == null){
            return find(query).list();
        }

        if (pipelineRequest.id() != null && !pipelineRequest.id().isEmpty()) {
            query.append("_id", new ObjectId(pipelineRequest.id()));
        }

        if (pipelineRequest.networkId() != null && !pipelineRequest.networkId().isEmpty()) {
            query.append("networkId", pipelineRequest.networkId());
        }

        if (pipelineRequest.name() != null) {
            query.append("name", new Document("$regex", pipelineRequest.name()).append("$options", "i"));
        }

        if (pipelineRequest.description() != null) {
            query.append("description", new Document("$regex", pipelineRequest.description()).append("$options", "i"));
        }

        return find(query)
                .page(pipelineRequest.page(), pipelineRequest.size())
                .list();
    }
}
