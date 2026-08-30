// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.apache.tuweni.v2.bytes.Utils.checkArgument;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;

import io.vertx.core.buffer.Buffer;

/**
 * A class that holds and delegates all operations to its inner bytes field.
 *
 * <p>This class may be used to create more types that represent bytes, but need a different name
 * for business logic.
 */
public class DelegatingBytes extends Bytes {

  final Bytes delegate;

  protected DelegatingBytes(Bytes delegate, int size) {
    super(size);
    this.delegate = delegate;
    checkArgument(delegate.size() == size, "Expected %s bytes but got %s", size, delegate.size());
  }

  @Override
  public byte get(int i) {
    return delegate.get(i);
  }

  @Override
  public Bytes slice(int index, int length) {
    return delegate.slice(index, length);
  }

  @Override
  public byte[] toArrayUnsafe() {
    return delegate.toArrayUnsafe();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public Bytes getImpl() {
    return delegate.getImpl();
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    delegate.and(bytesArray, offset, length);
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    delegate.or(bytesArray, offset, length);
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    delegate.xor(bytesArray, offset, length);
  }

  @Override
  public int getInt(int i) {
    return delegate.getInt(i);
  }

  @Override
  public int getInt(int i, ByteOrder order) {
    return delegate.getInt(i, order);
  }

  @Override
  public int toInt() {
    return delegate.toInt();
  }

  @Override
  public int toInt(ByteOrder order) {
    return delegate.toInt(order);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public long getLong(int i) {
    return delegate.getLong(i);
  }

  @Override
  public long getLong(int i, ByteOrder order) {
    return delegate.getLong(i, order);
  }

  @Override
  public long toLong() {
    return delegate.toLong();
  }

  @Override
  public long toLong(ByteOrder order) {
    return delegate.toLong(order);
  }

  @Override
  public BigInteger toBigInteger() {
    return delegate.toBigInteger();
  }

  @Override
  public BigInteger toBigInteger(ByteOrder order) {
    return delegate.toBigInteger(order);
  }

  @Override
  public BigInteger toUnsignedBigInteger() {
    return delegate.toUnsignedBigInteger();
  }

  @Override
  public BigInteger toUnsignedBigInteger(ByteOrder order) {
    return delegate.toUnsignedBigInteger(order);
  }

  @Override
  public boolean isZero() {
    return delegate.isZero();
  }

  @Override
  public boolean hasLeadingZero() {
    return delegate.hasLeadingZero();
  }

  @Override
  public int numberOfLeadingZeros() {
    return delegate.numberOfLeadingZeros();
  }

  @Override
  public boolean hasLeadingZeroByte() {
    return delegate.hasLeadingZeroByte();
  }

  @Override
  public int numberOfLeadingZeroBytes() {
    return delegate.numberOfLeadingZeroBytes();
  }

  @Override
  public int numberOfTrailingZeroBytes() {
    return delegate.numberOfTrailingZeroBytes();
  }

  @Override
  public int bitLength() {
    return delegate.bitLength();
  }

  @Override
  public Bytes slice(int i) {
    return delegate.slice(i);
  }

  @Override
  public void appendTo(ByteBuffer byteBuffer) {
    delegate.appendTo(byteBuffer);
  }

  @Override
  public MutableBytes mutableCopy() {
    return delegate.mutableCopy();
  }

  @Override
  public void appendTo(Buffer buffer) {
    delegate.appendTo(buffer);
  }

  @Override
  public <T extends Appendable> T appendHexTo(T appendable) {
    return delegate.appendHexTo(appendable);
  }

  @Override
  public int commonPrefixLength(Bytes other) {
    return delegate.commonPrefixLength(other);
  }

  @Override
  public Bytes commonPrefix(Bytes other) {
    return delegate.commonPrefix(other);
  }

  @Override
  public Bytes trimLeadingZeros() {
    return delegate.trimLeadingZeros();
  }

  @Override
  public void update(MessageDigest digest) {
    delegate.update(digest);
  }

  @Override
  public String toHexString() {
    return delegate.toHexString();
  }

  @Override
  public String toUnprefixedHexString() {
    return delegate.toUnprefixedHexString();
  }

  @Override
  public String toEllipsisHexString() {
    return delegate.toEllipsisHexString();
  }

  @Override
  public String toShortHexString() {
    return delegate.toShortHexString();
  }

  @Override
  public String toQuantityHexString() {
    return delegate.toQuantityHexString();
  }

  @Override
  public String toBase64String() {
    return delegate.toBase64String();
  }

  @Override
  public int compareTo(Bytes b) {
    return delegate.compareTo(b);
  }

  @Override
  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  public boolean equals(final Object o) {
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}
