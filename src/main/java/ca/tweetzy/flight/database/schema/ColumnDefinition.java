/*
 * Flight
 * Copyright 2022 Kiran Hart
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.tweetzy.flight.database.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a database column definition
 */
public class ColumnDefinition {
    
    private final String name;
    private final String sqlType;
    private final boolean nullable;
    private final boolean unique;
    private final boolean primaryKey;
    private final String defaultValue;
    private final String deprecatedSince;
    private final String removalVersion;
    
    public ColumnDefinition(@NotNull String name, 
                           @NotNull String sqlType,
                           boolean nullable,
                           boolean unique,
                           boolean primaryKey,
                           @Nullable String defaultValue,
                           @Nullable String deprecatedSince,
                           @Nullable String removalVersion) {
        this.name = name;
        this.sqlType = sqlType;
        this.nullable = nullable;
        this.unique = unique;
        this.primaryKey = primaryKey;
        this.defaultValue = defaultValue;
        this.deprecatedSince = deprecatedSince;
        this.removalVersion = removalVersion;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSqlType() {
        return sqlType;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    public boolean isUnique() {
        return unique;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public String getDeprecatedSince() {
        return deprecatedSince;
    }
    
    public String getRemovalVersion() {
        return removalVersion;
    }
    
    /**
     * Check if this column should be removed based on plugin version
     */
    public boolean shouldBeRemoved(@NotNull String currentVersion) {
        if (removalVersion == null || removalVersion.isEmpty()) {
            return false;
        }
        return compareVersions(currentVersion, removalVersion) >= 0;
    }
    
    /**
     * Check if this column is deprecated based on plugin version
     */
    public boolean isDeprecated(@NotNull String currentVersion) {
        if (deprecatedSince == null || deprecatedSince.isEmpty()) {
            return false;
        }
        return compareVersions(currentVersion, deprecatedSince) >= 0;
    }
    
    /**
     * Compare two version strings (X.Y.Z format)
     * @return negative if v1 < v2, zero if equal, positive if v1 > v2
     */
    private int compareVersions(@NotNull String v1, @NotNull String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        int maxLength = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnDefinition that = (ColumnDefinition) o;
        return name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return "ColumnDefinition{" +
                "name='" + name + '\'' +
                ", sqlType='" + sqlType + '\'' +
                ", nullable=" + nullable +
                ", unique=" + unique +
                ", primaryKey=" + primaryKey +
                '}';
    }
}

