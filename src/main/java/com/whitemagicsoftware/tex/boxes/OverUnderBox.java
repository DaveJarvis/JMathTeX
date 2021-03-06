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

package com.whitemagicsoftware.tex.boxes;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * A box representing another box with a delimiter box and a script box above
 * or under it, with script and delimiter separated by a kern.
 */
public final class OverUnderBox extends Box {

   // base, delimiter and script atom
   private final Box base;
   private final Box del;
   private final Box script;

   // kern amount between the delimiter and the script
   private final float kern;

   // whether the delimiter should be drawn over (<-> under) the base atom 
   private final boolean over;

   /**
    * the parameter boxes must have an equal width!!
    * 
    * @param b
    *           base box to be drawn on the baseline
    * @param d
    *           delimiter box
    * @param script
    *           subscript or superscript box
    * @param over
    *           true : draws delimiter and script box above the base box, false : under the
    *           base box
    */
   public OverUnderBox(Box b, Box d, Box script, float kern, boolean over) {
      base = b;
      del = d;
      this.script = script;
      this.kern = kern;
      this.over = over;

      // calculate metrics of the box
      width = b.getWidth();
      height = b.height
            + (over ? d.getWidth() : 0)
            + (over && script != null ? script.height + script.depth + kern : 0);
      depth = b.depth
            + (over ? 0 : d.getWidth())
            + (!over && script == null ? 0 : script.height + script.depth + kern);
   }

   public void draw( Graphics2D g, float x, float y ) {
      base.draw( g, x, y);

      float yVar = y - base.height;
      if (over) { // draw delimiter and script above base box
         double transX = x + del.getWidth() / 2, transY = yVar - del.getWidth()
               / 2;
         AffineTransform oldAt = g.getTransform();
         g.translate( transX, transY);
         g.rotate( Math.PI / 2);
         del.draw( g, -del.getWidth() / 2, -del.depth + del.getWidth() / 2);
         g.setTransform( oldAt);
         yVar -= del.getWidth();

         // draw superscript
         if (script != null)
            script.draw( g, x, yVar - kern - script.depth);
      }

      yVar = y + base.depth;
      if (!over) { // draw delimiter and script under base box
         yVar += del.getWidth();
         double transX = x + del.getWidth() / 2, transY = yVar - del.getWidth()
               / 2;
         AffineTransform oldAt = g.getTransform();
         g.translate( transX, transY);
         g.rotate( Math.PI / 2);
         del.draw( g, -del.getWidth() / 2, -del.depth + del.getWidth() / 2);
         g.setTransform( oldAt);

         // draw subscript
         if (script != null)
            script.draw( g, x, yVar + kern + script.height);
      }

   }

   public int getLastFontId() {
      return base.getLastFontId();
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "{" +
          ", kern=" + kern +
          ", over=" + over +
          '}';
   }
}
