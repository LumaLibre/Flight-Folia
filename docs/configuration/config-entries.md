# Config Entries

ConfigEntry provides type-safe access to configuration values.

## Basic Usage

```java
ConfigEntry<String> message = new ConfigEntry<>(config, "messages.welcome", "Welcome!");

// Get value
String welcome = message.get();

// Set value
message.set("New welcome message");
config.save();
```

## Read-Only Entries

```java
ReadOnlyConfigEntry<String> message = new ReadOnlyConfigEntry<>(config, "messages.welcome", "Welcome!");
String value = message.get(); // Can't set
```

## Writeable Entries

```java
WriteableConfigEntry<String> message = new WriteableConfigEntry<>(config, "messages.welcome", "Welcome!");
message.set("New value");
config.save();
```

## See Also

- [YAML Config](yaml-config.md) - YAML configuration
- [Configuration](../configuration.md) - Overview

