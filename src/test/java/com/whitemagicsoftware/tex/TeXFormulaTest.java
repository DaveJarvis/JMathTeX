/*
 * JMathTeX is a Java library for rendering mathematical notation.
 * Copyright 2020 White Magic Software, Ltd.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package com.whitemagicsoftware.tex;

import be.ugent.caagt.jmathtex.DefaultTeXFont;
import be.ugent.caagt.jmathtex.TeXEnvironment;
import be.ugent.caagt.jmathtex.TeXFormula;
import be.ugent.caagt.jmathtex.TeXIcon;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static be.ugent.caagt.jmathtex.TeXConstants.STYLE_DISPLAY;
import static java.awt.Color.BLACK;
import static java.awt.RenderingHints.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.batik.dom.GenericDOMImplementation.getDOMImplementation;

public class TeXFormulaTest {
  private final static Map<Object, Object> DEFAULT_HINTS = Map.of(
      KEY_ANTIALIASING,
      VALUE_ANTIALIAS_ON,
      KEY_ALPHA_INTERPOLATION,
      VALUE_ALPHA_INTERPOLATION_QUALITY,
      KEY_COLOR_RENDERING,
      VALUE_COLOR_RENDER_QUALITY,
      KEY_DITHERING,
      VALUE_DITHER_DISABLE,
      KEY_FRACTIONALMETRICS,
      VALUE_FRACTIONALMETRICS_ON,
      KEY_INTERPOLATION,
      VALUE_INTERPOLATION_BICUBIC,
      KEY_RENDERING,
      VALUE_RENDER_QUALITY,
      KEY_STROKE_CONTROL,
      VALUE_STROKE_PURE,
      KEY_TEXT_ANTIALIASING,
      VALUE_TEXT_ANTIALIAS_ON
  );

  private final static String[] MATHS = {
      "(a+b)^2=a^2 + 2ab + b^2",
      "S_x = sqrt((SS_x)/(N-1))",
      "e^{\\pi i} + 1 = 0",
      "\\sigma=\\sqrt{\\sum_{i=1}^{k} p_i(x_i-\\mu)^2}",
      "\\sqrt[n]{\\pi}",
      "\\sqrt[n]{|z| . e^{i \\theta}} = " +
          "\\sqrt[n]{|z| . e^{i (\\frac{\\theta + 2 k \\pi}{n})}}," +
          " k \\in \\lbrace 0, ..., n-1 \\rbrace, n \\in NN",
      "\\vec{u}^2",
      "\\sum_{i=1}^n i = (\\sum_{i=1}^{n-1} i) + n =\n" +
          "\\frac{(n-1)(n)}{2} + n = \\frac{n(n+1)}{2}",
      "\\int_{a}^{b} x^2 dx",
      "\\prod_{i=a}^{b} f(i)",
  };

  private final String TEX = MATHS[ MATHS.length - 1 ];

  @Test
  public void test_MathML_SimpleFormula_Success() throws IOException {
    final var size = 100f;

    final var formula = new TeXFormula( TEX );
    final var font = new DefaultTeXFont( size );
    final var env = new TeXEnvironment( STYLE_DISPLAY, font );
    final var box = formula.createBox( env );

    final var dom = getDOMImplementation();
    final var ns = "http://www.w3.org/2000/svg";
    final var doc = dom.createDocument( ns, "svg", null );
    final var context = SVGGeneratorContext.createDefault( doc );
    final var svgg = new SVGGraphics2D( context, true );

    svgg.setSVGCanvasSize( box.toDimension() );
    svgg.setColor( BLACK );

    box.draw( svgg, 0, box.getHeight() );

    final var fos = new FileOutputStream( "/tmp/saved.svg" );
    final var out = new OutputStreamWriter( fos, UTF_8 );
    svgg.stream( out, false );
    fos.close();
  }

  @Test
  public void test2() throws IOException {
    final String file = "/tmp/latex.svg";
    final boolean fontAsShapes = true;
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    String ns = "http://www.w3.org/2000/svg";
    Document document = domImpl.createDocument( ns, "svg", null );
    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault( document );

    SVGGraphics2D g2 = new SVGGraphics2D( ctx, fontAsShapes );
    TeXFormula formula = new TeXFormula( TEX );
    TeXIcon icon = formula.createTeXIcon( STYLE_DISPLAY, 20 );
    icon.setInsets( new Insets( 5, 5, 5, 5 ) );
    g2.setSVGCanvasSize( new Dimension( icon.getIconWidth(),
                                        icon.getIconHeight() ) );

    final JLabel jl = new JLabel();
    jl.setForeground( BLACK );
    icon.paintIcon( jl, g2, 0, 0 );

    try( final FileOutputStream svgs = new FileOutputStream( file );
         final Writer out = new OutputStreamWriter( svgs, UTF_8 ) ) {
      g2.stream( out, true );
    }
  }
}
