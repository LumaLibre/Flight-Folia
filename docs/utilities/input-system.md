# Input System

Flight provides an input system for getting text input from players.

## Basic Usage

Extend the `Input` class:

```java
new Input(plugin, player) {
    @Override
    public boolean onInput(String input) {
        // Handle input
        player.sendMessage("You entered: " + input);
        return true; // true = close input, false = keep open
    }

    @Override
    public String getTitle() {
        return "Enter your name";
    }

    @Override
    public String getSubtitle() {
        return "";
    }

    @Override
    public String getActionBar() {
        return "Type in chat";
    }
};
```

## TitleInput

For simpler title-based input:

```java
new TitleInput(plugin, player) {
    @Override
    public boolean onInput(String input) {
        player.sendMessage("Input: " + input);
        return true;
    }

    @Override
    public String getTitle() {
        return "Enter text";
    }

    @Override
    public String getSubtitle() {
        return "Type in chat";
    }
};
```

## Input Session Lock

The input system uses session locks to prevent conflicts:

```java
// Check if player has active input
if (InputSessionLock.hasActiveInput(player)) {
    player.sendMessage("You already have an input session!");
    return;
}
```

## Complete Example

```java
public void openNameInput(Player player) {
    new TitleInput(plugin, player) {
        @Override
        public boolean onInput(String input) {
            if (input.isEmpty()) {
                player.sendMessage("§cName cannot be empty!");
                return false; // Keep input open
            }

            if (input.length() > 16) {
                player.sendMessage("§cName too long!");
                return false;
            }

            // Save name
            savePlayerName(player, input);
            player.sendMessage("§aName set to: " + input);
            return true; // Close input
        }

        @Override
        public String getTitle() {
            return "§aEnter your name";
        }

        @Override
        public String getSubtitle() {
            return "§7Type in chat (max 16 characters)";
        }
    };
}
```

## See Also

- [Sign GUI](../gui/sign-gui.md) - Multi-line input
- [Utilities](../utilities.md) - Other utilities
