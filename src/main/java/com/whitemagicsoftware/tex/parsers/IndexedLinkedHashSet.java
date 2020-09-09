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
package com.whitemagicsoftware.tex.parsers;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extends {@link LinkedHashSet} with the ability to retrieve a key's index
 * into the underlying insertion-order {@link Set}.
 *
 * @param <K> The type of key included in the set.
 */
public class IndexedLinkedHashSet<K> extends LinkedHashSet<K> {
  private final Map<K, Integer> mIndexes = new HashMap<>();

  @Override
  public boolean add( final K key ) {
    mIndexes.put( key, size() );
    return super.add( key );
  }

  @Override
  public void clear() {
    super.clear();
    mIndexes.clear();
  }

  @Override
  public boolean remove( final Object key ) {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds the given {@code key} then returns the index corresponding to that
   * {@code key}.
   *
   * @param key The key to add to the {@link Set}.
   * @return The index of the key that was added to the list, or -1 if the
   * key could not be added to the {@link Set}.
   */
  public int addIndex( final K key ) {
    if( !contains( key ) ) {
      add( key );
    }

    return getIndex( key );
  }

  /**
   * Returns the index corresponding to the {@code key} that was added this
   * {@link Set} previously.
   *
   * @param key The key to find in the list.
   * @return The index of the key that was added to the list, or -1 if the
   * key has not been added to the {@link Set}.
   */
  public int getIndex( final K key ) {
    return mIndexes.getOrDefault( key, -1 );
  }
}
