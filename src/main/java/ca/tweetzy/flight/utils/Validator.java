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

package ca.tweetzy.flight.utils;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validation framework for input validation.
 * 
 * @author Kiran Hart
 */
public class Validator {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private final Map<String, ValidationRule> rules = new HashMap<>();
    private final List<String> errors = new ArrayList<>();
    
    /**
     * Create a new validator
     */
    @NonNull
    public static Validator create() {
        return new Validator();
    }
    
    /**
     * Require a string field with length constraints
     * 
     * @param key The field key
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return This validator
     */
    @NonNull
    public Validator requireString(@NonNull String key, int minLength, int maxLength) {
        rules.put(key, new StringRule(minLength, maxLength));
        return this;
    }
    
    /**
     * Require an integer field with range constraints
     * 
     * @param key The field key
     * @param min Minimum value
     * @param max Maximum value
     * @return This validator
     */
    @NonNull
    public Validator requireInt(@NonNull String key, int min, int max) {
        rules.put(key, new IntRule(min, max));
        return this;
    }
    
    /**
     * Require a double field with range constraints
     * 
     * @param key The field key
     * @param min Minimum value
     * @param max Maximum value
     * @return This validator
     */
    @NonNull
    public Validator requireDouble(@NonNull String key, double min, double max) {
        rules.put(key, new DoubleRule(min, max));
        return this;
    }
    
    /**
     * Require an email field
     * 
     * @param key The field key
     * @return This validator
     */
    @NonNull
    public Validator requireEmail(@NonNull String key) {
        rules.put(key, new EmailRule());
        return this;
    }
    
    /**
     * Require a field to match a pattern
     * 
     * @param key The field key
     * @param pattern The regex pattern
     * @return This validator
     */
    @NonNull
    public Validator requirePattern(@NonNull String key, @NonNull Pattern pattern) {
        rules.put(key, new PatternRule(pattern));
        return this;
    }
    
    /**
     * Validate data
     * 
     * @param data The data to validate
     * @return true if valid
     */
    public boolean validate(@NonNull Map<String, Object> data) {
        errors.clear();
        
        for (Map.Entry<String, ValidationRule> entry : rules.entrySet()) {
            String key = entry.getKey();
            ValidationRule rule = entry.getValue();
            Object value = data.get(key);
            
            if (!rule.validate(value)) {
                errors.add(rule.getErrorMessage(key, value));
            }
        }
        
        return errors.isEmpty();
    }
    
    /**
     * Get validation errors
     * 
     * @return List of error messages
     */
    @NonNull
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Check if validation has errors
     * 
     * @return true if has errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Validation rule interface
     */
    private interface ValidationRule {
        boolean validate(Object value);
        String getErrorMessage(String key, Object value);
    }
    
    /**
     * String validation rule
     */
    private static class StringRule implements ValidationRule {
        private final int minLength;
        private final int maxLength;
        
        StringRule(int minLength, int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }
        
        @Override
        public boolean validate(Object value) {
            if (value == null) {
                return false;
            }
            String str = value.toString();
            return str.length() >= minLength && str.length() <= maxLength;
        }
        
        @Override
        public String getErrorMessage(String key, Object value) {
            if (value == null) {
                return key + " is required";
            }
            String str = value.toString();
            if (str.length() < minLength) {
                return key + " must be at least " + minLength + " characters";
            }
            if (str.length() > maxLength) {
                return key + " must be at most " + maxLength + " characters";
            }
            return key + " is invalid";
        }
    }
    
    /**
     * Integer validation rule
     */
    private static class IntRule implements ValidationRule {
        private final int min;
        private final int max;
        
        IntRule(int min, int max) {
            this.min = min;
            this.max = max;
        }
        
        @Override
        public boolean validate(Object value) {
            if (value == null) {
                return false;
            }
            try {
                int intValue = value instanceof Number 
                    ? ((Number) value).intValue() 
                    : Integer.parseInt(value.toString());
                return intValue >= min && intValue <= max;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        @Override
        public String getErrorMessage(String key, Object value) {
            if (value == null) {
                return key + " is required";
            }
            return key + " must be between " + min + " and " + max;
        }
    }
    
    /**
     * Double validation rule
     */
    private static class DoubleRule implements ValidationRule {
        private final double min;
        private final double max;
        
        DoubleRule(double min, double max) {
            this.min = min;
            this.max = max;
        }
        
        @Override
        public boolean validate(Object value) {
            if (value == null) {
                return false;
            }
            try {
                double doubleValue = value instanceof Number 
                    ? ((Number) value).doubleValue() 
                    : Double.parseDouble(value.toString());
                return doubleValue >= min && doubleValue <= max;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        @Override
        public String getErrorMessage(String key, Object value) {
            if (value == null) {
                return key + " is required";
            }
            return key + " must be between " + min + " and " + max;
        }
    }
    
    /**
     * Email validation rule
     */
    private static class EmailRule implements ValidationRule {
        @Override
        public boolean validate(Object value) {
            if (value == null) {
                return false;
            }
            return EMAIL_PATTERN.matcher(value.toString()).matches();
        }
        
        @Override
        public String getErrorMessage(String key, Object value) {
            if (value == null) {
                return key + " is required";
            }
            return key + " must be a valid email address";
        }
    }
    
    /**
     * Pattern validation rule
     */
    private static class PatternRule implements ValidationRule {
        private final Pattern pattern;
        
        PatternRule(Pattern pattern) {
            this.pattern = pattern;
        }
        
        @Override
        public boolean validate(Object value) {
            if (value == null) {
                return false;
            }
            return pattern.matcher(value.toString()).matches();
        }
        
        @Override
        public String getErrorMessage(String key, Object value) {
            if (value == null) {
                return key + " is required";
            }
            return key + " does not match the required pattern";
        }
    }
}

