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

package com.whitemagicsoftware.tex;

import java.awt.*;
import java.util.ListIterator;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Math.max;

/**
 * A box composed of a horizontal row of child boxes.
 */
public final class HorizontalBox extends Box {

  /**
   * Basic horizontal box.
   */
  public HorizontalBox() {
  }

  public HorizontalBox( final Box b ) {
    add( b );
  }

  public HorizontalBox( final Box b, final float w, final int alignment ) {
    final float rest = w - b.getWidth();
    switch( alignment ) {
      case TeXConstants.ALIGN_CENTER -> {
        final var s = new StrutBox( rest / 2 );
        add( s );
        add( b );
        add( s );
      }
      case TeXConstants.ALIGN_LEFT -> {
        add( b );
        add( new StrutBox( rest ) );
      }
      case TeXConstants.ALIGN_RIGHT -> {
        add( new StrutBox( rest ) );
        add( b );
      }
    }
  }

  public HorizontalBox( final Color fg, final Color bg ) {
    super( fg, bg );
  }

  public void draw( final Graphics2D g, final float x, final float y ) {
    startDraw( g, x, y );
    float xPos = x;
    for( final Box box : children ) {
      box.draw( g, xPos, y + box.getShift() );
      xPos += box.getWidth();
    }
    endDraw( g );
  }

  public final void add( final Box b ) {
    recalculate( b );
    super.add( b );
  }

  private void recalculate( final Box b ) {
    width += b.getWidth();
    height = max( children.size() == 0 ? NEGATIVE_INFINITY : height,
                  b.height - b.shift );
    depth = max( children.size() == 0 ? NEGATIVE_INFINITY : depth,
                 b.depth + b.shift );
  }

  /**
   * Iterates from the last child box (the lowest) to the first (the highest)
   * until a font id is found that's not equal to {@link Box#NO_FONT}.
   *
   * @return {@link Box#NO_FONT} if there's no font ID in this
   * {@link Box}'s list of child instances.
   */
  @Override
  public int getLastFontId() {
    int fontId = NO_FONT;

    final ListIterator<Box> it = children.listIterator( children.size() );

    while( fontId == NO_FONT && it.hasPrevious() ) {
      fontId = it.previous().getLastFontId();
    }

    return fontId;
  }
}
