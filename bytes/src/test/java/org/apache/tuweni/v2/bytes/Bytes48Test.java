// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class Bytes48Test {

  @Test
  void failsWhenWrappingArraySmallerThan48() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Bytes48.wrap(new byte[47]));
    assertEquals("Expected 48 bytes from offset 0 but got 47", exception.getMessage());
  }

  @Test
  void failsWhenWrappingArrayLargerThan48() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Bytes48.wrap(new byte[49]));
    assertEquals("Expected 48 bytes from offset 0 but got 49", exception.getMessage());
  }

  @Test
  void wrapReturnsInstanceBytes48() {
    assertThat(Bytes48.wrapHexString("0x")).isExactlyInstanceOf(Bytes48.class);
  }

  @Test
  void rightPadAValueToBytes48() {
    Bytes48 b48 = Bytes48.wrap(Bytes.of(1, 2, 3).mutableCopy().rightPad(48));
    assertEquals(48, b48.size());
    for (int i = 3; i < 48; ++i) {
      assertEquals((byte) 0, b48.get(i));
    }
    assertEquals((byte) 1, b48.get(0));
    assertEquals((byte) 2, b48.get(1));
    assertEquals((byte) 3, b48.get(2));
  }

  @Test
  void leftPadAValueToBytes48() {
    Bytes48 b48 = Bytes48.wrap(Bytes.of(1, 2, 3).mutableCopy().leftPad(48));
    assertEquals(48, b48.size());
    for (int i = 0; i < 28; ++i) {
      assertEquals((byte) 0, b48.get(i));
    }
    assertEquals((byte) 1, b48.get(45));
    assertEquals((byte) 2, b48.get(46));
    assertEquals((byte) 3, b48.get(47));
  }

  @Test
  void failsWhenLeftPaddingValueLargerThan48() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> Bytes48.wrap(Bytes.EMPTY.mutableCopy().leftPad(49)));
    assertEquals("Expected 48 bytes but got 49", exception.getMessage());
  }

  @Test
  void failsWhenRightPaddingValueLargerThan48() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> Bytes48.wrap(Bytes.EMPTY.mutableCopy().rightPad(49)));
    assertEquals("Expected 48 bytes but got 49", exception.getMessage());
  }

  @Test
  void hexString() {
    Bytes initial = Bytes48.fromRandom();
    assertEquals(initial, Bytes48.fromHexStringLenient(initial.toHexString()));
    assertEquals(initial, Bytes48.fromHexString(initial.toHexString()));
    assertEquals(initial, Bytes48.fromHexStringStrict(initial.toHexString()));
  }

  @Test
  void size() {
    assertEquals(48, Bytes48.fromRandom().size());
  }

  @Test
  void wrap() {
    Bytes source = Bytes.random(96);
    Bytes48 value = Bytes48.wrap(source, 2);
    assertEquals(source.slice(2, 48), value);
  }

  @Test
  void padding() {
    Bytes source = Bytes48.fromRandom();
    assertEquals(source, Bytes48.wrap(source.mutableCopy().leftPad(48)));
    assertEquals(source, Bytes48.wrap(source.mutableCopy().rightPad(48)));
  }
}
