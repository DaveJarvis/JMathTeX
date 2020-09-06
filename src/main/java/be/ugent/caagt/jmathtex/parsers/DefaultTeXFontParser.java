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

package be.ugent.caagt.jmathtex.parsers;

import be.ugent.caagt.jmathtex.CharFont;
import be.ugent.caagt.jmathtex.FontInfo;
import be.ugent.caagt.jmathtex.exceptions.ResourceParseException;
import be.ugent.caagt.jmathtex.exceptions.XMLResourceParseException;
import be.ugent.caagt.jmathtex.resources.FontResourceReader;
import be.ugent.caagt.jmathtex.resources.XMLResourceReader;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.ugent.caagt.jmathtex.DefaultTeXFont.*;
import static java.lang.String.format;

/**
 * Parses the font information from an XML-file.
 */
public class DefaultTeXFontParser {

    private static interface CharChildParser { // NOPMD
        public void parse(Element el, char ch, FontInfo info) throws
            XMLResourceParseException;
    }

    private static class ExtensionParser implements CharChildParser {
        ExtensionParser() {
            // avoid generation of access class
        }

        public void parse(Element el, char ch, FontInfo info)
        throws ResourceParseException {
            int[] extensionChars = new int[4];
            extensionChars[ REP ] = parseIntElement( "rep", el );
            extensionChars[ TOP ] = parseOptionalInt( "top", el, NONE );
            extensionChars[ MID ] = parseOptionalInt( "mid", el, NONE );
            extensionChars[ BOT ] = parseOptionalInt( "bot", el, NONE );

            // parsing OK, add extension info
            info.setExtension(ch, extensionChars);
        }
    }

    private static class KernParser implements CharChildParser {
        KernParser() {
            // avoid generation of access class
        }

        public void parse(Element el, char ch, FontInfo info)
        throws ResourceParseException {
            int code = parseIntElement( "code", el);
            float kernAmount = parseFloatElement( "val", el);

            // parsing OK, add kern info
            info.addKern(ch, (char) code, kernAmount);
        }
    }

    private static class LigParser implements CharChildParser {
        LigParser() {
            // avoid generation of access class
        }

        public void parse(Element el, char ch, FontInfo info)
        throws ResourceParseException {
            int code = parseIntElement( "code", el);
            int ligCode = parseIntElement( "ligCode", el);

            // parsing OK, add ligature info
            info.addLigature(ch, (char) code, (char) ligCode);
        }
    }

    private static class NextLargerParser implements CharChildParser {
        NextLargerParser() {
            // avoid generation of access class
        }

        public void parse(Element el, char ch, FontInfo info)
        throws ResourceParseException {
            int fontId = parseFontIdElement( el );
            int code = parseIntElement( "code", el);

            // parsing OK, add "next larger" info
            info.setNextLarger(ch, (char) code, fontId);
        }
    }

    private static int parseFontIdElement( final Element element ) {
        return parseFontIdElement( "fontId", element );
    }

    private static int resolveFontId( final String fontId ) {
        return mResolvedFontInfo.addIndex( fontId );
    }

    /**
     *
     * @param attribute Name of the font ID attribute (usually "fontId").
     * @param element Element containing the font ID attribute to parse.
     * @return
     */
    private static int parseFontIdElement(
        final String attribute, final Element element ) {
        final String fontId = parseStringElement( attribute, element );
        return resolveFontId( fontId );
    }

    public static final String RESOURCE_NAME = "DefaultTeXFont.xml";

    public static final String STYLE_MAPPING_EL = "TextStyleMapping";
    public static final String SYMBOL_MAPPING_EL = "SymbolMapping";
    public static final String GEN_SET_EL = "GeneralSettings";
    public static final String MUFONTID_ATTR = "mufontid";
    public static final String SPACEFONTID_ATTR = "spacefontid";

    private static final IndexedLinkedHashSet<String> mResolvedFontInfo =
        new IndexedLinkedHashSet<>();

    // string-to-constant mappings
    private static Map<String,Integer> rangeTypeMappings = new HashMap<>();
    static {
        rangeTypeMappings.put("numbers", NUMBERS);
        rangeTypeMappings.put("capitals", CAPITALS);
        rangeTypeMappings.put("small", SMALL);
        rangeTypeMappings.put("unicode", UNICODE);
    }

    // parsers for the child elements of a "Char"-element
    private static Map<String, CharChildParser> charChildParsers =
        new HashMap<>();
    static {
        charChildParsers.put("Kern", new KernParser());
        charChildParsers.put("Lig", new LigParser());
        charChildParsers.put("NextLarger", new NextLargerParser());
        charChildParsers.put("Extension", new ExtensionParser());
    }

    private Map<String, CharFont[]> parsedTextStyles;

    private Element root;

    public DefaultTeXFontParser() throws ResourceParseException {
        root = new XMLResourceReader( RESOURCE_NAME ).read();
        // parse textstyles ahead of the rest, because it's used while
        // parsing the default text style
        parsedTextStyles = parseStyleMappings();
    }

