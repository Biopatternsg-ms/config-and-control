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

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.ReportFormat;
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
        if (id == null || !ObjectId.isValid(id)) {
            return null;
        }
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

    public ReportFormat<PipelineCollection> findByUserAndFilters(PipelineConfig findPipeline,
                                                                 List<String> networkIdList, int page, int size){

        Document query = new Document();
        query.append("networkId", new Document("$in", networkIdList));

        if(findPipeline != null){
            if (findPipeline.getId() != null && !findPipeline.getId().isEmpty()) {
                query.append("_id", new ObjectId(findPipeline.getId()));
            }

            if (findPipeline.getNetworkId() != null && !findPipeline.getNetworkId().isEmpty()) {
                query.append("networkId", findPipeline.getNetworkId());
            }

            if (findPipeline.getName() != null) {
                query.append("name", new Document("$regex", findPipeline.getName()).append("$options", "i"));
            }

            if (findPipeline.getDescription() != null) {
                query.append("description", new Document("$regex", findPipeline.getDescription()).append("$options", "i"));
            }
        }

        long count = find(query).count();
        List<PipelineCollection> list = (page < 0 || size <= 0)
            ? find(query).list()
            : find(query).page(page, size).list();

        return new ReportFormat<>(count, list);
    }
}
