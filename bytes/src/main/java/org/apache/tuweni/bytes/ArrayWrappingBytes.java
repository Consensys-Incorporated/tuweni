// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static org.apache.tuweni.bytes.Checks.checkArgument;
import static org.apache.tuweni.bytes.Checks.checkElementIndex;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;

import io.vertx.core.buffer.Buffer;

class ArrayWrappingBytes extends AbstractBytes {

  // Unaligned multi-byte views over the backing array, one constant per (width, byte order), so
  // that every access site below uses a handle that is a compile-time constant.
  private static final VarHandle LONG_BIG_ENDIAN =
      MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
  private static final VarHandle LONG_LITTLE_ENDIAN =
      MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
  private static final VarHandle INT_BIG_ENDIAN =
      MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
  private static final VarHandle INT_LITTLE_ENDIAN =
      MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);

  protected final byte[] bytes;
  protected final int offset;
  protected final int length;

  ArrayWrappingBytes(byte[] bytes) {
    this(bytes, 0, bytes.length);
  }

  ArrayWrappingBytes(byte[] bytes, int offset, int length) {
    checkArgument(length >= 0, "Invalid negative length");
    if (bytes.length > 0) {
      checkElementIndex(offset, bytes.length);
    }
    checkArgument(
        offset + length <= bytes.length,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        bytes.length - offset,
        offset);

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
    // Check bounds because while the array access would throw, the error message would be confusing
    // for the caller.
    checkElementIndex(i, size());
    return bytes[offset + i];
  }

  @Override
  public int getInt(int i, ByteOrder order) {
    checkElementIndex(i, length);
    if (i > length - 4) {
      throw notEnoughBytes(i, 4, "int");
    }
    if (order == BIG_ENDIAN) {
      return (int) INT_BIG_ENDIAN.get(bytes, offset + i);
    }
    return (int) INT_LITTLE_ENDIAN.get(bytes, offset + i);
  }

  @Override
  public long getLong(int i, ByteOrder order) {
    checkElementIndex(i, length);
    if (i > length - 8) {
      throw notEnoughBytes(i, 8, "long");
    }
    if (order == BIG_ENDIAN) {
      return (long) LONG_BIG_ENDIAN.get(bytes, offset + i);
    }
    return (long) LONG_LITTLE_ENDIAN.get(bytes, offset + i);
  }

  // Kept out of line so that the read methods above stay small enough to be inlined.
  private IndexOutOfBoundsException notEnoughBytes(int i, int width, String type) {
    return new IndexOutOfBoundsException(
        String.format(
            "Value of size %s has not enough bytes to read a %s bytes %s from index %s",
            length, width, type, i));
  }

  @Override
  public Bytes slice(int i, int length) {
    if (i == 0 && length == this.length) {
      return this;
    }
    if (length == 0) {
      return Bytes.EMPTY;
    }

    checkElementIndex(i, this.length);
    checkArgument(
        i + length <= this.length,
        "Provided length %s is too big: the value has size %s and has only %s bytes from %s",
        length,
        this.length,
        this.length - i,
        i);

    return length == Bytes32.SIZE
        ? new ArrayWrappingBytes32(bytes, offset + i)
        : new ArrayWrappingBytes(bytes, offset + i, length);
  }

  // MUST be overridden by mutable implementations
  @Override
  public Bytes copy() {
    if (offset == 0 && length == bytes.length) {
      return this;
    }
    return new ArrayWrappingBytes(toArray());
  }

  @Override
  public MutableBytes mutableCopy() {
    return new MutableArrayWrappingBytes(toArray());
  }

  @Override
  public int commonPrefixLength(Bytes other) {
    if (!(other instanceof ArrayWrappingBytes)) {
      return super.commonPrefixLength(other);
    }
    ArrayWrappingBytes o = (ArrayWrappingBytes) other;
    int i = 0;
    while (i < length && i < o.length && bytes[offset + i] == o.bytes[o.offset + i]) {
      i++;
    }
    return i;
  }

  @Override
  public void update(MessageDigest digest) {
    digest.update(bytes, offset, length);
  }

  @Override
  public void copyTo(MutableBytes destination, int destinationOffset) {
    if (!(destination instanceof MutableArrayWrappingBytes)) {
      super.copyTo(destination, destinationOffset);
      return;
    }

    int size = size();
    if (size == 0) {
      return;
    }

    checkElementIndex(destinationOffset, destination.size());
    checkArgument(
        destination.size() - destinationOffset >= size,
        "Cannot copy %s bytes, destination has only %s bytes from index %s",
        size,
        destination.size() - destinationOffset,
        destinationOffset);

    MutableArrayWrappingBytes d = (MutableArrayWrappingBytes) destination;
    System.arraycopy(bytes, offset, d.bytes, d.offset + destinationOffset, size);
  }

  @Override
  public void appendTo(ByteBuffer byteBuffer) {
    byteBuffer.put(bytes, offset, length);
  }

  @Override
  public void appendTo(Buffer buffer) {
    buffer.appendBytes(bytes, offset, length);
  }

  @Override
  public byte[] toArray() {
    return Arrays.copyOfRange(bytes, offset, offset + length);
  }

  @Override
  public byte[] toArrayUnsafe() {
    if (offset == 0 && length == bytes.length) {
      return bytes;
    }
    return toArray();
  }
}
