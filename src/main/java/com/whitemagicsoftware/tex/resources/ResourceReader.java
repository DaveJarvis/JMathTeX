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

import com.whitemagicsoftware.tex.exceptions.XMLResourceParseException;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;

/**
 * Offers common behaviours for various resource readers.
 *
 * @param <T> The type of resource to read.
 */
public class ResourceReader<T> {
  private final Path mPath;

  public ResourceReader( final Path path ) {
    mPath = path;
  }

  public T read( final Function<InputStream, T> f )
      throws XMLResourceParseException {
    try( final var stream = getResourceAsStream( mPath ) ) {
      return f.apply( stream );
    } catch( final Exception e ) {
      throw new XMLResourceParseException( mPath.toString(), e );
    }
  }

  /**
   * Returns the path to the resource.
   *
   * @return The path to a resource, starting at the root level of the
   * container.
   */
  public String getAbsolutePath() throws URISyntaxException {
    final var url = getClass().getClassLoader().getResource( mPath.toString() );
    final var uri = Objects.requireNonNull( url ).toURI();
    final var file = Paths.get( uri ).getParent().toFile();
    return file.getAbsolutePath();
  }

  /**
   * Opens the given path as a resource.
   *
   * @param path The path to a resource, starting at the root level of the
   *             container.
   * @return An open {@link InputStream} instance that must be closed after
   * reading the contents.
   */
  protected static InputStream getResourceAsStream( final Path path ) {
    final Path root = Path.of( File.separator, path.toString() );
    return ResourceReader.class.getResourceAsStream( root.toString() );
  }
}
