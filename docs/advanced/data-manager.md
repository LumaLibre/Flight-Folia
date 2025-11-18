# Data Manager

DataManagerAbstract provides a foundation for managing data operations with async support and task queuing.

## Overview

DataManagerAbstract provides:

- **Async operations** - Non-blocking data operations
- **Task queuing** - Queue data operations
- **Shutdown handling** - Graceful shutdown

## Usage

Extend `DataManagerAbstract`:

```java
public class MyDataManager extends DataManagerAbstract {
    public MyDataManager(DatabaseConnector connector) {
        super(connector);
    }
    
    // Implement data operations
}
```

## Shutdown

FlightPlugin provides shutdown handling:

```java
@Override
protected void onSleep() {
    shutdownDataManager(dataManager);
}
```

## See Also

- [Database System](../database.md) - Database features
- [Advanced Features](../advanced.md) - Other advanced features

