# Sign GUI

Sign GUI allows players to input multi-line text using Minecraft's sign interface. This is useful for text input that requires multiple lines.

## Basic Usage

Create a Sign GUI using the builder pattern:

```java
SignGUI.builder()
    .plugin(plugin)
    .player(player)
    .lines("", "", "", "") // Initial lines (empty for blank sign)
    .onComplete((player, lines) -> {
        // Handle input
        String line1 = lines[0];
        String line2 = lines[1];
        String line3 = lines[2];
        String line4 = lines[3];
        
        player.sendMessage("You entered: " + String.join(" ", lines));
        
        return SignGUI.Response.close(); // Close after input
    })
    .open();
```

## With Pre-filled Text

Pre-fill the sign with default text:

```java
SignGUI.builder()
    .plugin(plugin)
    .player(player)
    .lines("Enter", "your", "text", "here")
    .onComplete((player, lines) -> {
        // Process input
        return SignGUI.Response.close();
    })
    .open();
```

## Keeping Sign Open

Return `Response.keepOpen()` to keep the sign open after input:

```java
SignGUI.builder()
    .plugin(plugin)
    .player(player)
    .lines("", "", "", "")
    .onComplete((player, lines) -> {
        // Process input but keep sign open
        player.sendMessage("Input received, sign still open");
        return SignGUI.Response.keepOpen();
    })
    .open();
```

## Complete Example

```java
public void openNameInput(Player player) {
    SignGUI.builder()
        .plugin(plugin)
        .player(player)
        .lines("", "Enter your name", "", "")
        .onComplete((p, lines) -> {
            String name = String.join(" ", lines).trim();
            
            if (name.isEmpty()) {
                p.sendMessage("§cName cannot be empty!");
                return SignGUI.Response.keepOpen();
            }
            
            // Save name
            savePlayerName(p, name);
            p.sendMessage("§aName set to: " + name);
            
            return SignGUI.Response.close();
        })
        .open();
}
```

## Response Types

- `Response.close()` - Close the sign after input
- `Response.keepOpen()` - Keep the sign open for more input

## Notes

- Sign GUI works best on Minecraft 1.20+
- The sign is created temporarily and removed after use
- Each line can contain up to 15 characters (Minecraft limit)

## See Also

- [Input System](../../utilities/input-system.md) - Other input methods

