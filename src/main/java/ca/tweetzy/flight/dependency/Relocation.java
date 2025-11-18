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

/**
 * Represents a package relocation for a dependency
 */
public class Relocation {
    
    private final String originalPackage;
    private final String relocatedPackage;
    
    public Relocation(@NotNull String originalPackage, @NotNull String relocatedPackage) {
        this.originalPackage = originalPackage;
        this.relocatedPackage = relocatedPackage;
    }
    
    @NotNull
    public String getOriginalPackage() {
        return originalPackage;
    }
    
    @NotNull
    public String getRelocatedPackage() {
        return relocatedPackage;
    }
}

