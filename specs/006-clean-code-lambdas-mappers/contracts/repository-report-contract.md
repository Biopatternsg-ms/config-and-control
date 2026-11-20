# Contract: Repository Adapters Report Transformation

**Components**:
- `NetworkRepositoryAdapter.findByFilters`
- `PipelineRepositoryAdapter.findByFilters`
- `UserRepositoryAdapter.list`

## Standard Contract

Outbound repository adapters that return paginated domain entities must delegate report conversions to their respective static mapper's `toDomainReport` method:

### 1. `NetworkRepositoryAdapter`
```java
@Override
public ReportFormat<NetworkConfig> findByFilters(NetworkConfig findNetwork, int page, int size) {
    var reportCollection = networkRepositoryDB.findByUserAndFilters(findNetwork, sessionUtil.getUserId(), page, size);
    return NetworkMapper.toDomainReport(reportCollection);
}
```

### 2. `PipelineRepositoryAdapter`
```java
@Override
public ReportFormat<PipelineConfig> findByFilters(PipelineConfig findPipeline, int page, int size) {
    var reportCollection = pipelineRepositoryDB.findByUserAndFilters(findPipeline, networkIdList(), page, size);
    return PipelineMapper.toDomainReport(reportCollection);
}
```

### 3. `UserRepositoryAdapter`
```java
@Override
public ReportFormat<UserConfig> list(UserConfig filters, int page, int size) {
    var reportCollection = userRepositoryDB.findByFilters(filters, page, size);
    return UserMapper.toDomainReport(reportCollection);
}
```

## Invariants
1. Repository adapters contain zero stream `.map(...)` pipelines for paginated reports.
2. The count (`reportCollection.count()`) is preserved intact.
3. Null safety guarantees that an empty report is returned if the database returns a null report or null collection list.
