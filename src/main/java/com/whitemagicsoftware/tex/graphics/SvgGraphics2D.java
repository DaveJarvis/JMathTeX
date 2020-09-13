/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.whitemagicsoftware.tex.graphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Responsible for building a SVG version of a TeX formula. Both Batik and
 * JFreeSVG can accomplish the same thing, but they are general-purpose
 * solutions for a greater problem set. JMathTeX uses draws equations using
 * a small subset of the entire functionality. JFreeSVG is faster than Batik,
 * but is still bogs down rendering of the final equation. This is a custom
 * drop-in replacement for {@link Graphics2D} that supports only the necessary
 * subset of drawing functionality necessary to render TeX formulae.
 * <p>
 * For example, this class will only produce outlines of fonts, does not
 * support embedded fonts, nor supports embedded images.
 * </p>
 * <p>
 * This class is not thread-safe, but can be reset for performance purposes.
 * </p>
 */
@SuppressWarnings("unused")
public final class SvgGraphics2D extends AbstractGraphics2D {
  private static final int DEFAULT_SVG_BUFFER_SIZE = 65536;
  private static final String HEADER =
      "<svg xmlns='http://www.w3.org/2000/svg' version='1.1' ";

  /**
   * Initialized with a capacity of {@link #DEFAULT_SVG_BUFFER_SIZE} to
   * minimize the number of memory reallocations.
   */
  private final StringBuilder mSvg;

  /**
   * Filled when drawing paths, not thread-safe.
   */
  private final float[] mCoords = new float[ 6 ];

  /**
   * Transform attribute value, a matrix function.
   */
  private String mTransform = "";

  /**
   * Creates a new instance with a default buffer size. Client classes must
   * call {@link #initialize(int, int)} before using the class to ensure
   * the width and height are added to the document.
   */
  public SvgGraphics2D() {
    this( DEFAULT_SVG_BUFFER_SIZE );
  }

  /**
   * Creates a new instance with a given buffer size. Calling classes must
   * call {@link #initialize(int, int)} before using the class to ensure
   * the width and height are added to the document.
   */
  public SvgGraphics2D( final int initialBufferSize ) {
    mSvg = new StringBuilder( initialBufferSize ).append( HEADER );
  }

  @Override
  public void initialize( final int w, final int h ) {
    reset();
    appendDimensions( w, h );
  }

  @Override
  public void initialize( final int id, final int w, final int h ) {
    reset();
    mSvg.append( "id='" )
        .append( id )
        .append( "' " );
    appendDimensions( w, h );
  }

  @Override
  public void draw( final Shape shape ) {
    mSvg.append( "<g" );

    if( !isIdentityTransform() ) {
      mSvg.append( " transform='" )
          .append( mTransform )
          .append( '\'' );
    }

    mSvg.append( '>' );
    appendPath( (Path2D) shape );
    mSvg.append( "</g>" );
  }

  /**
   * Resets the internal buffer to start writing after the {@link #HEADER}
   * text.
   */
  private void reset() {
    mSvg.setLength( HEADER.length() );
  }

  /**
   * Appends the dimensions, which are the last items added to the
   * {@code <svg>} element.
   *
   * @param w The final document width (in pixels).
   * @param h The final document height (in pixels).
   */
  private void appendDimensions( final int w, final int h ) {
    mSvg.append( "width='" )
        .append( w )
        .append( "px' height='" )
        .append( h )
        .append( "px'>" );
  }

  private void appendPath( final Path2D path ) {
    mSvg.append( "<path " );

    if( path.getWindingRule() == 0 ) {
      mSvg.append( "fill-rule='evenodd' " );
    }

    mSvg.append( "d='" );
    final var iterator = path.getPathIterator( null );

    while( !iterator.isDone() ) {
      switch( iterator.currentSegment( mCoords ) ) {
        case 0 -> mSvg.append( 'M' )
                      .append( toGeometryPrecision( mCoords[ 0 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 1 ] ) );
        case 1 -> mSvg.append( 'L' )
                      .append( toGeometryPrecision( mCoords[ 0 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 1 ] ) );
        case 2 -> mSvg.append( 'Q' )
                      .append( toGeometryPrecision( mCoords[ 0 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 1 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 2 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 3 ] ) );
        case 3 -> mSvg.append( 'C' )
                      .append( toGeometryPrecision( mCoords[ 0 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 1 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 2 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 3 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 4 ] ) )
                      .append( ' ' )
                      .append( toGeometryPrecision( mCoords[ 5 ] ) );
        case 4 -> mSvg.append( 'Z' );
      }

      iterator.next();
    }

    mSvg.append( "'/>" );
  }

  @Override
  public void fill( final Shape shape ) {
    if( shape instanceof Rectangle2D ) {
      final var rect = (Rectangle2D) shape;

      mSvg.append( "<rect x='" )
          .append( toGeometryPrecision( rect.getX() ) )
          .append( "' y='" )
          .append( toGeometryPrecision( rect.getY() ) )
          .append( "' width='" )
          .append( toGeometryPrecision( rect.getWidth() ) )
          .append( "' height='" )
          .append( toGeometryPrecision( rect.getHeight() ) );

      if( !isIdentityTransform() ) {
        mSvg.append( "' transform='" )
            .append( mTransform );
      }

      // Double-duty: closes either height or transform.
      mSvg.append( "'/>" );
    }
    else {
      draw( shape );
    }
  }

  @Override
  public void setTransform( final AffineTransform at ) {
    assert at != null;
    super.setTransform( at );
    mTransform = toString( at );
  }

  /**
   * Call when no more graphics operations are pending and the content is safe
   * to convert to an SVG representation. This method is idempotent.
   *
   * @return A complete SVG string that can be rendered to reproduce the TeX
   * primitives.
   */
  @Override
  public String toString() {
    return mSvg.append( "</svg>" ).toString();
  }

  /**
   * Generates a matrix transformation string of the given transform.
   *
   * @param at The transform to convert into a string.
   * @return A matrix transformation string.
   */
  private String toString( final AffineTransform at ) {
    return "matrix(" +
        toTransformPrecision( at.getScaleX() ) + ',' +
        toTransformPrecision( at.getShearY() ) + ',' +
        toTransformPrecision( at.getShearX() ) + ',' +
        toTransformPrecision( at.getScaleY() ) + ',' +
        toTransformPrecision( at.getTranslateX() ) + ',' +
        toTransformPrecision( at.getTranslateY() ) + ')';
  }
}
