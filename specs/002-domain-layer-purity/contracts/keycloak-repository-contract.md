# Interface Contract: KeycloakRepository (Domain Outbound Port)

## Contract Definition

- **Class Name**: `com.biopatternsg.domain.port.out.repositories.KeycloakRepository`
- **Architectural Role**: Outbound Port (Domain Layer)
- **Primary Consumers**: `UserManagementUseCase` (`com.biopatternsg.application.usecase`)
- **Implementer**: `KeycloakAdapter` (`com.biopatternsg.infrastructure.adapters.out.repositories`)

---

## Method Specification

```java
package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.UserAuth;
import com.biopatternsg.domain.models.UserConfig;
import java.util.List;

public interface KeycloakRepository {

    /**
     * Authenticates a user against Keycloak.
     *
     * @param user username or email
     * @param pass password
     * @return UserAuth containing tokens
     */
    UserAuth login(String user, String pass);

    /**
     * Refreshes an authentication session.
     *
     * @param refreshToken existing refresh token
     * @return updated UserAuth
     */
    UserAuth refreshToken(String refreshToken);

    /**
     * Provisions a new user in the external identity provider.
     *
     * @param newUser the domain configuration for the new user
     * @return the assigned identityProviderId (Keycloak subject ID)
     * @throws com.biopatternsg.domain.exceptions.KeycloakServiceException on registration failure
     */
    String register(UserConfig newUser);

    /**
     * Triggers verification/action emails for a user.
     *
     * @param userId Keycloak user ID
     * @param actions list of required actions (e.g., VERIFY_EMAIL)
     */
    void sendEmail(String userId, List<String> actions);

    /**
     * Lists users matching filter criteria.
     *
     * @param userFilters filter criteria
     * @return list of matching UserConfig models
     */
    List<UserConfig> listUsers(UserConfig userFilters);

    /**
     * Updates an existing user's attributes in Keycloak.
     *
     * @param userId Keycloak user ID
     * @param userConfig updated user attributes
     */
    void updateUser(String userId, UserConfig userConfig);
}
```

---

## Architectural Rules

1. **Zero Framework Imports**: No classes under `jakarta.ws.rs.*`, `org.jboss.*`, or other external transport packages may be imported.
2. **Exception Safety**: Transport failures (HTTP 4xx/5xx) must be mapped to domain exceptions (`KeycloakServiceException`) by the adapter implementation before crossing this port interface.
3. **No Header Leaks**: The port contract returns only domain values (`String identityProviderId`), completely masking HTTP `Location` headers or status codes.
