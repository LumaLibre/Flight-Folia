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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Maven dependency to be loaded at runtime
 */
public class Dependency {
    
    private final String repository;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final boolean autoLoad;
    private final Relocation relocation;
    
    public Dependency(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        this(repository, groupId, artifactId, version, true, null);
    }
    
    public Dependency(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version, boolean autoLoad) {
        this(repository, groupId, artifactId, version, autoLoad, null);
    }
    
    public Dependency(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version, boolean autoLoad, @Nullable Relocation relocation) {
        this.repository = repository;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.autoLoad = autoLoad;
        this.relocation = relocation;
    }
    
    @NotNull
    public String getRepository() {
        return repository;
    }
    
    @NotNull
    public String getGroupId() {
        return groupId;
    }
    
    @NotNull
    public String getArtifactId() {
        return artifactId;
    }
    
    @NotNull
    public String getVersion() {
        return version;
    }
    
    public boolean isAutoLoad() {
        return autoLoad;
    }
    
    @Nullable
    public Relocation getRelocation() {
        return relocation;
    }
    
    /**
     * Get the Maven path for this dependency
     */
    @NotNull
    public String getMavenPath() {
        return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Dependency that = (Dependency) o;
        
        if (!groupId.equals(that.groupId)) return false;
        if (!artifactId.equals(that.artifactId)) return false;
        return version.equals(that.version);
    }
    
    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}

