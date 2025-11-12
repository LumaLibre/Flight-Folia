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

package ca.tweetzy.flight.config.yaml;

import ca.tweetzy.flight.config.ValueConverter;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default value converters for YAML configuration
 */
public final class YamlValueConverters {
    private YamlValueConverters() {
    }

    /**
     * Creates the default set of value converters
     */
    @NotNull
    public static List<ValueConverter> createDefaultConverters() {
        return Arrays.asList(
                // Float to Double
                value -> value instanceof Float ? ((Float) value).doubleValue() : null,
                
                // Character to String
                value -> value instanceof Character ? value.toString() : null,
                
                // Enum to String
                value -> value.getClass().isEnum() ? ((Enum<?>) value).name() : null,
                
                // Array converters
                new ArrayConverter()
        );
    }

    /**
     * Handles conversion of various array types to lists
     */
    private static class ArrayConverter implements ValueConverter {
        @Override
        @Nullable
        public Object convert(@NotNull Object value) {
            if (!value.getClass().isArray()) {
                return null;
            }

            // Primitive arrays
            if (value instanceof int[]) {
                return Arrays.asList(ArrayUtils.toObject((int[]) value));
            }
            if (value instanceof long[]) {
                return Arrays.asList(ArrayUtils.toObject((long[]) value));
            }
            if (value instanceof short[]) {
                List<Integer> list = new ArrayList<>(((short[]) value).length);
                for (short s : (short[]) value) {
                    list.add((int) s);
                }
                return list;
            }
            if (value instanceof byte[]) {
                List<Integer> list = new ArrayList<>(((byte[]) value).length);
                for (byte b : (byte[]) value) {
                    list.add((int) b);
                }
                return list;
            }
            if (value instanceof double[]) {
                return Arrays.asList(ArrayUtils.toObject((double[]) value));
            }
            if (value instanceof float[]) {
                List<Double> list = new ArrayList<>(((float[]) value).length);
                for (float f : (float[]) value) {
                    list.add((double) f);
                }
                return list;
            }
            if (value instanceof boolean[]) {
                return Arrays.asList(ArrayUtils.toObject((boolean[]) value));
            }
            if (value instanceof char[]) {
                List<String> list = new ArrayList<>(((char[]) value).length);
                for (char c : (char[]) value) {
                    list.add(String.valueOf(c));
                }
                return list;
            }

            // Object arrays
            return Arrays.asList((Object[]) value);
        }
    }
}

