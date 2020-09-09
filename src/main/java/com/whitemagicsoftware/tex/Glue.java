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

import com.whitemagicsoftware.tex.boxes.Box;
import com.whitemagicsoftware.tex.boxes.GlueBox;
import com.whitemagicsoftware.tex.parsers.GlueSettingsParser;

/**
 * Represents glue by its 3 components. Contains the "glue rules".
 */
public class Glue {

    // contains the different glue types
    private static final Glue[] glueTypes;

    // the glue table representing the "glue rules" (as in TeX)
    private static final int[][][] glueTable;

    static {
        final GlueSettingsParser parser = new GlueSettingsParser();
        glueTypes = parser.getGlueTypes();
        glueTable = parser.createGlueTable();
    }

    // the glue components
    private final float space;
    private final float stretch;
    private final float shrink;
    
    private final String name;
    
    public Glue(float space, float stretch, float shrink, String name) {
        this.space = space;
        this.stretch = stretch;
        this.shrink = shrink;
        this.name = name;
    }
    
    /**
     * Name of this glue object.
     */
    public String getName () {
       return this.name; 
    }
    
    /**
     * Creates a box representing the glue type according to the "glue rules" based
     * on the atom types between which the glue must be inserted.
     *
     * @param lType left atom type
     * @param rType right atom type
     * @param env the TeXEnvironment
     * @return a box containing representing the glue
     */
    public static Box get(
        final int lType, final int rType, final TeXEnvironment env) {
        // types > INNER are considered of type ORD for glue calculations
        final int l = lType > 7 ? TeXConstants.TYPE_ORDINARY : lType;
        final int r = rType > 7 ? TeXConstants.TYPE_ORDINARY : rType;

        // search right glue-type in "glue-table"
        return glueTypes[glueTable[l][r][env.getStyle() / 2]].createBox(env);
    }

    /**
     * Use "quad" from a font marked as a "mu font"
     */
    private Box createBox(final TeXEnvironment env) {
        final TeXFont tf = env.getTeXFont();
        float quad = tf.getQuad( env.getStyle(), tf.getMuFontId() );
        return new GlueBox( space / 18.0f * quad );
    }
}
