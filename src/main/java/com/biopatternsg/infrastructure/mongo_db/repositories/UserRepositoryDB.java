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
package com.biopatternsg.infrastructure.mongo_db.repositories;

import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.infrastructure.mongo_db.collections.UserCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.bson.Document;

@ApplicationScoped
public class UserRepositoryDB implements PanacheMongoRepository<UserCollection> {

    public UserCollection findByKeycloakId(String keycloakId){
        UserCollection user = find("{'identityProviderId': :keycloakId}",
                Parameters.with("keycloakId", keycloakId))
                .firstResult();
        if (user == null) {
            user = findUsername(keycloakId);
        }
        return user;
    }

    public UserCollection findUsername(String username) {
        return find("{'username': :username}",
                Parameters.with("username", username))
                .firstResult();
    }

    public ReportFormat<UserCollection> findByFilters(UserConfig filters, int page, int size) {
        Document query = new Document();

        if (filters != null) {
            if (filters.getUsername() != null && !filters.getUsername().isEmpty()) {
                query.append("username", filters.getUsername());
            }

            if (filters.getFirstName() != null && !filters.getFirstName().isEmpty()) {
                query.append("firstName", new Document("$regex", filters.getFirstName()).append("$options", "i"));
            }

            if (filters.getLastName() != null && !filters.getLastName().isEmpty()) {
                query.append("lastName", new Document("$regex", filters.getLastName()).append("$options", "i"));
            }

            if (filters.getEnabled() != null) {
                query.append("enabled", filters.getEnabled());
            }
        }

        long count = find(query).count();
        List<UserCollection> list = (page < 0 || size <= 0)
                ? find(query).list()
                : find(query).page(page, size).list();

        return new ReportFormat<>(count, list);
    }
}
