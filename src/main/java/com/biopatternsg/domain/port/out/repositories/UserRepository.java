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

import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.models.UserConfig;

public interface UserRepository {

    void create(UserConfig userConfig);
    void update(UserConfig userConfig);
    UserConfig find(String username);
    UserConfig findById(String id);
    UserConfig findByKeycloakId(String keycloakId);
    ReportFormat<UserConfig> list(UserConfig userFilters, int page, int size);
}
