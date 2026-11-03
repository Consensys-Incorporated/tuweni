// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class Bytes32Test {

  @Test
  void testConcatenation() {
    Bytes wrapped = Bytes.wrap(Bytes32.wrap(new byte[32]), Bytes32.wrap(new byte[32]));
    assertEquals(64, wrapped.size());
    wrapped = wrapped.slice(0, 32);
    assertEquals(32, wrapped.size());
    wrapped = wrapped.slice(31, 0);
    assertEquals(0, wrapped.size());
  }

  @Test
  void constantBytes32Slice() {
    assertEquals(Bytes32.ZERO.slice(12, 20).size(), 20);
  }

  @Test
  void constantBytesslice() {
    assertEquals(Bytes.repeat((byte) 1, 63).slice(12, 20).size(), 20);
  }

  @Test
  void testMutableBytes32WrapWithOffset() {
    Bytes bytes =
        Bytes.fromHexString(
            "0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    MutableBytes mutableBytes = bytes.mutableCopy();
    assertEquals(
        "0x112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00",
        Bytes32.wrap(mutableBytes, 1).toHexString());
  }

  @Test
  void testBytes32SliceWithOffset() {
    Bytes bytes =
        Bytes.fromHexString(
            "0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    assertEquals(
        "0x112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00",
        Bytes32.wrap(bytes.slice(1, 32)).toHexString());
    assertEquals(
        "0xaabbccddeeff00112233445566778899aabbccddeeff00112233445566778899",
        Bytes32.wrap(bytes.slice(10, 32)).toHexString());
  }

  @Test
  void failsWhenWrappingArraySmallerThan32() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Bytes32.wrap(new byte[31]));
    assertEquals("Expected 32 bytes from offset 0 but got 31", exception.getMessage());
  }

  @Test
  void failsWhenWrappingArrayLargerThan32() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Bytes32.wrap(new byte[33]));
    assertEquals("Expected 32 bytes from offset 0 but got 33", exception.getMessage());
  }

  @Test
  void wrapReturnsInstanceBytes32() {
    assertThat(Bytes32.wrapHexString("0x")).isExactlyInstanceOf(Bytes32.class);
  }

  @Test
  void leftPadAValueToBytes32() {
    Bytes32 b32 = Bytes32.wrap(Bytes.of(1, 2, 3).mutableCopy().leftPad(32));
    assertEquals(32, b32.size());
    for (int i = 0; i < 28; ++i) {
      assertEquals((byte) 0, b32.get(i));
    }
    assertEquals((byte) 1, b32.get(29));
    assertEquals((byte) 2, b32.get(30));
    assertEquals((byte) 3, b32.get(31));
  }

  @Test
  void rightPadAValueToBytes32() {
    Bytes32 b32 = Bytes32.wrap(Bytes.of(1, 2, 3).mutableCopy().rightPad(32));
    assertEquals(32, b32.size());
    for (int i = 3; i < 32; ++i) {
      assertEquals((byte) 0, b32.get(i));
    }
    assertEquals((byte) 1, b32.get(0));
    assertEquals((byte) 2, b32.get(1));
    assertEquals((byte) 3, b32.get(2));
  }

  @Test
  void failsWhenLeftPaddingValueLargerThan32() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> Bytes32.wrap(Bytes.EMPTY.mutableCopy().leftPad(33)));
    assertEquals("Expected 32 bytes but got 33", exception.getMessage());
  }

  @Test
  void failsWhenRightPaddingValueLargerThan32() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> Bytes32.wrap(Bytes.EMPTY.mutableCopy().rightPad(33)));
    assertEquals("Expected 32 bytes but got 33", exception.getMessage());
  }

  @Test
  void testWrapSlicesCorrectly() {
    Bytes input =
        Bytes.fromHexString(
            "0xA99A76ED7796F7BE22D5B7E85DEEB7C5677E88E511E0B337618F8C4EB61349B4BF2D153F649F7B53359FE8B94A38E44C00000000000000000000000000000000");
    Bytes32 value = Bytes32.wrap(input, 0);
    assertEquals(
        Bytes.fromHexString("0xA99A76ED7796F7BE22D5B7E85DEEB7C5677E88E511E0B337618F8C4EB61349B4"),
        value);

    Bytes32 secondValue = Bytes32.wrap(input, 32);
    assertEquals(
        Bytes.fromHexString("0xBF2D153F649F7B53359FE8B94A38E44C00000000000000000000000000000000"),
        secondValue);
  }

  @Test
  void wrap() {
    Bytes source = Bytes.random(96);
    Bytes32 value = Bytes32.wrap(source, 2);
    assertEquals(source.slice(2, 32), value);
  }

  @Test
  void hexString() {
    Bytes initial = Bytes32.fromRandom();
    assertEquals(initial, Bytes32.fromHexStringLenient(initial.toHexString()));
    assertEquals(initial, Bytes32.fromHexString(initial.toHexString()));
    assertEquals(initial, Bytes32.fromHexStringStrict(initial.toHexString()));
  }

  @Test
  void size() {
    assertEquals(32, Bytes32.fromRandom().size());
  }

  @Test
  void padding() {
    Bytes source = Bytes32.fromRandom();
    assertEquals(source, Bytes32.wrap(source.mutableCopy().leftPad(32)));
    assertEquals(source, Bytes32.wrap(source.mutableCopy().rightPad(32)));
  }
}
