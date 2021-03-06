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

import com.whitemagicsoftware.tex.atoms.Atom;
import com.whitemagicsoftware.tex.atoms.CharSymbolAtom;
import com.whitemagicsoftware.tex.atoms.FixedCharAtom;
import com.whitemagicsoftware.tex.atoms.SpaceAtom;
import com.whitemagicsoftware.tex.boxes.Box;

/**
 * Used by RowAtom. The "textSymbol"-property and the type of an atom can change
 * (according to the TeX-algorithms used). Or this atom can be replaced by a
 * ligature, (if it was a CharAtom). But atoms cannot be changed, otherwise
 * different boxes could be made from the same TeXFormula, and that is not
 * desired! This "dummy atom" makes sure that changes to an atom (during the
 * createBox-method of a RowAtom) will be reset.
 */
public class Dummy {

  private Atom el;

  private boolean textSymbol;

  private int type = -1;

  /**
   * Creates a new Dummy for the given atom.
   *
   * @param a an atom
   */
  public Dummy( final Atom a ) {
    el = a;
  }

  /**
   * Changes the type of the atom
   *
   * @param t the new type
   */
  public void setType( int t ) {
    type = t;
  }

  /**
   * @return the changed type, or the old left type if it hasn't been changed
   */
  public int getLeftType() {
    return (type >= 0 ? type : el.getLeftType());
  }

  /**
   * @return the changed type, or the old right type if it hasn't been changed
   */
  public int getRightType() {
    return (type >= 0 ? type : el.getRightType());
  }

  public boolean isCharSymbol() {
    return el instanceof CharSymbolAtom;
  }

  /**
   * This method will only be called if {@link #isCharSymbol()} returns {@code
   * true}.
   */
  public CharFont getCharFont( final TeXFont tf ) {
    return ((CharSymbolAtom) el).getCharFont( tf );
  }

  /**
   * Changes this atom into the given "ligature atom".
   *
   * @param a the ligature atom
   */
  public void changeAtom( final FixedCharAtom a ) {
    textSymbol = false;
    type = -1;
    el = a;
  }

  public Box createBox( final TeXEnvironment rs ) {
    if( textSymbol ) {
      ((CharSymbolAtom) el).markAsTextSymbol();
    }
    final Box b = el.createBox( rs );
    if( textSymbol ) {
      // atom remains unchanged!
      ((CharSymbolAtom) el).removeMark();
    }
    return b;
  }

  public void markAsTextSymbol() {
    textSymbol = true;
  }

  public boolean isKern() {
    return el instanceof SpaceAtom;
  }

  /**
   * Only for Row-elements
   */
  public void setPreviousAtom( final Dummy prev ) {
    if( el instanceof Row ) {
      ((Row) el).setPreviousAtom( prev );
    }
  }
}
