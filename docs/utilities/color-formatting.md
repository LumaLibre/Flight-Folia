# Color Formatting

Flight provides utilities for color and gradient formatting in messages.

## Basic Colors

Use standard Minecraft color codes:

```java
String message = "&aGreen &cRed &bBlue";
Common.tell(player, message);
```

## Gradient Colors

Create gradient text:

```java
// Using Common utility
String gradient = Common.colorize("#FF0000Hello #00FF00World");
```

## Color Patterns

Flight supports various color patterns:

- **Solid Color** - Single color
- **Gradient** - Smooth color transition
- **Rainbow** - Animated rainbow effect

## See Also

- [Component Message](component-message.md) - Component-based messages
- [Utilities](../utilities.md) - Other utilities

