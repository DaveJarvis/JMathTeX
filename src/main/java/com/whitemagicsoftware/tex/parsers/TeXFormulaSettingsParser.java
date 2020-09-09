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

package com.whitemagicsoftware.tex.parsers;

import com.whitemagicsoftware.tex.FontInfo;
import com.whitemagicsoftware.tex.TeXFormula;
import com.whitemagicsoftware.tex.exceptions.ResourceParseException;
import com.whitemagicsoftware.tex.exceptions.XMLResourceParseException;
import com.whitemagicsoftware.tex.resources.XMLResourceReader;
import org.jdom2.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parse predefined {@link TeXFormula}s from an XML file.
 */
public class TeXFormulaSettingsParser {
    
    public static final String RESOURCE_NAME = "TeXFormulaSettings.xml";
    public static final String CHARTODEL_MAPPING_EL = "Map";
    
    private final Element root;
    
    public TeXFormulaSettingsParser() throws ResourceParseException {
        root = new XMLResourceReader( RESOURCE_NAME ).read();
    }
    
    public String[] parseSymbolMappings() throws ResourceParseException {
        String[] mappings = new String[ FontInfo.NUMBER_OF_CHAR_CODES];
        Element charToSymbol = root.getChild("CharacterToSymbolMappings");
        if (charToSymbol != null)
            addToMap(charToSymbol.getChildren("Map"), mappings);
        return mappings;
    }
    
    public String[] parseDelimiterMappings() throws ResourceParseException {
        String[] mappings = new String[FontInfo.NUMBER_OF_CHAR_CODES];
        Element charToDelimiter = root.getChild("CharacterToDelimiterMappings");
        if (charToDelimiter != null)
            addToMap(charToDelimiter.getChildren(CHARTODEL_MAPPING_EL),
                    mappings);
        return mappings;
    }
    
    private static void addToMap(List<Element> mapList, String[] table)
        throws ResourceParseException {
        for (Object obj : mapList) {
            Element map = (Element) obj;
            String ch = map.getAttributeValue("char");
            String symbol = map.getAttributeValue("symbol");

            if (ch == null)
                throw new XMLResourceParseException(RESOURCE_NAME, map.getName(),
                        "char", null);

            if (symbol == null)
                throw new XMLResourceParseException(RESOURCE_NAME, map.getName(),
                        "symbol", null);

            // valid element found
            if (ch.length() == 1)
                table[ch.charAt(0)] =  symbol;
            else
                // only single-character mappings allowed, ignore others
                throw new XMLResourceParseException(RESOURCE_NAME, map.getName(),
                        "char",
                        "must have a value that contains exactly 1 character!");
        }
    }
    
    public Set<String> parseTextStyles() throws ResourceParseException {
        final Set<String> res = new HashSet<>();
        final Element textStyles = root.getChild("TextStyles");
        if (textStyles != null) {
            for (final var style : textStyles.getChildren("TextStyle")) {
                final String name = style.getAttributeValue("name");
                if( name == null ) {
                    throw new XMLResourceParseException( RESOURCE_NAME, style
                        .getName(), "name", null );
                }

                res.add(name);
            }
        }
        return res;
    }
}
