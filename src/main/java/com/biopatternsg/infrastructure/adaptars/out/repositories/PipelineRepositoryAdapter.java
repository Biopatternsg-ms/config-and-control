package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.adaptars.mappers.PipelineMapper;
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

        var pipelineCollection = PipelineMapper.toPipelineCollection(pipelineConfig);
        pipelineCollection.persistOrUpdate();
        return PipelineMapper.toPipelineConfig(pipelineCollection);
    }

    @Override
    public PipelineConfig findById(String id) {

        var pipelineObject = pipelineRepositoryDB.findByIdAndUser(id, sessionUtil.getUserId());
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByName(String name) {

        var pipelineObject = pipelineRepositoryDB.findByNameAndUser(name, sessionUtil.getUserId());
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByNameExists(String id, String name) {

        var pipelineObject = pipelineRepositoryDB.findByNameAndUserExists(id, name, sessionUtil.getUserId());
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public List<PipelineConfig> findByNetworkId(String networkId) {

        var pipelineList = pipelineRepositoryDB.findByNetworkIdAndUser(networkId, sessionUtil.getUserId());
        return PipelineMapper.toPipelineConfigList(pipelineList);
    }
}
