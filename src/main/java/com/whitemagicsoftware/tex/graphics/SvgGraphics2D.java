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
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import static com.whitemagicsoftware.tex.graphics.RyuDouble.doubleToString;
import static java.awt.Color.BLACK;

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
public final class SvgGraphics2D extends Graphics2D {
  private static final int DEFAULT_SVG_BUFFER_SIZE = 65536;
  private static final String XML_HEADER =
      "<svg xmlns='http://www.w3.org/2000/svg' version='1.1' ";

  /**
   * Number of decimal places for geometric shapes.
   */
  private static final int DECIMALS_GEOMETRY = 4;

  /**
   * Number of decimal places for matrix transforms.
   */
  private static final int DECIMALS_TRANSFORM = 6;

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

  private Color mColour = BLACK;
  private Font mFont = new Font( "Default", Font.PLAIN, 12 );
  private AffineTransform mAffineTransform = new AffineTransform();
  private final FontRenderContext mRenderContext =
      new FontRenderContext( null, false, true );

  /**
   * Creates a new instance with a default buffer size. Client classes must
   * call {@link #setDimensions(int, int)} before using the class to ensure
   * the width and height are added to the document.
   */
  public SvgGraphics2D() {
    this( DEFAULT_SVG_BUFFER_SIZE );
  }

  /**
   * Creates a new instance with a given buffer size. Calling classes must
   * call {@link #setDimensions(int, int)} before using the class to ensure
   * the width and height are added to the document.
   */
  public SvgGraphics2D( final int initialBufferSize ) {
    mSvg = new StringBuilder( initialBufferSize ).append( XML_HEADER );
  }

  /**
   * Resets the SVG buffer to a new state. This method must be called before
   * calling drawing primitives.
   *
   * @param w The final document width (in pixels).
   * @param h The final document height (in pixels).
   */
  public void setDimensions( final int w, final int h ) {
    mSvg.setLength( XML_HEADER.length() );
    mSvg.append( "width='" )
        .append( w )
        .append( "px' height='" )
        .append( h )
        .append( "px'>" );
  }

  @Override
  public void draw( final Shape shape ) {
    mSvg.append( "<g" );

    if( !mAffineTransform.isIdentity() ) {
      mSvg.append( " transform='" )
          .append( mTransform )
          .append( '\'' );
    }

    mSvg.append( '>' )
        .append( "<path " );
    appendPath( (Path2D) shape, mSvg );
    mSvg.append( "/>" )
        .append( "</g>" );
  }

