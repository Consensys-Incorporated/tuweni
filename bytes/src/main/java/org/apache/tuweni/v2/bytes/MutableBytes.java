// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static java.lang.String.format;
import static org.apache.tuweni.v2.bytes.Utils.checkArgument;
import static org.apache.tuweni.v2.bytes.Utils.checkElementIndex;
import static org.apache.tuweni.v2.bytes.Utils.checkLength;
import static org.apache.tuweni.v2.bytes.Utils.checkNotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

/** A class for doing modifications on a {@link Bytes} value without modifying the original. */
public class MutableBytes extends ArrayWrappingBytes {

  MutableBytes(int size) {
    super(new byte[size]);
  }

  MutableBytes(byte[] bytesArray) {
    super(new byte[bytesArray.length]);
    System.arraycopy(bytesArray, 0, bytes, 0, bytesArray.length);
  }

  MutableBytes(byte[] bytesArray, int offset, int length) {
    super(new byte[length]);
    System.arraycopy(bytesArray, offset, bytes, 0, length);
  }

  /**
   * Create a new mutable bytes value.
   *
   * @param size The size of the returned value.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes create(int size) {
    return new MutableBytes(size);
  }

  /**
   * Create a {@link MutableBytes} value from a byte array.
   *
   * @param value The value to wrap.
   * @return A {@link MutableBytes} value wrapping {@code value}.
   */
  public static MutableBytes fromArray(byte[] value) {
    checkNotNull(value);
    if (value.length == 0) {
      return MutableBytes.create(0);
    }
    return new MutableBytes(value);
  }

  /**
   * Wrap a slice of a byte array as a {@link MutableBytes} value.
   *
   * <p>Note that value is not copied and thus any future update to {@code value} within the slice
   * will be reflected in the returned value.
   *
   * @param value The value to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, o, l).get(0) == value[o]}.
   * @param length The length of the resulting value.
   * @return A {@link Bytes} value that expose the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + length} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.length > 0 && offset >=
   *     value.length)}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > value.length}.
   */
  public static MutableBytes fromArray(byte[] value, int offset, int length) {
    checkNotNull(value);
    checkArgument(length >= 0, "Invalid negative length");
    if (value.length > 0) {
      checkElementIndex(offset, value.length);
    }
    checkLength(value.length, offset, length);
    if (length == 0) {
      return MutableBytes.create(0);
    }
    return new MutableBytes(value, offset, length);
  }

  /**
   * Wrap a full Vert.x {@link Buffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value.
   *
   * @param buffer The buffer to wrap.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes fromBuffer(Buffer buffer) {
    checkNotNull(buffer);
    if (buffer.length() == 0) {
      return MutableBytes.create(0);
    }
    return new MutableBytes(buffer.getBytes());
  }

  /**
   * Wrap a slice of a Vert.x {@link Buffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value,
   * and any change to the returned value will be reflected in the buffer.
   *
   * @param buffer The buffer to wrap.
   * @param offset The offset in {@code buffer} from which to expose the bytes in the returned
   *     value. That is, {@code wrapBuffer(buffer, i, 1).get(0) == buffer.getByte(i)}.
   * @param length The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (buffer.length() > 0 && offset >=
   *     buffer.length())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > buffer.length()}.
   */
  public static MutableBytes fromBuffer(Buffer buffer, int offset, int length) {
    checkNotNull(buffer);
    checkArgument(length >= 0, "Invalid negative length");
    if (buffer.length() > 0) {
      checkElementIndex(offset, buffer.length());
    }
    checkLength(buffer.length(), offset, length);
    if (length == 0) {
      return MutableBytes.create(0);
    }
    return new MutableBytes(buffer.getBytes(), offset, length);
  }

