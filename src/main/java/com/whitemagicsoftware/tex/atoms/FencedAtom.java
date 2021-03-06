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

package com.whitemagicsoftware.tex.atoms;

import com.whitemagicsoftware.tex.*;
import com.whitemagicsoftware.tex.boxes.Box;
import com.whitemagicsoftware.tex.boxes.HorizontalBox;

/**
 * An atom representing a base atom surrounded with delimiters that change their size
 * according to the height of the base.
 */
public class FencedAtom extends Atom {

   // parameters used in the TeX algorithm
   private static final int DELIMITER_FACTOR = 901;

   private static final float DELIMITER_SHORTFALL = 0.5f;

   // base atom
   private final Atom base;

   // delimiters
   private final SymbolAtom left; 
   private final SymbolAtom right;

   /**
    * Creates a new FencedAtom from the given base and delimiters
    * 
    * @param base the base to be surrounded with delimiters
    * @param l the left delimiter
    * @param r the right delimiter
    */
   public FencedAtom(Atom base, SymbolAtom l, SymbolAtom r) {
      if (base == null)
         this.base = new RowAtom(); // empty base
      else
         this.base = base;
      left = l;
      right = r;
   }

   public int getLeftType() {
      return TeXConstants.TYPE_OPENING;
   }

   public int getRightType() {
      return TeXConstants.TYPE_CLOSING;
   }

   /**
    * Centers the given box with resprect to the given axis, by setting an appropriate
    * shift value.
    * 
    * @param box
    *           box to be vertically centered with respect to the axis
    */
   private static void center( Box box, float axis) {
      float h = box.getHeight(), total = h + box.getDepth();
      box.setShift(-(total / 2 - h) - axis);
   }

   public Box createBox( TeXEnvironment env) {
      TeXFont tf = env.getTeXFont();

      Box content = base.createBox(env);
      float axis = axis = tf.getAxisHeight(env.getStyle()), delta = Math.max(
            content.getHeight() - axis, content.getDepth() + axis), minHeight = Math
            .max((delta / 500) * DELIMITER_FACTOR, 2 * delta
                  - DELIMITER_SHORTFALL);

      // construct box
      HorizontalBox hBox = new HorizontalBox();

      // left delimiter
      if (left != null) {
         Box b = DelimiterFactory.create(left.getName(), env, minHeight);
         center(b, axis);
         hBox.add(b);
      }

      // glue between left delimiter and content (if not whitespace)
      if (!(base instanceof SpaceAtom))
         hBox.add(Glue.get(TeXConstants.TYPE_OPENING, base.getLeftType(), env));

      // add content
      hBox.add(content);

      // glue between right delimiter and content (if not whitespace)
      if (!(base instanceof SpaceAtom))
         hBox
               .add(Glue.get(base.getRightType(), TeXConstants.TYPE_CLOSING,
                     env));

      // right delimiter
      if (right != null) {
         Box b = DelimiterFactory.create(right.getName(), env, minHeight);
         center(b, axis);
         hBox.add(b);
      }
      return hBox;
   }

}
