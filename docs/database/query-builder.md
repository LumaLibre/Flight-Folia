# Query Builder

QueryBuilder provides type-safe query construction.

## Basic Usage

```java
QueryBuilder builder = new QueryBuilder(connector, "prefix_");

// Select query
SelectQuery select = builder.select("players")
    .where("name", "=", "PlayerName")
    .build();

// Insert query
InsertQuery insert = builder.insert("players")
    .set("name", "PlayerName")
    .set("balance", 100)
    .build();

// Update query
UpdateQuery update = builder.update("players")
    .set("balance", 200)
    .where("name", "=", "PlayerName")
    .build();

// Delete query
DeleteQuery delete = builder.delete("players")
    .where("name", "=", "PlayerName")
    .build();
```

## See Also

- [Repositories](repositories.md) - Repository pattern
- [Database System](../database.md) - Overview

