package com.biopatternsg.application.usecase;

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

        var networkModel = networkRepository.findById(networkConfig.getId());
        if(networkModel == null){
            networkModel = networkRepository.update(networkConfig);
        }

        return networkModel;
    }
}
