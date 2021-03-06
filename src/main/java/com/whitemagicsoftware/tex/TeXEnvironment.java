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

import java.awt.*;

import static com.whitemagicsoftware.tex.TeXConstants.*;
import static com.whitemagicsoftware.tex.boxes.Box.NO_FONT;

/**
 * Contains the used TeXFont-object, color settings and the current style in
 * which a formula must be drawn. It's used in the createBox-methods. Contains
 * methods that apply the style changing rules for subformula's.
 */
public class TeXEnvironment {
    private Color background;

    private Color foreground;

    // current style
    private int style;

    // TeXFont used
    private final TeXFont tf;

    // last used font
    private int lastFontId = NO_FONT;

    /**
     * Creates a new {@link TeXEnvironment} with a default point size with
     * symbols rendered in the largest display size.
     *
     * @param tf Font to use for rendering the text.
     */
    public TeXEnvironment( final TeXFont tf ) {
        this( STYLE_DISPLAY, tf, null, null );
    }

    /**
     * Creates a new {@link TeXEnvironment} with a default point size.
     *
     * @param style Controls some symbol sizes.
     * @param tf    Font to use for rendering the text.
     */
    public TeXEnvironment( final int style, final TeXFont tf ) {
        this( style, tf, null, null );
    }

    private TeXEnvironment(
        final int style, final TeXFont tf, final Color bg, final Color fg ) {
        // check if style is valid
        // if not : DISPLAY = default value
        if( style == STYLE_DISPLAY ||
            style == STYLE_TEXT ||
            style == STYLE_SCRIPT ||
            style == STYLE_SCRIPT_SCRIPT ) {
            this.style = style;
        }
        else {
            this.style = STYLE_DISPLAY;
        }

        this.tf = tf;
        this.background = bg;
        this.foreground = fg;
    }

    public TeXEnvironment copy() {
        return new TeXEnvironment( style, tf, background, foreground );
    }

    /**
     * @return a copy of the environment, but in a cramped style.
     */
    public TeXEnvironment crampStyle() {
        final var s = copy();
        s.style = (style % 2 == 1 ? style : style + 1);
        return s;
    }

    /**
     * @return a copy of the environment, but in denominator style.
     */
    public TeXEnvironment denomStyle() {
        final var s = copy();
        s.style = 2 * (style / 2) + 1 + 2 - 2 * (style / 6);
        return s;
    }

    /**
     * @return the background color setting
     */
    public Color getBackground() {
        return background;
    }

    /**
     * @return the foreground color setting
     */
    public Color getColor() {
        return foreground;
    }

    /**
     * @return the point size of the TeXFont
     */
    public float getFontPointSize() {
        return tf.getPointSize();
    }

    /**
     * @return the current style
     */
    public int getStyle() {
        return style;
    }

    /**
     * @return the TeXFont to be used
     */
    public TeXFont getTeXFont() {
        return tf;
    }

    /**
     * @return a copy of the environment, but in numerator style.
     */
    public TeXEnvironment numStyle() {
        final var s = copy();
        s.style = style + 2 - 2 * (style / 6);
        return s;
    }

    /**
     * Resets the color settings.
     */
    public void reset() {
        foreground = null;
        background = null;
    }

    /**
     * @return a copy of the environment, but with the style changed for roots
     */
    public TeXEnvironment rootStyle() {
        final var s = copy();
        s.style = STYLE_SCRIPT_SCRIPT;
        return s;
    }

    /**
     * @param c the background color to be set
     */
    public void setBackground(Color c) {
        background = c;
    }

    /**
     * @param c the foreground color to be set
     */
    public void setColor(Color c) {
        foreground = c;
    }

    /**
     * @return a copy of the environment, but in subscript style.
     */
    public TeXEnvironment subStyle() {
        final var s = copy();
        s.style = 2 * (style / 4) + 4 + 1;
        return s;
    }

    /**
     * @return a copy of the environment, but in superscript style.
     */
    public TeXEnvironment supStyle() {
        final var s = copy();
        s.style = 2 * (style / 4) + 4 + (style % 2);
        return s;
    }

    public float getSpace() {
        return tf.getSpace(style);
    }

    public void setLastFontId(int id) {
        lastFontId = id;
    }

    public int getLastFontId() {
        // if there was no last font id (whitespace boxes only), use default "mu font"
        return (lastFontId == NO_FONT ? tf.getMuFontId() : lastFontId);
    }
}
