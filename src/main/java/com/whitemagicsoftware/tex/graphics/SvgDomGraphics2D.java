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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.StringWriter;

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

/**
 * Use this class to generate a W3C document object model (DOM); see
 * {@link Document} for details. Even uDOM as a dependency seems overkill;
 * see <a href="https://www.w3.org/TR/SVGTiny12/svgudom.html">SVG uDOM</a> for
 * details. To generate SVG strings most efficiently, use {@link SvgGraphics2D}.
 * <p>
 * This class is not thread-safe, but can be reset for performance purposes.
 * </p>
 */
@SuppressWarnings("unused")
public final class SvgDomGraphics2D extends AbstractGraphics2D {
  private static final String LOAD_EXTERNAL_DTD =
      "http://apache.org/xml/features/nonvalidating/load-external-dtd";

  private static final DocumentBuilderFactory FACTORY_DOC = newInstance();
  private static DocumentBuilder BUILDER_DOC;

  static {
    try {
      FACTORY_DOC.setFeature( LOAD_EXTERNAL_DTD, false );
      BUILDER_DOC = FACTORY_DOC.newDocumentBuilder();
    } catch( final Exception ex ) {
      BUILDER_DOC = new NullDocumentBuilder();
    }
  }

  /**
   * Contains path data in a non-thread-safe way.
   */
  private static final StringBuilder sData = new StringBuilder( 16384 );

  private static final String NAMESPACE = "http://www.w3.org/2000/svg";
  private static final String ATTR_NAME_VERSION = "version";
  private static final String ATTR_VALUE_VERSION = "1.1";

  private static final String ATTR_NAME_ID = "id";
  private static final String ATTR_NAME_X = "x";
  private static final String ATTR_NAME_Y = "y";
  private static final String ATTR_NAME_WIDTH = "width";
  private static final String ATTR_NAME_HEIGHT = "height";
  private static final String ATTR_NAME_TRANSFORM = "transform";
  private static final String ATTR_NAME_PATH_FILL_RULE = "fill-rule";
  private static final String ATTR_NAME_PATH_DATA = "d";

  /**
   * Number of decimal places for geometric shapes.
   */
  private static final int DECIMALS_GEOMETRY = 4;

  /**
   * Number of decimal places for matrix transforms.
   */
  private static final int DECIMALS_TRANSFORM = 6;

  /**
   * Filled when drawing paths, not thread-safe.
   */
  private final float[] mCoords = new float[ 6 ];

  /**
   * Transform attribute value, a matrix function.
   */
  private String mTransform = "";

  private final Document mDocument;
  private Element mRoot;

  /**
   * Creates a new instance with a default buffer size. Client classes must
   * call {@link #initialize(int, int)} before using the class to ensure
   * the width and height are added to the document.
   */
  public SvgDomGraphics2D() {
    mDocument = BUILDER_DOC.newDocument();
  }

  /**
   * Resets the SVG buffer to a new state. One of the {@link #initialize}
   * methods must be called before calling drawing primitives.
   *
   * @param w The final document width (in pixels).
   * @param h The final document height (in pixels).
   */
  public void initialize( final int w, final int h ) {
    reset();
    setDimensions( w, h );
  }

  /**
   * Resets the SVG buffer to a new state. One of the {@link #initialize}
   * methods must be called before calling drawing primitives.
   * <p>
   * Sets the SVG document's root-level {@code id} attribute. See the
   * <a href="https://www.w3.org/TR/SVG2/struct.html#SVGElement">W3C Spec</a>
   * for details. This allows developers to mark documents having the same
   * content with the same code, which can allow for performance operations
   * (e.g., avoid transcoding the same document twice).
   * </p>
   *
   * @param id The unique identifier for the {@code <svg>} element.
   * @param w  The final document width (in pixels).
   * @param h  The final document height (in pixels).
   */
  public void initialize( final int id, final int w, final int h ) {
    initialize( w, h );
    mRoot.setAttributeNS( null, ATTR_NAME_ID, Integer.toString( id ) );
  }

  /**
   * Replaces the {@link Document} root node with a new SVG {@link Element}.
   * The root {@link Element} must be initialized before drawing with this
   * instance.
   */
  private void reset() {
    if( mRoot != null ) {
      mDocument.removeChild( mRoot );
    }

    mRoot = mDocument.createElementNS( NAMESPACE, "svg" );
    mRoot.setAttributeNS( null, ATTR_NAME_VERSION, ATTR_VALUE_VERSION );
    mDocument.appendChild( mRoot );
  }

