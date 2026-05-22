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
package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.adaptars.mappers.PipelineMapper;
import com.biopatternsg.infrastructure.dtos.FindPipelineRequest;
import com.biopatternsg.infrastructure.dtos.PipelineResponse;
import com.biopatternsg.infrastructure.mongo_db.repositories.NetworkRepositoryDB;
import com.biopatternsg.infrastructure.mongo_db.repositories.PipelineRepositoryDB;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class PipelineRepositoryAdapter implements PipelineRepository {

    @Inject
    private SessionUtil sessionUtil;
    @Inject
    private PipelineRepositoryDB pipelineRepositoryDB;
    @Inject
    private NetworkRepositoryDB networkRepositoryDB;

    @Override
    public PipelineConfig save(PipelineConfig pipelineConfig) {

        var pipelineCollection = PipelineMapper.toPipelineCollection(pipelineConfig);
        pipelineCollection.persistOrUpdate();
        return PipelineMapper.toPipelineConfig(pipelineCollection);
    }

    @Override
    public PipelineConfig findById(String id) {

        var pipelineObject = pipelineRepositoryDB.findByIdAndUser(id, networkIdList());
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByName(String name) {

        var pipelineObject = pipelineRepositoryDB.findByName(name, networkIdList());
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByNameExists(String networkId, String name) {

        var pipelineObject = pipelineRepositoryDB.findByNameIfExists(networkId, name);
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public List<PipelineResponse> findByFilters(FindPipelineRequest findPipelineRequest) {
        var pipelineObject = pipelineRepositoryDB.findByUserAndFilters(findPipelineRequest, networkIdList());
        return PipelineMapper.toPipelineResponseList(pipelineObject);
    }

    private List<String> networkIdList(){

        var networkList = networkRepositoryDB.findByUserAndFilters(null, sessionUtil.getUserId());
        return networkList.stream()
                .map(network -> network.id.toString())
                .toList();
    }
}
