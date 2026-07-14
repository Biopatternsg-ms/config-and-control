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

import com.biopatternsg.domain.models.UserFilters;
import com.biopatternsg.infrastructure.mongo_db.collections.UserCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.bson.Document;

@ApplicationScoped
public class UserRepositoryDB implements PanacheMongoRepository<UserCollection> {

    public UserCollection findByKeycloakId(String keycloakId){
        return find("{'keycloakId': :keycloakId}",
                Parameters.with("keycloakId", keycloakId))
                .firstResult();
    }

    public UserCollection findUsername(String username) {
        return find("{'username': :username}",
                Parameters.with("username", username))
                .firstResult();
    }

    public List<UserCollection> findByFirstName(String firstName) {
        return find("{'firstName': {'$regex': :firstName, '$options': 'i'}}",
                Parameters.with("firstName", firstName))
                .list();
    }

    public List<UserCollection> findByLastName(String lastName) {
        return find("{'lastName': {'$regex': :lastName, '$options': 'i'}}",
                Parameters.with("lastName", lastName))
                .list();
    }

    public List<UserCollection> findByFilters(UserFilters findUser, int page, int size) {
        Document query = new Document();
        
        if (findUser == null) {
            if (page <= 0 || size <= 0) return find(query).list();
            return find(query).page(page, size).list();
        }

        if (findUser.getUsername() != null && !findUser.getUsername().isEmpty()) {
            query.append("username", findUser.getUsername());
        }

        if (findUser.getFirstName() != null && !findUser.getFirstName().isEmpty()) {
            query.append("firstName", new Document("$regex", findUser.getFirstName()).append("$options", "i"));
        }

        if (findUser.getLastName() != null && !findUser.getLastName().isEmpty()) {
            query.append("lastName", new Document("$regex", findUser.getLastName()).append("$options", "i"));
        }

        if (page <= 0 || size <= 0) return find(query).list();
        return find(query).page(page, size).list();
    }
}
