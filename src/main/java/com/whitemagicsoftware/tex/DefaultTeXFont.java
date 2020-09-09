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

import com.whitemagicsoftware.tex.exceptions.SymbolMappingNotFoundException;
import com.whitemagicsoftware.tex.exceptions.TextStyleMappingNotFoundException;
import com.whitemagicsoftware.tex.parsers.DefaultTeXFontParser;

import java.util.Map;

import static com.whitemagicsoftware.tex.TeXFormula.PIXELS_PER_POINT;
import static com.whitemagicsoftware.tex.parsers.DefaultTeXFontParser.MUFONTID_ATTR;
import static com.whitemagicsoftware.tex.parsers.DefaultTeXFontParser.SPACEFONTID_ATTR;

/**
 * The default implementation of {@link TeXFont}. All font information
 * is read from XML.
 */
public class DefaultTeXFont implements TeXFont {

    /**
     * No extension part for that kind (TOP, MID, REP, or BOT).
     */
    public static final int NO_EXTENSION = -1;
    public static final int NUMBERS = 0, CAPITALS = 1, SMALL = 2, UNICODE = 3;
    public static final int TOP = 0, MID = 1, REP = 2, BOT = 3;
    public static final int WIDTH = 0, HEIGHT = 1, DEPTH = 2, ITALIC = 3;

    private static final String[] defaultTextStyleMappings;
    private static final Map<String, CharFont[]> textStyleMappings;
    private static final Map<String, CharFont> symbolMappings;
    private static final Map<String, Float> parameters;
    private static final Map<String, Number> generalSettings;
    private static final FontInfo[] fontInfo;

    private static final int sMuFontId;
    private static final float sScriptFactor;
    private static final float sScriptScriptFactor;
    private static final int sSpaceFontId;

    static {
        final var parser = new DefaultTeXFontParser();
        // general font parameters
        parameters = parser.parseParameters();
        // general settings
        generalSettings = parser.parseGeneralSettings();
        // text style mappings
        textStyleMappings = parser.getParsedTextStyleMappings();
        // default text style : style mappings
        defaultTextStyleMappings = parser.parseDefaultTextStyleMappings();
        // symbol mappings
        symbolMappings = parser.parseSymbolMappings();
        // fonts + font descriptions
        fontInfo = parser.parseFontDescriptions();

        sMuFontId = generalSettings.get( MUFONTID_ATTR ).intValue();
        sScriptFactor = generalSettings.get( "scriptfactor" ).floatValue();
        sScriptScriptFactor = generalSettings.get( "scriptscriptfactor" ).floatValue();
        sSpaceFontId = generalSettings.get( SPACEFONTID_ATTR ).intValue();
    }

    private final float pointSize;

    public DefaultTeXFont(final float pointSize) {
        this.pointSize = pointSize;
    }

    @Override
    public float getAxisHeight( int style ) {
        return getResizedParameter( "axisheight", style );
    }

    @Override
    public float getBigOpSpacing1( int style ) {
        return getResizedParameter( "bigopspacing1", style );
    }

    @Override
    public float getBigOpSpacing2( int style ) {
        return getResizedParameter( "bigopspacing2", style );
    }

    @Override
    public float getBigOpSpacing3( int style ) {
        return getResizedParameter( "bigopspacing3", style );
    }

    @Override
    public float getBigOpSpacing4( int style ) {
        return getResizedParameter( "bigopspacing4", style );
    }

    @Override
    public float getBigOpSpacing5( int style ) {
        return getResizedParameter( "bigopspacing5", style );
    }

    @Override
    public float getDefaultRuleThickness( int style ) {
        return getResizedParameter( "defaultrulethickness", style );
    }

    @Override
    public float getDenom1( int style ) {
        return getResizedParameter( "denom1", style );
    }

    @Override
    public float getDenom2( int style ) {
        return getResizedParameter( "denom2", style );
    }

    @Override
    public float getSub1( int style ) {
        return getResizedParameter( "sub1", style );
    }

    @Override
    public float getSub2( int style ) {
        return getResizedParameter( "sub2", style );
    }

    @Override
    public float getSubDrop( int style ) {
        return getResizedParameter( "subdrop", style );
    }

    @Override
    public float getSup1( int style ) {
        return getResizedParameter( "sup1", style );
    }

    @Override
    public float getSup2( int style ) {
        return getResizedParameter( "sup2", style );
    }

    @Override
    public float getSup3( int style ) {
        return getResizedParameter( "sup3", style );
    }

    @Override
    public float getSupDrop( int style ) {
        return getResizedParameter( "supdrop", style );
    }

    @Override
    public float getNum1( int style ) {
        return getResizedParameter( "num1", style );
    }

    @Override
    public float getNum2( int style ) {
        return getResizedParameter( "num2", style );
    }

    @Override
    public float getNum3( int style ) {
        return getResizedParameter( "num3", style );
    }

    @Override
    public float getSpace( int style ) {
        final FontInfo info = getFontInfo( sSpaceFontId );
        return info.getSpace( getScaledSizeFactor( style ) );
    }

    @Override
    public float getPointSize() {
        return pointSize;
    }

    private float getResizedParameter(final String p, final int style) {
        return getParameter( p ) * getSizeFactor( style ) * PIXELS_PER_POINT;
    }

    private Char getChar( final char c, final CharFont[] cf, final int style ) {
        int kind = CAPITALS;
        int offset = c - 'A';

        if (c >= '0' && c <= '9') {
            kind = NUMBERS;
            offset = c - '0';
        } else if (c >= 'a' && c <= 'z') {
            kind = SMALL;
            offset = c - 'a';
        }

        // if the mapping for the character's range, then use the default style
        if( cf[ kind ] == null ) {
            return getDefaultChar( c, style );
        }

        return getChar(new CharFont((char) (cf[kind].c + offset),
                    cf[kind].fontId), style);
    }

