/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package be.ugent.caagt.jmathtex.resources;

import be.ugent.caagt.jmathtex.exceptions.XMLResourceParseException;

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