    public Map<Integer, FontInfo> parseFontDescriptions()
        throws ResourceParseException {
        final Map<Integer, FontInfo> res = new HashMap<>();
        final Element fontDescriptions = root.getChild( "FontDescriptions" );

        if (fontDescriptions != null) {
            for( final var font : fontDescriptions.getChildren( "Font" ) ) {
                final String name = parseStringElement( "name", font);
                final int id = parseFontIdElement( "id", font);
                float space = parseFloatElement( "space", font);
                float xHeight = parseFloatElement( "xHeight", font);
                float quad = parseFloatElement( "quad", font);

                int skewChar = parseOptionalInt( "skewChar", font, -1);

                final Font f = new FontResourceReader( name ).read();
                final FontInfo info = new FontInfo(id, f, xHeight, space, quad);

                if( skewChar != -1 ) {
                    info.setSkewChar( (char) skewChar );
                }

                for( final var element : font.getChildren( "Char" ) ) {
                    processCharElement( element, info );
                }

                // parsing OK, add to table
                res.put( id, info );
            }
        }

        return res;
    }

    private static void processCharElement(Element charElement, FontInfo info)
    throws ResourceParseException {
        // retrieve required integer attribute
        char ch = (char) parseIntElement( "code", charElement);
        // retrieve optional float attributes
        float[] metrics = new float[4];
        metrics[ WIDTH ] = parseOptionalFloat( "width", charElement, 0 );
        metrics[ HEIGHT ] = parseOptionalFloat( "height", charElement, 0 );
        metrics[ DEPTH ] = parseOptionalFloat( "depth", charElement, 0 );
        metrics[ ITALIC ] = parseOptionalFloat( "italic", charElement, 0 );
        // set metrics
        info.setMetrics(ch, metrics);

        // process children
        for( final var el : charElement.getChildren() ) {
            final var elName = el.getName();
            final var parser = charChildParsers.get( elName );
            if( parser == null ) {
                final String msg = format(
                    "%s: a <Char>-element has unknown child element '%s'",
                    RESOURCE_NAME, elName );
                throw new XMLResourceParseException( msg );
            }

            // process the child element
            parser.parse( el, ch, info );
        }
    }

    public Map<String,CharFont> parseSymbolMappings() throws ResourceParseException {
        final Element symbolMappings = root.getChild("SymbolMappings");
        if (symbolMappings == null) {
            throw new XMLResourceParseException( RESOURCE_NAME,"SymbolMappings" );
        }

        final Map<String,CharFont> res = new HashMap<>();

        // iterate all mappings
        for (Element mapping : symbolMappings.getChildren(SYMBOL_MAPPING_EL)) {
            String symbolName = parseStringElement( "name", mapping);
            int ch = parseIntElement( "ch", mapping);
            int fontId = parseFontIdElement( mapping );
            res.put(symbolName, new CharFont((char) ch, fontId));
        }

        // "sqrt" must allways be present (used internally only!)
        if (res.get("sqrt") == null)
            throw new XMLResourceParseException(
                    RESOURCE_NAME
                    + ": the required mapping <SymbolMap name=\"sqrt\" ... /> is not found!");
        // parsing OK
        return res;
    }

    public String[] parseDefaultTextStyleMappings()
    throws ResourceParseException {
        Element defaultTextStyleMappings = root.getChild("DefaultTextStyleMapping");
        if (defaultTextStyleMappings == null) {
            throw new XMLResourceParseException( RESOURCE_NAME,
                                                 "DefaultTextStyleMapping" );
        }
        String[] res = new String[3];
        for (final var mapping : defaultTextStyleMappings.getChildren("MapStyle")) {
            // get range name and check if it's valid
            String code = parseStringElement( "code", mapping);
            Integer codeMapping = rangeTypeMappings.get(code);
            if (codeMapping == null) // unknown range name
                throw new XMLResourceParseException(RESOURCE_NAME, "MapStyle",
                        "code", "contains an unknown \"range name\" '" + code
                        + "'!");
            // get mapped style and check if it exists
            String textStyleName = parseStringElement( "textStyle",
                                                       mapping);
            CharFont[] styleMapping = parsedTextStyles.get(textStyleName);
            if (styleMapping == null) // unknown text style
                throw new XMLResourceParseException(RESOURCE_NAME, "MapStyle",
                        "textStyle", "contains an unknown text style '"
                        + textStyleName + "'!");
            // now check if the range is defined within the mapped text style
            CharFont[] charFonts = parsedTextStyles.get(textStyleName);

            if (charFonts[codeMapping] == null) // range not defined
                throw new XMLResourceParseException(RESOURCE_NAME
                        + ": the default text style mapping '" + textStyleName
                        + "' for the range '" + code
                        + "' contains no mapping for that range!");

            // everything OK, put mapping in table
            res[codeMapping] = textStyleName;
        }
        return res;
    }