    @Override
    public Char getChar( final char c, final String textStyle, final int style )
        throws TextStyleMappingNotFoundException {
        final CharFont[] mapping = textStyleMappings.get( textStyle );
        if( mapping == null ) {
            throw new TextStyleMappingNotFoundException( textStyle );
        }

        return getChar( c, mapping, style );
    }

    @Override
    public Char getChar(final CharFont cf, final int style) {
        float size = getSizeFactor(style);
        return new Char( cf.c, cf.fontId, getMetrics( cf, size ) );
    }

    @Override
    public Char getChar( final String symbolName, final int style )
        throws SymbolMappingNotFoundException {
        final CharFont charFont = symbolMappings.get( symbolName );
        if( charFont == null ) {
            throw new SymbolMappingNotFoundException( symbolName );
        }

        return getChar( charFont, style );
    }

    /**
     * Returns the default character for a given code point and style.
     * A default text style always exists because it's checked during parsing.
     * @param c     alphanumeric character (code point)
     * @param style the style in which the atom should be drawn
     */
    @Override
    public Char getDefaultChar(final char c, final int style) {
        if (c >= '0' && c <= '9')
            return getChar(c, defaultTextStyleMappings[NUMBERS], style);
        else if (c >= 'a' && c <= 'z')
            return getChar(c, defaultTextStyleMappings[SMALL], style);

        return getChar(c, defaultTextStyleMappings[CAPITALS], style);
    }

    @Override
    public Extension getExtension( final Char c, final int style ) {
        final int fontId = c.getFontId();
        final float s = getSizeFactor( style );

        // construct Char for every part
        final FontInfo info = getFontInfo( fontId );
        final int[] ext = info.getExtension( c.getChar() );
        final Char[] parts = new Char[ ext.length ];

        for( int i = 0; i < ext.length; i++ ) {
            if( ext[ i ] == NO_EXTENSION ) {
                parts[ i ] = null;
            }
            else {
                parts[ i ] = new Char(
                    (char) ext[ i ],
                    fontId,
                    getMetrics( new CharFont( (char) ext[ i ], fontId ), s ) );
            }
        }

        return new Extension(
            parts[ TOP ], parts[ MID ], parts[ REP ], parts[ BOT ] );
    }

    @Override
    public float getKern(
        final CharFont left, final CharFont right, final int style ) {
        if (left.fontId == right.fontId){
            final FontInfo info = getFontInfo(left.fontId);
            return info.getKern(left.c, right.c, getScaledSizeFactor(style) );
        }
        return 0;
    }

    @Override
    public CharFont getLigature( final CharFont left, final CharFont right ) {
        if (left.fontId == right.fontId) {
            final FontInfo info =  getFontInfo(left.fontId);
            return info.getLigature(left.c, right.c);
        }

        return null;
    }

    private Metrics getMetrics( final CharFont cf, final float size ) {
        final FontInfo info = getFontInfo(cf.fontId);
        final float[] m = info.getMetrics(cf.c);

        return new Metrics(
            m[ WIDTH ], m[ HEIGHT ], m[ DEPTH ], m[ ITALIC ],
            size, size * PIXELS_PER_POINT );
    }

    @Override
    public int getMuFontId() {
        return sMuFontId;
    }

    @Override
    public Char getNextLarger( final Char c, final int style ) {
        final FontInfo info = getFontInfo(c.getFontId());
        final CharFont ch = info.getNextLarger(c.getChar());
        final var sizeFactor = getSizeFactor(style);

        return new Char(ch.c, ch.fontId, getMetrics(ch, sizeFactor));
    }

    @Override
    public float getQuad( final int style, final int fontId ) {
        final FontInfo info = getFontInfo( fontId );
        return info.getQuad( getScaledSizeFactor( style ) );
    }

    @Override
    public float getSkew( final CharFont cf, final int style ) {
        final FontInfo info = getFontInfo( cf.fontId );
        final char skew = info.getSkewChar();

        return getKern( cf, new CharFont( skew, cf.fontId ), style );
    }

    @Override
    public float getXHeight(final int style, final int fontCode) {
        final FontInfo info = getFontInfo(fontCode);
        return info.getXHeight(getScaledSizeFactor(style));
    }

    @Override
    public boolean hasNextLarger(final Char c) {
        final FontInfo info = getFontInfo(c.getFontId());
        return info.getNextLarger(c.getChar()) != null;
    }

    @Override
    public boolean hasSpace(final int font) {
        final FontInfo info = getFontInfo(font);
        return info.hasSpace();
    }

    @Override
    public boolean isExtensionChar(final Char c) {
        final FontInfo info = getFontInfo(c.getFontId());
        return info.getExtension(c.getChar()) != null;
    }

    @Override
    public FontInfo getFontInfo(final int fontId) {
        return fontInfo[ fontId ];
    }

    private static float getParameter(final String parameterName) {
        final Float param = parameters.get( parameterName);
        return param == null ? 0 : param;
    }

    private static float getSizeFactor(final int style) {
        if( style < TeXConstants.STYLE_SCRIPT ) {
            return 1;
        }
        else if( style < TeXConstants.STYLE_SCRIPT_SCRIPT ) {
            return sScriptFactor;
        }

        return sScriptScriptFactor;
    }

    private float getScaledSizeFactor( final int style ) {
        return getSizeFactor(style) * PIXELS_PER_POINT;
    }
}
