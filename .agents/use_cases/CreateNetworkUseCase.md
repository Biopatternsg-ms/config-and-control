# Caso de Uso: `CreateNetworkUseCase`

- **Clase:** `com.biopatternsg.application.usecase.CreateNetworkUseCase`
- **Puerto de Entrada:** `com.biopatternsg.domain.port.in.CreateNetwork`
- **Puerto de Salida:** `com.biopatternsg.domain.port.out.repositories.NetworkRepository`
- **Controlador Invocador:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.NetworkController`

---

## 1. Propósito
Permite registrar una nueva red biológica (`NetworkConfig`) en el sistema, asegurando que no existan colisiones de nombres para mantener la unicidad de las redes.

---

## 2. Flujo de Ejecución

1. Recibe un objeto de dominio `NetworkConfig` desde el controlador REST.
2. Consulta `NetworkRepository.findByName(name)` para verificar si ya existe una red con el mismo nombre.
3. Si la red ya existe, lanza `UnprocessableEntityException("The network already exists")`.
4. Si no existe, persiste la entidad invocando `NetworkRepository.save(networkConfig)` y retorna el modelo creado.

---

## 3. Diagrama de Flujo (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as API Client / Frontend
    participant Controller as NetworkController
    participant UseCase as CreateNetworkUseCase
    participant Repo as NetworkRepository
    participant DB as MongoDB

    Cliente->>Controller: POST /api/network (CreateNetworkRequest)
    Controller->>UseCase: execute(NetworkConfig)
    UseCase->>Repo: findByName(networkConfig.name)
    Repo->>DB: query by name
    DB-->>Repo: network entity or null
    Repo-->>UseCase: NetworkConfig or null
    alt La red ya existe
        UseCase-->>Controller: throw UnprocessableEntityException
        Controller-->>Cliente: 422 Unprocessable Entity
    else La red no existe
        UseCase->>Repo: save(networkConfig)
        Repo->>DB: persist
        DB-->>Repo: saved entity
        Repo-->>UseCase: saved NetworkConfig
        UseCase-->>Controller: saved NetworkConfig
        Controller-->>Cliente: 201 / 200 OK
    end
```
