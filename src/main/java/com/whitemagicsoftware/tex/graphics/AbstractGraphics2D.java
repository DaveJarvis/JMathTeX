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
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import static java.awt.Color.BLACK;

/**
 * Allows subclasses to implement only those methods that are necessary
 * to produce the desired output format.
 */
public abstract class AbstractGraphics2D extends Graphics2D {
  private Color mColour = BLACK;
  private Font mFont = new Font( "Default", Font.PLAIN, 12 );
  private AffineTransform mAffineTransform = new AffineTransform();
  private final FontRenderContext mRenderContext =
      new FontRenderContext( null, false, true );

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

  /**
   * Multiple calls to this method will scale the scaling.
   *
   * @param sx The scaling factor for the x dimension.
   * @param sy The scaling factor for the y dimension.
   */
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
  }

  @Override
  public AffineTransform getTransform() {
    return (AffineTransform) mAffineTransform.clone();
  }

  /**
   * Answers whether this is the identity transform.
   *
   * @return {@code true} when this is the identity transform.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  protected boolean isIdentityTransform() {
    return mAffineTransform.isIdentity();
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

  @Override
  public FontMetrics getFontMetrics( final Font f ) {
    return null;
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
  public void dispose() {
  }

  @Override
  public void drawString( final AttributedCharacterIterator iterator,
                          final float x,
                          final float y ) {
  }

  @Override
  public void fill( final Shape s ) {
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


  @Override
  public void setPaintMode() {
  }

  @Override
  public void setXORMode( final Color c1 ) {
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
  public void transform( final AffineTransform Tx ) {
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
}
