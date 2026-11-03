// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.units.bigints;

import static org.apache.tuweni.v2.bytes.Utils.checkElementIndex;

import org.apache.tuweni.v2.bytes.Bytes;
import org.apache.tuweni.v2.bytes.MutableBytes;

import java.math.BigInteger;

/**
 * An unsigned 64-bit precision number.
 *
 * <p>This is a raw 64-bit precision unsigned number of no particular unit.
 */
public final class UInt64 extends Bytes {
  private static final int MAX_CONSTANT = 64;
  private static UInt64[] CONSTANTS = new UInt64[MAX_CONSTANT + 1];

  static {
    CONSTANTS[0] = new UInt64(0);
    for (int i = 1; i <= MAX_CONSTANT; ++i) {
      CONSTANTS[i] = new UInt64(i);
    }
  }

  /** The minimum value of a UInt64 */
  public static final UInt64 MIN_VALUE = valueOf(0);

  /** The maximum value of a UInt64 */
  public static final UInt64 MAX_VALUE = new UInt64(~0L);

  /** The value 0 */
  public static final UInt64 ZERO = valueOf(0);

  /** The value 1 */
  public static final UInt64 ONE = valueOf(1);

  private static final BigInteger P_2_64 = BigInteger.valueOf(2).pow(64);

  private final long value;

  /**
   * Return a {@code UInt64} containing the specified value.
   *
   * @param value The value to create a {@code UInt64} for.
   * @return A {@code UInt64} containing the specified value.
   * @throws IllegalArgumentException If the value is negative.
   */
  public static UInt64 valueOf(long value) {
    if (value < 0) {
      throw new IllegalArgumentException("Argument must be positive");
    }
    return create(value);
  }

  /**
   * Return a {@link UInt64} containing a random value.
   *
   * @return a {@link UInt64} containing a random value
   */
  public static UInt64 random() {
    return UInt64.fromBytes(Bytes.random(8));
  }

  /**
   * Return a {@link UInt64} containing the specified value.
   *
   * @param value the value to create a {@link UInt64} for
   * @return a {@link UInt64} containing the specified value
   * @throws IllegalArgumentException if the value is negative or too large to be represented as a
   *     UInt64
   */
  public static UInt64 valueOf(BigInteger value) {
    if (value.signum() < 0) {
      throw new IllegalArgumentException("Argument must be positive");
    }
    if (value.bitLength() > 64) {
      throw new IllegalArgumentException("Argument is too large to represent a UInt64");
    }
    return create(value.longValue());
  }

  /**
   * Return a {@link UInt64} containing the value described by the specified bytes.
   *
   * @param bytes The bytes containing a {@link UInt64}.
   * @return A {@link UInt64} containing the specified value.
   * @throws IllegalArgumentException if {@code bytes.size() > 8}.
   */
  public static UInt64 fromBytes(Bytes bytes) {
    if (bytes.size() > 8) {
      throw new IllegalArgumentException("Argument is greater than 8 bytes");
    }
    return create(bytes.toLong());
  }

  /**
   * Parse a hexadecimal string into a {@link UInt64}.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x". That
   *     representation may contain less than 8 bytes, in which case the result is left padded with
   *     zeros.
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation or contains more than 8 bytes.
   */
  public static UInt64 fromHexString(String str) {
    return fromBytes(Bytes.fromHexStringLenient(str));
  }

  private static UInt64 create(long value) {
    if (value >= 0 && value <= MAX_CONSTANT) {
      return CONSTANTS[(int) value];
    }
    return new UInt64(value);
  }

  private UInt64(long value) {
    super(8);
    this.value = value;
  }

  @Override
  public boolean isZero() {
    return this.value == 0;
  }

  public UInt64 add(UInt64 value) {
    if (value.value == 0) {
      return this;
    }
    if (this.value == 0) {
      return value;
    }
    return create(this.value + value.value);
  }

  public UInt64 add(long value) {
    if (value == 0) {
      return this;
    }
    return create(this.value + value);
  }

