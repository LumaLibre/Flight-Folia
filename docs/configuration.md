# Configuration System

Flight provides a powerful configuration system for managing YAML configuration files with comments, type-safe access, and automatic saving.

## Overview

The Flight configuration system offers:

- **YAML Configuration** - Easy-to-read YAML files
- **Comments** - Preserve comments in config files
- **Type-Safe Access** - ConfigEntry system for type safety
- **Automatic Saving** - Save changes automatically

## Quick Start

### Creating a Config

```java
TweetzyYamlConfig config = new TweetzyYamlConfig(plugin, "config.yml");

// Initialize (loads and saves)
config.init();

// Or manually
config.load();
config.save();
```

### Accessing Values

```java
// Get value
String value = config.getString("path.to.value", "default");

// Set value
config.set("path.to.value", "new value");
config.save();
```

### Using ConfigEntry

```java
ConfigEntry<String> message = new ConfigEntry<>(config, "messages.welcome", "Welcome!");
String welcome = message.get();
message.set("New welcome message");
```

## Documentation Sections

### [YAML Config](configuration/yaml-config.md)
Working with YAML configuration files.

### [Config Entries](configuration/config-entries.md)
Type-safe config access with ConfigEntry.

## See Also

- [Commands](commands.md) - Store command settings
- [GUI System](gui.md) - Configurable GUIs

