# Common Utilities

Flight provides various utility classes for common tasks.

## Common

The `Common` class provides utility methods:

```java
// Colorize text
String colored = Common.colorize("&aHello &cWorld");

// Tell player
Common.tell(player, "Message");
Common.tellNoPrefix(player, "Message without prefix");

// Log
Common.log("Log message");
```

## LocationUtil

Location utilities:

```java
// Serialize location
String serialized = LocationUtil.serialize(location);

// Deserialize location
Location location = LocationUtil.deserialize(serialized);
```

## PlayerUtil

Player utilities:

```java
// Check if player is online
boolean online = PlayerUtil.isOnline(uuid);

// Get player name
String name = PlayerUtil.getName(uuid);
```

## TimeUtil

Time formatting:

```java
// Format seconds
String formatted = TimeUtil.formatTime(3600); // "1h"
```

## MathUtil

Math utilities:

```java
// Clamp value
int clamped = MathUtil.clamp(value, min, max);

// Random between
int random = MathUtil.randomBetween(min, max);
```

## See Also

- [Utilities](../utilities.md) - Overview
- [QuickItem](quick-item.md) - Item building
