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
package com.biopatternsg.infrastructure.adapters.out.repositories;

import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.domain.models.UserFilters;
import com.biopatternsg.domain.port.out.repositories.UserRepository;
import com.biopatternsg.infrastructure.adapters.mappers.UserMapper;
import com.biopatternsg.infrastructure.mongo_db.collections.UserCollection;
import com.biopatternsg.infrastructure.mongo_db.repositories.UserRepositoryDB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class UserRepositoryAdapter implements UserRepository {

    @Inject
    UserRepositoryDB userRepositoryDB;

    @Override
    public void create(UserConfig userConfig) {
        UserCollection userCollection = UserMapper.toCollection(userConfig);
        userRepositoryDB.persistOrUpdate(userCollection);
        Response.ok().build();
    }

    @Override
    public List<UserConfig> list(UserFilters userFilters) {
        List<UserCollection> collections = userRepositoryDB.findByFilters(userFilters);
        return collections.stream()
                .map(UserMapper::toDomain)
                .toList();
    }
}
