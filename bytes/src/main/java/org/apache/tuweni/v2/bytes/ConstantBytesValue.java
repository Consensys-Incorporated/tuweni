// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.apache.tuweni.v2.bytes.Utils.checkArgument;
import static org.apache.tuweni.v2.bytes.Utils.checkElementIndex;
import static org.apache.tuweni.v2.bytes.Utils.checkLength;

import java.util.Arrays;

/**
 * A Bytes value with just one constant value throughout. Ideal to avoid allocating large byte
 * arrays filled with the same byte.
 */
class ConstantBytesValue extends Bytes {

  private final byte value;

  ConstantBytesValue(byte b, int size) {
    super(size);
    this.value = b;
  }

  @Override
  public byte get(int i) {
    return this.value;
  }

  @Override
  public Bytes slice(int offset, int length) {
    checkArgument(length >= 0, "Invalid negative length");
    if (size() > 0) {
      checkElementIndex(offset, size());
    }
    checkLength(size(), offset, length);
    if (length == size()) {
      return this;
    }
    return new ConstantBytesValue(this.value, length);
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      bytesArray[offset + i] = (byte) (value & bytesArray[offset + i]);
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      bytesArray[offset + i] = (byte) (value | bytesArray[offset + i]);
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      bytesArray[offset + i] = (byte) (value ^ bytesArray[offset + i]);
    }
  }

  @Override
  public MutableBytes mutableCopy() {
    MutableBytes mutableBytes = MutableBytes.create(size());
    mutableBytes.fill(value);
    return mutableBytes;
  }

  @Override
  public byte[] toArrayUnsafe() {
    byte[] array = new byte[size()];
    Arrays.fill(array, value);
    return array;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Bytes other)) {
      return false;
    }

    if (this.size() != other.size()) {
      return false;
    }

    for (int i = 0; i < size(); i++) {
      if (value != other.get(i)) {
        return false;
      }
    }

    return true;
  }

  @Override
  protected int computeHashcode() {
    int result = 1;
    for (int i = 0; i < size(); i++) {
      result = 31 * result + value;
    }
    return result;
  }
}
