// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import javax.annotation.Nullable;

import com.google.errorprone.annotations.FormatMethod;

public final class Utils {

  static void checkNotNull(@Nullable Object object) {
    if (object == null) {
      throw new NullPointerException("argument cannot be null");
    }
  }

  public static void checkElementIndex(int index, int size) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("index is out of bounds");
    }
  }

  @FormatMethod
  static void checkArgument(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }

  @FormatMethod
  static void checkArgument(boolean condition, String message, int arg1) {
    if (!condition) {
      throw new IllegalArgumentException(String.format(message, arg1));
    }
  }

  @FormatMethod
  static void checkArgument(boolean condition, String message, int arg1, int arg2) {
    if (!condition) {
      throw new IllegalArgumentException(String.format(message, arg1, arg2));
    }
  }

  @FormatMethod
  static void checkArgument(boolean condition, String message, int arg1, int arg2, int arg3) {
    if (!condition) {
      throw new IllegalArgumentException(String.format(message, arg1, arg2, arg3));
    }
  }

  @FormatMethod
  static void checkArgument(
      boolean condition, String message, int arg1, int arg2, int arg3, int arg4) {
    if (!condition) {
      throw new IllegalArgumentException(String.format(message, arg1, arg2, arg3, arg4));
    }
  }

  @FormatMethod
  static void checkArgument(boolean condition, String message, long arg1) {
    if (!condition) {
      throw new IllegalArgumentException(String.format(message, arg1));
    }
  }

  static void checkLength(int arrayLength, int offset, int length) {
    checkArgument(
        offset + length <= arrayLength,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        arrayLength - offset,
        offset);
  }

  static void and(
      byte[] sourceBytesArray,
      int sourceOffset,
      byte[] destBytesArray,
      int destOffset,
      int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      destBytesArray[destOffset + i] =
          (byte) (sourceBytesArray[sourceOffset + i] & destBytesArray[destOffset + i]);
    }
  }

  static void or(
      byte[] sourceBytesArray,
      int sourceOffset,
      byte[] destBytesArray,
      int destOffset,
      int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      destBytesArray[destOffset + i] =
          (byte) (sourceBytesArray[sourceOffset + i] | destBytesArray[destOffset + i]);
    }
  }

  static void xor(
      byte[] sourceBytesArray,
      int sourceOffset,
      byte[] destBytesArray,
      int destOffset,
      int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      destBytesArray[destOffset + i] =
          (byte) (sourceBytesArray[sourceOffset + i] ^ destBytesArray[destOffset + i]);
    }
  }
}
