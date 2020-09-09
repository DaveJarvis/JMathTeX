package com.whitemagicsoftware.tex;

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
import java.util.HashMap;
import java.util.Map;

import static com.whitemagicsoftware.tex.RyuDouble.doubleToString;
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
public final class FastGraphics2D extends Graphics2D {
  // Used to determine what method subset is necessary for TeX.
  final Map<String, Long> mTallies = new HashMap<>();

  private static final int DEFAULT_SVG_BUFFER_SIZE = 65536;
  private static final String XML_HEADER = "<?xml version='1.0'?><svg ";

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
   * Creates a new instance with a default buffer size. Calling classes must
   * call {@link #setDimensions(int, int)} before using the class to ensure
   * the width and height are added to the document.
   */
  public FastGraphics2D() {
    this( DEFAULT_SVG_BUFFER_SIZE );
  }

  /**
   * Creates a new instance with a given buffer size. Calling classes must
   * call {@link #setDimensions(int, int)} before using the class to ensure
   * the width and height are added to the document.
   */
  public FastGraphics2D( final int initialBufferSize ) {
    mSvg = new StringBuilder( initialBufferSize ).append( XML_HEADER );
  }

  @Override
  public void draw( final Shape shape ) {
    mSvg.append( "<g" );

    if( !mAffineTransform.isIdentity() ) {
      mSvg.append( " transform='" )
          .append( mTransform )
          .append( "'" );
    }

    mSvg.append( ">" )
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
        case 0 -> buffer.append( "M" )
                        .append( toGeometryPrecision( mCoords[ 0 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 1 ] ) );
        case 1 -> buffer.append( "L" )
                        .append( toGeometryPrecision( mCoords[ 0 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 1 ] ) );
        case 2 -> buffer.append( "Q" )
                        .append( toGeometryPrecision( mCoords[ 0 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 1 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 2 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 3 ] ) );
        case 3 -> buffer.append( "C" )
                        .append( toGeometryPrecision( mCoords[ 0 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 1 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 2 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 3 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 4 ] ) )
                        .append( " " )
                        .append( toGeometryPrecision( mCoords[ 5 ] ) );
        case 4 -> buffer.append( "Z" );
      }

      iterator.next();
    }

    buffer.append( "'" );
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

  /**
   * Has no effect; call {@link #setDimensions(int, int)} to reset this instance
   * to create another SVG document.
   */
  @Override
  public void dispose() {
  }

  @Override
  public void drawGlyphVector( final GlyphVector g, final float x,
                               final float y ) {
    fill( g.getOutline( x, y ) );
  }

