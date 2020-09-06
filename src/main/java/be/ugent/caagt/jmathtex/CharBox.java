/*
 * =========================================================================
 * This file is part of the JMathTeX Library - http://jmathtex.sourceforge.net
 *
 * Copyright (C) 2004-2007 Universiteit Gent
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 *
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 */

package be.ugent.caagt.jmathtex;

import java.awt.*;
import java.awt.geom.AffineTransform;

import static be.ugent.caagt.jmathtex.TeXFormula.FONT_SCALE_FACTOR;
import static be.ugent.caagt.jmathtex.TeXFormula.PREC;

/**
 * A box representing a single character.
 */
public class CharBox extends Box {

  /**
   * The font use when drawing the character.
   */
  private final int fontId;

  /**
   * The character to draw.
   */
  private final char textChar;

  /**
   * Create a new {@link CharBox} that will represent the character defined
   * by the given {@link Char} object.
   *
   * @param c a Char-object containing the character's font information.
   */
  public CharBox( Char c ) {
    width = c.getWidth();
    height = c.getHeight();
    depth = c.getDepth();
    fontId = c.getFontId();
    textChar = c.getChar();
  }

  public void draw( Graphics2D g2, float x, float y ) {
    AffineTransform at = g2.getTransform();
    g2.translate( x, y );

    if( Math.abs( 1 - FONT_SCALE_FACTOR ) > PREC ) {
      g2.scale( 1 / FONT_SCALE_FACTOR, 1 / FONT_SCALE_FACTOR );
    }

    final Font font = FontInfo.getFont( fontId );

    if( g2.getFont() != font ) {
      g2.setFont( font );
    }

    g2.drawChars( new char[]{textChar}, 0, 1, 0, 0 );
    g2.setTransform( at );
  }

  public int getLastFontId() {
    return fontId;
  }
}
