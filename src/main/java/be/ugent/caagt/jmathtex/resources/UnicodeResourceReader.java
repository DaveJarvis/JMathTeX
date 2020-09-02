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