  /**
   * Appends the dimensions, which are the last items added to the
   * {@code <svg>} element.
   *
   * @param w The final document width (in pixels).
   * @param h The final document height (in pixels).
   */
  private void setDimensions( final int w, final int h ) {
    mRoot.setAttributeNS( null, ATTR_NAME_WIDTH, w + "px" );
    mRoot.setAttributeNS( null, ATTR_NAME_HEIGHT, h + "px" );
  }

  @Override
  public void draw( final Shape shape ) {
    final var e = mDocument.createElementNS( NAMESPACE, "g" );
    mRoot.appendChild( e );

    if( !isIdentityTransform() ) {
      e.setAttributeNS( null, ATTR_NAME_TRANSFORM, mTransform );
    }

    appendPath( (Path2D) shape, e );
  }

  private void appendPath( final Path2D path, final Element parent ) {
    final var e = mDocument.createElementNS( NAMESPACE, "path" );
    parent.appendChild( e );

    if( path.getWindingRule() == 0 ) {
      e.setAttributeNS( null, ATTR_NAME_PATH_FILL_RULE, "evenodd" );
    }

    final var iterator = path.getPathIterator( null );

    while( !iterator.isDone() ) {
      switch( iterator.currentSegment( mCoords ) ) {
        case 0:
          sData.append( 'M' )
               .append( toGeometryPrecision( mCoords[ 0 ] ) )
               .append( ' ' )
               .append( toGeometryPrecision( mCoords[ 1 ] ) );
          break;
        case 1:
          sData.append( 'L' )
               .append( toGeometryPrecision( mCoords[ 0 ] ) )
               .append( ' ' )
               .append( toGeometryPrecision( mCoords[ 1 ] ) );
          break;
        case 2:
          sData.append( 'Q' )
               .append( toGeometryPrecision( mCoords[ 0 ] ) )
               .append( ' ' )
               .append( toGeometryPrecision( mCoords[ 1 ] ) )
               .append( ' ' )
               .append( toGeometryPrecision( mCoords[ 2 ] ) )
               .append( ' ' )
               .append( toGeometryPrecision( mCoords[ 3 ] ) );
          break;
        case 3:
          sData.append( 'C' )
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
          break;
        case 4:
          sData.append( 'Z' );
          break;
      }

      iterator.next();
    }

    e.setAttributeNS( null, ATTR_NAME_PATH_DATA, sData.toString() );

    // Clear out the path data for the next export.
    sData.setLength( 0 );
  }

  @Override
  public void fill( final Shape shape ) {
    if( shape instanceof Rectangle2D ) {
      final var e = mDocument.createElementNS( NAMESPACE, "rect" );
      mRoot.appendChild( e );

      final var r = (Rectangle2D) shape;

      e.setAttributeNS( null, ATTR_NAME_X, toGeometryPrecision( r.getX() ) );
      e.setAttributeNS( null, ATTR_NAME_Y, toGeometryPrecision( r.getY() ) );
      e.setAttributeNS(
          null, ATTR_NAME_WIDTH, toGeometryPrecision( r.getWidth() ) );
      e.setAttributeNS(
          null, ATTR_NAME_HEIGHT, toGeometryPrecision( r.getHeight() ) );

      if( !isIdentityTransform() ) {
        e.setAttributeNS( null, ATTR_NAME_TRANSFORM, mTransform );
      }
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
   * Returns the underlying {@link Document} object containing SVG
   * {@link Element}s, provided both initialization and at least one drawing
   * primitive have been called.
   *
   * @return The SVG document as a document object model.
   */
  public Document toDom() {
    return mDocument;
  }

  /**
   * Returns the SVG document as a string. This is a heavyweight method,
   * suitable only for unit tests. No attempts at optimizing this method
   * have been made.
   *
   * @return The SVG document transformed into a string.
   */
  @Override
  public String toString() {
    try( final var writer = new StringWriter() ) {
      final var t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty( OMIT_XML_DECLARATION, "yes" );
      t.transform( new DOMSource( mDocument ), new StreamResult( writer ) );

      return writer.toString();
    } catch( final Exception ex ) {
      return "";
    }
  }
}
