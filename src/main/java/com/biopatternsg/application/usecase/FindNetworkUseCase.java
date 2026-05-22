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