    public Map<String,Float> parseParameters() throws ResourceParseException {
        final Element parameters = root.getChild( "Parameters" );
        if( parameters == null ) {
            throw new XMLResourceParseException( RESOURCE_NAME, "Parameters" );
        }
        final Map<String, Float> res = new HashMap<>();
        for( Attribute attr : parameters.getAttributes() ) {
            String name = attr.getName();
            res.put( name, parseFloatElement( name, parameters ) );
        }
        return res;

    }

    public Map<String, Number> parseGeneralSettings()
        throws ResourceParseException {

        // TODO: must this be 'Number' ?
        Element generalSettings = root.getChild( "GeneralSettings" );

        if( generalSettings == null ) {
            throw new XMLResourceParseException(
                RESOURCE_NAME, "GeneralSettings" );
        }

        final Map<String, Number> result = new HashMap<>();
        // set required int values (if valid)
        result.put( MUFONTID_ATTR, parseFontIdElement(
            MUFONTID_ATTR, generalSettings ) );
        result.put( SPACEFONTID_ATTR, parseFontIdElement(
            SPACEFONTID_ATTR, generalSettings ) );

        // set required float values (if valid)
        result.put( "scriptfactor", parseFloatElement(
            "scriptfactor", generalSettings ) );
        result.put( "scriptscriptfactor", parseFloatElement(
            "scriptscriptfactor", generalSettings ) );

        return result;
    }

    public Map<String, CharFont[]> parseTextStyleMappings() {
        return parsedTextStyles;
    }

    private Map<String, CharFont[]> parseStyleMappings()
        throws ResourceParseException {
        final Element textStyleMappings = root.getChild("TextStyleMappings");

        if( textStyleMappings == null ) {
            throw new XMLResourceParseException(
                RESOURCE_NAME, "TextStyleMappings" );
        }

        final Map<String,CharFont[]> res = new HashMap<>();

        for (final var mapping : textStyleMappings.getChildren(STYLE_MAPPING_EL)) {
            // get required string attribute
            String textStyleName = parseStringElement( "name",mapping);
            List<Element> mapRangeList = mapping.getChildren("MapRange");
            CharFont[] charFonts = new CharFont[3];

            for (final var mapRange : mapRangeList) {
                int fontId = parseFontIdElement( mapRange );
                int ch = parseIntElement( "start", mapRange);
                String code = parseStringElement( "code", mapRange);
                Integer codeMapping = rangeTypeMappings.get(code);

                if (codeMapping == null) {
                    throw new XMLResourceParseException(
                        RESOURCE_NAME,
                        "MapRange",
                        "code",
                        "contains an unknown \"range name\" '" + code + "'!" );
                }

                charFonts[ codeMapping ] = new CharFont( (char) ch, fontId );
            }
            res.put(textStyleName, charFonts);
        }
        return res;
    }


    public static float parseFloatElement( String attrName, Element element )
        throws ResourceParseException {
        String attrValue = parseStringElement( attrName, element);

        // try parsing string to float value
        float res = 0;
        try {
            res = (float) Double.parseDouble(attrValue);
        } catch (NumberFormatException e) {
            throw new XMLResourceParseException(RESOURCE_NAME, element.getName(),
                    attrName, "has an invalid real value!");
        }
        // parsing OK
        return res;
    }

    public static int parseIntElement( String attrName, Element element )
        throws ResourceParseException {
        String attrValue = parseStringElement( attrName, element );

        // try parsing string to integer value
        int res = 0;
        try {
            res = Integer.parseInt(attrValue);
        } catch (NumberFormatException e) {
            throw new XMLResourceParseException(RESOURCE_NAME, element.getName(),
                    attrName, "must be an integer value, not: '" + attrValue + "'");
        }
        // parsing OK
        return res;
    }

    public static int parseOptionalInt(
        String attrName, Element element, int defaultValue )
        throws ResourceParseException {
        String attrValue = element.getAttributeValue(attrName);
        if (attrValue == null) // attribute not present
            return defaultValue;
        else {
            // try parsing string to integer value
            int res = 0;
            try {
                res = Integer.parseInt(attrValue);
            } catch (NumberFormatException e) {
                throw new XMLResourceParseException(RESOURCE_NAME, element
                        .getName(), attrName, "has an invalid integer value!");
            }
            // parsing OK
            return res;
        }
    }

    public static float parseOptionalFloat(
        String attrName, Element element, float defaultValue )
        throws ResourceParseException {
        String attrValue = element.getAttributeValue(attrName);
        if (attrValue == null) // attribute not present
            return defaultValue;
        else {
            // try parsing string to float value
            float res = 0;
            try {
                res = (float) Double.parseDouble(attrValue);
            } catch (NumberFormatException e) {
                throw new XMLResourceParseException(RESOURCE_NAME, element
                        .getName(), attrName, "has an invalid float value!");
            }
            // parsing OK
            return res;
        }
    }

    private static String parseStringElement( String attrName, Element element )
        throws ResourceParseException {
        String attrValue = element.getAttributeValue( attrName );
        if( attrValue == null ) {
            throw new XMLResourceParseException(
                RESOURCE_NAME, element.getName(), attrName, null );
        }
        return attrValue;
    }
}
