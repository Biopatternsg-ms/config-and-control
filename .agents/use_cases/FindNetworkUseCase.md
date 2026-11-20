# Use Case: `FindNetworkUseCase`

- **Class:** `com.biopatternsg.application.usecase.FindNetworkUseCase`
- **Inbound Port:** `com.biopatternsg.domain.port.in.FindNetwork`
- **Outbound Port:** `com.biopatternsg.domain.port.out.repositories.NetworkRepository`
- **Invoking Controller:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.NetworkController`

---

## 1. Purpose
Provides query operations for biological networks: lookup by unique identifier (`byId`), exact lookup by name (`byName`), and paginated search using dynamic filters (`byFilters`).

---

## 2. Methods & Execution Flow

1. **`byId(String id)`:** Delegates to `NetworkRepository.findById(id)`.
2. **`byName(String name)`:** Delegates to `NetworkRepository.findByName(name)`.
3. **`byFilters(NetworkConfig findNetwork, int page, int size)`:** Queries `NetworkRepository.findByFilters(...)` with pagination and returns a `ReportFormat<NetworkConfig>` envelope containing the matching items and total count.

---

## 3. Sequence Diagram (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Client as API Client / Frontend
    participant Controller as NetworkController
    participant UseCase as FindNetworkUseCase
    participant Repo as NetworkRepository
    participant DB as MongoDB

    alt Lookup by ID or Name
        Client->>Controller: GET /config-and-control/networks/{id}
        Controller->>UseCase: byId(id) / byName(name)
        UseCase->>Repo: findById(id) / findByName(name)
        Repo->>DB: find
        DB-->>Repo: entity or null
        Repo-->>UseCase: NetworkConfig
        UseCase-->>Controller: NetworkConfig
        Controller-->>Client: 200 OK (NetworkResponse)
    else Paginated Filtered Search
        Client->>Controller: GET /config-and-control/networks?page=0&size=10
        Controller->>UseCase: byFilters(filterConfig, page, size)
        UseCase->>Repo: findByFilters(filterConfig, page, size)
        Repo->>DB: query + skip + limit + count
        DB-->>Repo: paginated results
        Repo-->>UseCase: ReportFormat<NetworkConfig>
        UseCase-->>Controller: ReportFormat<NetworkConfig>
        Controller-->>Client: 200 OK (ReportFormat<NetworkResponse>)
    end
```
