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

import static be.ugent.caagt.jmathtex.TeXConstants.*;
import static be.ugent.caagt.jmathtex.TeXFont.NO_FONT;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * A box composed of other boxes, put one above the other.
 */
public class VerticalBox extends Box {

  private float leftMostPos = Float.MAX_VALUE;
  private float rightMostPos = Float.MIN_VALUE; // NOPMD

  public VerticalBox() {
  }

  public VerticalBox( final Box b, final float rest, final int alignment ) {
    this();
    add( b );
    switch( alignment ) {
      case ALIGN_CENTER -> {
        final StrutBox s = new StrutBox( 0, rest / 2, 0, 0 );
        super.add( 0, s );
        height += rest / 2;
        depth += rest / 2;
        super.add( s );
      }
      case ALIGN_TOP -> {
        depth += rest;
        super.add( new StrutBox( 0, rest, 0, 0 ) );
      }
      case ALIGN_BOTTOM -> {
        height += rest;
        super.add( 0, new StrutBox( 0, rest, 0, 0 ) );
      }
    }
  }

  public final void add( final Box b ) {
    super.add( b );
    if( children.size() == 1 ) {
      height = b.height;
      depth = b.depth;
    }
    else {
      depth += b.height + b.depth;
    }
    recalculateWidth( b );
  }

  public void add( final int pos, final Box b ) {
    super.add( pos, b );
    if( pos == 0 ) {
      depth += b.depth + height;
      height = b.height;
    }
    else {
      depth += b.height + b.depth;
    }
    recalculateWidth( b );
  }

  private void recalculateWidth( final Box b ) {
    leftMostPos = min( leftMostPos, b.shift );
    rightMostPos = max( rightMostPos, b.shift + (b.width > 0 ? b.width : 0) );
    width = rightMostPos - leftMostPos;
  }

  public void draw( final Graphics2D g, final float x, final float y ) {
    float yPos = y - height;
    for( final Box b : children ) {
      yPos += b.getHeight();
      b.draw( g, x + b.getShift() - leftMostPos, yPos );
      yPos += b.getDepth();
    }
  }

  public int getSize() {
    return children.size();
  }

  /**
   * Iterates from the last child box (the lowest) to the first (the
   * highest) until a font id is found that's not equal to
   * {@link TeXFont#NO_FONT}.
   *
   * @return {@link TeXFont#NO_FONT} if there's no font ID in this
   * {@link Box}'s list of child instances.
   */
  @Override
  public int getLastFontId() {
    int fontId = NO_FONT;
    final var it = children.listIterator( children.size() );
    while( fontId == NO_FONT && it.hasPrevious() ) {
      fontId = it.previous().getLastFontId();
    }

    return fontId;
  }
}
