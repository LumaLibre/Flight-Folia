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

package ca.tweetzy.flight.dependency;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Level;

/**
 * Loads Maven dependencies at runtime
 */
public class DependencyLoader {
    
    private final Plugin plugin;
    private final File libsFolder;
    
    public DependencyLoader(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.libsFolder = new File(plugin.getDataFolder(), "libs");
        
        if (!libsFolder.exists()) {
            libsFolder.mkdirs();
        }
    }
    
    /**
     * Load all dependencies
     */
    public void loadDependencies(@NotNull Set<Dependency> dependencies) {
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        
        for (Dependency dependency : dependencies) {
            if (!dependency.isAutoLoad()) {
                continue;
            }
            
            try {
                File jarFile = downloadDependency(dependency);
                if (jarFile != null && jarFile.exists()) {
                    addToClasspath(classLoader, jarFile.toURI().toURL());
                    plugin.getLogger().info("Loaded dependency: " + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load dependency: " + dependency.getGroupId() + ":" + dependency.getArtifactId(), e);
            }
        }
    }
    
    /**
     * Download a dependency from Maven repository
     */
    @NotNull
    private File downloadDependency(@NotNull Dependency dependency) throws IOException {
        String fileName = dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar";
        File jarFile = new File(libsFolder, fileName);
        
        // If already downloaded, return it
        if (jarFile.exists()) {
            return jarFile;
        }
        
        // Download from Maven repository
        String mavenPath = dependency.getMavenPath();
        String urlString = dependency.getRepository();
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        urlString += mavenPath;
        
        plugin.getLogger().info("Downloading dependency: " + urlString);
        
        try (InputStream in = new URL(urlString).openStream();
             FileOutputStream out = new FileOutputStream(jarFile)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
        return jarFile;
    }
    
    /**
     * Add a URL to the classpath
     * Works with both URLClassLoader (Java 8) and modern Java (9+)
     */
    private void addToClasspath(@NotNull ClassLoader classLoader, @NotNull URL url) {
        try {
            // Try URLClassLoader method first (Java 8)
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURLMethod.setAccessible(true);
                addURLMethod.invoke(urlClassLoader, url);
                return;
            }
            
            // For Java 9+, use the Instrumentation API or reflection on the classloader
            // Try to find and use the addURL method via reflection
            try {
                Method addURLMethod = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                addURLMethod.setAccessible(true);
                addURLMethod.invoke(classLoader, url);
                return;
            } catch (NoSuchMethodException ignored) {
                // Method doesn't exist, try alternative approach
            }
            
            // Alternative: Use Java 9+ ModuleLayer or Instrumentation
            // For now, try to use the system property approach
            String classPath = System.getProperty("java.class.path");
            System.setProperty("java.class.path", classPath + File.pathSeparator + new File(url.toURI()).getAbsolutePath());
            
            // Also try to add via reflection to the ucp (UnnamedModuleClassLoader) in Java 9+
            try {
                Object ucp = getField(classLoader, "ucp");
                if (ucp != null) {
                    Method addURL = ucp.getClass().getDeclaredMethod("addURL", URL.class);
                    addURL.setAccessible(true);
                    addURL.invoke(ucp, url);
                    return;
                }
            } catch (Exception ignored) {
                // Fall through
            }
            
            plugin.getLogger().warning("Could not add URL to classpath using any method: " + url);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add URL to classpath: " + url, e);
        }
    }
    
    /**
     * Get a field value via reflection
     */
    private Object getField(Object obj, String fieldName) {
        try {
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(obj);
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}

