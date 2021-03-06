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
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract graphical representation of a formula, that can be painted. All characters, font
 * sizes, positions are fixed. Only special Glue boxes could possibly stretch or shrink.
 * A box has 3 dimensions (width, height and depth), can be composed of other child boxes
 * that can possibly be shifted (up, down, left or right). Child boxes can also be positioned
 * outside their parent's box (defined by it's dimensions).
 * <p>
 * Subclasses must implement the abstract {@link #draw(Graphics2D, float, float)} method
 * (that paints the box). <b> This implementation must start with calling the method
 * {@link #startDraw(Graphics2D, float, float)} and end with calling the method
 * {@link #endDraw(Graphics2D)} to set and restore the color's that must be used for
 * painting the box and to draw the background!</b> They must also implement the abstract
 * {@link #getLastFontId()} method (the last font
 * that will be used when this box will be painted).
 */
public abstract class Box {

   public static final int NO_FONT = -1;

   /**
    * The foreground color of the whole box. Child boxes can override this color.
    * If it's null and it has a parent box, the foreground color of the parent will
    * be used. If it has no parent, the foreground color of the component on which it
    * will be painted, will be used.
    */
   protected Color foreground;

   /**
    * The background color of the whole box. Child boxes can paint a background on top of
    * this background. If it's null, no background will be painted.
    */
   protected Color background;

   // used temporarily in startDraw and endDraw
   private Color prevColor;

   /**
    * The width of this box, i.e. the value that will be used for further
    * calculations.
    */
   protected float width;

   /**
    * The height of this box, i.e. the value that will be used for further
    * calculations.
    */
   protected float height;

   /**
    * The depth of this box, i.e. the value that will be used for further
    * calculations.
    */
   protected float depth;

   /**
    * The shift amount: the meaning depends on the particular kind of box
    * (up, down, left, right)
    */
   protected float shift;

   /**
    * List of child boxes
    */
   protected List<Box> children = new LinkedList<>();

   /**
    * Creates an empty box (no children) with all dimensions set to 0 and no
    * foreground and background color set (default values will be used: null)
    */
   protected Box() {
      this (null, null);
   }

   /**
    * Creates an empty box (no children) with all dimensions set to 0 and sets
    * the foreground and background color of the box.
    *
    * @param fg the foreground color
    * @param bg the background color
    */
   protected Box(final Color fg, final Color bg) {
      foreground = fg;
      background = bg;
   }

   /**
    * Inserts the given box at the end of the list of child boxes.
    *
    * @param b the box to be inserted
    */
   public void add(final Box b) {
      children.add(b);
   }

   /**
    * Inserts the given box at the given position in the list of child boxes.
    *
    * @param pos the position at which to insert the given box
    * @param b the box to be inserted
    */
   public void add(final int pos, final Box b) {
      children.add(pos, b);
   }

   /**
    * Get the width of this box.
    *
    * @return the width of this box
    */
   public float getWidth() {
      return width;
   }

   /**
    * Set the width for this box.
    *
    * @param w the width
    */
   public void setWidth( float w ) {
      width = w;
   }

   /**
    * Get the height of this box.
    *
    * @return the height of this box
    */
   public float getHeight() {
      return height;
   }

   /**
    * Get the depth of this box.
    *
    * @return the depth of this box
    */
   public float getDepth() {
      return depth;
   }

   /**
    * Get the shift amount for this box.
    *
    * @return the shift amount
    */
   public float getShift() {
      return shift;
   }

   /**
    * Set the depth for this box.
    *
    * @param d the depth
    */
   public void setDepth(final float d) {
      depth = d;
   }

   /**
    * Set the height for this box.
    *
    * @param h the height
    */
   public void setHeight(final float h) {
      height = h;
   }

   /**
    * Set the shift amount for this box.
    *
    * @param s the shift amount
    */
   public void setShift(final float s) {
      shift = s;
   }

   /**
    * Paints this box at the given coordinates using the given graphics
    * context. The default implementation does has no effect, draws nothing.
    *
    * @param g the graphics (2D) context to use for painting
    * @param x the x-coordinate
    * @param y the y-coordinate
    */
   public void draw( final Graphics2D g, final float x, final float y ) {
   }

   /**
    * Get the id of the font that will be used the last when this box will be
    * painted.
    *
    * @return {@link Box#NO_FONT} by default.
    */
   public int getLastFontId() {
      return NO_FONT;
   }

   /**
    * Stores the old color setting, draws the background of the box (if not null)
    * and sets the foreground color (if not null).
    *
    * @param g the graphics (2D) context
    * @param x the x-coordinate
    * @param y the y-coordinate
    */
   protected void startDraw(
       final Graphics2D g, final float x, final float y ) {
      prevColor = g.getColor();

      if( background != null ) {
         g.setColor( background );
         g.fill( new Rectangle2D.Float(
             x, y - height, getWidth(), getHeight() + getDepth() )
         );
      }

      g.setColor( foreground == null ? prevColor : foreground );
   }

   /**
    * Restores the previous color setting.
    *
    * @param g the graphics (2D) context
    */
   protected void endDraw(final Graphics2D g) {
      g.setColor(prevColor);
   }

   @Override
   public String toString() {
      return  getClass().getSimpleName() + "{" +
          ", width=" + width +
          ", height=" + height +
          ", depth=" + depth +
          ", shift=" + shift +
          '}';
   }
}
