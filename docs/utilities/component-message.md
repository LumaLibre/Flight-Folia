# Component Message

ComponentMessage provides component-based message building for modern Minecraft versions.

## Basic Usage

```java
ComponentMessage message = new ComponentMessage("Hello, ")
    .append("World", NamedTextColor.GREEN)
    .append("!", NamedTextColor.WHITE);

message.send(player);
```

## See Also

- [Utilities](../utilities.md) - Other utilities
- [Commands](../commands.md) - Using components in commands
