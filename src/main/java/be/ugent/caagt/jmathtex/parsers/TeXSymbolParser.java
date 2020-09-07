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

import be.ugent.caagt.jmathtex.SymbolAtom;
import be.ugent.caagt.jmathtex.exceptions.ResourceParseException;
import be.ugent.caagt.jmathtex.exceptions.XMLResourceParseException;
import be.ugent.caagt.jmathtex.resources.XMLResourceReader;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;

import static be.ugent.caagt.jmathtex.TeXConstants.*;

/**
 * Parses TeX symbol definitions from an XML-file.
 */
public class TeXSymbolParser {

   public static final String RESOURCE_NAME = "TeXSymbols.xml";
   public static final String DELIMITER_ATTR = "del";
   public static final String TYPE_ATTR = "type";

   private static final Map<String, Integer> typeMappings = new HashMap<>();

   static {
      typeMappings.put( "ord", TYPE_ORDINARY );
      typeMappings.put( "op", TYPE_BIG_OPERATOR );
      typeMappings.put( "bin", TYPE_BINARY_OPERATOR );
      typeMappings.put( "rel", TYPE_RELATION );
      typeMappings.put( "open", TYPE_OPENING );
      typeMappings.put( "close", TYPE_CLOSING );
      typeMappings.put( "punct", TYPE_PUNCTUATION );
      typeMappings.put( "acc", TYPE_ACCENT );
   }

   private final Element root;

   public TeXSymbolParser() throws ResourceParseException {
      root = new XMLResourceReader( RESOURCE_NAME ).read();
   }

   public Map<String, SymbolAtom> readSymbols() throws ResourceParseException {
      Map<String,SymbolAtom> res = new HashMap<>();
      // iterate all "symbol"-elements
      for (final Element symbol : root.getChildren("Symbol")) {
         String name = getAttrValueAndCheckIfNotNull("name", symbol);
         String type = getAttrValueAndCheckIfNotNull(TYPE_ATTR, symbol);
         String del = symbol.getAttributeValue(DELIMITER_ATTR);

         boolean isDelimiter = (del != null && del.equals("true"));
         // check if type is known
         Integer typeVal = typeMappings.get( type);
         if (typeVal == null) // unknown type
            throw new XMLResourceParseException(RESOURCE_NAME, "Symbol",
                  "type", "has an unknown value '" + type + "'");
         // add symbol to the hash table
         res.put(name, new SymbolAtom( name, typeVal, isDelimiter));
      }
      return res;
   }

   private static String getAttrValueAndCheckIfNotNull(String attrName,
         Element element) throws ResourceParseException {
      String attrValue = element.getAttributeValue(attrName);
      if (attrValue == null)
         throw new XMLResourceParseException(RESOURCE_NAME, element.getName(),
               attrName, null);
      return attrValue;
   }
}
