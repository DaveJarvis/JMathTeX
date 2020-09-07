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

import static be.ugent.caagt.jmathtex.FontInfo.getFont;
import static be.ugent.caagt.jmathtex.TeXFormula.FONT_SCALE_FACTOR;
import static be.ugent.caagt.jmathtex.TeXFormula.PREC;
import static java.lang.Math.abs;

/**
 * A box representing a single character.
 *
 * <p>
 * <strong>Caution:</strong> This class is not thread-safe.
 * </p>>
 */
public final class CharBox extends Box {

  /**
   * The font to use when drawing the character, the character to draw,
   * and the size to draw it in.
   */
  private final Char c;

  /**
   * Create a new {@link CharBox} that will represent the character defined
   * by the given {@link Char} object.
   *
   * @param c a Char-object containing the character's font information.
   */
  public CharBox( final Char c ) {
    this.width = c.getWidth();
    this.height = c.getHeight();
    this.depth = c.getDepth();
    this.c = c;
  }

  public void draw( final Graphics2D g, final float x, final float y ) {
    final var at = g.getTransform();
    g.translate( x, y );

    final var size = c.getSize();

    if( abs( size - FONT_SCALE_FACTOR ) > PREC ) {
      g.scale( size / FONT_SCALE_FACTOR, size / FONT_SCALE_FACTOR );
    }

    g.setFont( getFont( c.getFontId() ) );
    g.drawChars( new char[]{c.getChar()}, 0, 1, 0, 0 );
    g.setTransform( at );
  }

  public int getLastFontId() {
    return c.getFontId();
  }
}
