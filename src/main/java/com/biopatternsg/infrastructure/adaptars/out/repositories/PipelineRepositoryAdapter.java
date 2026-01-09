package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;
import com.biopatternsg.infrastructure.mongo_db.mappers.PipelineMapper;
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
    SessionUtil sessionUtil;
    private PipelineRepositoryDB pipelineRepositoryDB;

    @Override
    public PipelineConfig save(PipelineConfig pipelineConfig) {

        var getPipelineConfig = findByName(pipelineConfig.getName());
        if(getPipelineConfig != null){
            return getPipelineConfig;
        }

        var pipelineObject = saveObject(pipelineConfig);
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig update(PipelineConfig pipelineConfig) {

        var pipelineObject = pipelineRepositoryDB.findByIdAndUser(pipelineConfig.getId(), sessionUtil.getUserId());
        if(pipelineObject == null){
            return null;
        }

        pipelineObject.setName(pipelineConfig.getName());
        pipelineObject.setDescription(pipelineConfig.getDescription());
        pipelineObject.update();

        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findById(String id) {
        var pipelineObject = pipelineRepositoryDB.findByIdAndUser(id, sessionUtil.getUserId());
        if(pipelineObject == null){
            return null;
        }

        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByName(String name) {

        var pipelineObject = pipelineRepositoryDB.findByNameAndUser(name, sessionUtil.getUserId());
        if(pipelineObject == null){
            return null;
        }

        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public List<PipelineConfig> findByNetworkId(String networkId) {

        var pipelineList = pipelineRepositoryDB.findByNetworkIdAndUser(networkId, sessionUtil.getUserId());
        return PipelineMapper.toPipelineConfigList(pipelineList);
    }

    private PipelineCollection saveObject(PipelineConfig pipelineConfig){

        PipelineCollection object = new PipelineCollection();

        object.setName(pipelineConfig.getName());
        object.setDescription(pipelineConfig.getDescription());
        object.setNetworkId(pipelineConfig.getNetworkId());
        object.setExpertObjects(pipelineConfig.getExpertObjects());
        object.setTranscriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig());
        object.setLevels(pipelineConfig.getLevels());

        object.persist();

        return object;
    }
}