  /**
   * Wrap a full Netty {@link ByteBuf} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value.
   *
   * @param byteBuf The {@link ByteBuf} to wrap.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes fromByteBuf(ByteBuf byteBuf) {
    checkNotNull(byteBuf);
    if (byteBuf.capacity() == 0) {
      return MutableBytes.create(0);
    }
    MutableBytes mutableBytes = MutableBytes.create(byteBuf.capacity());
    byteBuf.getBytes(0, mutableBytes.bytes);
    return mutableBytes;
  }

  /**
   * Wrap a slice of a Netty {@link ByteBuf} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value,
   * and any change to the returned value will be reflected in the buffer.
   *
   * @param byteBuf The {@link ByteBuf} to wrap.
   * @param offset The offset in {@code byteBuf} from which to expose the bytes in the returned
   *     value. That is, {@code wrapByteBuf(byteBuf, i, 1).get(0) == byteBuf.getByte(i)}.
   * @param length The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (byteBuf.capacity() > 0 && offset >=
   *     byteBuf.capacity())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > byteBuf.capacity()}.
   */
  public static MutableBytes fromByteBuf(ByteBuf byteBuf, int offset, int length) {
    checkNotNull(byteBuf);
    checkArgument(length >= 0, "Invalid negative length");
    final int byteBufLength = byteBuf.capacity();
    if (byteBufLength > 0) {
      checkElementIndex(offset, byteBuf.capacity());
    }
    checkLength(byteBufLength, offset, length);
    if (length == 0) {
      return MutableBytes.create(0);
    }
    MutableBytes mutableBytes = MutableBytes.create(length);
    byteBuf.getBytes(offset, mutableBytes.bytes);
    return mutableBytes;
  }

  /**
   * Wrap a full Java NIO {@link ByteBuffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value.
   *
   * @param byteBuffer The {@link ByteBuffer} to wrap.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes fromByteBuffer(ByteBuffer byteBuffer) {
    checkNotNull(byteBuffer);
    if (byteBuffer.limit() == 0) {
      return MutableBytes.create(0);
    }
    MutableBytes mutableBytes = MutableBytes.create(byteBuffer.limit());
    byteBuffer.get(0, mutableBytes.bytes);
    return mutableBytes;
  }

  /**
   * Wrap a slice of a Java NIO {@link ByteBuffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value,
   * and any change to the returned value will be reflected in the buffer.
   *
   * @param byteBuffer The {@link ByteBuffer} to wrap.
   * @param offset The offset in {@code byteBuffer} from which to expose the bytes in the returned
   *     value. That is, {@code wrapByteBuffer(byteBuffer, i, 1).get(0) == byteBuffer.getByte(i)}.
   * @param length The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (byteBuffer.limit() > 0 && offset >=
   *     byteBuffer.limit())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > byteBuffer.limit()}.
   */
  public static MutableBytes fromByteBuffer(ByteBuffer byteBuffer, int offset, int length) {
    checkNotNull(byteBuffer);
    checkArgument(length >= 0, "Invalid negative length");
    final int byteBufferLength = byteBuffer.limit();
    if (byteBufferLength > 0) {
      checkElementIndex(offset, byteBuffer.limit());
    }
    checkLength(byteBufferLength, offset, length);
    if (length == 0) {
      return MutableBytes.create(0);
    }
    MutableBytes mutableBytes = MutableBytes.create(length);
    byteBuffer.get(offset, mutableBytes.bytes);
    return mutableBytes;
  }

  /**
   * Create a value that contains the specified bytes in their specified order.
   *
   * @param bytes The bytes that must compose the returned value.
   * @return A value containing the specified bytes.
   */
  public static MutableBytes of(byte... bytes) {
    return fromArray(bytes);
  }

