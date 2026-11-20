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
package unit.usecase;

import com.biopatternsg.application.usecase.UserManagementUseCase;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.models.UserAuth;
import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.domain.port.out.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserManagementUseCaseTest {

    private FakeUserRepository userRepository;
    private FakeKeycloakRepository keycloakRepository;
    private UserManagementUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        keycloakRepository = new FakeKeycloakRepository();
        useCase = new UserManagementUseCase(userRepository, keycloakRepository);
    }

    @Test
    @DisplayName("register should obtain userId from KeycloakRepository, assign identityProviderId, create user and send email")
    void testRegisterSuccess() {
        String expectedUserId = "kc-user-uuid-12345";
        keycloakRepository.userIdToReturn = expectedUserId;

        UserConfig userConfig = UserConfig.builder()
                .username("researcher@biopatternsg.org")
                .firstName("Ada")
                .lastName("Lovelace")
                .build();

        useCase.register(userConfig);

        assertEquals(expectedUserId, userConfig.getIdentityProviderId(),
                "identityProviderId should match the string returned by KeycloakRepository");
        assertEquals(1, userRepository.createdUsers.size(),
                "UserRepository.create should be called once");
        assertEquals(expectedUserId, userRepository.createdUsers.getFirst().getIdentityProviderId());
        assertEquals(expectedUserId, keycloakRepository.lastEmailUserId);
        assertEquals(List.of("VERIFY_EMAIL"), keycloakRepository.lastEmailActions);
    }

    static class FakeKeycloakRepository implements KeycloakRepository {
        String userIdToReturn = "default-user-id";
        String lastEmailUserId;
        List<String> lastEmailActions;

        @Override
        public UserAuth login(String user, String pass) {
            return null;
        }

        @Override
        public UserAuth refreshToken(String refreshToken) {
            return null;
        }

        @Override
        public String register(UserConfig newUser) {
            return userIdToReturn;
        }

        @Override
        public List<UserConfig> listUsers(UserConfig userFilters) {
            return List.of();
        }

        @Override
        public void sendEmail(String userId, List<String> actions) {
            this.lastEmailUserId = userId;
            this.lastEmailActions = actions;
        }

        @Override
        public void updateEnabled(String userId, boolean enabled) {
        }
    }

    static class FakeUserRepository implements UserRepository {
        List<UserConfig> createdUsers = new ArrayList<>();

        @Override
        public void create(UserConfig userConfig) {
            createdUsers.add(userConfig);
        }

        @Override
        public void update(UserConfig userConfig) {
        }

        @Override
        public UserConfig find(String username) {
            return null;
        }

        @Override
        public UserConfig findById(String id) {
            return null;
        }

        @Override
        public UserConfig findByKeycloakId(String keycloakId) {
            return null;
        }

        @Override
        public ReportFormat<UserConfig> list(UserConfig userFilters, int page, int size) {
            return null;
        }
    }
}
