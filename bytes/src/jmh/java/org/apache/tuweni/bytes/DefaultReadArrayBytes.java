// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes;

import static org.apache.tuweni.bytes.Checks.checkElementIndex;

/**
 * Benchmark baseline: an array-backed {@link Bytes} whose {@code get(int)} is the one {@link
 * ArrayWrappingBytes} had before it overrode {@code getLong} and {@code getInt}, so that those two
 * methods run the default {@link Bytes} implementations (one bounds-checked {@code get} per byte).
 */
final class DefaultReadArrayBytes extends AbstractBytes {

  private final byte[] bytes;
  private final int offset;
  private final int length;

  DefaultReadArrayBytes(byte[] bytes, int offset, int length) {
    this.bytes = bytes;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public int size() {
    return length;
  }

  @Override
  public byte get(int i) {
    checkElementIndex(i, size());
    return bytes[offset + i];
  }

  @Override
  public Bytes slice(int i, int length) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Bytes copy() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MutableBytes mutableCopy() {
    throw new UnsupportedOperationException();
  }
}