  /**
   * Create a value that contains the specified bytes in their specified order.
   *
   * @param bytes The bytes.
   * @return A value containing bytes are the one from {@code bytes}.
   * @throws IllegalArgumentException if any of the specified would be truncated when storing as a
   *     byte.
   */
  public static MutableBytes of(int... bytes) {
    checkNotNull(bytes);
    byte[] result = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      int b = bytes[i];
      checkArgument(b == (((byte) b) & 0xff), "%sth value %s does not fit a byte", i + 1, b);
      result[i] = (byte) b;
    }
    return fromArray(result);
  }

  /**
   * Set a byte in this value.
   *
   * @param index The offset of the bytes to set.
   * @param bytes The value to set bytes to.
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {offset >= size()}.
   * @throws IllegalArgumentException if {@code offset + bytes.size() > this.length}.
   */
  public void set(int index, Bytes bytes) {
    checkNotNull(bytes);
    if (bytes.isEmpty()) {
      return;
    }
    checkElementIndex(index, size);
    checkLength(this.bytes.length, index, bytes.size());
    for (int i = 0; i < bytes.size(); i++) {
      set(i + index, bytes.get(i));
    }
  }

  /**
   * Set a byte array in this value.
   *
   * @param index The offset of the bytes to set.
   * @param bytes The value to set bytes to.
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {offset >= bytes.length}.
   * @throws IllegalArgumentException if {@code offset + bytes.length > this.length}.
   */
  public void set(int index, byte[] bytes) {
    checkNotNull(bytes);
    if (bytes.length == 0) {
      return;
    }
    checkElementIndex(index, size);
    checkLength(this.bytes.length, index, bytes.length);
    for (int i = 0; i < bytes.length; i++) {
      set(i + index, bytes[i]);
    }
  }

  /**
   * Set the 4 bytes starting at the specified index to the specified integer value.
   *
   * @param index The index, which must less than or equal to {@code size() - 4}.
   * @param value The integer value.
   * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index > size() - 4}.
   */
  public void setInt(int index, int value) {
    checkElementIndex(index, size);
    if (index > (size - 4)) {
      throw new IndexOutOfBoundsException(
          format(
              "Value of size %s has not enough bytes to write a 4 bytes int from index %s",
              size, index));
    }

    set(index++, (byte) (value >>> 24));
    set(index++, (byte) ((value >>> 16) & 0xFF));
    set(index++, (byte) ((value >>> 8) & 0xFF));
    set(index, (byte) (value & 0xFF));
  }

  /**
   * Set the 8 bytes starting at the specified index to the specified long value.
   *
   * @param index The index, which must less than or equal to {@code size() - 8}.
   * @param value The long value.
   * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index > size() - 8}.
   */
  public void setLong(int index, long value) {
    checkElementIndex(index, size);
    if (index > (size - 8)) {
      throw new IndexOutOfBoundsException(
          format(
              "Value of length %s has not enough bytes to write a 8 bytes long from index %s",
              size, index));
    }

    set(index++, (byte) (value >>> 56));
    set(index++, (byte) ((value >>> 48) & 0xFF));
    set(index++, (byte) ((value >>> 40) & 0xFF));
    set(index++, (byte) ((value >>> 32) & 0xFF));
    set(index++, (byte) ((value >>> 24) & 0xFF));
    set(index++, (byte) ((value >>> 16) & 0xFF));
    set(index++, (byte) ((value >>> 8) & 0xFF));
    set(index, (byte) (value & 0xFF));
  }

  /**
   * Set a byte in this value.
   *
   * @param index The index of the byte to set.
   * @param b The value to set that byte to.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {i >= size()}.
   */
  public void set(int index, byte b) {
    checkElementIndex(index, size);
    bytes[index] = b;
    hashCode = null;
  }

  /**
   * Increments the value of the bytes by 1, treating the value as big endian.
   *
   * <p>If incrementing overflows the value then all bits flip, i.e. incrementing 0xFFFF will return
   * 0x0000.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes increment() {
    for (int i = size - 1; i >= 0; --i) {
      if (bytes[i] == (byte) 0xFF) {
        bytes[i] = (byte) 0x00;
      } else {
        byte currentValue = bytes[i];
        bytes[i] = ++currentValue;
        break;
      }
    }
    hashCode = null;
    return this;
  }

  /**
   * Decrements the value of the bytes by 1, treating the value as big endian.
   *
   * <p>If decrementing underflows the value then all bits flip, i.e. decrementing 0x0000 will
   * return 0xFFFF.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes decrement() {
    for (int i = size - 1; i >= 0; --i) {
      if (bytes[i] == (byte) 0x00) {
        bytes[i] = (byte) 0xFF;
      } else {
        byte currentValue = bytes[i];
        bytes[i] = --currentValue;
        break;
      }
    }
    hashCode = null;
    return this;
  }

  /**
   * Fill all the bytes of this value with the specified byte.
   *
   * @param b The byte to use to fill the value.
   * @return This mutable bytes instance.
   */
  public MutableBytes fill(byte b) {
    for (int i = 0; i < size; i++) {
      bytes[i] = b;
    }
    hashCode = null;
    return this;
  }

  /**
   * Set all bytes in this value to 0.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes clear() {
    fill((byte) 0);
    return this;
  }

  /**
   * Computes the reverse array of bytes of the current bytes.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes reverse() {
    byte[] reverse = new byte[size];
    for (int i = 0; i < size; i++) {
      reverse[size - 1 - i] = bytes[i];
    }
    bytes = reverse;
    hashCode = null;
    return this;
  }

  /**
   * Calculate a bit-wise AND of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return This mutable bytes instance.
   */
  public MutableBytes and(Bytes other) {
    checkNotNull(other);
    int otherSize = other.size();
    if (size == otherSize) {
      other.and(bytes, 0, size);
      hashCode = null;
      return this;
    }

    int otherOffset = 0;
    if (size < otherSize) {
      byte[] newBytesArray = new byte[otherSize];
      System.arraycopy(bytes, 0, newBytesArray, otherSize - size, size);
      bytes = newBytesArray;
      size = otherSize;
    } else {
      Arrays.fill(bytes, 0, size - otherSize, (byte) 0);
      otherOffset = size - otherSize;
    }
    other.and(bytes, otherOffset, otherSize);
    hashCode = null;
    return this;
  }

  /**
   * Calculate a bit-wise OR of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return This mutable bytes instance.
   */
  public MutableBytes or(Bytes other) {
    checkNotNull(other);
    int otherSize = other.size();
    if (size == otherSize) {
      other.or(bytes, 0, size);
      hashCode = null;
      return this;
    }

    int otherOffset = 0;
    if (size < otherSize) {
      byte[] newBytesArray = new byte[otherSize];
      System.arraycopy(bytes, 0, newBytesArray, otherSize - size, size);
      bytes = newBytesArray;
      size = otherSize;
    } else {
      otherOffset = size - otherSize;
    }
    other.or(bytes, otherOffset, otherSize);
    hashCode = null;
    return this;
  }

  /**
   * Calculate a bit-wise XOR of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return This mutable bytes instance.
   */
  public MutableBytes xor(Bytes other) {
    checkNotNull(other);
    int otherSize = other.size();
    if (size == otherSize) {
      other.xor(bytes, 0, size);
      hashCode = null;
      return this;
    }

    int otherOffset = 0;
    if (size < otherSize) {
      byte[] newBytesArray = new byte[otherSize];
      System.arraycopy(bytes, 0, newBytesArray, otherSize - size, size);
      bytes = newBytesArray;
      size = otherSize;
    } else {
      otherOffset = size - otherSize;
    }
    other.xor(bytes, otherOffset, otherSize);
    hashCode = null;
    return this;
  }

  /**
   * Calculate a bit-wise NOT of these bytes.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes not() {
    for (int i = 0; i < size; i++) {
      bytes[i] = (byte) ~bytes[i];
    }
    hashCode = null;
    return this;
  }

  /**
   * Shift all bits in this value to the right.
   *
   * @param distance The number of bits to shift by.
   * @return This mutable bytes instance.
   */
  public MutableBytes shiftRight(int distance) {
    checkArgument(distance >= 0, "Invalid negative distance");
    if (distance == 0 || size() == 0) {
      return this;
    }
    distance = Math.min(distance, size * 8);
    int byteShift = distance / 8;
    int bitShift = distance % 8;

    if (byteShift > 0) {
      for (int i = size - 1; i >= 0; i--) {
        byte previousByte = (i < byteShift) ? 0 : bytes[i - byteShift];
        bytes[i] = previousByte;
      }
    }

    if (bitShift > 0) {
      for (int i = size - 1; i >= 0; i--) {
        byte currentByte = bytes[i];
        byte previousByte = (i == 0) ? 0 : bytes[i - 1];
        int rightSide = (currentByte & 0XFF) >>> bitShift;
        int leftSide = previousByte << (8 - bitShift);
        bytes[i] = (byte) (leftSide | rightSide);
      }
    }
    hashCode = null;
    return this;
  }

  /**
   * Shift all bits in this value to the left.
   *
   * @param distance The number of bits to shift by.
   * @return This mutable bytes instance.
   */
  public MutableBytes shiftLeft(int distance) {
    checkArgument(distance >= 0, "Invalid negative distance");
    if (distance == 0 || size() == 0) {
      return this;
    }
    distance = Math.min(distance, size * 8);
    int byteShift = distance / 8;
    int bitShift = distance % 8;

    if (byteShift > 0) {
      for (int i = 0; i < size; i++) {
        byte nextByte = (i + byteShift < size) ? bytes[i + byteShift] : 0;
        bytes[i] = nextByte;
      }
    }

    if (bitShift > 0) {
      for (int i = 0; i < size; i++) {
        byte currentByte = bytes[i];
        byte nextByte = (i == size - 1) ? 0 : bytes[i + 1];
        int leftSide = currentByte << bitShift;
        int rightSide = (nextByte & 0XFF) >>> (8 - bitShift);
        bytes[i] = (byte) (leftSide | rightSide);
      }
    }
    hashCode = null;
    return this;
  }

  /**
   * Left pad these mutable values with zero bytes up to the specified length. Resulting bytes are
   * guaranteed to have at least {@code length} bytes in length but not necessarily that exact
   * amount. If length already exceeds {@code length} then bytes are not modified.
   *
   * @param length The new length of the bytes.
   * @throws IllegalArgumentException if {@code length} is negative.
   * @return This mutable bytes instance.
   */
  public MutableBytes leftPad(int length) {
    checkArgument(length >= 0, "Invalid negative length");
    if (length <= size) {
      return this;
    }
    byte[] newBytesArray = new byte[length];
    System.arraycopy(bytes, 0, newBytesArray, length - size, size);
    bytes = newBytesArray;
    size = length;
    hashCode = null;
    return this;
  }

  /**
   * Right pad these mutable values with zero bytes up to the specified length. Resulting bytes are
   * guaranteed to have at least {@code length} bytes in length but not necessarily that exact
   * amount. If length already exceeds {@code length} then bytes are not modified.
   *
   * @param length The new length of the bytes.
   * @throws IllegalArgumentException if {@code length} is negative.
   * @return This mutable bytes instance.
   */
  public MutableBytes rightPad(int length) {
    checkArgument(length >= 0, "Invalid negative length");
    if (length <= size) {
      return this;
    }
    byte[] newBytesArray = new byte[length];
    System.arraycopy(bytes, 0, newBytesArray, 0, size);
    bytes = newBytesArray;
    size = length;
    hashCode = null;
    return this;
  }

  public byte[] toArray() {
    return toArrayUnsafe();
  }

  /**
   * Parse a hexadecimal string into a {@link MutableBytes} value.
   *
   * <p>This method requires that {@code str} have an even length.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x".
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation, or is of an odd length.
   */
  public static MutableBytes fromHexString(CharSequence str) {
    checkNotNull(str);
    return MutableBytes.fromArray(BytesValues.fromRawHexString(str, -1, false));
  }
}
