# Database Getting Started

Learn how to set up and use Flight's database system.

## Database Connectors

Flight supports two database types:

### SQLite (Default)

SQLite is file-based and requires no setup:

```java
DatabaseConnector connector = new SQLiteConnector(plugin, "database.db");
```

### MySQL

MySQL requires a server connection:

```java
DatabaseConnector connector = new MySQLConnector(
    plugin,
    "localhost",    // Host
    3306,           // Port
    "database",     // Database name
    "username",     // Username
    "password"      // Password
);
```

## Initializing the Connector

```java
public class MyPlugin extends FlightPlugin {
    private DatabaseConnector connector;
    
    @Override
    protected void onFlight() {
        // Initialize connector
        connector = new SQLiteConnector(this, "data.db");
        
        // Use connector...
    }
    
    @Override
    protected void onSleep() {
        // Close connection
        if (connector != null) {
            connector.closeConnection();
        }
    }
}
```

## Basic Usage

### Executing Queries

```java
connector.connect(connection -> {
    // Use connection
    PreparedStatement stmt = connection.prepareStatement("SELECT * FROM players");
    ResultSet rs = stmt.executeQuery();
    // Process results
});
```

### Connection Management

The `connect()` method automatically:
- Opens a connection
- Executes your callback
- Closes the connection
- Handles errors

## See Also

- [Repositories](repositories.md) - Repository pattern
- [Entity Mapping](entity-mapping.md) - Entity annotations

