# Arquitectura del Servicio `config-and-control`

> **Documento para Agentes de IA y Desarrolladores**  
> **Servicio:** `config-and-control`  
> **Ecosistema:** BioPatternsG Microservices  
> **Framework:** Quarkus (Java 21)  
> **Estilo de Arquitectura:** Arquitectura Hexagonal (Ports & Adapters) / Clean Architecture  

---

## 1. Propósito del Servicio

`config-and-control` es el servicio núcleo de orquestación, gestión de configuraciones y control de usuarios y pipelines dentro del ecosistema **BioPatternsG**. Sus responsabilidades principales incluyen:

1. **Gestión de Redes Biológicas (`Network`):** Creación, búsqueda y actualización de redes biológicas asociadas a usuarios.
2. **Configuración y Ciclo de Vida de Pipelines (`PipelineConfig`):** Configuración de pasos, parámetros de factores de transcripción, niveles de análisis y objetos expertos.
3. **Lanzamiento y Seguimiento de Ejecuciones (`PipelineExecution`):** Disparo de pipelines biológicos hacia servicios externos (`inferences`, `pubmed-integration`, etc.) y monitoreo del estado de ejecución.
4. **Seguridad y Usuarios (`Keycloak` / `User`):** Autenticación, refresco de credenciales y administración de usuarios delegada en Keycloak con almacenamiento local de auditoría/perfil.

---

## 2. Diagrama Conceptual de Capas (Arquitectura Hexagonal)

```
+---------------------------------------------------------------------------------------+
|                                    INFRASTRUCTURE                                     |
|                                                                                       |
|   [Inbound Adapters (REST Controllers)]                                               |
|   AdminController, NetworkController, PipelineController, UserController              |
|                                         |                                             |
|                                         v (invoca a través de Interfaces)             |
|   +-------------------------------------+-----------------------------------------+   |
|   |                                     |                                         |   |
|   |         +---------------------------v---------------------------+             |   |
|   |         |                      APPLICATION                      |             |   |
|   |         |                                                       |             |   |
|   |         |   [Application Services & Use Cases]                  |             |   |
|   |         |   - Services: PipelineService, etc.                   |             |   |
|   |         |   - Use Cases (application.usecase):                  |             |   |
|   |         |     Implementan los puertos de entrada (port.in)      |             |   |
|   |         |                               |                       |             |   |
|   |         |                               v (utiliza)             |             |   |
|   |         |   +---------------------------v-------------------+   |             |   |
|   |         |   |                     DOMAIN                    |   |             |   |
|   |         |   |                                               |   |             |   |
|   |         |   |   - Models: Network, PipelineConfig,          |   |             |   |
|   |         |   |             PipelineExecution, User           |   |             |   |
|   |         |   |   - Exceptions de Negocio                     |   |             |   |
|   |         |   |   - Ports IN (Interfaces de Casos de Uso)     |   |             |   |
|   |         |   |   - Ports OUT (Interfaces de Repositorios     |   |             |   |
|   |         |   |                y Clientes Externos)           |   |             |   |
|   |         |   +---------------------------^-------------------+   |             |   |
|   |         |                               |                       |             |   |
|   |         +-------------------------------+-----------------------+             |   |
|   |                                         ^                                     |   |
|   |                                         | (implementan Ports OUT)             |   |
|   +-----------------------------------------+-------------------------------------+   |
|                                         ^                                             |
|                                         |                                             |
|   [Outbound Adapters (Persistence & External Services)]                               |
|   - MongoDB Panache: NetworkRepositoryAdapter, PipelineRepositoryAdapter,             |
|                      UserRepositoryAdapter                                            |
|   - External REST Clients: InferencesAdapter, KeycloakAdapter,                        |
|                            PubmedIntegrationAdapter, BiologicalObjectAdapter          |
+---------------------------------------------------------------------------------------+
```

---

## 3. Estructura de Paquetes (`com.biopatternsg.*`)

