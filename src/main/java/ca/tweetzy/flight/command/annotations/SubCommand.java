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
 * Annotation to mark a method as a subcommand handler
 * 
 * Usage:
 * <pre>
 * {@code
 * @SubCommand(parent = "example", name = "give", executor = AllowedExecutor.PLAYER)
 * public void giveSubCommand(CommandContext context) {
 *     // Subcommand logic
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {
    
    /**
     * The parent command name (required)
     */
    String parent();
    
    /**
     * The subcommand name (required)
     */
    String name();
    
    /**
     * Subcommand aliases (optional)
     */
    String[] aliases() default {};
    
    /**
     * Who can execute this subcommand
     */
    AllowedExecutor executor() default AllowedExecutor.BOTH;
    
    /**
     * Permission node (null = no permission required)
     */
    String permission() default "";
    
    /**
     * Subcommand description
     */
    String description() default "";
    
    /**
     * Subcommand syntax (for help messages)
     */
    String syntax() default "";
    
    /**
     * Whether this subcommand should run asynchronously
     */
    boolean async() default false;
}

