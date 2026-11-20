# Use Case: `CreateNetworkUseCase`

- **Class:** `com.biopatternsg.application.usecase.CreateNetworkUseCase`
- **Inbound Port:** `com.biopatternsg.domain.port.in.CreateNetwork`
- **Outbound Port:** `com.biopatternsg.domain.port.out.repositories.NetworkRepository`
- **Invoking Controller:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.NetworkController`

---

## 1. Purpose
Registers a new biological network (`NetworkConfig`) in the system, ensuring no name collisions occur so network names remain unique per user.

---

## 2. Execution Flow

1. Receives a `NetworkConfig` domain object from the REST controller.
2. Queries `NetworkRepository.findByName(name)` to verify whether a network with the same name already exists.
3. If the network already exists, throws `UnprocessableEntityException("The network already exists")`.
4. Otherwise, persists the entity by calling `NetworkRepository.save(networkConfig)` and returns the created domain model.

---

## 3. Sequence Diagram (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Client as API Client / Frontend
    participant Controller as NetworkController
    participant UseCase as CreateNetworkUseCase
    participant Repo as NetworkRepository
    participant DB as MongoDB

    Client->>Controller: POST /config-and-control/networks (CreateNetworkRequest)
    Controller->>UseCase: execute(NetworkConfig)
    UseCase->>Repo: findByName(networkConfig.name)
    Repo->>DB: query by name
    DB-->>Repo: network entity or null
    Repo-->>UseCase: NetworkConfig or null
    alt Network already exists
        UseCase-->>Controller: throw UnprocessableEntityException
        Controller-->>Client: 422 Unprocessable Entity
    else Network does not exist
        UseCase->>Repo: save(networkConfig)
        Repo->>DB: persist
        DB-->>Repo: saved entity
        Repo-->>UseCase: saved NetworkConfig
        UseCase-->>Controller: saved NetworkConfig
        Controller-->>Client: 201 Created
    end
```

---

## 4. Error Cases

- **Duplicate Network Name:** Throws `UnprocessableEntityException("The network already exists")` when a network with the same name already exists for the user.