```text
com.biopatternsg/
├── domain/                      <-- Capa de Dominio (Pura, sin dependencias de frameworks)
│   ├── enums/                   <-- Tipos enumerados (ej. PipelineExecutionStatus)
│   ├── exceptions/              <-- Excepciones de negocio (NotFoundException, etc.)
│   ├── models/                  <-- Entidades y modelos del dominio (POJOs/Lombok)
│   │   ├── pipeline_config/
│   │   └── user_registration/
│   ├── services/                <-- Servicios del dominio (Interfaz PipelineService)
│   └── port/
│       ├── in/                  <-- Puertos de Entrada (Interfaces de Use Cases)
│       └── out/                 <-- Puertos de Salida (Interfaces de integración)
│           └── repositories/    <-- Puertos de persistencia y servicios
│
├── application/                 <-- Capa de Aplicación
│   ├── services/                <-- Implementaciones de servicios (PipelineServiceImpl, PipelineStepOrchestrator)
│   └── usecase/                 <-- Casos de uso concretos (*UseCase.java)
│
└── infrastructure/              <-- Capa de Infraestructura (Quarkus, MongoDB, HTTP)
    ├── adapters/
    │   ├── in/restcontrollers/  <-- Controladores REST (*Controller.java)
    │   ├── mappers/             <-- Mapeadores Entity <-> Model <-> DTO
    │   └── out/repositories/   <-- Implementaciones de puertos de salida (*Adapter.java)
    ├── clients/                 <-- Clientes HTTP MicroProfile RestClient
    ├── config/                  <-- Filtros, Interceptores (ej. CORS, Auth)
    ├── dtos/                    <-- Records/DTOs de transferencia de red (JSON)
    ├── exceptions/              <-- ExceptionMappers de Jakarta/Quarkus
    ├── mongo_db/                <-- Documentos MongoDB (PanacheMongoRepository)
    └── session/                 <-- Contexto de sesión de usuario
```

---

## 4. Reglas Arquitectónicas Estrictas (Enforced by ArchUnit)

Cualquier cambio de código realizado por un agente **DEBE** respetar las reglas validadas en `src/test/java/archtest/ArchitectureLayerTest.java`:

| Regla | Descripción |
|---|---|
| **Aislamiento de Dominio** | `com.biopatternsg.domain..` **NO** puede depender de `application..` ni de `infrastructure..`. |
| **Aislamiento de Aplicación** | `com.biopatternsg.application..` **NO** puede depender de `infrastructure..`. |
| **Encapsulamiento de Aplicación (layerApplicationTest)** | com.biopatternsg.application.. solo puede ser accedida por clases de la misma capa Application (la comunicación desde Infrastructure se realiza exclusivamente mediante interfaces en domain). |
| **Acceso a Casos de Uso** | Las implementaciones en `application.usecase..` no deben instanciarse directamente entre capas; se interactúa a través de sus puertos `domain.port.in`. |
| **Nomenclatura UseCase** | Toda clase en `application.usecase..` debe terminar en `UseCase` (ej. `CreatePipelineUseCase`). |
| **Nomenclatura Controller** | Toda clase en `infrastructure.adapters.in.restcontrollers..` debe terminar en `Controller` (ej. `PipelineController`). |
| **Nomenclatura Adapter** | Toda clase en `infrastructure.adapters.out..` debe terminar en `Adapter` (ej. `PipelineRepositoryAdapter`). |

---

## 5. Casos de Uso Principales

Ubicación: `com.biopatternsg.application.usecase.*` (Documentación detallada con diagramas de flujo disponible en [`.agents/use_cases/`](use_cases/)):

