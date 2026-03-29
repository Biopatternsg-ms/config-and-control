package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.in.FindNetwork;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class FindNetworkUseCase implements FindNetwork {

    private final NetworkRepository networkRepository;

    @Override
    public NetworkConfig byId(String id) {
        return networkRepository.findById(id);
    }

    @Override
    public NetworkConfig byName(String name) {
        return networkRepository.findByName(name);
    }

    @Override
    public List<NetworkConfig> byFilters(FindNetworkRequest findNetworkRequest) {
        return networkRepository.findByFilters(findNetworkRequest);
    }
}
