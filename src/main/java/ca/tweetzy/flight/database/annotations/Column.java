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

package ca.tweetzy.flight.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the column name and properties for a field
 * If not specified, the field name will be used as the column name (converted to snake_case)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * The column name
     */
    String value();
    
    /**
     * Whether the column can be null (default: true)
     */
    boolean nullable() default true;
    
    /**
     * Whether the column is unique (default: false)
     */
    boolean unique() default false;
    
    /**
     * Maximum length for VARCHAR/TEXT columns (0 = unlimited)
     */
    int length() default 0;
    
    /**
     * Explicit SQL type override (e.g., "TEXT", "BIGINT", "VARCHAR(255)")
     * If empty, type will be auto-detected from Java type
     */
    String sqlType() default "";
}

