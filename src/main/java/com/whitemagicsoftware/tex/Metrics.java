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

/**
 * Contains the metrics for 1 character.
 */
public class Metrics {

  /**
   * Character width.
   */
  private final float w;
  /**
   * Character height.
   */
  private final float h;
  /**
   * Character depth.
   */
  private final float d;
  /**
   * Metric correction for italics.
   */
  private final float i;
  /**
   * Font size.
   */
  private final float s;

  /**
   * @param w      Character width.
   * @param h      Character height.
   * @param d      Character depth.
   * @param i      Correction for italics.
   * @param s      Character font size.
   * @param factor Scaling factor applied to metrics (not size).
   */
  public Metrics( float w, float h, float d, float i, float s, float factor ) {
    this.w = w * factor;
    this.h = h * factor;
    this.d = d * factor;
    this.i = i * factor;
    this.s = s;
  }

  public float getWidth() {
    return w;
  }

  public float getHeight() {
    return h;
  }

  public float getDepth() {
    return d;
  }

  public float getItalic() {
    return i;
  }

  public float getSize() {
    return s;
  }
}
