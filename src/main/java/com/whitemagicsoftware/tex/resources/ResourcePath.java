/*
 * JMathTeX is a Java library for rendering mathematical notation.
 * Copyright 2020 White Magic Software, Ltd.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package com.whitemagicsoftware.tex.resources;

/**
 * Facilitates joining paths to reference resource files. Resource paths
 * differ from paths in Java's nio library. Cross-platform slashes are to
 * be avoided because slashes for resource files are always forward ones.
 */
public class ResourcePath {
  public static final String SEPARATOR = "/";

  /**
   * Join the given names with the resource separator char {@link #SEPARATOR}.
   * The names can contain all directory names or any number of directory names
   * followed by a terminal a file name. This method does not enforce or check
   * that the directory names (or file name) exist.
   *
   * @param names The paths to join with the separator used for resources.
   * @return The given set of names concatenated with {@link #SEPARATOR} in
   * between.
   */
  public static String join( final String... names ) {
    return String.join( SEPARATOR, names );
  }

  private ResourcePath() {
  }
}
