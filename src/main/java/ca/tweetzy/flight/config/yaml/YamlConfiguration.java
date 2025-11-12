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

import ca.tweetzy.flight.config.HeaderCommentable;
import ca.tweetzy.flight.config.IConfiguration;
import ca.tweetzy.flight.config.NodeCommentable;
import ca.tweetzy.flight.config.ValueConverter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The original author of this code is SpraxDev, the original is from SongodaCore,
 * the following code below, may not reflect the original version.
 */
public class YamlConfiguration implements IConfiguration, HeaderCommentable, NodeCommentable {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    protected final @NotNull Yaml yaml;
    protected final @NotNull DumperOptions yamlDumperOptions;
    protected final @NotNull YamlCommentRepresenter yamlCommentRepresenter;

    protected final @NotNull Map<String, Object> values;
    protected final @NotNull Map<String, Supplier<String>> nodeComments;
    protected @Nullable Supplier<String> headerComment;
    
    // Key path cache to avoid repeated split() calls
    private final @NotNull Map<String, String[]> keyPathCache = new ConcurrentHashMap<>();
    
    // Value converters for type conversion
    private final @NotNull List<ValueConverter> valueConverters;

    public YamlConfiguration() {
        this(new LinkedHashMap<>(DEFAULT_INITIAL_CAPACITY), new LinkedHashMap<>(DEFAULT_INITIAL_CAPACITY));
    }

    protected YamlConfiguration(@NotNull Map<String, Object> values, @NotNull Map<String, Supplier<String>> nodeComments) {
        this.values = Objects.requireNonNull(values);
        this.nodeComments = Objects.requireNonNull(nodeComments);
        this.valueConverters = new ArrayList<>(YamlValueConverters.createDefaultConverters());

        this.yamlDumperOptions = createDefaultYamlDumperOptions();
        this.yamlCommentRepresenter = new YamlCommentRepresenter(this.yamlDumperOptions, this.nodeComments);
        this.yaml = createDefaultYaml(this.yamlDumperOptions, this.yamlCommentRepresenter);
    }
    
    /**
     * Registers a custom value converter
     *
     * @param converter The converter to register
     */
    public void registerConverter(@NotNull ValueConverter converter) {
        this.valueConverters.add(Objects.requireNonNull(converter));
    }
    
    /**
     * Gets or computes the key path array for a given key
     *
     * @param key The configuration key
     * @return The split key path array
     */
    @NotNull
    protected String[] getKeyPath(@NotNull String key) {
        return this.keyPathCache.computeIfAbsent(key, k -> k.split("\\."));
    }

    @Override
    @Contract(pure = true, value = "null -> false")
    public boolean has(String key) {
        if (key == null) {
            return false;
        }

        String[] fullKeyPath = getKeyPath(key);
        String[] parentPath = Arrays.copyOf(fullKeyPath, fullKeyPath.length - 1);

        synchronized (this.values) {
            Map<String, ?> innerMap = getInnerMap(this.values, parentPath, false);

            if (innerMap != null) {
                return innerMap.containsKey(fullKeyPath[fullKeyPath.length - 1]);
            }
        }

        return false;
    }

    @Override
    @Contract(pure = true, value = "null -> null")
    public @Nullable Object get(String key) {
        if (key == null) {
            return null;
        }

        synchronized (this.values) {
            try {
                return getInnerValueForKey(this.values, key);
            } catch (IllegalArgumentException ignore) {
            }
        }

        return null;
    }

    @Override
    @Contract(pure = true, value = "null,_ -> param2")
    public @Nullable Object getOr(String key, @Nullable Object fallbackValue) {
        Object value = get(key);

        return value == null ? fallbackValue : value;
    }

    public @NotNull Set<String> getKeys(String key) {
        if (key == null) {
            return Collections.emptySet();
        }

        if (key.equals("")) {
            synchronized (this.values) {
                return Collections.unmodifiableSet(this.values.keySet());
            }
        }

        Map<String, ?> innerMap = null;

        synchronized (this.values) {
            try {
                innerMap = getInnerMap(this.values, getKeyPath(key), false);
            } catch (IllegalArgumentException ignore) {
            }
        }

        if (innerMap != null) {
            return Collections.unmodifiableSet(innerMap.keySet());
        }

        return Collections.emptySet();

    }

    @Override
    public Object set(@NotNull String key, @Nullable Object value) {
        if (value != null) {
            // Try each converter until one handles the value
            for (ValueConverter converter : this.valueConverters) {
                Object converted = converter.convert(value);
                if (converted != null) {
                    value = converted;
                    break;
                }
            }
        }

        synchronized (this.values) {
            return setInnerValueForKey(this.values, key, value);
        }
    }

    @Override
    public Object unset(String key) {
        if (key == null) {
            return null;
        }
        
        String[] fullKeyPath = getKeyPath(key);
        String[] parentPath = Arrays.copyOf(fullKeyPath, fullKeyPath.length - 1);

        synchronized (this.values) {
            Map<String, ?> innerMap = getInnerMap(this.values, parentPath, false);

            if (innerMap != null) {
                Object removed = ((Map<String, Object>) innerMap).remove(fullKeyPath[fullKeyPath.length - 1]);
                // Clear cache entry if key was removed
                if (removed != null) {
                    this.keyPathCache.remove(key);
                }
                return removed;
            }
        }

        return null;
    }

    @Override
    public void reset() {
        synchronized (this.values) {
            this.values.clear();
        }
        this.keyPathCache.clear();
    }

