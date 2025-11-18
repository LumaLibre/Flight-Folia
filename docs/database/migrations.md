# Database Migrations

Migrations allow you to version and update your database schema.

## Overview

Migrations help you:

- **Version schemas** - Track database versions
- **Update schemas** - Automatically update on plugin updates
- **Data migration** - Migrate data between versions

## Usage

```java
DataMigrationManager migrationManager = new DataMigrationManager(connector);

// Register migrations
migrationManager.addMigration(new Migration1());
migrationManager.addMigration(new Migration2());

// Run migrations
migrationManager.migrate();
```

## See Also

- [Database System](../database.md) - Overview
- [Repositories](repositories.md) - Repository pattern

