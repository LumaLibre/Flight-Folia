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

package ca.tweetzy.flight.command.annotations;

import ca.tweetzy.flight.command.AllowedExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a command handler
 * 
 * Usage:
 * <pre>
 * {@code
 * @Command(name = "example", aliases = {"ex", "test"}, executor = AllowedExecutor.PLAYER)
 * public void exampleCommand(CommandContext context) {
 *     // Command logic
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    
    /**
     * The command name (required)
     */
    String name();
    
    /**
     * Command aliases (optional)
     */
    String[] aliases() default {};
    
    /**
     * Who can execute this command
     */
    AllowedExecutor executor() default AllowedExecutor.BOTH;
    
    /**
     * Permission node (null = no permission required)
     */
    String permission() default "";
    
    /**
     * Command description
     */
    String description() default "";
    
    /**
     * Command syntax (for help messages)
     */
    String syntax() default "";
    
    /**
     * Whether this command should run asynchronously
     */
    boolean async() default false;
}

