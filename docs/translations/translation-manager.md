# Translation Manager

TranslationManager provides multi-language support for your plugin.

## Creating Translations

Extend `TranslationManager`:

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

## Using Translations

```java
TranslationManager translations = new MyTranslations(plugin);

// Get translation
String message = translations.string(player, MyTranslations.WELCOME);

// With variables
String message = translations.string(player, MyTranslations.WELCOME, "player", player.getName());

// List translation
List<String> lines = translations.list(player, MyTranslations.MULTILINE);
```

## PlaceholderAPI Support

Translations automatically support PlaceholderAPI placeholders:

```java
TranslationEntry MESSAGE = create("message", "&aHello %player_name%!");
```

## See Also

- [Translations](../translations.md) - Overview
- [Configuration](../configuration.md) - Store translation files

