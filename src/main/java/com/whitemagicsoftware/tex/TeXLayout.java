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
package com.whitemagicsoftware.tex;

import com.whitemagicsoftware.tex.boxes.Box;

import java.awt.*;

/**
 * Responsible for computing the preferred layout dimensions for an instance
 * of {@link Box}. This provides similar functionality to {@link TeXIcon},
 * but without {@link Insets} or painting ability. It is meant to help with
 * calculating final layout dimensions that can be used to draw the
 * {@link Box} using third-party libraries.
 * <p>
 * The dimensions returned for the width and height are in pixels, by default.
 * </p>
 */
public class TeXLayout {
  private static final float ROUND = 0.99f;

  /**
   * The generated {@link Box} width is a little off for some reason (possibly
   * italics?), so this enlarges the resulting dimension a little.
   */
  private static final float TWEAK_WIDTH = 0.18f;

  /**
   * The generated {@link Box} height is a little off for some reason (possibly
   * italics?), so this enlarges the resulting dimension a little.
   */
  private static final float TWEAK_HEIGHT = 0.18f;

  private final Box mBox;
  private final float mSize;
  private final Insets mInsets;

  public TeXLayout( final Box box, final float size ) {
    mBox = box;
    mSize = size;
    mInsets = new Insets( 5, 5, 5, 5 );
  }

  public void setInsets( final Insets insets ) {
    final float size = getSize();

    mInsets.top = (int) (insets.top + TWEAK_HEIGHT * size);
    mInsets.bottom = (int) (insets.bottom + TWEAK_HEIGHT * size);
    mInsets.left = (int) (insets.left + TWEAK_WIDTH * size);
    mInsets.right = (int) (insets.right + TWEAK_WIDTH * size);
  }

  /**
   * Returns the suggested X position for to (mostly) center-align the result.
   *
   * @return Recommended X location for drawing the {@link Box}.
   */
  public float getX() {
    return mInsets.left / getSize();
  }

  /**
   * Returns the suggested Y position for to (mostly) center-align the result.
   *
   * @return Recommended Y location for drawing the {@link Box}.
   */
  public float getY() {
    return mInsets.top / getSize() + getBox().getHeight();
  }

  /**
   * Get the total occupied space to render the {@link Box} width.
   *
   * @return The {@link Box} width multiplied by the size.
   */
  public int getWidth() {
    final var box = getBox();
    final var size = getSize();
    return (int) (box.getWidth() * size + ROUND + mInsets.left + mInsets.right);

    //return (int) (getBox().getWidth() * getSize() + ROUND + (TWEAK_WIDTH *
    // getSize()));
  }

  /**
   * Get the total occupied space to render the {@link Box} height.
   *
   * @return The {@link Box} height and depth multiplied by the size.
   */
  public int getHeight() {
    final var box = getBox();
    final var size = getSize();

    return (int) (box.getHeight() * size + ROUND + mInsets.top) +
        (int) (box.getDepth() * size + ROUND + mInsets.bottom);

    //return (int) ((box.getHeight() + box.getDepth()) * size + (2 * ROUND) +
    // (TWEAK_HEIGHT * size));
  }

  private Box getBox() {
    return mBox;
  }

  public float getSize() {
    return mSize;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "mBox=" + mBox +
        ", mSize=" + mSize +
        ", width=" + getWidth() +
        ", height=" + getHeight() +
        '}';
  }
}
