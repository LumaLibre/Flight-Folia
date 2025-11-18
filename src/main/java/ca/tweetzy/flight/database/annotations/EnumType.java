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
 * Controls how enum values are stored in the database
 * Default is VARCHAR for portability across database systems
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumType {
    /**
     * How to store the enum value
     */
    EnumStorage value() default EnumStorage.VARCHAR;
    
    enum EnumStorage {
        /**
         * Store as VARCHAR (portable, works with all databases)
         * Values stored as Enum.name() (e.g., "ACTIVE", "INACTIVE")
         */
        VARCHAR,
        
        /**
         * Store as database-specific ENUM type (MySQL/MariaDB only)
         * More efficient but not portable
         */
        ENUM
    }
}

