# Data Model & Architectural Boundaries: Domain Layer Purity and Framework Decoupling

## Overview

This document specifies the domain models, value objects, and boundary interfaces impacted by the decoupling of external framework dependencies and the elimination of the domain services package.

---

## 1. Core Domain Entities & Models

### 1.1. `UserConfig` (`com.biopatternsg.domain.models.UserConfig`)
- **Package**: `com.biopatternsg.domain.models`
- **Role**: Represents user configuration, authentication attributes, and profile data in the domain layer.
- **Key Attributes**:
  - `id`: `String` (Internal system identifier)
  - `username`: `String` (Email / Username)
  - `firstName`: `String` (Given name)
  - `lastName`: `String` (Family name)
  - `identityProviderId`: `String` (Assigned external identifier from Keycloak OIDC)
- **Lifecycle & Boundaries**:
  - `UserManagementUseCase` receives `UserConfig` without `identityProviderId`.
  - Invokes `KeycloakRepository.register(userConfig)`.
  - Attaches returned `identityProviderId` to `UserConfig`.
  - Persists `UserConfig` to MongoDB via `UserRepository.create(userConfig)`.

### 1.2. `PipelineConfig` & `PipelineStatus` (`com.biopatternsg.domain.models.*`)
- **Package**: `com.biopatternsg.domain.models`
- **Role**: Aggregate root representing biological pipeline execution configuration, current step, and status history.
- **Attributes**:
  - `id`: `String` (Pipeline execution ID)
  - `networkId`: `String` (Associated network ID)
  - `step`: `PipelineSteps` (Current active step)
  - `statuses`: `List<PipelineStatus>` (History of step execution states and metrics)

---

## 2. Port & Service Contracts

### 2.1. `KeycloakRepository` (Domain Outbound Port)
- **Package**: `com.biopatternsg.domain.port.out.repositories`
- **Signature Change**:
  ```java
  // BEFORE (Violates Hexagonal purity):
  jakarta.ws.rs.core.Response register(UserConfig newUser);

  // AFTER (Pure Domain Contract):
  String register(UserConfig newUser);
  ```
- **Constraints**:
  - Zero imports from `jakarta.ws.rs.*` or any external transport library.
  - Return value is the generated `identityProviderId` (String).
  - Throws `KeycloakServiceException` (or domain exceptions) on failures.

### 2.2. `PipelineService` (Application Service Interface)
- **Package Relocation**:
  ```java
  // BEFORE (Mismatched / Forbidden Package):
  package com.biopatternsg.domain.services;
  public interface PipelineService { ... }

  // AFTER (Compliant Application Service):
  package com.biopatternsg.application.services;
  public interface PipelineService { ... }
  ```
- **Methods**:
  - `void updateStep(String pipelineId, PipelineSteps step, Status status)`
  - `void updateStep(String pipelineId, PipelineSteps step, Status status, Map<String, String> metrics)`
- **Constraints**:
  - Package `com.biopatternsg.domain.services` is completely removed from the filesystem.

---

## 3. Layer Dependency Diagram

```mermaid
graph TD
    subgraph Infrastructure Layer
        KC_Adapter["KeycloakAdapter\n(adapters.out.repositories)"]
        KC_Client["KeycloakHttpClient\n(infrastructure.clients)"]
    end

    subgraph Application Layer
        User_UC["UserManagementUseCase\n(application.usecase)"]
        Pipe_Svc["PipelineService\n(application.services)"]
        Pipe_Impl["PipelineServiceImpl\n(application.services)"]
    end

    subgraph Domain Layer
        KC_Port["KeycloakRepository\n(domain.port.out.repositories)"]
        User_Model["UserConfig\n(domain.models)"]
        Pipe_Model["PipelineConfig\n(domain.models)"]
    end

    User_UC -->|Calls register -> returns String| KC_Port
    KC_Adapter -.->|Implements| KC_Port
    KC_Adapter -->|Parses HTTP Response & Location| KC_Client
    Pipe_Impl -.->|Implements| Pipe_Svc
    User_UC --> User_Model
    Pipe_Svc --> Pipe_Model

    classDef pureDomain fill:#d4edda,stroke:#28a745,stroke-width:2px;
    classDef appLayer fill:#d1ecf1,stroke:#17a2b8,stroke-width:2px;
    classDef infraLayer fill:#fff3cd,stroke:#ffc107,stroke-width:2px;

    class KC_Port,User_Model,Pipe_Model pureDomain;
    class User_UC,Pipe_Svc,Pipe_Impl appLayer;
    class KC_Adapter,KC_Client infraLayer;
```
