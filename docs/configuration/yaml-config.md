# YAML Configuration

Flight provides a YAML configuration system with comment preservation and type-safe access.

## Creating a Config

```java
TweetzyYamlConfig config = new TweetzyYamlConfig(plugin, "config.yml");

// Initialize (loads and saves)
config.init();

// Or manually
config.load();
config.save();
```

## Accessing Values

```java
// Get values
String value = config.getString("path.to.value", "default");
int number = config.getInt("path.to.number", 0);
boolean bool = config.getBoolean("path.to.bool", false);
List<String> list = config.getStringList("path.to.list");

// Set values
config.set("path.to.value", "new value");
config.save();
```

## Comments

Comments are preserved when saving:

```yaml
# This is a comment
setting: value
```

## See Also

- [Config Entries](config-entries.md) - Type-safe config access
- [Configuration](../configuration.md) - Overview

