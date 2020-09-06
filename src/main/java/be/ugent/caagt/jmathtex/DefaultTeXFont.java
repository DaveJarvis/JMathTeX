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
import be.ugent.caagt.jmathtex.exceptions.XMLResourceParseException;
import be.ugent.caagt.jmathtex.parsers.DefaultTeXFontParser;

import java.util.Map;

import static be.ugent.caagt.jmathtex.TeXFormula.PIXELS_PER_POINT;
import static be.ugent.caagt.jmathtex.parsers.DefaultTeXFontParser.*;

/**
 * The default implementation of the TeXFont-interface. All font information is read
 * from an xml-file.
 */
public class DefaultTeXFont implements TeXFont {

    private static final String[] defaultTextStyleMappings;

    /**
     * No extension part for that kind (TOP,MID,REP or BOT)
     */
    public static final int NONE = -1;

    public static final int NUMBERS = 0, CAPITALS = 1, SMALL = 2, UNICODE = 3;
    public static final int TOP = 0, MID = 1, REP = 2, BOT = 3;
    public static final int WIDTH = 0, HEIGHT = 1, DEPTH = 2, ITALIC = 3;

    private static final Map<String, CharFont[]> textStyleMappings;
    private static final Map<String, CharFont> symbolMappings;
    private static final Map<Integer, FontInfo> fontInfo;
    private static final Map<String, Float> parameters;
    private static final Map<String, Number> generalSettings;

    private static final int sMuFontId;
    private static final float sScriptFactor;
    private static final float sScriptScriptFactor;

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
        if( fontInfo.get( sMuFontId ) == null ) {
            throw new XMLResourceParseException(
                RESOURCE_NAME, GEN_SET_EL, MUFONTID_ATTR,
                "contains an unknown font for %s: " + sMuFontId );
        }

