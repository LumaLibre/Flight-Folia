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

package ca.tweetzy.flight.gui.config;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Safe expression evaluation engine for GUI configs.
 * Supports variables, placeholders, boolean logic, and basic math operations.
 * No code injection - only safe expression evaluation.
 */
public final class GuiConfigExpressionEngine {

    private static final java.util.regex.Pattern VARIABLE_PATTERN = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * Evaluate a boolean expression.
     * Supports: &&, ||, !, ==, !=, <, >, <=, >=
     * 
     * @param expression The expression to evaluate
     * @param context The context containing variables
     * @return The boolean result, or false if evaluation fails
     */
    public static boolean evaluateBoolean(@Nullable String expression, @NonNull GuiConfigContext context) {
        if (expression == null || expression.trim().isEmpty()) {
            return true; // Empty condition is considered true
        }

        try {
            String resolved = resolveVariables(expression, context);
            return evaluateBooleanExpression(resolved);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Resolve variables and placeholders in a string.
     * Replaces ${variable} with actual values from context.
     * 
     * @param input The input string with variables
     * @param context The context containing variables
     * @return The resolved string
     */
    @NonNull
    public static String resolveVariables(@Nullable String input, @NonNull GuiConfigContext context) {
        if (input == null) {
            return "";
        }

        String result = input;
        
        // Replace ${variable} patterns
        java.util.regex.Matcher matcher = VARIABLE_PATTERN.matcher(result);
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = getVariableValue(varName, context);
            String replacement = value != null ? value.toString() : "";
            result = result.replace(matcher.group(0), replacement);
        }
        
        return result;
    }

    /**
     * Get a variable value from context, supporting dot notation (e.g., "player.name")
     */
    @Nullable
    private static Object getVariableValue(@NonNull String varName, @NonNull GuiConfigContext context) {
        // Handle dot notation for nested properties
        if (varName.contains(".")) {
            String[] parts = varName.split("\\.", 2);
            Object base = context.getVariable(parts[0]);
            
            if (base == null) {
                return null;
            }
            
            // Try to access property via reflection (safe, read-only)
            return getPropertyValue(base, parts[1]);
        }
        
        return context.getVariable(varName);
    }

    /**
     * Safely get a property value from an object using reflection.
     * Only allows read access to public fields and getter methods.
     */
    @Nullable
    private static Object getPropertyValue(@NonNull Object obj, @NonNull String property) {
        try {
            // Try getter method first
            String getterName = "get" + capitalize(property);
            try {
                java.lang.reflect.Method method = obj.getClass().getMethod(getterName);
                return method.invoke(obj);
            } catch (NoSuchMethodException e) {
                // Try boolean getter
                getterName = "is" + capitalize(property);
                try {
                    java.lang.reflect.Method method = obj.getClass().getMethod(getterName);
                    return method.invoke(obj);
                } catch (NoSuchMethodException e2) {
                    // Try field access
                    try {
                        java.lang.reflect.Field field = obj.getClass().getField(property);
                        return field.get(obj);
                    } catch (NoSuchFieldException e3) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    private static String capitalize(@NonNull String str) {
        if (str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Evaluate a boolean expression string.
     * Supports: &&, ||, !, ==, !=, <, >, <=, >=
     */
    private static boolean evaluateBooleanExpression(@NonNull String expression) {
        expression = expression.trim();
        
        // Handle negation
        if (expression.startsWith("!")) {
            return !evaluateBooleanExpression(expression.substring(1).trim());
        }
        
        // Handle parentheses (simple support)
        if (expression.startsWith("(") && expression.endsWith(")")) {
            return evaluateBooleanExpression(expression.substring(1, expression.length() - 1));
        }
        
        // Handle && (AND)
        if (expression.contains("&&")) {
            String[] parts = splitByOperator(expression, "&&");
            boolean result = true;
            for (String part : parts) {
                result = result && evaluateBooleanExpression(part.trim());
            }
            return result;
        }
        
        // Handle || (OR)
        if (expression.contains("||")) {
            String[] parts = splitByOperator(expression, "||");
            boolean result = false;
            for (String part : parts) {
                result = result || evaluateBooleanExpression(part.trim());
            }
            return result;
        }
        
        // Handle comparison operators
        if (expression.contains("==")) {
            String[] parts = splitByOperator(expression, "==");
            if (parts.length == 2) {
                return compareValues(parts[0].trim(), parts[1].trim()) == 0;
            }
        }
        
        if (expression.contains("!=")) {
            String[] parts = splitByOperator(expression, "!=");
            if (parts.length == 2) {
                return compareValues(parts[0].trim(), parts[1].trim()) != 0;
            }
        }
        
        if (expression.contains("<=")) {
            String[] parts = splitByOperator(expression, "<=");
            if (parts.length == 2) {
                return compareValues(parts[0].trim(), parts[1].trim()) <= 0;
            }
        }
        
        if (expression.contains(">=")) {
            String[] parts = splitByOperator(expression, ">=");
            if (parts.length == 2) {
                return compareValues(parts[0].trim(), parts[1].trim()) >= 0;
            }
        }
        
        if (expression.contains("<")) {
            String[] parts = splitByOperator(expression, "<");
            if (parts.length == 2) {
                return compareValues(parts[0].trim(), parts[1].trim()) < 0;
            }
        }
        
        if (expression.contains(">")) {
            String[] parts = splitByOperator(expression, ">");
            if (parts.length == 2) {
                return compareValues(parts[0].trim(), parts[1].trim()) > 0;
            }
        }
        
        // Try to parse as boolean
        if ("true".equalsIgnoreCase(expression)) {
            return true;
        }
        if ("false".equalsIgnoreCase(expression)) {
            return false;
        }
        
        // Try to parse as number (non-zero is true)
        try {
            double num = Double.parseDouble(expression);
            return num != 0;
        } catch (NumberFormatException e) {
            // Not a number
        }
        
        // Default: non-empty string is true
        return !expression.isEmpty();
    }

    @NonNull
    private static String[] splitByOperator(@NonNull String expression, @NonNull String operator) {
        int index = expression.indexOf(operator);
        if (index == -1) {
            return new String[]{expression};
        }
        return new String[]{
            expression.substring(0, index),
            expression.substring(index + operator.length())
        };
    }

    private static int compareValues(@NonNull String left, @NonNull String right) {
        // Try numeric comparison first
        try {
            double leftNum = Double.parseDouble(left);
            double rightNum = Double.parseDouble(right);
            return Double.compare(leftNum, rightNum);
        } catch (NumberFormatException e) {
            // String comparison
            return left.compareTo(right);
        }
    }
}

