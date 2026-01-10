package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.exceptions.UnauthorizedServiceException;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.in.CreateNetwork;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class CreateNetworkUseCase implements CreateNetwork {

    final NetworkRepository networkRepository;

    @Override
    public NetworkConfig execute(NetworkConfig networkConfig) {

        var networkModel = networkRepository.findByName(networkConfig.getName());
        if(networkModel != null){
            throw new UnprocessableEntityException("The network already exists");
        }

        return networkRepository.save(networkConfig);
    }
}
