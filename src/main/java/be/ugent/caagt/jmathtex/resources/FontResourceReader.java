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

import java.awt.*;
import java.nio.file.Path;

import static be.ugent.caagt.jmathtex.TeXFormula.FONT_SCALE_FACTOR;
import static be.ugent.caagt.jmathtex.TeXFormula.PIXELS_PER_POINT;
import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.createFont;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;

/**
 * Responsible for reading font resources located in the {@code fonts}
 * directory.
 */
public class FontResourceReader extends ResourceReader<Font> {

  /**
   * Location under the {@code resources} directory where font files are found.
   */
  private static final String DIR_FONT = "fonts";

  private static final String DIR_FONT_COMPUTER_MODERN = "cm";

  public FontResourceReader( final String filename ) {
    super( Path.of( DIR_FONT, DIR_FONT_COMPUTER_MODERN, filename ) );
  }

  /**
   * Reads a true type font file into memory.
   *
   * @return A new {@link Font} instance loaded from a font resource.
   */
  public Font read() {
    return super.read( ( stream ) -> {
      try {
        final var font = createFont( TRUETYPE_FONT, stream )
            .deriveFont( PIXELS_PER_POINT * FONT_SCALE_FACTOR );
        getLocalGraphicsEnvironment().registerFont( font );

        return font;
      } catch( final Exception e ) {
        throw new XMLResourceParseException( e.getMessage() );
      }
    } );
  }
}
