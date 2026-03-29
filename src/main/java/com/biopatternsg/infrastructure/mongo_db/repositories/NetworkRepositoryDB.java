package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class NetworkRepositoryDB implements PanacheMongoRepository <NetworkCollection>{

    public NetworkCollection findByIdAndIdUser(String id, String userId){

        return find("{'_id': :id, 'userId': :userId}",
                Parameters.with("id", new ObjectId(id)).and("userId", userId))
                .firstResult();
    }

    public NetworkCollection findByUserAndName(String name, String userId){

        return find("{'name': :name, 'userId': :userId}",
                Parameters.with("name", name).and("userId", userId))
                .firstResult();
    }

    public NetworkCollection findByUserAndNameExists(String id, String name, String userId){

        return find("{'_id': {'$ne': :id}, 'name': :name, 'userId': :userId}",
                Parameters.with("id", new ObjectId(id)).and("userId", userId).and("name", name))
                .firstResult();
    }

    public List<NetworkCollection> findByUserAndFilters(FindNetworkRequest findNetwork, String userId){

        Document query = new Document();
        query.append("userId", userId);

        if(findNetwork == null){
            return find(query).list();
        }

        if (findNetwork.id() != null && !findNetwork.id().isEmpty()) {
            query.append("_id", new ObjectId(findNetwork.id()));
        }

        if (findNetwork.name() != null) {
            query.append("name", new Document("$regex", findNetwork.name()).append("$options", "i"));
        }

        if (findNetwork.description() != null) {
            query.append("description", new Document("$regex", findNetwork.description()).append("$options", "i"));
        }

        return find(query)
                .page(findNetwork.page(), findNetwork.size())
                .list();
    }
}