| Caso de Uso | Puerto In | Responsabilidad | Documento |
|---|---|---|---|
| `CreatePipelineUseCase` | `CreatePipeline` | Valida y crea una configuración de pipeline. | [`CreatePipelineUseCase.md`](use_cases/CreatePipelineUseCase.md) |
| `UpdatePipelineUseCase` | `UpdatePipeline` | Modifica parámetros, niveles y objetos de un pipeline. | [`UpdatePipelineUseCase.md`](use_cases/UpdatePipelineUseCase.md) |
| `UpdatePipelineStepUseCase` | `UpdatePipelineStep` | Actualiza estado de un paso y orquesta la siguiente fase. | [`UpdatePipelineStepUseCase.md`](use_cases/UpdatePipelineStepUseCase.md) |
| `FindPipelineUseCase` | `FindPipeline` | Consulta y filtra pipelines por usuario o red. | [`FindPipelineUseCase.md`](use_cases/FindPipelineUseCase.md) |
| `LaunchPipelineUseCase` | `LaunchPipeline` | Orquesta el inicio de un pipeline disparando servicios externos. | [`LaunchPipelineUseCase.md`](use_cases/LaunchPipelineUseCase.md) |
| `GetPipelineExecutionUseCase` | `GetPipelineExecution` | Reconstruye estado consolidado, duraciones y métricas. | [`GetPipelineExecutionUseCase.md`](use_cases/GetPipelineExecutionUseCase.md) |
| `CreateNetworkUseCase` | `CreateNetwork` | Registra una nueva red biológica en base de datos. | [`CreateNetworkUseCase.md`](use_cases/CreateNetworkUseCase.md) |
| `UpdateNetworkUseCase` | `UpdateNetwork` | Modifica atributos o estado de una red biológica. | [`UpdateNetworkUseCase.md`](use_cases/UpdateNetworkUseCase.md) |
| `FindNetworkUseCase` | `FindNetwork` | Búsqueda y filtrado de redes por usuario. | [`FindNetworkUseCase.md`](use_cases/FindNetworkUseCase.md) |
| `UserAuthenticationUseCase` | `UserAuthentication` | Login y refresh token interactuando con Keycloak. | [`UserAuthenticationUseCase.md`](use_cases/UserAuthenticationUseCase.md) |
| `UserManagementUseCase` | `UserManagement` | Registro, roles y administración de usuarios. | [`UserManagementUseCase.md`](use_cases/UserManagementUseCase.md) |
| `ResumePipelineUseCase` | `ResumePipeline` | Reanuda pipelines en progreso tras reinicios o interrupciones de servicios. | [`ResumePipelineUseCase.md`](use_cases/ResumePipelineUseCase.md) |

---

## 6. Persistencia y Almacenamiento

- **Motor:** MongoDB.
- **Acceso a Datos:** Quarkus MongoDB con Panache (`PanacheMongoRepositoryBase`).
- **Colecciones Principales (`infrastructure/mongo_db/collections/`):**
  - `NetworkCollection`: Almacena redes biológicas.
  - `PipelineConfigCollection`: Almacena las especificaciones de pipelines.
  - `PipelineExecutionCollection`: Almacena el historial y estado de ejecuciones.
  - `UserCollection`: Información complementaria local del usuario.

---

## 7. Integraciones Externas (MicroProfile REST Clients)

Configuradas en `application.properties`:

- **Keycloak (`KC_URL`):** Autenticación y tokens OIDC.
- **`biological-objects` (`biological-object-api`):** Consulta metadatos biológicos.
- **`pubmed-integration` (`pubmed-integration-api`):** Disparo de búsquedas y minería en literatura médica.
- **`inferences` (`inferences-api`):** Servicio de cálculo e inferencias estadísticas sobre grafos.

---

## 8. Guía de Intervención y Estándares de Código

Para el conjunto completo de reglas mandatorias de codificación, clean code y estándares de diseño, consulte el documento:
👉 [**`.agents/rules.md`**](rules.md)

### Resumen Rápido para Nuevas Funcionalidades:
1. **Nuevo Caso de Uso:**
   1. Crear la interfaz del puerto de entrada en `domain/port/in/MiOperacion.java`.
   2. Implementar la clase en `application/usecase/MiOperacionUseCase.java` anotada con `@ApplicationScoped`.
   3. Documentar el nuevo caso de uso con su diagrama Mermaid en `.agents/use_cases/MiOperacionUseCase.md`.
   4. Exponer el endpoint en el controlador respectivo (`infrastructure/adapters/in/restcontrollers/`) inyectando únicamente la interfaz `MiOperacion`.
2. **Nueva Integración / Repositorio:**
   1. Crear la interfaz del puerto de salida en `domain/port/out/repositories/`.
   2. Implementar el adaptador en `infrastructure/adapters/out/repositories/MiAdaptadorAdapter.java`.
3. **Verificación Obligatoria:**
   - Ejecutar `./mvnw test -Dtest=ArchitectureLayerTest` para comprobar que las reglas de arquitectura no se hayan violado.
   - Ejecutar `./mvnw clean compile` para asegurar compilación exitosa.
