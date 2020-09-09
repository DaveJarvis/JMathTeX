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
import be.ugent.caagt.jmathtex.TeXLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import static be.ugent.caagt.jmathtex.TeXConstants.STYLE_DISPLAY;
import static java.awt.RenderingHints.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jfree.svg.SVGHints.KEY_DRAW_STRING_TYPE;
import static org.jfree.svg.SVGHints.VALUE_DRAW_STRING_TYPE_VECTOR;

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
      VALUE_TEXT_ANTIALIAS_ON,
      KEY_DRAW_STRING_TYPE,
      VALUE_DRAW_STRING_TYPE_VECTOR
  );

  private final static String[] EQUATIONS = {
      "(a+b)^2=a^2 + 2ab + b^2",
      "S_x = sqrt((SS_x)/(N-1))",
      "e^{\\pi i} + 1 = 0",
      "\\sigma=\\sqrt{\\sum_{i=1}^{k} p_i(x_i-\\mu)^2}",
      "\\sqrt[n]{\\pi}",
      "\\sqrt[n]{|z| . e^{i \\theta}} = " +
          "\\sqrt[n]{|z| . e^{i (\\frac{\\theta + 2 k \\pi}{n})}}," +
          " k \\in \\lbrace 0, ..., n-1 \\rbrace, n \\in NN",
      "\\vec{u}^2 \\tilde{\\nu}",
      "\\sum_{i=1}^n i = (\\sum_{i=1}^{n-1} i) + n =\n" +
          "\\frac{(n-1)(n)}{2} + n = \\frac{n(n+1)}{2}",
      "\\int_{a}^{b} x^2 dx",
      "G_{\\mu \\nu} = \\frac{8 \\pi G}{c^4} T_{{\\mu \\nu}}",
      "\\prod_{i=a}^{b} f(i)",
  };

  //@Test
  public void test_MathML_SimpleFormula_Success() throws IOException {
    final var size = 20f;
    final var texFont = new DefaultTeXFont( size );
    final var g = new FastGraphics2D();
    g.scale( size, size );

    var svg = "";

    final long startTime = System.currentTimeMillis();

    for( int j = 0; j < EQUATIONS.length; j++ ) {
      final var filename = "/tmp/eq-" + j + ".svg";

      for( int i = 0; i < 100 / EQUATIONS.length; i++ ) {
        final var formula = new TeXFormula( EQUATIONS[ j ] );
        final var env = new TeXEnvironment( STYLE_DISPLAY, texFont );
        final var box = formula.createBox( env );
        final var layout = new TeXLayout( box, size );

//        final var g2 = new SVGGraphics2D(
//            layout.getWidth(), layout.getHeight(), PX, buffer );
//        g2.setRenderingHints( DEFAULT_HINTS );

        g.setDimensions( layout.getWidth(), layout.getHeight() );
        box.draw( g, layout.getX(), layout.getY() );
        svg = g.toString();
//        box.draw( g2, layout.getX(), layout.getY() );
//        svg = g2.getSVGElement( null, true, null, null, null );
//        buffer.setLength( 0 );
      }


      try( final var fos = new FileOutputStream( filename );
           final var out = new OutputStreamWriter( fos, UTF_8 ) ) {
        out.write( svg );
      }
    }

    System.out.println( g );

    System.out.println( g.tallies() );

    System.out.println( System.currentTimeMillis() - startTime );
  }

  public static void main( String[] args ) throws IOException {
    final var test = new TeXFormulaTest();
    test.test_MathML_SimpleFormula_Success();
  }
}