        sScriptFactor = generalSettings.get( "scriptfactor" ).floatValue();
        sScriptScriptFactor = generalSettings.get( "scriptscriptfactor" ).floatValue();
    }

    private final float size;

    public DefaultTeXFont(float pointSize) {
        size = pointSize;
    }

    private float getResizedParameter(final String p, final int style) {
        return getParameter( p ) * getSizeFactor( style ) * PIXELS_PER_POINT;
    }

    public float getAxisHeight( int style ) {
        return getResizedParameter( "axisheight", style );
    }

    public float getBigOpSpacing1( int style ) {
        return getResizedParameter( "bigopspacing1", style );
    }

    public float getBigOpSpacing2( int style ) {
        return getResizedParameter( "bigopspacing2", style );
    }

    public float getBigOpSpacing3( int style ) {
        return getResizedParameter( "bigopspacing3", style );
    }

    public float getBigOpSpacing4( int style ) {
        return getResizedParameter( "bigopspacing4", style );
    }

    public float getBigOpSpacing5( int style ) {
        return getResizedParameter( "bigopspacing5", style );
    }

    public float getDefaultRuleThickness( int style ) {
        return getResizedParameter( "defaultrulethickness", style );
    }

    public float getDenom1( int style ) {
        return getResizedParameter( "denom1", style );
    }

    public float getDenom2( int style ) {
        return getResizedParameter( "denom2", style );
    }

    public float getSub1( int style ) {
        return getResizedParameter( "sub1", style );
    }

    public float getSub2( int style ) {
        return getResizedParameter( "sub2", style );
    }

    public float getSubDrop( int style ) {
        return getResizedParameter( "subdrop", style );
    }

    public float getSup1( int style ) {
        return getResizedParameter( "sup1", style );
    }

    public float getSup2( int style ) {
        return getResizedParameter( "sup2", style );
    }

    public float getSup3( int style ) {
        return getResizedParameter( "sup3", style );
    }

    public float getSupDrop( int style ) {
        return getResizedParameter( "supdrop", style );
    }

    public float getNum1( int style ) {
        return getResizedParameter( "num1", style );
    }

    public float getNum2( int style ) {
        return getResizedParameter( "num2", style );
    }

    public float getNum3( int style ) {
        return getResizedParameter( "num3", style );
    }

    public float getSpace( int style ) {
        int spaceFontId = generalSettings.get( SPACEFONTID_ATTR ).intValue();
        FontInfo info = getFontInfo( spaceFontId );
        return info.getSpace( getScaledSizeFactor( style ) );
    }

    private Char getChar(char c, CharFont[] cf, int style) {
        final int kind, offset;

        if (c >= '0' && c <= '9') {
            kind = NUMBERS;
            offset = c - '0';
        } else if (c >= 'a' && c <= 'z') {
            kind = SMALL;
            offset = c - 'a';
        } else {
            kind = CAPITALS;
            offset = c - 'A';
        }

        // if the mapping for the character's range, then use the default style
        if( cf[ kind ] == null ) {
            return getDefaultChar( c, style );
        }

        return getChar(new CharFont((char) (cf[kind].c + offset),
                    cf[kind].fontId), style);
    }

    public Char getChar( final char c, final String textStyle, final int style )
        throws TextStyleMappingNotFoundException {
        final CharFont[] mapping = textStyleMappings.get( textStyle );
        if( mapping == null ) {
            throw new TextStyleMappingNotFoundException( textStyle );
        }

        return getChar( c, mapping, style );
    }

    public Char getChar(CharFont cf, int style) {
        float size = getSizeFactor(style);
        return new Char( cf.c, cf.fontId, getMetrics( cf, size ) );
    }

    public Char getChar(String symbolName, int style)
    throws SymbolMappingNotFoundException {
        final CharFont charFont = symbolMappings.get( symbolName );
        if( charFont == null ) {
            throw new SymbolMappingNotFoundException( symbolName );
        }

        return getChar( charFont, style );
    }

    public Char getDefaultChar(char c, int style) {
        // these default text style mappings will allways exist,
        // because it's checked during parsing
        if (c >= '0' && c <= '9')
            return getChar(c, defaultTextStyleMappings[NUMBERS], style);
        else if (c >= 'a' && c <= 'z')
            return getChar(c, defaultTextStyleMappings[SMALL], style);

        return getChar(c, defaultTextStyleMappings[CAPITALS], style);
    }

    public Extension getExtension(Char c, int style) {
        int fc = c.getFontId();
        float s = getSizeFactor(style);

        // construct Char for every part
        FontInfo info = fontInfo.get(fc);
        int[] ext = info.getExtension(c.getChar());
        Char[] parts = new Char[ext.length];
        for (int i = 0; i < ext.length; i++) {
            if (ext[i] == NONE)
                parts[i] = null;
            else
                parts[i] = new Char((char) ext[i], fc, getMetrics(new CharFont(
                        (char) ext[i], fc), s));
        }

        return new Extension(parts[TOP], parts[MID], parts[REP], parts[BOT]);
    }

    public float getKern(CharFont left, CharFont right, int style) {
        if (left.fontId == right.fontId){
            final FontInfo info = getFontInfo(left.fontId);
            return info.getKern(left.c, right.c, getScaledSizeFactor(style) );
        }
        return 0;
    }

    public CharFont getLigature(CharFont left, CharFont right) {
        if (left.fontId == right.fontId) {
            final FontInfo info =  getFontInfo(left.fontId);
            return info.getLigature(left.c, right.c);
        }

        return null;
    }

    private Metrics getMetrics(CharFont cf, float size) {
        final FontInfo info = getFontInfo(cf.fontId);
        final float[] m = info.getMetrics(cf.c);

        return new Metrics(
            m[ WIDTH ], m[ HEIGHT ], m[ DEPTH ], m[ ITALIC ],
            size, size * PIXELS_PER_POINT );
    }

    public int getMuFontId() {
        return sMuFontId;
    }

    public float getSize() {
        return size;
    }

    public Char getNextLarger(Char c, int style) {
        final FontInfo info = getFontInfo(c.getFontId());
        final CharFont ch = info.getNextLarger(c.getChar());
        final var sizeFactor = getSizeFactor(style);

        return new Char(ch.c, ch.fontId, getMetrics(ch, sizeFactor));
    }

    public float getQuad( final int style, final int fontId ) {
        final FontInfo info = getFontInfo( fontId );
        return info.getQuad( getScaledSizeFactor( style ) );
    }

    public float getSkew( final CharFont cf, final int style ) {
        final FontInfo info = getFontInfo( cf.fontId );
        final char skew = info.getSkewChar();

        return getKern( cf, new CharFont( skew, cf.fontId ), style );
    }

    public float getXHeight(int style, int fontCode) {
        final FontInfo info = getFontInfo(fontCode);
        return info.getXHeight(getScaledSizeFactor(style));
    }

    public boolean hasNextLarger(Char c) {
        final FontInfo info = getFontInfo(c.getFontId());
        return info.getNextLarger(c.getChar()) != null;
    }

    public boolean hasSpace(int font) {
        final FontInfo info = getFontInfo(font);
        return info.hasSpace();
    }

    public boolean isExtensionChar(Char c) {
        final FontInfo info = getFontInfo(c.getFontId());
        return info.getExtension(c.getChar()) != null;
    }

    public FontInfo getFontInfo(final int fontId) {
        return fontInfo.get( fontId );
    }

    private static float getParameter(String parameterName) {
        final Float param = parameters.get( parameterName);
        return param == null ? 0 : param;
    }

    private static float getSizeFactor(int style) {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "size=" + size +
            '}';
    }
}
