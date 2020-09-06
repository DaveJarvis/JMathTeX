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

import be.ugent.caagt.jmathtex.exceptions.SymbolMappingNotFoundException;
import be.ugent.caagt.jmathtex.exceptions.TextStyleMappingNotFoundException;

/**
 * An interface representing a "TeXFont", which is responsible for all the
 * necessary fonts and font information.
 *
 * @author Kurt Vermeulen
 */
interface TeXFont {

  int NO_FONT = -1;

  FontInfo getFontInfo( int fontId );

  float getAxisHeight( int style );

  float getBigOpSpacing1( int style );

  float getBigOpSpacing2( int style );

  float getBigOpSpacing3( int style );

  float getBigOpSpacing4( int style );

  float getBigOpSpacing5( int style );

  /**
   * Get a Char-object specifying the given character in the given text
   * style with
   * metric information depending on the given "style".
   *
   * @param c         alphanumeric character
   * @param textStyle the text style in which the character should be drawn
   * @param style     the style in which the atom should be drawn
   * @return the Char-object specifying the given character in the given
   * text style
   * @throws TextStyleMappingNotFoundException if there's no text style
   * defined with
   *                                           the given name
   */
  Char getChar( char c, String textStyle, int style )
      throws TextStyleMappingNotFoundException;

  /**
   * Get a Char-object for this specific character containing the metric
   * information
   *
   * @param cf    CharFont-object determining a specific character of a
   *              specific font
   * @param style the style in which the atom should be drawn
   * @return the Char-object for this character containing metric information
   */
  Char getChar( CharFont cf, int style );

  /**
   * Get a Char-object for the given symbol with metric information
   * depending on
   * "style".
   *
   * @param name  the symbol name
   * @param style the style in which the atom should be drawn
   * @return a Char-object for this symbol with metric information
   * @throws SymbolMappingNotFoundException if there's no symbol defined with
   * the given
   *                                        name
   */
  Char getChar( String name, int style )
      throws SymbolMappingNotFoundException;

  /**
   * Get a Char-object specifying the given character in the default text
   * style with metric information depending on the given "style".
   *
   * @param c     alphanumeric character
   * @param style the style in which the atom should be drawn
   * @return the Char-object specifying the given character in the default
   * text style
   */
  Char getDefaultChar( char c, int style );

  float getDefaultRuleThickness( int style );

  float getDenom1( int style );

  float getDenom2( int style );

  /**
   * Get an Extension-object for the given Char containing the 4 possible
   * parts to
   * build an arbitrary large variant. This will only be called if
   * isExtensionChar(Char)
   * returns true.
   *
   * @param c     a Char-object for a specific character
   * @param style the style in which the atom should be drawn
   * @return an Extension object containing the 4 possible parts
   */
  Extension getExtension( Char c, int style );

  /**
   * Get the kern value to be inserted between the given characters in the
   * given style.
   *
   * @param left  left character
   * @param right right character
   * @param style the style in which the atom should be drawn
   * @return the kern value between both characters (default 0)
   */
  float getKern( CharFont left, CharFont right, int style );

  /**
   * Get the ligature that replaces both characters (if any).
   *
   * @param left  left character
   * @param right right character
   * @return a ligature replacing both characters (or null: no ligature)
   */
  CharFont getLigature( CharFont left, CharFont right );

  int getMuFontId();

  /**
   * Get the next larger version of the given character. This is only called if
   * hasNextLarger(Char) returns true.
   *
   * @param c     character
   * @param style the style in which the atom should be drawn
   * @return the next larger version of this character
   */
  Char getNextLarger( Char c, int style );

  float getNum1( int style );

  float getNum2( int style );

  float getNum3( int style );

  float getQuad( int style, int fontCode );

  /**
   * @return the point size of this TeXFont
   */
  float getSize();

  /**
   * Get the kern amount of the character defined by the given CharFont
   * followed by the
   * "skewchar" of it's font. This is used in the algorithm for placing an
   * accent above
   * a single character.
   *
   * @param cf    the character and it's font above which an accent has to be
   *             placed
   * @param style the render style
   * @return the kern amount of the character defined by cf followed by the
   * "skewchar" of it's font.
   */
  float getSkew( CharFont cf, int style );

  float getSpace( int style );

  float getSub1( int style );

  float getSub2( int style );

  float getSubDrop( int style );

  float getSup1( int style );

  float getSup2( int style );

  float getSup3( int style );

  float getSupDrop( int style );

  float getXHeight( int style, int fontCode );

  /**
   * @param c a character
   * @return true if the given character has a larger version, false otherwise
   */
  boolean hasNextLarger( Char c );

  boolean hasSpace( int font );

  /**
   * @param c a character
   * @return true if the given character contains extension information to buid
   * an arbitrary large version of this character.
   */
  boolean isExtensionChar( Char c );

}
