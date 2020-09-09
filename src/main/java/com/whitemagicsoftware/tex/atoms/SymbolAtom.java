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
import com.whitemagicsoftware.tex.boxes.CharBox;
import com.whitemagicsoftware.tex.exceptions.InvalidSymbolTypeException;
import com.whitemagicsoftware.tex.exceptions.SymbolNotFoundException;
import com.whitemagicsoftware.tex.parsers.TeXSymbolParser;

import java.util.BitSet;
import java.util.Map;
import java.util.Optional;

/**
 * A box representing a symbol (a non-alphanumeric character).
 */
public class SymbolAtom extends CharSymbolAtom {

    // contains all the possible valid symbol types
    private static final BitSet validSymbolTypes;

    // contains all defined symbols
    private static final Map<String, SymbolAtom> symbols;

    static {
        // set valid symbol types
        validSymbolTypes = new BitSet( 16 );
        validSymbolTypes.set( TeXConstants.TYPE_ORDINARY );
        validSymbolTypes.set( TeXConstants.TYPE_BIG_OPERATOR );
        validSymbolTypes.set( TeXConstants.TYPE_BINARY_OPERATOR );
        validSymbolTypes.set( TeXConstants.TYPE_RELATION );
        validSymbolTypes.set( TeXConstants.TYPE_OPENING );
        validSymbolTypes.set( TeXConstants.TYPE_CLOSING );
        validSymbolTypes.set( TeXConstants.TYPE_PUNCTUATION );
        validSymbolTypes.set( TeXConstants.TYPE_ACCENT );

        // The parser creates instances of this class, and instances of this
        // class will check against the validSymbolTypes, so be sure that the
        // validSymbolTypes are initialized before reading symbols.
        symbols = new TeXSymbolParser().readSymbols();
    }

    // symbol name
    private final String name;

    // whether it's is a delimiter symbol
    private final boolean delimiter;
    
    public SymbolAtom( final SymbolAtom symbolAtom, final int type )
        throws InvalidSymbolTypeException {
        this( symbolAtom.name, type, symbolAtom.delimiter );
    }
    
    /**
     * Constructs a new symbol. This used by "TeXSymbolParser" and the symbol
     * types are guaranteed to be valid.
     *
     * @param name symbol name
     * @param type symbol type constant
     * @param delimiter whether the symbol is a delimiter
     */
    public SymbolAtom(String name, int type, boolean delimiter)
        throws InvalidSymbolTypeException {
        if (!validSymbolTypes.get(type))
            throw new InvalidSymbolTypeException(
                "The symbol type was not valid! "
                    + "Use one of the symbol type constants from the class 'TeXConstants'.");

        this.name = name;
        this.type = type;
        this.delimiter = delimiter;
    }

    /**
     * Looks up the name in the table and returns the corresponding
     * {@link SymbolAtom} representing the symbol, if found.
     * <p>
     * TODO: Use {@link Optional} instead.
     * </p>
     *
     * @param name Symbol name.
     * @return An existing {@link SymbolAtom} representing the found symbol.
     * @throws SymbolNotFoundException No symbol matches the given name.
     */
    public static SymbolAtom get( final String name )
        throws SymbolNotFoundException {
        final SymbolAtom obj = getNullable( name );
        if( obj == null ) {
            throw new SymbolNotFoundException( name );
        }

        return obj;
    }

    /**
     * Looks up the name in the table and returns the corresponding
     * {@link SymbolAtom} representing the symbol, if found.
     * <p>
     * TODO: Use {@link Optional} instead.
     * </p>
     *
     * @param name Symbol name.
     * @return An existing {@link SymbolAtom} representing the found symbol,
     * or {@code null} if not found.
     */
    public static SymbolAtom getNullable( final String name ) {
        return symbols.get( name );
    }
    
    /**
     *
     * @return true if this symbol can act as a delimiter to embrace formulas
     */
    public boolean isDelimiter() {
        return delimiter;
    }
    
    public String getName() {
        return name;
    }

    public Box createBox( final TeXEnvironment env ) {
        final TeXFont tf = env.getTeXFont();
        final int style = env.getStyle();
        return new CharBox( tf.getChar( name, style ) );
    }

    public CharFont getCharFont( TeXFont tf ) {
        // style doesn't matter here
        return tf.getChar( name, TeXConstants.STYLE_DISPLAY ).getCharFont();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "name='" + name + '\'' +
            ", type=" + type +
            ", delimiter=" + delimiter +
            '}';
    }
}