    @Override
    public void load(Reader reader) throws IOException {
        Object yamlData = this.yaml.load(reader);
        if (yamlData == null) {
            yamlData = Collections.emptyMap();
        }

        if (!(yamlData instanceof Map)) {
            throw new IllegalStateException("The YAML file does not have the expected tree structure: " + yamlData.getClass().getName());
        }

        synchronized (this.values) {
            this.values.clear();

            for (Map.Entry<?, ?> yamlEntry : ((Map<?, ?>) yamlData).entrySet()) {
                this.values.put(yamlEntry.getKey().toString(), yamlEntry.getValue());
            }
        }
        
        // Clear key path cache since keys may have changed
        this.keyPathCache.clear();
    }

    @Override
    public void save(Writer writer) throws IOException {
        String headerCommentLines = generateHeaderCommentLines();
        writer.write(headerCommentLines);

        Map<String, Object> valuesCopy;
        synchronized (this.values) {
            valuesCopy = new LinkedHashMap<>(this.values);
            cleanValuesMap(valuesCopy);
        }

        if (valuesCopy.size() > 0) {
            if (headerCommentLines.length() > 0) {
                writer.write(this.yamlDumperOptions.getLineBreak().getString());
            }

            this.yaml.dump(valuesCopy, writer);
        }
    }

    @Override
    public void setHeaderComment(@Nullable Supplier<String> comment) {
        this.headerComment = comment;
    }

    @Override
    public @Nullable Supplier<String> getHeaderComment() {
        return this.headerComment;
    }

    @Override
    public @NotNull String generateHeaderCommentLines() {
        StringBuilder sb = new StringBuilder();

        String headerCommentString = this.headerComment == null ? null : this.headerComment.get();
        if (headerCommentString != null) {
            for (String commentLine : headerCommentString.split("\r?\n")) {
                sb.append("# ")
                        .append(commentLine)
                        .append(this.yamlDumperOptions.getLineBreak().getString());
            }
        }

        return sb.toString();
    }

    @Override
    public void setNodeComment(@NotNull String key, @Nullable Supplier<String> comment) {
        this.nodeComments.put(key, comment);
    }

    @Override
    public @Nullable Supplier<String> getNodeComment(@Nullable String key) {
        return this.nodeComments.get(key);
    }

    public String toYamlString() throws IOException {
        StringWriter writer = new StringWriter();
        save(writer);

        return writer.toString();
    }

    @Override
    public String toString() {
        return "YamlConfiguration{" +
                "values=" + this.values +
                ", headerComment=" + this.headerComment +
                '}';
    }

    protected static DumperOptions createDefaultYamlDumperOptions() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setProcessComments(true);
        dumperOptions.setAllowUnicode(true);

        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndentWithIndicator(true);
        dumperOptions.setIndicatorIndent(2);

        return dumperOptions;
    }

    protected static Yaml createDefaultYaml(DumperOptions dumperOptions, Representer representer) {
        LoaderOptions yamlOptions = new LoaderOptions();
        yamlOptions.setAllowDuplicateKeys(false);

        return new Yaml(new Constructor(yamlOptions), representer, dumperOptions, yamlOptions);
    }

    protected Object setInnerValueForKey(@NotNull Map<String, Object> map, @NotNull String key, @Nullable Object value) {
        String[] fullKeyPath = getKeyPath(key);
        String[] parentPath = Arrays.copyOf(fullKeyPath, fullKeyPath.length - 1);

        Map<String, ?> innerMap = getInnerMap(map, parentPath, true);

        return ((Map<String, Object>) innerMap).put(fullKeyPath[fullKeyPath.length - 1], value);
    }

    protected Object getInnerValueForKey(@NotNull Map<String, Object> map, @NotNull String key) {
        String[] fullKeyPath = getKeyPath(key);
        String[] parentPath = Arrays.copyOf(fullKeyPath, fullKeyPath.length - 1);

        Map<String, ?> innerMap = getInnerMap(map, parentPath, false);

        if (innerMap != null) {
            return innerMap.get(fullKeyPath[fullKeyPath.length - 1]);
        }

        return null;
    }

    @Contract("_,_,true -> !null")
    protected static Map<String, ?> getInnerMap(@NotNull Map<String, ?> map, @NotNull String[] keys, boolean createMissingMaps) {
        if (keys.length == 0) {
            return map;
        }

        int currentKeyIndex = 0;
        Map<String, ?> currentMap = map;

        while (true) {
            Object currentValue = currentMap.get(keys[currentKeyIndex]);

            if (currentValue == null) {
                if (!createMissingMaps) {
                    return null;
                }

                currentValue = new HashMap<>();
                ((Map<String, Object>) currentMap).put(keys[currentKeyIndex], currentValue);
            }

            if (!(currentValue instanceof Map)) {
                if (!createMissingMaps) {
                    throw new IllegalArgumentException("Expected a Map when resolving key '" + String.join(".", keys) + "' at '" + String.join(".", Arrays.copyOf(keys, currentKeyIndex + 1)) + "'");
                }

                currentValue = new HashMap<>();
                ((Map<String, Object>) currentMap).put(keys[currentKeyIndex], currentValue);
            }

            if (currentKeyIndex == keys.length - 1) {
                return (Map<String, ?>) currentValue;
            }

            currentMap = (Map<String, ?>) currentValue;
            ++currentKeyIndex;
        }
    }

    /**
     * This takes a map and removes all keys that have a value of null.<br>
     * Additionally, if the value is a {@link Map}, it will be recursively cleaned too.<br>
     * {@link Map}s that are or get empty, will be removed (recursively).<br>
     */
    protected void cleanValuesMap(Map<?, ?> map) {
        for (Object key : map.keySet().toArray()) {
            Object value = map.get(key);

            if (value instanceof Map) {
                cleanValuesMap((Map<?, ?>) value);
            }

            if (value == null || (value instanceof Map && ((Map<?, ?>) value).isEmpty())) {
                map.remove(key);
            }
        }
    }
}
