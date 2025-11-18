# Database System

Flight provides a powerful database system with support for SQLite and MySQL, repository pattern, entity mapping, migrations, and Redis synchronization.

## Overview

The Flight database system offers:

- **Multiple Database Types** - SQLite (default) and MySQL support
- **Repository Pattern** - Clean data access layer
- **Entity Mapping** - Automatic mapping between Java objects and database tables
- **Migrations** - Database schema versioning and updates
- **Redis Sync** - Cross-server data synchronization
- **Query Builder** - Type-safe query construction

## Quick Start

### 1. Create a Database Connector

```java
// SQLite (default)
DatabaseConnector connector = new SQLiteConnector(plugin, "database.db");

// MySQL
DatabaseConnector connector = new MySQLConnector(
    plugin,
    "localhost",
    3306,
    "database",
    "username",
    "password"
);
```

### 2. Create an Entity

```java
@Table(name = "players")
public class PlayerData {
    @Id
    private UUID id;
    
    @Column
    private String name;
    
    @Column
    private int balance;
    
    // Getters and setters
}
```

### 3. Create a Repository

```java
public class PlayerRepository extends BaseRepository<PlayerData, UUID> {
    public PlayerRepository(DatabaseConnector connector) {
        super(connector, "prefix_", "players", new AnnotatedEntityMapper<>(PlayerData.class));
    }
}
```

### 4. Use the Repository

```java
PlayerRepository repository = new PlayerRepository(connector);

// Save
repository.save(playerData, null);

// Find by ID
repository.findFirst("id", id, data -> {
    // Handle result
});
```

## Documentation Sections

### [Getting Started](database/getting-started.md)
Learn how to set up databases and create connectors.

### [Repositories](database/repositories.md)
Use the repository pattern for data access.

### [Entity Mapping](database/entity-mapping.md)
Map Java objects to database tables using annotations.

### [Migrations](database/migrations.md)
Manage database schema changes with migrations.

### [Redis Sync](database/redis-sync.md)
Synchronize data across multiple servers.

### [Query Builder](database/query-builder.md)
Build type-safe queries.

## See Also

- [Configuration](configuration.md) - Store database settings
- [Advanced Features](advanced.md) - DataManager usage

