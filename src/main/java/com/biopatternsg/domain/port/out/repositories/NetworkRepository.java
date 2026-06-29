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
package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;

import java.util.List;

public interface NetworkRepository {

    NetworkConfig save(NetworkConfig networkConfig);
    NetworkConfig findById(String id);
    NetworkConfig findByName(String name);
    NetworkConfig findByNameExists(String id, String name);
    List<NetworkConfig> findByFilters(NetworkConfig findNetwork, int page, int size);
}
