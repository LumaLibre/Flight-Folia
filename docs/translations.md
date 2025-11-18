# Translation System

Flight provides a translation system for multi-language support in your plugins.

## Overview

The translation system allows you to:

- **Support Multiple Languages** - Easy language switching
- **Placeholder Support** - Use PlaceholderAPI placeholders
- **Variable Replacement** - Replace variables in translations
- **File-Based Translations** - Store translations in files

## Quick Start

### 1. Extend TranslationManager

```java
public class MyTranslations extends TranslationManager {
    public MyTranslations(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void registerLanguages() {
        registerLanguage(plugin, "en_us");
        registerLanguage(plugin, "es_es");
    }

    // Define translations
    public static TranslationEntry WELCOME = create("welcome", "&aWelcome to the server!");
    public static TranslationEntry GOODBYE = create("goodbye", "&cGoodbye!");
}
```

### 2. Use Translations

```java
TranslationManager translations = new MyTranslations(plugin);

// Get translation
String message = translations.string(player, MyTranslations.WELCOME);

// With variables
String message = translations.string(player, MyTranslations.WELCOME, "player", player.getName());
```

## Documentation Sections

### [Translation Manager](translations/translation-manager.md)

Managing translations and languages.

## See Also

- [Configuration](configuration.md) - Store translation files
- [Commands](commands.md) - Use translations in commands
