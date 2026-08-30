// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.units.bigints;

public class Utils {
  static void and(
      int[] sourceBytesArray, int sourceOffset, byte[] destBytesArray, int destOffset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      destBytesArray[destOffset + i] =
          (byte) (unpackByte(sourceBytesArray, sourceOffset + i) & destBytesArray[destOffset + i]);
    }
  }

  static void or(
      int[] sourceBytesArray, int sourceOffset, byte[] destBytesArray, int destOffset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      destBytesArray[destOffset + i] =
          (byte) (unpackByte(sourceBytesArray, sourceOffset + i) | destBytesArray[destOffset + i]);
    }
  }

  static void xor(
      int[] sourceBytesArray, int sourceOffset, byte[] destBytesArray, int destOffset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      destBytesArray[destOffset + i] =
          (byte) (unpackByte(sourceBytesArray, sourceOffset + i) ^ destBytesArray[destOffset + i]);
    }
  }

  static byte unpackByte(int[] ints, int index) {
    int whichInt = index / 4;
    return unpackByte(ints[whichInt], index);
  }

  static byte unpackByte(int integer, int index) {
    int whichIndex = 3 - index % 4;
    return (byte) ((integer >> (8 * whichIndex)) & 0xFF);
  }

  static byte unpackByte(long value, int index) {
    int whichIndex = 7 - index;
    return (byte) ((value >> (8 * whichIndex)) & 0xFF);
  }
}
