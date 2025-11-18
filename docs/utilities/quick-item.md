# QuickItem

QuickItem is a builder for creating ItemStacks easily and quickly.

## Basic Usage

```java
ItemStack item = QuickItem.of(Material.DIAMOND_SWORD)
    .name("&a&lLegendary Sword")
    .lore("&7This is a powerful sword!")
    .make();
```

## Methods

### Material

```java
QuickItem.of(Material.DIAMOND)
QuickItem.of(CompMaterial.DIAMOND) // Version-compatible
```

### Name and Lore

```java
QuickItem.of(Material.DIAMOND)
    .name("&aDiamond")
    .lore("&7Line 1", "&7Line 2")
    .make();
```

### Enchantments

```java
QuickItem.of(Material.DIAMOND_SWORD)
    .enchant(Enchantment.SHARPNESS, 5)
    .make();
```

### Item Flags

```java
QuickItem.of(Material.DIAMOND)
    .flag(ItemFlag.HIDE_ENCHANTS)
    .make();
```

### Glow

```java
QuickItem.of(Material.DIAMOND)
    .glow()
    .make();
```

### Amount

```java
QuickItem.of(Material.DIAMOND)
    .amount(64)
    .make();
```

### Custom Model Data

```java
QuickItem.of(Material.DIAMOND)
    .modelData(1001)
    .make();
```

### Player Head

```java
QuickItem.of(Material.PLAYER_HEAD)
    .skull(player)
    .make();
```

### Leather Armor Color

```java
QuickItem.of(Material.LEATHER_CHESTPLATE)
    .color(Color.RED)
    .make();
```

## Complete Example

```java
ItemStack item = QuickItem.of(Material.DIAMOND_SWORD)
    .name("&a&lLegendary Sword")
    .lore(
        "&7This is a powerful sword!",
        "",
        "&eDamage: &c+10",
        "&eDurability: &a100%"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .enchant(Enchantment.UNBREAKING, 3)
    .flag(ItemFlag.HIDE_ENCHANTS)
    .glow()
    .amount(1)
    .modelData(1001)
    .make();
```

## See Also

- [GUI System](../gui.md) - Using QuickItem in GUIs
- [Commands](../commands.md) - Using QuickItem in commands

