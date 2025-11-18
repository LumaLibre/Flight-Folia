# Entity Mapping

Flight automatically maps Java objects to database tables using annotations.

## Entity Annotations

```java
@Table(name = "players")
public class PlayerData {
    @Id
    private UUID id;
    
    @Column
    private String name;
    
    @Column
    private int balance;
    
    @Ignore
    private transient String tempData; // Not saved to database
    
    // Getters and setters
}
```

## Annotations

- `@Table` - Table name
- `@Id` - Primary key
- `@Column` - Column mapping
- `@Ignore` - Ignore field
- `@Nested` - Nested object

## See Also

- [Repositories](repositories.md) - Using repositories
- [Getting Started](getting-started.md) - Database setup

