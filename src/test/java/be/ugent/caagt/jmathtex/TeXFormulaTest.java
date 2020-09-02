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
package be.ugent.caagt.jmathtex;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static be.ugent.caagt.jmathtex.TeXConstants.STYLE_DISPLAY;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class TeXFormulaTest {
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
      "\\under{lim}{x\\to\\infty} f(x)"
  };

  private final String TEX = MATHS[ MATHS.length - 1 ];

  @Test
  public void test_MathML_SimpleFormula_Success() throws IOException {
    final var formula = new TeXFormula( TEX );

    final var size = 100f;

    final var env = new TeXEnvironment(
        STYLE_DISPLAY, new DefaultTeXFont( size ) );
    final var box = formula.createBox( env );
    final var width = (int) (box.getWidth() * size);
    final var height = (int) ((box.getHeight() + box.getDepth()) * size);
    final var image = new BufferedImage( width, height, TYPE_INT_ARGB );
    final var g = image.createGraphics();
    g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
    g.setColor( Color.WHITE );
    g.fillRect( 0, 0, width, height );
    g.setColor( Color.BLACK );
    g.scale( size, size );

    box.draw( g, 0, box.getHeight() );

    final var file = new File( "/tmp/saved.png" );
    ImageIO.write( image, "png", file );
  }
}
