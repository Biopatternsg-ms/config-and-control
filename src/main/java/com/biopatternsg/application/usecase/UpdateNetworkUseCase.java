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
package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.in.UpdateNetwork;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.infrastructure.dtos.UpdateNetworkRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdateNetworkUseCase implements UpdateNetwork {

    final NetworkRepository networkRepository;

    @Override
    public NetworkConfig execute(UpdateNetworkRequest updateConfig) {


        var networkConfig = requestToConfig(updateConfig);
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

    private NetworkConfig requestToConfig(UpdateNetworkRequest updateNetwork){

        return NetworkConfig.builder()
                .id(updateNetwork.id())
                .name(updateNetwork.name())
                .description(updateNetwork.description())
                .build();
    }
}
