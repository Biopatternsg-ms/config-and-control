# Caso de Uso: `FindNetworkUseCase`

- **Clase:** `com.biopatternsg.application.usecase.FindNetworkUseCase`
- **Puerto de Entrada:** `com.biopatternsg.domain.port.in.FindNetwork`
- **Puerto de Salida:** `com.biopatternsg.domain.port.out.repositories.NetworkRepository`
- **Controlador Invocador:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.NetworkController`

---

## 1. Propósito
Provee métodos de consulta de redes biológicas: búsqueda por identificador único (`id`), búsqueda exacta por nombre (`name`) y búsqueda paginada aplicando filtros dinámicos (`byFilters`).

---

## 2. Métodos y Flujo de Ejecución

1. **`byId(String id)`:** Delega en `NetworkRepository.findById(id)`.
2. **`byName(String name)`:** Delega en `NetworkRepository.findByName(name)`.
3. **`byFilters(NetworkConfig findNetwork, int page, int size)`:** Consulta `NetworkRepository.findByFilters(...)` aplicando paginación y retornando un `ReportFormat<NetworkConfig>` (lista de resultados, total de páginas y conteo).

---

## 3. Diagrama de Flujo (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as API Client / Frontend
    participant Controller as NetworkController
    participant UseCase as FindNetworkUseCase
    participant Repo as NetworkRepository
    participant DB as MongoDB

    alt Consulta por ID o Nombre
        Cliente->>Controller: GET /api/network/{id} o /api/network/name/{name}
        Controller->>UseCase: byId(id) / byName(name)
        UseCase->>Repo: findById(id) / findByName(name)
        Repo->>DB: find
        DB-->>Repo: entity or null
        Repo-->>UseCase: NetworkConfig
        UseCase-->>Controller: NetworkConfig
        Controller-->>Cliente: 200 OK (NetworkConfig)
    else Consulta con Filtros Paginados
        Cliente->>Controller: POST /api/network/filters?page=0&size=10
        Controller->>UseCase: byFilters(filterConfig, page, size)
        UseCase->>Repo: findByFilters(filterConfig, page, size)
        Repo->>DB: query + skip + limit + count
        DB-->>Repo: paginated results
        Repo-->>UseCase: ReportFormat<NetworkConfig>
        UseCase-->>Controller: ReportFormat<NetworkConfig>
        Controller-->>Cliente: 200 OK
    end
```
