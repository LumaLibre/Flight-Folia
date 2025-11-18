# Dependency Loading

Flight can load dependencies at runtime, automatically downloading and relocating them to prevent conflicts.

## Overview

Dependency loading allows you to:

- **Load dependencies at runtime** - No need to shade dependencies
- **Automatic relocation** - Prevent package conflicts
- **Optional dependencies** - Load only when needed

## Usage

Flight automatically loads common dependencies in `FlightPlugin.onLoad()`:

- SQLite JDBC
- Jedis (Redis)

## Custom Dependencies

Override `getOptionalDependencies()` in your plugin:

```java
@Override
protected Set<Dependency> getOptionalDependencies() {
    Set<Dependency> dependencies = new HashSet<>();

    dependencies.add(new Dependency(
        "https://repo1.maven.org/maven2",
        "com.example",
        "library",
        "1.0.0",
        true,
        new Relocation("com.example", "ca.tweetzy.flight.third_party.com.example")
    ));

    return dependencies;
}
```

## See Also

- [Database System](../database.md) - Database dependencies
- [Advanced Features](../advanced.md) - Other advanced features
