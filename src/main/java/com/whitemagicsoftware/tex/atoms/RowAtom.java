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
import com.whitemagicsoftware.tex.boxes.StrutBox;
import com.whitemagicsoftware.tex.exceptions.EmptyFormulaException;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * An atom representing a horizontal row of other atoms, to be separated by glue.
 * It's also responsible for inserting kerns and ligatures.
 */
public class RowAtom extends Atom implements Row {
    
    // atoms to be displayed horizontally next to eachother
    protected List<Atom> elements = new LinkedList<>();
    
    // previous atom (for nested Row atoms)
    private Dummy previousAtom;
    
    // set of atom types that make a previous bin atom change to ord
    private static final BitSet binSet;
    
    // set of atom types that can possibly need a kern or, together with the
    // previous atom, be replaced by a ligature
    private static final BitSet ligKernSet;
    
    static {
        // fill binSet
        binSet = new BitSet (16);
        binSet.set( TeXConstants.TYPE_BINARY_OPERATOR);
        binSet.set( TeXConstants.TYPE_BIG_OPERATOR);
        binSet.set( TeXConstants.TYPE_RELATION);
        binSet.set( TeXConstants.TYPE_OPENING);
        binSet.set( TeXConstants.TYPE_PUNCTUATION);
        
        // fill ligKernSet
        ligKernSet = new BitSet (16);
        ligKernSet.set( TeXConstants.TYPE_ORDINARY);
        ligKernSet.set( TeXConstants.TYPE_BIG_OPERATOR);
        ligKernSet.set( TeXConstants.TYPE_BINARY_OPERATOR);
        ligKernSet.set( TeXConstants.TYPE_RELATION);
        ligKernSet.set( TeXConstants.TYPE_OPENING);
        ligKernSet.set( TeXConstants.TYPE_CLOSING);
        ligKernSet.set( TeXConstants.TYPE_PUNCTUATION);
    }
    
    protected RowAtom() {
        // empty
    }
    
    public RowAtom(Atom el) {
        if (el != null) {
            if (el instanceof RowAtom)
                // no need to make an mrow the only element of an mrow
                elements.addAll(((RowAtom) el).elements);
            else
                elements.add(el);
        }
    }
    
    /**
     * Only used while parsing MathML. An empty Mrow is not allowed, otherwise
     * it's possible to create fractions with an empty numerator or denominator.
     *
     * @param l
     *           list of objects of the type Formula
     */
    public RowAtom(List<TeXFormula> l) throws EmptyFormulaException {
        for( final TeXFormula f : l ) {
            final var root = f.getRoot();
            if( root != null ) {
                elements.add( root );
            }
        }

        if( elements.isEmpty() ) {
            throw new EmptyFormulaException();
        }
    }
    
    public final void add(Atom el) {
        if( el != null ) {
            elements.add( el );
        }
    }
    
    /**
     *
     * @param cur
     *           current atom being processed
     * @param prev
     *           previous atom
     */
    private void changeToOrd(Dummy cur, Dummy prev, Atom next) {
        int type = cur.getLeftType();
        if (type == TeXConstants.TYPE_BINARY_OPERATOR
                && (prev == null || binSet.get(prev.getRightType())))
            cur.setType( TeXConstants.TYPE_ORDINARY);
        else if (next != null
                && cur.getRightType() == TeXConstants.TYPE_BINARY_OPERATOR) {
            int nextType = next.getLeftType();
            if (nextType == TeXConstants.TYPE_RELATION
                    || nextType == TeXConstants.TYPE_CLOSING
                    || nextType == TeXConstants.TYPE_PUNCTUATION)
                cur.setType( TeXConstants.TYPE_ORDINARY);
        }
    }
    
    public Box createBox( final TeXEnvironment env) {
        final TeXFont tf = env.getTeXFont();
        final HorizontalBox hBox = new HorizontalBox(
            env.getColor(), env.getBackground() );
        env.reset();
        
        final ListIterator<Atom> it = elements.listIterator();

        // convert atoms to boxes and add to the horizontal box
        while( it.hasNext() ) {
            final Dummy atom = new Dummy( it.next() );
            
            // if necessary, change BIN type to ORD
            Atom nextAtom = null;
            if (it.hasNext()) {
                nextAtom = it.next();
                it.previous();
            }
            changeToOrd(atom, previousAtom, nextAtom);
            
            // check for ligatures or kerning
            float kern = 0;
            if (it.hasNext() && atom.getRightType() == TeXConstants.TYPE_ORDINARY
                    && atom.isCharSymbol()) {
                final Atom next = it.next();
                if (next instanceof CharSymbolAtom
                        && ligKernSet.get(next.getLeftType())) {
                    atom.markAsTextSymbol();
                    final CharFont l = atom.getCharFont(tf);
                    final CharFont r = ((CharSymbolAtom) next).getCharFont( tf);
                    final CharFont lig = tf.getLigature(l, r);
                    if (lig == null) {
                        kern = tf.getKern(l, r, env.getStyle());
                        // iterator remains unchanged (no ligature!)
                        it.previous();
                    } 
                    else {
                        // go on with the ligature
                        atom.changeAtom(new FixedCharAtom(lig));
                    }
                } else {
                    // iterator remains unchanged
                    it.previous();
                }
            }
            
            // insert glue, unless it's the first element of the row
            // OR this element or the next is a Kern.
            if (it.previousIndex() != 0 && previousAtom != null
                    && !previousAtom.isKern() && !atom.isKern())
                hBox.add(Glue.get(previousAtom.getRightType(), atom.getLeftType(),
                        env));
            
            // insert atom's box
            atom.setPreviousAtom(previousAtom);
            Box b = atom.createBox(env);
            hBox.add(b);
            
            // set last used fontId (for next atom)
            env.setLastFontId(b.getLastFontId());
            
            // insert kern
            if( kern > TeXFormula.PREC ) {
                hBox.add( new StrutBox( 0, kern, 0, 0 ) );
            }
            
            // kerns do not interfere with the normal glue-rules without kerns
            if( !atom.isKern() ) {
                previousAtom = atom;
            }
        }

        // reset previousAtom
        previousAtom = null;
        
        // return resulting horizontal box
        return hBox;
    }
    
    public void setPreviousAtom(Dummy prev) {
        previousAtom = prev;
    }
    
    public int getLeftType() {
        if (elements.isEmpty())
            return TeXConstants.TYPE_ORDINARY;
        else
            return elements.get(0).getLeftType();
    }
    
    public int getRightType() {
        if (elements.isEmpty())
            return TeXConstants.TYPE_ORDINARY;
        else
            return elements.get(elements.size() - 1).getRightType();
    }
}
