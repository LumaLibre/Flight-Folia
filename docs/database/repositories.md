# Repositories

Repositories provide a clean data access layer using the repository pattern.

## Creating a Repository

```java
public class PlayerRepository extends BaseRepository<PlayerData, UUID> {
    public PlayerRepository(DatabaseConnector connector) {
        super(connector, "prefix_", "players", new AnnotatedEntityMapper<>(PlayerData.class));
    }
}
```

## Repository Methods

```java
// Save
repository.save(entity, callback);

// Find by ID
repository.findFirst("id", id, callback);

// Find all
repository.findAll(callback);

// Delete
repository.delete(id, callback);
```

## See Also

- [Getting Started](getting-started.md) - Database setup
- [Entity Mapping](entity-mapping.md) - Entity annotations

