// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.apache.tuweni.v2.bytes.Utils.checkElementIndex;
import static org.apache.tuweni.v2.bytes.Utils.checkLength;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

class ByteBufWrappingBytes extends Bytes {

  protected final ByteBuf byteBuf;

  ByteBufWrappingBytes(ByteBuf byteBuf) {
    super(byteBuf.capacity());
    this.byteBuf = byteBuf;
  }

  ByteBufWrappingBytes(ByteBuf byteBuf, int offset, int length) {
    super(length);
    if (offset == 0 && length == byteBuf.capacity()) {
      this.byteBuf = byteBuf;
    } else {
      this.byteBuf = byteBuf.slice(offset, length);
    }
  }

  @Override
  public byte get(int i) {
    return byteBuf.getByte(i);
  }

  @Override
  public int getInt(int i) {
    return byteBuf.getInt(i);
  }

  @Override
  public long getLong(int i) {
    return byteBuf.getLong(i);
  }

  @Override
  public Bytes slice(int i, int length) {
    int size = byteBuf.capacity();
    if (i == 0 && length == size) {
      return this;
    }
    if (length == 0) {
      return EMPTY;
    }

    checkElementIndex(i, size);
    checkLength(size, i, length);

    return new ByteBufWrappingBytes(byteBuf.slice(i, length));
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromByteBuf(byteBuf, 0, byteBuf.capacity());
  }

  @Override
  public void appendTo(Buffer buffer) {
    buffer.appendBuffer(Buffer.buffer(this.byteBuf));
  }

  @Override
  public byte[] toArrayUnsafe() {
    byte[] array = new byte[byteBuf.capacity()];
    byteBuf.getBytes(0, array);
    return array;
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (byteBuf.getByte(i) & bytesArray[offset + i]);
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (byteBuf.getByte(i) | bytesArray[offset + i]);
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (byteBuf.getByte(i) ^ bytesArray[offset + i]);
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
      if (byteBuf.getByte(i) != other.get(i)) {
        return false;
      }
    }

    return true;
  }

  @Override
  protected int computeHashcode() {
    int result = 1;
    for (int i = 0; i < size(); i++) {
      result = 31 * result + byteBuf.getByte(i);
    }
    return result;
  }
}
