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
import java.util.HashMap;
import java.util.Map;

import static java.awt.Font.PLAIN;

/**
 * Contains all the font information for 1 font.
 */
public class FontInfo {

  /**
   * Maximum number of character codes in a TeX font.
   */
  public static final int NUMBER_OF_CHAR_CODES = 256;

  /**
   * The font to use when all other fonts go out.
   */
  private static final Font DEFAULT_FONT = new Font( null, PLAIN, 0 );

  /**
   * Contains a map of all fonts to font information.
   */
  private final static Map<Integer, Font> fonts = new HashMap<>();

  public static Font getFont( final int fontId ) {
    return fonts.getOrDefault( fontId, DEFAULT_FONT );
  }

  private final int fontId;
  private final Font font;

  private final float[][] metrics = new float[ NUMBER_OF_CHAR_CODES ][];
  private final Map<CharTuple, Character> lig = new HashMap<>();
  private final Map<CharTuple, Float> kern = new HashMap<>();
  private final CharFont[] nextLarger = new CharFont[ NUMBER_OF_CHAR_CODES ];
  private final int[][] extensions = new int[ NUMBER_OF_CHAR_CODES ][];

  /**
   * Skew character of the font (used for positioning accents).
   */
  private char skewChar = (char) -1;

  private final float xHeight;
  private final float space;
  private final float quad;

  public FontInfo(
      int fontId, Font font, float xHeight, float space, float quad ) {
    this.fontId = fontId;
    this.font = font;
    this.xHeight = xHeight;
    this.space = space;
    this.quad = quad;

    putFontInfo( fontId, this );
  }

  /**
   * @param left  left character
   * @param right right character
   * @param k     kern value
   */
  public void addKern( char left, char right, float k ) {
    kern.put( new CharTuple( left, right ), k );
  }

  /**
   * @param left    left character
   * @param right   right character
   * @param ligChar ligature to replace left and right character
   */
  public void addLigature( char left, char right, char ligChar ) {
    lig.put( new CharTuple( left, right ), ligChar );
  }

  public int[] getExtension( char ch ) {
    return extensions[ ch ];
  }

  public float getKern( char left, char right, float factor ) {
    Float obj = kern.get( new CharTuple( left, right ) );
    if( obj == null ) {
      return 0;
    }

    return obj * factor;
  }

  public CharFont getLigature( char left, char right ) {
    Character obj = lig.get( new CharTuple( left, right ) );
    if( obj == null ) {
      return null;
    }

    return new CharFont( obj, fontId );
  }

  public float[] getMetrics( char c ) {
    return metrics[ c ];
  }

  public CharFont getNextLarger( char ch ) {
    return nextLarger[ ch ];
  }

  public float getQuad( float factor ) {
    return quad * factor;
  }

  /**
   * @return the skew character of the font (for the correct positioning of
   * accents)
   */
  public char getSkewChar() {
    return skewChar;
  }

  public float getSpace( float factor ) {
    return space * factor;
  }

  public float getXHeight( float factor ) {
    return xHeight * factor;
  }

  public boolean hasSpace() {
    return space > TeXFormula.PREC;
  }

  public void setExtension( char ch, int[] ext ) {
    extensions[ ch ] = ext;
  }

  public void setMetrics( char c, float[] arr ) {
    metrics[ c ] = arr;
  }

  public void setNextLarger( char ch, char larger, int fontId ) {
    nextLarger[ ch ] = new CharFont( larger, fontId );
  }

  public void setSkewChar( char c ) {
    skewChar = c;
  }

  public Font getFont() {
    return font;
  }

  private static void putFontInfo(
      final int fontId, final FontInfo fontInfo ) {
    fonts.put( fontId, fontInfo.getFont() );
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "fontId=" + fontId +
        ", font=" + font +
        ", lig=" + lig +
        ", kern=" + kern +
        ", skewChar=" + skewChar +
        ", xHeight=" + xHeight +
        ", space=" + space +
        ", quad=" + quad +
        '}';
  }
}
