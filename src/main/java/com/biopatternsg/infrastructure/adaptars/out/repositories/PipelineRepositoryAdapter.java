package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.adaptars.mappers.PipelineMapper;
import com.biopatternsg.infrastructure.mongo_db.repositories.NetworkRepositoryDB;
import com.biopatternsg.infrastructure.mongo_db.repositories.PipelineRepositoryDB;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

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

        var pipelineObject = pipelineRepositoryDB.findById(new ObjectId(id));
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByName(String name) {

        var networkList = networkIdList();
        var pipelineObject = pipelineRepositoryDB.findByName(name, networkList);
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public PipelineConfig findByNameExists(String id, String name) {

        var pipelineObject = pipelineRepositoryDB.findByNameIfExists(id, name, networkIdList());
        return PipelineMapper.toPipelineConfig(pipelineObject);
    }

    @Override
    public List<PipelineConfig> findByNetworkId(String networkId) {

        var pipelineList = pipelineRepositoryDB.findByNetwork(networkId);
        return PipelineMapper.toPipelineConfigList(pipelineList);
    }

    @Override
    public List<PipelineConfig> findByUserId() {

        var pipelineObject = pipelineRepositoryDB.findByNetworkList(networkIdList());
        return PipelineMapper.toPipelineConfigList(pipelineObject);
    }

    private List<String> networkIdList(){

        var networkList = networkRepositoryDB.findByUser(sessionUtil.getUserId());
        return networkList.stream()
                .map(network -> network.id.toString())
                .toList();
    }
}
