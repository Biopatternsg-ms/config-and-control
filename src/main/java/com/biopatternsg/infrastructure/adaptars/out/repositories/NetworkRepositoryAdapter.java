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
package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.infrastructure.adaptars.mappers.NetworkMapper;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;
import com.biopatternsg.infrastructure.mongo_db.repositories.NetworkRepositoryDB;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class NetworkRepositoryAdapter implements NetworkRepository {

    @Inject
    private SessionUtil sessionUtil;
    @Inject
    private NetworkRepositoryDB networkRepositoryDB;

    @Override
    public NetworkConfig save(NetworkConfig networkConfig) {

        var networkCollection = NetworkMapper.toNetworkCollection(networkConfig, sessionUtil.getUserId());
        networkCollection.persistOrUpdate();
        return NetworkMapper.toNetworkConfig(networkCollection);
    }

    @Override
    public NetworkConfig findById(String id) {

        var networkCollection = networkRepositoryDB.findByIdAndIdUser(id, sessionUtil.getUserId());
        return NetworkMapper.toNetworkConfig(networkCollection);
    }

    @Override
    public NetworkConfig findByName(String name) {

        var networkCollection = networkRepositoryDB.findByUserAndName(name, sessionUtil.getUserId());
        return NetworkMapper.toNetworkConfig(networkCollection);
    }

    @Override
    public NetworkConfig findByNameExists(String id, String name) {

        var networkCollection = networkRepositoryDB.findByUserAndNameExists(id, name, sessionUtil.getUserId());
        return NetworkMapper.toNetworkConfig(networkCollection);
    }

    @Override
    public List<NetworkConfig> findByFilters(FindNetworkRequest findNetworkRequest) {

        var networkCollectionList = networkRepositoryDB.findByUserAndFilters(findNetworkRequest, sessionUtil.getUserId());
        return NetworkMapper.toNetworkConfigList(networkCollectionList);
    }
}
