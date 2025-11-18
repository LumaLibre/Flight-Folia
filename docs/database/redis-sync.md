# Redis Sync

Redis synchronization allows you to synchronize data across multiple servers.

## Overview

Redis sync provides:

- **Cross-server sync** - Sync data between servers
- **Real-time updates** - Instant data synchronization
- **Event-based** - Sync on data changes

## Setup

```java
RedisSyncManager syncManager = new RedisSyncManager(
    plugin,
    "localhost",  // Redis host
    6379,        // Redis port
    "password"   // Redis password (optional)
);
```

## Usage

```java
// Register sync listener
syncManager.registerListener(event -> {
    // Handle sync event
});

// Publish sync event
syncManager.publishEvent(new DatabaseEvent(...));
```

## See Also

- [Database System](../database.md) - Overview
- [Repositories](repositories.md) - Repository pattern

