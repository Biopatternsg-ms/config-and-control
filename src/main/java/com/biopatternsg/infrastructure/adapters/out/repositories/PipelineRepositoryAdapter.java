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
package com.biopatternsg.infrastructure.adapters.out.repositories;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.port.out.PipelineStepPort;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.adapters.mappers.PipelineMapper;
import com.biopatternsg.infrastructure.mongo_db.repositories.NetworkRepositoryDB;
import com.biopatternsg.infrastructure.mongo_db.repositories.PipelineRepositoryDB;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class PipelineRepositoryAdapter implements PipelineRepository, PipelineStepPort {

    @Inject
    private SessionUtil sessionUtil;
    @Inject
    private PipelineRepositoryDB pipelineRepositoryDB;
    @Inject
    private NetworkRepositoryDB networkRepositoryDB;

    @Override
    public PipelineConfig save(PipelineConfig pipelineConfig) {

        var pipelineCollection = PipelineMapper.configToCollection(pipelineConfig);
        pipelineCollection.persistOrUpdate();
        return PipelineMapper.collectionToConfig(pipelineCollection);
    }

    @Override
    public PipelineConfig findById(String id) {

        var pipelineObject = pipelineRepositoryDB.findByIdAndUser(id, networkIdList());
        return PipelineMapper.collectionToConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByName(String name) {

        var pipelineObject = pipelineRepositoryDB.findByName(name, networkIdList());
        return PipelineMapper.collectionToConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByNameExists(String networkId, String name) {

        var pipelineObject = pipelineRepositoryDB.findByNameIfExists(networkId, name);
        return PipelineMapper.collectionToConfig(pipelineObject);
    }

    @Override
    public ReportFormat<PipelineConfig> findByFilters(PipelineConfig findPipeline, int page, int size) {

        var reportCollection = pipelineRepositoryDB.findByUserAndFilters(findPipeline, networkIdList(), page, size);
        var modelList = reportCollection.list().stream().map(PipelineMapper::collectionToConfig).toList();
        return new ReportFormat<>(reportCollection.count(),modelList);
    }

    @Override
    public List<PipelineConfig> findAllFailed() {
        return pipelineRepositoryDB.findAllFailed().stream()
                .map(PipelineMapper::collectionToConfig)
                .toList();
    }

    @Override
    public PipelineConfig updateStep(String id, PipelineSteps step, Status status) {
        return updateStep(id, step, status, null);
    }

    @Override
    public PipelineConfig updateStep(String id, PipelineSteps step, Status status, Map<String, String> metrics) {
        var pipeline = findById(id);
        if (pipeline == null && ObjectId.isValid(id)) {
            var doc = pipelineRepositoryDB.findById(new ObjectId(id));
            if (doc != null) {
                pipeline = PipelineMapper.collectionToConfig(doc);
            }
        }
        if (pipeline == null) {
            throw new UnprocessableEntityException("The pipeline don't exists");
        }

        pipeline.setStep(step);
        pipeline.addStatusForStep(step, status, metrics);
        return save(pipeline);
    }

    private List<String> networkIdList(){

        var networkList = networkRepositoryDB.findByUserAndFilters(null,
                sessionUtil.getUserId(), 0, 0);
        return networkList.list().stream().map(network -> network.id.toString()).toList();
    }
}