  private void appendPath( final Path2D path, final StringBuilder buffer ) {
    if( path.getWindingRule() == 0 ) {
      buffer.append( "fill-rule='evenodd' " );
    }

    buffer.append( "d='" );
    final var iterator = path.getPathIterator( null );

    while( !iterator.isDone() ) {
      int type = iterator.currentSegment( mCoords );

      switch( type ) {
        case 0 -> buffer.append( 'M' )
                        .append( toGeometryPrecision( mCoords[ 0 ] ) )
                        .append( ' ' )
                        .append( toGeometryPrecision( mCoords[ 1 ] ) );
        case 1 -> buffer.append( 'L' )
                        .append( toGeometryPrecision( mCoords[ 0 ] ) )
                        .append( ' ' )
                        .append( toGeometryPrecision( mCoords[ 1 ] ) );
        case 2 -> buffer.append( 'Q' )
                        .append( toGeometryPrecision( mCoords[ 0 ] ) )
                        .append( ' ' )
                        .append( toGeometryPrecision( mCoords[ 1 ] ) )
                        .append( ' ' )
                        .append( toGeometryPrecision( mCoords[ 2 ] ) )
                        .append( ' ' )
                        .append( toGeometryPrecision( mCoords[ 3 ] ) );
        case 3 -> buffer.append( 'C' )
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
        case 4 -> buffer.append( 'Z' );
      }

      iterator.next();
    }

    buffer.append( '\'' );
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

      if( !mAffineTransform.isIdentity() ) {
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
  public void drawString( final String glyphs, final float x, final float y ) {
    assert glyphs != null;

    final var font = getFont();
    final var frc = getFontRenderContext();
    final var gv = font.createGlyphVector( frc, glyphs );
    drawGlyphVector( gv, x, y );
  }

  @Override
  public void drawString( final String glyphs, final int x, final int y ) {
    assert glyphs != null;
    drawString( glyphs, (float) x, (float) y );
  }

  @Override
  public void drawGlyphVector(
      final GlyphVector g, final float x, final float y ) {
    fill( g.getOutline( x, y ) );
  }

  @Override
  public void translate( final int x, final int y ) {
    translate( x, (double) y );
  }

  @Override
  public void translate( final double tx, final double ty ) {
    final var at = getTransform();
    at.translate( tx, ty );
    setTransform( at );
  }

  @Override
  public void scale( final double sx, final double sy ) {
    final var at = getTransform();
    at.scale( sx, sy );
    setTransform( at );
  }

  @Override
  public void setTransform( final AffineTransform at ) {
    assert at != null;
    mAffineTransform = new AffineTransform( at );
    mTransform = toString( at );
  }

  @Override
  public AffineTransform getTransform() {
    return (AffineTransform) mAffineTransform.clone();
  }

  @Override
  public FontRenderContext getFontRenderContext() {
    return mRenderContext;
  }

  @Override
  public Font getFont() {
    return mFont;
  }

  @Override
  public void setFont( final Font font ) {
    assert font != null;
    mFont = font;
  }

  @Override
  public Color getColor() {
    return mColour;
  }

  @Override
  public void setColor( final Color colour ) {
    mColour = colour;
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
   * Has no effect; call {@link #setDimensions(int, int)} to reset this instance
   * to create another SVG document.
   */
  @Override
  public void dispose() {
  }

  private static String toGeometryPrecision( final double value ) {
    return doubleToString( value, DECIMALS_GEOMETRY );
  }

  private static String toTransformPrecision( final double value ) {
    return doubleToString( value, DECIMALS_TRANSFORM );
  }

  @Override
  public void setPaintMode() {
  }

  @Override
  public void setXORMode( final Color c1 ) {
  }

  @Override
  public FontMetrics getFontMetrics( final Font f ) {
    return null;
  }

  @Override
  public Rectangle getClipBounds() {
    return null;
  }

  @Override
  public void clipRect( final int x, final int y, final int width,
                        final int height ) {
  }

  @Override
  public void setClip( final int x, final int y, final int width,
                       final int height ) {
  }

  @Override
  public Shape getClip() {
    return null;
  }

  @Override
  public void setClip( final Shape clip ) {
  }

  @Override
  public void copyArea( final int x, final int y, final int width,
                        final int height, final int dx, final int dy ) {
  }

  @Override
  public void drawLine( final int x1, final int y1, final int x2,
                        final int y2 ) {
  }

  @Override
  public void fillRect( final int x, final int y, final int width,
                        final int height ) {
  }

  @Override
  public void clearRect( final int x, final int y, final int width,
                         final int height ) {
  }

  @Override
  public void drawRoundRect( final int x, final int y, final int width,
                             final int height, final int arcWidth,
                             final int arcHeight ) {
  }

  @Override
  public void fillRoundRect( final int x, final int y, final int width,
                             final int height, final int arcWidth,
                             final int arcHeight ) {
  }

  @Override
  public void drawOval( final int x, final int y, final int width,
                        final int height ) {
  }

  @Override
  public void fillOval( final int x, final int y, final int width,
                        final int height ) {
  }

  @Override
  public void drawArc( final int x, final int y, final int width,
                       final int height, final int startAngle,
                       final int arcAngle ) {
  }

  @Override
  public void fillArc( final int x, final int y, final int width,
                       final int height, final int startAngle,
                       final int arcAngle ) {
  }

  @Override
  public void drawPolyline( final int[] xPoints, final int[] yPoints,
                            final int nPoints ) {
  }

  @Override
  public void drawPolygon( final int[] xPoints, final int[] yPoints,
                           final int nPoints ) {
  }

  @Override
  public void fillPolygon( final int[] xPoints, final int[] yPoints,
                           final int nPoints ) {
  }

  @Override
  public void rotate( final double theta ) {
  }

  @Override
  public void rotate( final double theta, final double x, final double y ) {
  }

  @Override
  public void shear( final double shx, final double shy ) {
  }

  @Override
  public void transform( final AffineTransform at ) {
  }

  @Override
  public Paint getPaint() {
    return null;
  }

  @Override
  public Composite getComposite() {
    return null;
  }

  @Override
  public void setBackground( final Color color ) {
  }

  @Override
  public Color getBackground() {
    return null;
  }

  @Override
  public Stroke getStroke() {
    return null;
  }

  @Override
  public void clip( final Shape s ) {
  }

  @Override
  public void drawString( final AttributedCharacterIterator iterator,
                          final int x, final int y ) {
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y,
                            final ImageObserver observer ) {
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y,
                            final int width, final int height,
                            final ImageObserver observer ) {
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y,
                            final Color bgcolor,
                            final ImageObserver observer ) {
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y,
                            final int width, final int height,
                            final Color bgcolor,
                            final ImageObserver observer ) {
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int dx1, final int dy1,
                            final int dx2, final int dy2,
                            final int sx1, final int sy1, final int sx2,
                            final int sy2,
                            final ImageObserver observer ) {
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int dx1, final int dy1,
                            final int dx2, final int dy2,
                            final int sx1, final int sy1, final int sx2,
                            final int sy2, final Color bgcolor,
                            final ImageObserver observer ) {
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final AffineTransform xform,
                            final ImageObserver obs ) {
    return false;
  }

  @Override
  public void drawImage( final BufferedImage img, final BufferedImageOp op,
                         final int x, final int y ) {
  }

  @Override
  public void drawRenderedImage( final RenderedImage img,
                                 final AffineTransform xform ) {
  }

  @Override
  public void drawRenderableImage( final RenderableImage img,
                                   final AffineTransform xform ) {
  }

  @Override
  public void drawString( final AttributedCharacterIterator iterator,
                          final float x,
                          final float y ) {
  }

  @Override
  public boolean hit( final Rectangle rect, final Shape s,
                      final boolean onStroke ) {
    return false;
  }

  @Override
  public GraphicsConfiguration getDeviceConfiguration() {
    return null;
  }

  @Override
  public void setComposite( final Composite comp ) {
  }

  @Override
  public void setPaint( final Paint paint ) {
  }

  @Override
  public void setStroke( final Stroke s ) {
  }

  @Override
  public void setRenderingHint( final RenderingHints.Key hintKey,
                                final Object hintValue ) {
  }

  @Override
  public Object getRenderingHint( final RenderingHints.Key hintKey ) {
    return null;
  }

  @Override
  public void setRenderingHints( final Map<?, ?> hints ) {
  }

  @Override
  public void addRenderingHints( final Map<?, ?> hints ) {
  }

  @Override
  public RenderingHints getRenderingHints() {
    return null;
  }

  @Override
  public Graphics create() {
    return null;
  }
}
