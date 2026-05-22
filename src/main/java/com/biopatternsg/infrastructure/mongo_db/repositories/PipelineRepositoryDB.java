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

        return find("{'name': :name, 'networkId': {'$in': :networkList}}",
                Parameters.with("name", name).and("networkList", networkIdList))
                .firstResult();
    }

    public PipelineCollection findByNameIfExists(String networkId, String name){

        return find("{'name': :name, 'networkId': :networkId}",
                Parameters.with("networkId", networkId).and("name", name))
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
