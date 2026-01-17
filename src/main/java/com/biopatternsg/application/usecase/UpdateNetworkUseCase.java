package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.in.UpdateNetwork;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdateNetworkUseCase implements UpdateNetwork {

    final NetworkRepository networkRepository;

    @Override
    public NetworkConfig execute(NetworkConfig networkConfig) {

        var networkConfigCurrent = networkRepository.findByNameExists(networkConfig.getId(), networkConfig.getName());
        if(networkConfigCurrent != null){
            throw new UnprocessableEntityException("The network already exists");
        }

        networkConfigCurrent = networkRepository.findById(networkConfig.getId());
        if(networkConfigCurrent == null){
            throw new UnprocessableEntityException("The network don't exists");
        }

        networkConfigCurrent.setName(networkConfig.getName());
        networkConfigCurrent.setDescription(networkConfig.getDescription());

        return networkRepository.save(networkConfigCurrent);
    }
}
