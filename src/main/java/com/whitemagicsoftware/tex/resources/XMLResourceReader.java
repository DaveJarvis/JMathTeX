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
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.nio.file.Path;

/**
 * Responsible for reading XML resources located in the {@code config}
 * directory.
 */
public class XMLResourceReader extends ResourceReader<Element> {

  /**
   * Location under the {@code resources} directory where XML configuration
   * files are found.
   */
  private final static String DIR_XML = "config";

  public XMLResourceReader( final String filename ) {
    super( DIR_XML + '/' + filename );
  }

  /**
   * Reads an XML file into a document object model.
   *
   * @return A new {@link Element} instance loaded from an XML resource.
   */
  public Element read() throws XMLResourceParseException {
    return super.read( ( stream ) -> {
      try {
        return new SAXBuilder().build( stream ).getRootElement();
      } catch( final Exception e ) {
        throw new XMLResourceParseException( e.getMessage() );
      }
    } );
  }
}
