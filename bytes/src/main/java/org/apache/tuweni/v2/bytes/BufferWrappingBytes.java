// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.apache.tuweni.v2.bytes.Utils.checkElementIndex;
import static org.apache.tuweni.v2.bytes.Utils.checkLength;

import io.vertx.core.buffer.Buffer;

class BufferWrappingBytes extends Bytes {
  protected final Buffer buffer;

  BufferWrappingBytes(Buffer buffer) {
    super(buffer.length());
    this.buffer = buffer;
  }

  BufferWrappingBytes(Buffer buffer, int offset, int length) {
    super(length);
    if (offset == 0 && length == buffer.length()) {
      this.buffer = buffer;
    } else {
      this.buffer = buffer.slice(offset, offset + length);
    }
  }

  @Override
  public byte get(int i) {
    return buffer.getByte(i);
  }

  @Override
  public int getInt(int i) {
    return buffer.getInt(i);
  }

  @Override
  public long getLong(int i) {
    return buffer.getLong(i);
  }

  @Override
  public Bytes slice(int i, int length) {
    int size = buffer.length();
    if (i == 0 && length == size) {
      return this;
    }
    if (length == 0) {
      return EMPTY;
    }

    checkElementIndex(i, size);
    checkLength(size, i, length);

    return new BufferWrappingBytes(buffer.slice(i, i + length));
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromArray(toArrayUnsafe());
  }

  @Override
  public void appendTo(Buffer buffer) {
    buffer.appendBuffer(this.buffer);
  }

  @Override
  public byte[] toArrayUnsafe() {
    return buffer.getBytes();
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (buffer.getByte(i) & bytesArray[offset + i]);
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (buffer.getByte(i) | bytesArray[offset + i]);
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (buffer.getByte(i) ^ bytesArray[offset + i]);
    }
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
      if (buffer.getByte(i) != other.get(i)) {
        return false;
      }
    }

    return true;
  }

  @Override
  protected int computeHashcode() {
    int result = 1;
    for (int i = 0; i < size(); i++) {
      result = 31 * result + buffer.getByte(i);
    }
    return result;
  }
}