  @Override
  public void translate( final int x, final int y ) {
    translate( x, (double) y );
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
  public void setPaintMode() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void setXORMode( final Color c1 ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public FontMetrics getFontMetrics( final Font f ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public Rectangle getClipBounds() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public void clipRect( final int x, final int y, final int width,
                        final int height ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void setClip( final int x, final int y, final int width,
                       final int height ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public Shape getClip() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public void setClip( final Shape clip ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void copyArea( final int x, final int y, final int width,
                        final int height, final int dx, final int dy ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawLine( final int x1, final int y1, final int x2,
                        final int y2 ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void fillRect( final int x, final int y, final int width,
                        final int height ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void clearRect( final int x, final int y, final int width,
                         final int height ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawRoundRect( final int x, final int y, final int width,
                             final int height, final int arcWidth,
                             final int arcHeight ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void fillRoundRect( final int x, final int y, final int width,
                             final int height, final int arcWidth,
                             final int arcHeight ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawOval( final int x, final int y, final int width,
                        final int height ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void fillOval( final int x, final int y, final int width,
                        final int height ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawArc( final int x, final int y, final int width,
                       final int height, final int startAngle,
                       final int arcAngle ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void fillArc( final int x, final int y, final int width,
                       final int height, final int startAngle,
                       final int arcAngle ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawPolyline( final int[] xPoints, final int[] yPoints,
                            final int nPoints ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawPolygon( final int[] xPoints, final int[] yPoints,
                           final int nPoints ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void fillPolygon( final int[] xPoints, final int[] yPoints,
                           final int nPoints ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
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

  /**
   * Resets the SVG buffer to a new state. This method must be called before
   * calling drawing primitives.
   *
   * @param w The final document width (in pixels).
   * @param h The final document height (in pixels).
   */
  public void setDimensions( final int w, final int h ) {
    mSvg.setLength( XML_HEADER.length() );
    mSvg.append( "width='" );
    mSvg.append( w );
    mSvg.append( "px' height='" );
    mSvg.append( h );
    mSvg.append( "px'>" );
  }

  /**
   * Call when no more graphics operations are pending and the content is safe
   * to convert to an SVG representation.
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
        toTransformPrecision( at.getScaleX() ) + "," +
        toTransformPrecision( at.getShearY() ) + "," +
        toTransformPrecision( at.getShearX() ) + "," +
        toTransformPrecision( at.getScaleY() ) + "," +
        toTransformPrecision( at.getTranslateX() ) + "," +
        toTransformPrecision( at.getTranslateY() ) + ")";
  }

  private static String toGeometryPrecision( final double value ) {
    return doubleToString( value, DECIMALS_GEOMETRY );
  }

  private static String toTransformPrecision( final double value ) {
    return doubleToString( value, DECIMALS_TRANSFORM );
  }

  private void tally( final String name ) {
    mTallies.compute( name, ( k, v ) -> v == null ? 0 : v + 1 );
  }

  public String tallies() {
    final var result = new StringBuilder();

    mTallies.forEach(
        ( key, value ) -> result.append( key )
                                .append( " " )
                                .append( value )
                                .append( "\n" )
    );

    return result.toString();
  }

  @Override
  public void rotate( final double theta ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void rotate( final double theta, final double x, final double y ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void shear( final double shx, final double shy ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void transform( final AffineTransform at ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public Paint getPaint() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public Composite getComposite() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public void setBackground( final Color color ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public Color getBackground() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public Stroke getStroke() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public void clip( final Shape s ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawString( final AttributedCharacterIterator iterator,
                          final int x, final int y ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() + "(ACIterator, int, int)" );
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y,
                            final ImageObserver observer ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y,
                            final int width, final int height,
                            final ImageObserver observer ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y,
                            final Color bgcolor,
                            final ImageObserver observer ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y,
                            final int width, final int height,
                            final Color bgcolor,
                            final ImageObserver observer ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int dx1, final int dy1,
                            final int dx2, final int dy2,
                            final int sx1, final int sy1, final int sx2,
                            final int sy2,
                            final ImageObserver observer ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final int dx1, final int dy1,
                            final int dx2, final int dy2,
                            final int sx1, final int sy1, final int sx2,
                            final int sy2, final Color bgcolor,
                            final ImageObserver observer ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return false;
  }

  @Override
  public boolean drawImage( final Image img, final AffineTransform xform,
                            final ImageObserver obs ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return false;
  }

  @Override
  public void drawImage( final BufferedImage img, final BufferedImageOp op,
                         final int x, final int y ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawRenderedImage( final RenderedImage img,
                                 final AffineTransform xform ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawRenderableImage( final RenderableImage img,
                                   final AffineTransform xform ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void drawString( final AttributedCharacterIterator iterator,
                          final float x,
                          final float y ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public boolean hit( final Rectangle rect, final Shape s,
                      final boolean onStroke ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return false;
  }

  @Override
  public GraphicsConfiguration getDeviceConfiguration() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public void setComposite( final Composite comp ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void setPaint( final Paint paint ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void setStroke( final Stroke s ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void setRenderingHint( final RenderingHints.Key hintKey,
                                final Object hintValue ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public Object getRenderingHint( final RenderingHints.Key hintKey ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public void setRenderingHints( final Map<?, ?> hints ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public void addRenderingHints( final Map<?, ?> hints ) {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
  }

  @Override
  public RenderingHints getRenderingHints() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }

  @Override
  public Graphics create() {
    tally( new Object() {
    }.getClass().getEnclosingMethod().getName() );
    return null;
  }
}
