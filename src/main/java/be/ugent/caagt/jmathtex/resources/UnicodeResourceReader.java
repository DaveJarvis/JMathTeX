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

import java.nio.file.Path;
import java.util.Properties;

/**
 * Responsible for reading unicode property resources located in the {@code
 * property} directory.
 */
public class UnicodeResourceReader extends ResourceReader<Properties> {

  /**
   * Location under the {@code resources} directory where XML configuration
   * files are found.
   */
  private final static String DIR_PROPERTY = "property";

  public UnicodeResourceReader( final String filename ) {
    super( Path.of( DIR_PROPERTY, filename ) );
  }

  /**
   * Reads a set of key/value pairs into a {@link Properties} instance.
   *
   * @return A new {@link Properties} instance loaded from a properties file
   * resource.
   */
  public Properties read() throws XMLResourceParseException {
    return super.read( ( stream ) -> {
      try {
        final Properties p = new Properties();
        p.load( stream );
        return p;
      } catch( final Exception e ) {
        throw new XMLResourceParseException( e.getMessage() );
      }
    } );
  }
}
