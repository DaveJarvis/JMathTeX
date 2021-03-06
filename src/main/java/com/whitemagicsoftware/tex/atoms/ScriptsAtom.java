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
import com.whitemagicsoftware.tex.boxes.*;

import static com.whitemagicsoftware.tex.boxes.Box.NO_FONT;
import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * An atom representing scripts to be attached to another atom.
 */
public class ScriptsAtom extends Atom {

   // TeX constant: what's the use???
   private final static SpaceAtom SCRIPT_SPACE = new SpaceAtom(
       TeXConstants.UNIT_POINT, 0.5f, 0, 0);

   private final Atom base;

   // subscript and superscript to be attached to the base (if not null)
   private final Atom subscript;
   private final Atom superscript;

   public ScriptsAtom(Atom base, Atom sub, Atom sup) {
      this.base = base;
      subscript = sub;
      superscript = sup;
   }

   public Box createBox( TeXEnvironment env) {
      final Box b = (base == null ? new StrutBox() : base.createBox( env ));
      if( subscript == null && superscript == null ) {
         return b;
      }

      final TeXFont tf = env.getTeXFont();
      final int style = env.getStyle();

      HorizontalBox hor = new HorizontalBox( b );

      int lastFontId = b.getLastFontId();
      // if no last font found (whitespace box), use default "mu font"
      if( lastFontId == NO_FONT ) {
         lastFontId = tf.getMuFontId();
      }

      final TeXEnvironment subStyle = env.subStyle();
      final TeXEnvironment supStyle = env.supStyle();

      // set delta and preliminary shift-up and shift-down values
      float delta = 0, shiftUp, shiftDown;

      // TODO: use polymorphism?
      if (base instanceof AccentedAtom) {
         // improve superscript position relative to the accent.
         Box box = ((AccentedAtom) base).base.createBox(env.crampStyle());
         shiftUp = box.getHeight() - tf.getSupDrop(supStyle.getStyle());
         shiftDown = box.getDepth() + tf.getSubDrop(subStyle.getStyle());
      } else if (base instanceof SymbolAtom
            && base.type == TeXConstants.TYPE_BIG_OPERATOR) { // single big operator symbol
         Char c = tf.getChar(((SymbolAtom) base).getName(), style);
         if (style < TeXConstants.STYLE_TEXT && tf.hasNextLarger(c)) // display
            // style
            c = tf.getNextLarger(c, style);
         Box x = new CharBox( c);

         x.setShift(-(x.getHeight() + x.getDepth()) / 2
               - env.getTeXFont().getAxisHeight(env.getStyle()));
         hor = new HorizontalBox(x);

         // include delta in width or not?
         delta = c.getItalic();
         if (delta > TeXFormula.PREC && subscript == null)
            hor.add(new StrutBox(delta));

         shiftUp = hor.getHeight() - tf.getSupDrop(supStyle.getStyle());
         shiftDown = hor.getDepth() + tf.getSubDrop(subStyle.getStyle());
      } else if (base instanceof CharSymbolAtom ) {
         shiftUp = shiftDown = 0;
         CharFont cf = ((CharSymbolAtom) base).getCharFont( tf );
         if (!((CharSymbolAtom) base).isMarkedAsTextSymbol()
               || !tf.hasSpace(cf.fontId))
            delta = tf.getChar(cf, style).getItalic();
         if (delta > TeXFormula.PREC && subscript == null) {
            hor.add(new StrutBox(delta));
            delta = 0;
         }
      } else {
         shiftUp = b.getHeight() - tf.getSupDrop(supStyle.getStyle());
         shiftDown = b.getDepth() + tf.getSubDrop(subStyle.getStyle());
      }

      if (superscript == null) { // only subscript
         final Box x = subscript.createBox(subStyle);
         // calculate and set shift amount
         x.setShift( max( max( shiftDown, tf.getSub1( style)), x
               .getHeight()
               - 4 * abs(tf.getXHeight(style, lastFontId)) / 5));

         hor.add(x);
         // add scriptspace (constant value!)
         hor.add(SCRIPT_SPACE.createBox(env));
      } else {
         final Box x = superscript.createBox(supStyle);
         final HorizontalBox sup = new HorizontalBox(x);
         // add scriptspace (constant value!)
         sup.add(SCRIPT_SPACE.createBox(env));

         // adjust shift-up
         final float p;

         if( style == TeXConstants.STYLE_DISPLAY ) {
            p = tf.getSup1( style );
         }
         else if( env.crampStyle().getStyle() == style ) {
            p = tf.getSup3( style );
         }
         else {
            p = tf.getSup2( style );
         }

         shiftUp = max( max( shiftUp, p ), x.getDepth()
             + abs( tf.getXHeight( style, lastFontId ) ) / 4 );

         if (subscript == null) { // only superscript
            sup.setShift(-shiftUp);
            hor.add(sup);
         } else { // both superscript and subscript
            Box y = subscript.createBox(subStyle);
            HorizontalBox sub = new HorizontalBox(y);
            // add scriptspace (constant value!)
            sub.add(SCRIPT_SPACE.createBox(env));
            // adjust shift-down
            shiftDown = max(shiftDown, tf.getSub2(style));
            // position both sub- and superscript
            float drt = tf.getDefaultRuleThickness(style);
            float interSpace = shiftUp - x.getDepth() + shiftDown
                  - y.getHeight(); // space between sub- en
            // superscript
            if (interSpace < 4 * drt) { // too small
               shiftUp += 4 * drt - interSpace;
               // set bottom superscript at least 4/5 of X-height
               // above
               // baseline
               float psi = 4 * abs(tf.getXHeight(style, lastFontId))
                     / 5 - (shiftUp - x.getDepth());

               if (psi > 0) {
                  shiftUp += psi;
                  shiftDown -= psi;
               }
            }
            // create total box

            VerticalBox vBox = new VerticalBox();
            sup.setShift(delta);
            vBox.add(sup);
            // recalculate interspace
            interSpace = shiftUp - x.getDepth() + shiftDown - y.getHeight();
            vBox.add(new StrutBox(0, interSpace, 0, 0));
            vBox.add(sub);
            vBox.setHeight(shiftUp + x.getHeight());
            vBox.setDepth(shiftDown + y.getDepth());
            hor.add(vBox);
         }
      }

      return hor;
   }

   public int getLeftType() {
      return base.getLeftType();
   }

   public int getRightType() {
      return base.getRightType();
   }
}
