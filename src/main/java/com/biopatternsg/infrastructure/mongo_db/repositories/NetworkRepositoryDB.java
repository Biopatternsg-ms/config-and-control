/*
 * Copyright © 2026 biopatternsg (biopatternsg@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.domain.models.NetworkConfig;
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

    public List<NetworkCollection> findByUserAndFilters(NetworkConfig findNetwork,
                                                        String userId, int page, int size){

        Document query = new Document();
        query.append("userId", userId);

        if(findNetwork == null){
            return find(query).list();
        }

        if (findNetwork.getId() != null && !findNetwork.getId().isEmpty()) {
            query.append("_id", new ObjectId(findNetwork.getId()));
        }

        if (findNetwork.getName() != null) {
            query.append("name", new Document("$regex", findNetwork.getName()).append("$options", "i"));
        }

        if (findNetwork.getDescription() != null) {
            query.append("description", new Document("$regex", findNetwork.getDescription()).append("$options", "i"));
        }

        if(page <= 0 || size <= 0)
            return find(query).list();

        return find(query).page(page, size).list();
    }
}
