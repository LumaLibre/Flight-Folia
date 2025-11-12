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
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Update checker for SpigotMC and GitHub releases.
 * 
 * @author Kiran Hart
 */
public class UpdateChecker {
    
    private static final String SPIGOT_API_URL = "https://api.spigotmc.org/legacy/update.php?resource=";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/releases/latest";
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+");
    
    private final JavaPlugin plugin;
    private final String currentVersion;
    private final UpdateSource source;
    private final String resourceId;
    private String latestVersion;
    private String downloadUrl;
    private boolean updateAvailable = false;
    private boolean checked = false;
    
    public enum UpdateSource {
        SPIGOTMC,
        GITHUB,
        CUSTOM
    }
    
    /**
     * Create an update checker for SpigotMC
     * 
     * @param plugin The plugin instance
     * @param resourceId The SpigotMC resource ID
     */
    public UpdateChecker(@NonNull JavaPlugin plugin, int resourceId) {
        this(plugin, UpdateSource.SPIGOTMC, String.valueOf(resourceId));
    }
    
    /**
     * Create an update checker for GitHub
     * 
     * @param plugin The plugin instance
     * @param repository The GitHub repository (owner/repo)
     */
    public UpdateChecker(@NonNull JavaPlugin plugin, @NonNull String repository) {
        this(plugin, UpdateSource.GITHUB, repository);
    }
    
    /**
     * Create a custom update checker
     * 
     * @param plugin The plugin instance
     * @param source The update source
     * @param resourceId The resource identifier
     */
    public UpdateChecker(@NonNull JavaPlugin plugin, @NonNull UpdateSource source, @NonNull String resourceId) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.source = source;
        this.resourceId = resourceId;
    }
    
    /**
     * Check for updates asynchronously
     * 
     * @param callback Callback with latest version and current version
     */
    public void checkAsync(@Nullable Consumer<UpdateResult> callback) {
        CompletableFuture.supplyAsync(() -> {
            try {
                String version = fetchLatestVersion();
                if (version != null) {
                    this.latestVersion = version;
                    this.updateAvailable = isNewerVersion(version, currentVersion);
                    this.checked = true;
                    
                    if (callback != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            callback.accept(new UpdateResult(latestVersion, currentVersion, updateAvailable, downloadUrl));
                        });
                    }
                    
                    return new UpdateResult(latestVersion, currentVersion, updateAvailable, downloadUrl);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
            return null;
        });
    }
    
    /**
     * Check for updates synchronously (blocking)
     * 
     * @return UpdateResult or null if check failed
     */
    @Nullable
    public UpdateResult check() {
        try {
            String version = fetchLatestVersion();
            if (version != null) {
                this.latestVersion = version;
                this.updateAvailable = isNewerVersion(version, currentVersion);
                this.checked = true;
                return new UpdateResult(latestVersion, currentVersion, updateAvailable, downloadUrl);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Fetch the latest version from the update source
     */
    private String fetchLatestVersion() throws Exception {
        String urlString;
        
        switch (source) {
            case SPIGOTMC:
                urlString = SPIGOT_API_URL + resourceId;
                break;
            case GITHUB:
                urlString = String.format(GITHUB_API_URL, resourceId);
                break;
            default:
                return null;
        }
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", plugin.getName() + "/" + currentVersion);
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                String responseBody = response.toString();
                
                if (source == UpdateSource.GITHUB) {
                    // Parse JSON response from GitHub
                    Matcher versionMatcher = VERSION_PATTERN.matcher(responseBody);
                    if (versionMatcher.find()) {
                        String version = versionMatcher.group();
                        
                        // Extract download URL
                        int urlIndex = responseBody.indexOf("\"browser_download_url\"");
                        if (urlIndex != -1) {
                            int urlStart = responseBody.indexOf("\"", urlIndex + 22) + 1;
                            int urlEnd = responseBody.indexOf("\"", urlStart);
                            if (urlEnd != -1) {
                                this.downloadUrl = responseBody.substring(urlStart, urlEnd);
                            }
                        }
                        
                        return version;
                    }
                } else {
                    // SpigotMC returns plain text version
                    return responseBody.trim();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if a version is newer than another
     */
    private boolean isNewerVersion(@NonNull String newVersion, @NonNull String currentVersion) {
        try {
            String[] newParts = newVersion.split("\\.");
            String[] currentParts = currentVersion.split("\\.");
            
            int maxLength = Math.max(newParts.length, currentParts.length);
            
            for (int i = 0; i < maxLength; i++) {
                int newPart = i < newParts.length ? Integer.parseInt(newParts[i]) : 0;
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                
                if (newPart > currentPart) {
                    return true;
                } else if (newPart < currentPart) {
                    return false;
                }
            }
            
            return false;
        } catch (NumberFormatException e) {
            // If version parsing fails, compare as strings
            return newVersion.compareTo(currentVersion) > 0;
        }
    }
    
    /**
     * Check if an update is available
     * 
     * @return true if update is available
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
    
    /**
     * Get the latest version
     * 
     * @return Latest version or null if not checked yet
     */
    @Nullable
    public String getLatestVersion() {
        return latestVersion;
    }
    
    /**
     * Get the current version
     * 
     * @return Current version
     */
    @NonNull
    public String getCurrentVersion() {
        return currentVersion;
    }
    
    /**
     * Get the download URL
     * 
     * @return Download URL or null
     */
    @Nullable
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    /**
     * Check if update check has been performed
     * 
     * @return true if checked
     */
    public boolean hasChecked() {
        return checked;
    }
    
    /**
     * Update result container
     */
    public static class UpdateResult {
        private final String latestVersion;
        private final String currentVersion;
        private final boolean updateAvailable;
        private final String downloadUrl;
        
        public UpdateResult(@NonNull String latestVersion, @NonNull String currentVersion, 
                          boolean updateAvailable, @Nullable String downloadUrl) {
            this.latestVersion = latestVersion;
            this.currentVersion = currentVersion;
            this.updateAvailable = updateAvailable;
            this.downloadUrl = downloadUrl;
        }
        
        @NonNull
        public String getLatestVersion() {
            return latestVersion;
        }
        
        @NonNull
        public String getCurrentVersion() {
            return currentVersion;
        }
        
        public boolean isUpdateAvailable() {
            return updateAvailable;
        }
        
        @Nullable
        public String getDownloadUrl() {
            return downloadUrl;
        }
    }
}

