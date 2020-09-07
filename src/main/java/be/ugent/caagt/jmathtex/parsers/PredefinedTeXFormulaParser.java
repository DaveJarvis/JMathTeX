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

import be.ugent.caagt.jmathtex.TeXFormula;
import be.ugent.caagt.jmathtex.exceptions.ResourceParseException;
import be.ugent.caagt.jmathtex.exceptions.XMLResourceParseException;
import be.ugent.caagt.jmathtex.resources.XMLResourceReader;
import org.jdom2.Element;

import java.util.Map;

/**
 * Parses and creates predefined TeXFormula objects form an XML-file.
 */
public class PredefinedTeXFormulaParser {
    
    public static final String RESOURCE_NAME = "PredefinedTeXFormulas.xml";
    
    private final Element root;
    
    public PredefinedTeXFormulaParser() throws ResourceParseException {
        root = new XMLResourceReader( RESOURCE_NAME ).read();
    }
    
    public void parse(final Map<String, TeXFormula> predefinedTeXFormulas) {
        // get required string attribute
        String enabledAll = getAttrValueAndCheckIfNotNull("enabled", root);
        if ("true".equals(enabledAll)) { // parse formula's
            // iterate all "Font"-elements
            for (final Element formula : root.getChildren("TeXFormula")) {
                // get required string attribute
                String enabled = getAttrValueAndCheckIfNotNull("enabled", formula);
                if ("true".equals (enabled)) { // parse this formula
                    // get required string attribute
                    String name = getAttrValueAndCheckIfNotNull("name", formula);
                    
                    // parse and build the formula and add it to the table
                    predefinedTeXFormulas.put(name, new TeXFormulaParser(name, formula).parse());
                }
            }
        }
    }

    private static String getAttrValueAndCheckIfNotNull(
        String attrName, Element element ) throws ResourceParseException {
        String attrValue = element.getAttributeValue( attrName );
        if( attrValue == null ) {
            throw new XMLResourceParseException(
                RESOURCE_NAME, element.getName(), attrName, null );
        }
        return attrValue;
    }
}
