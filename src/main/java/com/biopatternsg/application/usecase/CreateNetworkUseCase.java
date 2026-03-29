package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.in.CreateNetwork;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.infrastructure.dtos.CreateNetworkRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class CreateNetworkUseCase implements CreateNetwork {

    final NetworkRepository networkRepository;

    @Override
    public NetworkConfig execute(CreateNetworkRequest createNetwork) {

        var networkConfig = requestToConfig(createNetwork);
        var networkModel = networkRepository.findByName(networkConfig.getName());
        if(networkModel != null){
            throw new UnprocessableEntityException("The network already exists");
        }

        return networkRepository.save(networkConfig);
    }

    private NetworkConfig requestToConfig(CreateNetworkRequest createNetwork){

        return NetworkConfig.builder()
                .name(createNetwork.name())
                .description(createNetwork.description())
                .build();
    }
}
