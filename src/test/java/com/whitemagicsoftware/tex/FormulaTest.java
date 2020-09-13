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

import com.whitemagicsoftware.tex.graphics.AbstractGraphics2D;
import com.whitemagicsoftware.tex.graphics.SvgDomGraphics2D;
import com.whitemagicsoftware.tex.graphics.SvgGraphics2D;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FormulaTest {
  private static final String DIR_TEMP = getProperty( "java.io.tmpdir" );

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
      "u(n) \\Leftrightarrow \\frac{1}{1-e^{-jw}} + " +
          "\\sum_{k=-\\infty}^{\\infty} \\pi \\delta (\\omega + 2\\pi k)\n"
  };

  /**
   * Test that the framework can output a correct SVG string.
   *
   * @throws IOException Could not save the result.
   */
  @Test
  public void test_Parser_InputFormulas_OutputSvgString() throws IOException {
    test_Parser( new SvgGraphics2D() );
  }

  /**
   * Test that the framework can output a correct SVG document object model
   * (DOM) {@link Document}.
   *
   * @throws IOException Could not save the result.
   */
  @Test
  public void test_Parser_InputFormulas_OutputSvgDom() throws IOException {
    test_Parser( new SvgDomGraphics2D() );
  }

  private void test_Parser( final AbstractGraphics2D g )
      throws IOException {
    final var size = 100f;
    final var texFont = new DefaultTeXFont( size );
    final var env = new TeXEnvironment( texFont );
    g.scale( size, size );

    for( int j = 0; j < EQUATIONS.length; j++ ) {
      final var formula = new TeXFormula( EQUATIONS[ j ] );
      final var box = formula.createBox( env );
      final var layout = new TeXLayout( box, size );

      g.initialize( layout.getWidth(), layout.getHeight() );
      box.draw( g, layout.getX(), layout.getY() );

      final var path = Path.of( DIR_TEMP, format( "eq-%02d.svg", j ) );
      try( final var fos = new FileOutputStream( path.toFile() );
           final var out = new OutputStreamWriter( fos, UTF_8 ) ) {
        out.write( g.toString() );
      }
    }
  }

  public static void main( String[] args ) throws IOException {
    final var test = new FormulaTest();
    test.test_Parser_InputFormulas_OutputSvgString();
  }
}
