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

import static be.ugent.caagt.jmathtex.TeXConstants.STYLE_TEXT;
import static be.ugent.caagt.jmathtex.TeXConstants.UNIT_MU;
import static java.lang.Math.abs;

/**
 * An atom representing an nth-root construction.
 */
public class NthRoot extends Atom {

  private static final String sqrtSymbol = "sqrt";

  private static final float FACTOR = 0.55f;

  // base atom to be put under the root sign
  private final Atom base;

  // root atom to be put in the upper left corner above the root sign
  private final Atom root;

  public NthRoot( Atom base, Atom root ) {
    this.base = base == null ? new EmptyAtom() : base;
    this.root = root == null ? new EmptyAtom() : root;
  }

  public Box createBox( TeXEnvironment env ) {
    // first create a simple square root construction
    final TeXFont tf = env.getTeXFont();
    final int style = env.getStyle();

    // calculate minimum clearance clr
    float clr, drt = tf.getDefaultRuleThickness( style );

    if( style < STYLE_TEXT ) {
      clr = tf.getXHeight(
          style, tf.getChar( sqrtSymbol, style ).getFontId() );
    }
    else {
      clr = drt;
    }

    clr = drt + abs( clr ) / 4;

    // cramped style for the formula under the root sign
    final Box bs = base.createBox( env.crampStyle() );
    final HorizontalBox b = new HorizontalBox( bs );
    b.add( new SpaceAtom( UNIT_MU, 1, 0, 0 ).createBox( env.crampStyle() ) );
    // create radical sign
    final float totalH = b.getHeight() + b.getDepth();
    final Box rootSign = DelimiterFactory.create(
        sqrtSymbol, env, totalH + clr + drt );

    // add half the excess to clr
    final float delta = rootSign.getDepth() - (totalH + clr);
    clr += delta / 2;

    // create total box
    rootSign.setShift( -(b.getHeight() + clr) );
    final OverBar ob = new OverBar( b, clr, rootSign.getHeight() );
    ob.setShift( -(b.getHeight() + clr + drt) );
    final HorizontalBox squareRoot = new HorizontalBox( rootSign );
    squareRoot.add( ob );

    // create box from root for nth root
    final Box r = root.createBox( env.rootStyle() );

    // shift root up
    final float bottomShift =
        FACTOR * (squareRoot.getHeight() + squareRoot.getDepth());
    r.setShift( squareRoot.getDepth() - r.getDepth() - bottomShift );

    // negative kern
    final Box negativeKern =
        new SpaceAtom( UNIT_MU, -10f, 0, 0 ).createBox( env );

    // arrange both boxes together with the negative kern
    final Box result = new HorizontalBox();
    final float pos = r.getWidth() + negativeKern.getWidth();
    if( pos < 0 ) {
      result.add( new StrutBox( -pos ) );
    }

    result.add( r );
    result.add( negativeKern );
    result.add( squareRoot );
    return result;
  }
}
