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
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Offers common behaviours for various resource readers.
 *
 * @param <T> The type of resource to read.
 */
public class ResourceReader<T> {
  private final Path mPath;

  public ResourceReader( final Path path ) {
    mPath = Path.of( File.separator, path.toString() );
  }

  public T read( final Function<InputStream, T> f )
      throws XMLResourceParseException {
    final var filename = mPath.toString();
    try( final var stream = getClass().getResourceAsStream( filename ) ) {
      return f.apply( stream );
    } catch( final Exception e ) {
      throw new XMLResourceParseException( filename, e );
    }
  }
}