  public UInt64 addMod(UInt64 value, UInt64 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("addMod with zero modulus");
    }
    return create(toBigInteger().add(value.toBigInteger()).mod(modulus.toBigInteger()).longValue());
  }

  public UInt64 addMod(long value, UInt64 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("addMod with zero modulus");
    }
    return create(
        toBigInteger().add(BigInteger.valueOf(value)).mod(modulus.toBigInteger()).longValue());
  }

  public UInt64 addMod(long value, long modulus) {
    if (modulus == 0) {
      throw new ArithmeticException("addMod with zero modulus");
    }
    if (modulus < 0) {
      throw new ArithmeticException("addMod unsigned with negative modulus");
    }
    return create(
        toBigInteger().add(BigInteger.valueOf(value)).mod(BigInteger.valueOf(modulus)).longValue());
  }

  public UInt64 subtract(UInt64 value) {
    if (value.isZero()) {
      return this;
    }
    return create(this.value - value.value);
  }

  public UInt64 subtract(long value) {
    return add(-value);
  }

  public UInt64 multiply(UInt64 value) {
    if (this.value == 0 || value.value == 0) {
      return ZERO;
    }
    if (value.value == 1) {
      return this;
    }
    return create(this.value * value.value);
  }

  public UInt64 multiply(long value) {
    if (value < 0) {
      throw new ArithmeticException("multiply unsigned by negative");
    }
    if (value == 0 || this.value == 0) {
      return ZERO;
    }
    if (value == 1) {
      return this;
    }
    return create(this.value * value);
  }

  public UInt64 multiplyMod(UInt64 value, UInt64 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("multiplyMod with zero modulus");
    }
    if (this.value == 0 || value.value == 0) {
      return ZERO;
    }
    if (value.value == 1) {
      return mod(modulus);
    }
    return create(
        toBigInteger().multiply(value.toBigInteger()).mod(modulus.toBigInteger()).longValue());
  }

  public UInt64 multiplyMod(long value, UInt64 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("multiplyMod with zero modulus");
    }
    if (value == 0 || this.value == 0) {
      return ZERO;
    }
    if (value == 1) {
      return mod(modulus);
    }
    if (value < 0) {
      throw new ArithmeticException("multiplyMod unsigned by negative");
    }
    return create(
        toBigInteger().multiply(BigInteger.valueOf(value)).mod(modulus.toBigInteger()).longValue());
  }

  public UInt64 multiplyMod(long value, long modulus) {
    if (modulus == 0) {
      throw new ArithmeticException("multiplyMod with zero modulus");
    }
    if (modulus < 0) {
      throw new ArithmeticException("multiplyMod unsigned with negative modulus");
    }
    if (value == 0 || this.value == 0) {
      return ZERO;
    }
    if (value == 1) {
      return mod(modulus);
    }
    if (value < 0) {
      throw new ArithmeticException("multiplyMod unsigned by negative");
    }
    return create(
        toBigInteger()
            .multiply(BigInteger.valueOf(value))
            .mod(BigInteger.valueOf(modulus))
            .longValue());
  }

  public UInt64 divide(UInt64 value) {
    if (value.value == 0) {
      throw new ArithmeticException("divide by zero");
    }
    if (value.value == 1) {
      return this;
    }
    return create(toBigInteger().divide(value.toBigInteger()).longValue());
  }

  public UInt64 divide(long value) {
    if (value == 0) {
      throw new ArithmeticException("divide by zero");
    }
    if (value < 0) {
      throw new ArithmeticException("divide unsigned by negative");
    }
    if (value == 1) {
      return this;
    }
    if (isPowerOf2(value)) {
      return fromBytes(mutableCopy().shiftRight(log2(value)));
    }
    return create(toBigInteger().divide(BigInteger.valueOf(value)).longValue());
  }

  public UInt64 pow(UInt64 exponent) {
    return create(toBigInteger().modPow(exponent.toBigInteger(), P_2_64).longValue());
  }

  public UInt64 pow(long exponent) {
    return create(toBigInteger().modPow(BigInteger.valueOf(exponent), P_2_64).longValue());
  }

  public UInt64 mod(UInt64 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("mod by zero");
    }
    return create(toBigInteger().mod(modulus.toBigInteger()).longValue());
  }

  public UInt64 mod(long modulus) {
    if (modulus == 0) {
      throw new ArithmeticException("mod by zero");
    }
    if (modulus < 0) {
      throw new ArithmeticException("mod by negative");
    }
    return create(this.value % modulus);
  }

  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof UInt64 other)) {
      return false;
    }
    return this.value == other.value;
  }

  @Override
  public int computeHashcode() {
    return Long.hashCode(this.value);
  }

  public int compareTo(UInt64 other) {
    return Long.compareUnsigned(this.value, other.value);
  }

  public boolean fitsInt() {
    return this.value >= 0 && this.value <= Integer.MAX_VALUE;
  }

  public int intValue() {
    if (!fitsInt()) {
      throw new ArithmeticException("Value does not fit a 4 byte int");
    }
    return (int) this.value;
  }

  public boolean fitsLong() {
    return this.value >= 0;
  }

  @Override
  public byte get(int i) {
    checkElementIndex(i, size());
    return Utils.unpackByte(value, i);
  }

  @Override
  public long toLong() {
    if (!fitsLong()) {
      throw new ArithmeticException("Value does not fit a 8 byte long");
    }
    return this.value;
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toArrayUnsafe());
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (get(i) & bytesArray[offset + i]);
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (get(i) | bytesArray[offset + i]);
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (get(i) ^ bytesArray[offset + i]);
    }
  }

  public UInt64 toUInt64() {
    return this;
  }

  public Bytes toBytes() {
    MutableBytes bytes = MutableBytes.create(8);
    bytes.setLong(0, this.value);
    return bytes;
  }

  public Bytes toMinimalBytes() {
    int requiredBytes = 8 - (Long.numberOfLeadingZeros(this.value) / 8);
    MutableBytes bytes = MutableBytes.create(requiredBytes);
    int j = 0;
    switch (requiredBytes) {
      case 8:
        bytes.set(j++, (byte) (this.value >>> 56));
        // fall through
      case 7:
        bytes.set(j++, (byte) ((this.value >>> 48) & 0xFF));
        // fall through
      case 6:
        bytes.set(j++, (byte) ((this.value >>> 40) & 0xFF));
        // fall through
      case 5:
        bytes.set(j++, (byte) ((this.value >>> 32) & 0xFF));
        // fall through
      case 4:
        bytes.set(j++, (byte) ((this.value >>> 24) & 0xFF));
        // fall through
      case 3:
        bytes.set(j++, (byte) ((this.value >>> 16) & 0xFF));
        // fall through
      case 2:
        bytes.set(j++, (byte) ((this.value >>> 8) & 0xFF));
        // fall through
      case 1:
        bytes.set(j, (byte) (this.value & 0xFF));
    }
    return bytes;
  }

  @Override
  public int numberOfLeadingZeros() {
    return Long.numberOfLeadingZeros(this.value);
  }

  @Override
  public int bitLength() {
    return 64 - Long.numberOfLeadingZeros(this.value);
  }

  @Override
  public Bytes slice(int i, int length) {
    return toBytes().slice(i, length);
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromArray(toArrayUnsafe());
  }

  @Override
  public byte[] toArrayUnsafe() {
    byte[] bytesArray = new byte[8];
    bytesArray[0] = (byte) ((this.value >>> 56) & 0xFF);
    bytesArray[1] = (byte) ((this.value >>> 48) & 0xFF);
    bytesArray[2] = (byte) ((this.value >>> 40) & 0xFF);
    bytesArray[3] = (byte) ((this.value >>> 32) & 0xFF);
    bytesArray[4] = (byte) ((this.value >>> 24) & 0xFF);
    bytesArray[5] = (byte) ((this.value >>> 16) & 0xFF);
    bytesArray[6] = (byte) ((this.value >>> 8) & 0xFF);
    bytesArray[7] = (byte) (this.value & 0xFF);
    return bytesArray;
  }

  private static boolean isPowerOf2(long n) {
    assert n > 0;
    return (n & (n - 1)) == 0;
  }

  private static int log2(long v) {
    assert v > 0;
    return 63 - Long.numberOfLeadingZeros(v);
  }

  /**
   * Returns a value that is {@code (this + value)}.
   *
   * @param value the amount to be added to this value
   * @return {@code this + value}
   * @throws ArithmeticException if the result of the addition overflows
   */
  public UInt64 addExact(UInt64 value) {
    UInt64 result = add(value);
    if (compareTo(result) > 0) {
      throw new ArithmeticException("UInt64 overflow");
    }
    return result;
  }

  /**
   * Returns a value that is {@code (this + value)}.
   *
   * @param value the amount to be added to this value
   * @return {@code this + value}
   * @throws ArithmeticException if the result of the addition overflows
   */
  public UInt64 addExact(long value) {
    UInt64 result = add(value);
    if ((value > 0 && compareTo(result) > 0) || (value < 0 && compareTo(result) < 0)) {
      throw new ArithmeticException("UInt64 overflow");
    }
    return result;
  }

  /**
   * Returns a value that is {@code (this - value)}.
   *
   * @param value the amount to be subtracted to this value
   * @return {@code this - value}
   * @throws ArithmeticException if the result of the subtraction overflows
   */
  public UInt64 subtractExact(UInt64 value) {
    UInt64 result = subtract(value);
    if (compareTo(result) < 0) {
      throw new ArithmeticException("UInt64 overflow");
    }
    return result;
  }

  /**
   * Returns a value that is {@code (this - value)}.
   *
   * @param value the amount to be subtracted to this value
   * @return {@code this - value}
   * @throws ArithmeticException if the result of the subtraction overflows
   */
  public UInt64 subtractExact(long value) {
    UInt64 result = subtract(value);
    if ((value > 0 && compareTo(result) < 0) || (value < 0 && compareTo(result) > 0)) {
      throw new ArithmeticException("UInt64 overflow");
    }
    return result;
  }

  /**
   * Returns the decimal representation of this value as a String.
   *
   * @return the decimal representation of this value as a String.
   */
  public String toDecimalString() {
    return toBigInteger().toString(10);
  }
}
