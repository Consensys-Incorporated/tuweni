// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;

class ConcatenatedBytesTest {

  @ParameterizedTest
  @MethodSource("concatenatedWrapProvider")
  void concatenatedWrap(Object arr1, Object arr2) {
    byte[] first = (byte[]) arr1;
    byte[] second = (byte[]) arr2;
    byte[] res = Bytes.wrap(Bytes.wrap(first), Bytes.wrap(second)).toArrayUnsafe();
    assertArrayEquals(Arrays.copyOfRange(res, 0, first.length), first);
    assertArrayEquals(Arrays.copyOfRange(res, first.length, res.length), second);
  }

  @SuppressWarnings("UnusedMethod")
  private static Stream<Arguments> concatenatedWrapProvider() {
    return Stream.of(
        Arguments.of(new byte[] {}, new byte[] {}),
        Arguments.of(new byte[] {}, new byte[] {1, 2, 3}),
        Arguments.of(new byte[] {1, 2, 3}, new byte[] {}),
        Arguments.of(new byte[] {1, 2, 3}, new byte[] {4, 5}));
  }

  @Test
  void testConcatenatedWrapReflectsUpdates() {
    byte[] first = new byte[] {1, 2, 3};
    byte[] second = new byte[] {4, 5};
    byte[] expected1 = new byte[] {1, 2, 3, 4, 5};
    Bytes res = Bytes.wrap(Bytes.wrap(first), Bytes.wrap(second));
    assertArrayEquals(res.toArrayUnsafe(), expected1);

    first[1] = 42;
    second[0] = 42;
    byte[] expected2 = new byte[] {1, 42, 3, 42, 5};
    assertArrayEquals(res.toArrayUnsafe(), expected2);
  }

  @Test
  void shouldReadConcatenatedValue() {
    Bytes bytes = Bytes.wrap(Bytes.fromHexString("0x01234567"), Bytes.fromHexString("0x89ABCDEF"));
    assertEquals(8, bytes.size());
    assertEquals("0x0123456789abcdef", bytes.toHexString());
  }

  static Stream<Arguments> sliceProvider() {
    return Stream.of(
        Arguments.of("0x", 4, 0),
        Arguments.of("0x01234567", 0, 4),
        Arguments.of("0x0123", 0, 2),
        Arguments.of("0x2345", 1, 2),
        Arguments.of("0x6789", 3, 2),
        Arguments.of("0x89abcdef", 4, 4),
        Arguments.of("0xabcd", 5, 2),
        Arguments.of("0xef012345", 7, 4),
        Arguments.of("0x01234567", 8, 4),
        Arguments.of("0x456789abcdef", 10, 6),
        Arguments.of("0x89abcdef", 12, 4),
        Arguments.of("0xabcd", 13, 2),
        Arguments.of("0x0123456789abcdef0123456789ab", 0, 14),
        Arguments.of("0x0123456789abcdef0123456789abcd", 0, 15),
        Arguments.of("0x0123456789abcdef0123456789abcdef", 0, 16));
  }

  @ParameterizedTest
  @MethodSource("sliceProvider")
  void sliceValue(String expectedHex, int sliceOffset, int sliceLength) {
    Bytes bytes =
        Bytes.wrap(
            Bytes.fromHexString("0x01234567"),
            Bytes.fromHexString("0x89ABCDEF"),
            Bytes.fromHexString("0x01234567"),
            Bytes.fromHexString("0x89ABCDEF"));
    assertEquals(expectedHex, bytes.slice(sliceOffset, sliceLength).toHexString());
  }

  @ParameterizedTest
  @MethodSource("sliceProvider")
  void slicedValueToArrayUnsafe(String expectedHex, int sliceOffset, int sliceLength) {
    Bytes bytes =
        Bytes.wrap(
            Bytes.fromHexString("0x01234567"),
            Bytes.fromHexString("0x89ABCDEF"),
            Bytes.fromHexString("0x01234567"),
            Bytes.fromHexString("0x89ABCDEF"));
    assertThat(Bytes.fromHexString(expectedHex).toArrayUnsafe())
        .containsExactly(bytes.slice(sliceOffset, sliceLength).toArrayUnsafe());
  }

  @Test
  void shouldReadDeepConcatenatedValue() {
    Bytes bytes =
        Bytes.wrap(
            Bytes.wrap(Bytes.fromHexString("0x01234567"), Bytes.fromHexString("0x89ABCDEF")),
            Bytes.wrap(Bytes.fromHexString("0x01234567"), Bytes.fromHexString("0x89ABCDEF")),
            Bytes.fromHexString("0x01234567"),
            Bytes.fromHexString("0x89ABCDEF"));
    assertEquals(24, bytes.size());
    assertEquals("0x0123456789abcdef0123456789abcdef0123456789abcdef", bytes.toHexString());
  }

  @Test
  void testMutableCopy() {
    Bytes bytes = Bytes.wrap(Bytes.fromHexString("0x01234567"), Bytes.fromHexString("0x89ABCDEF"));
    assertEquals(bytes, bytes.mutableCopy());
  }

  @Test
  void testHashcodeUpdates() {
    MutableBytes dest = MutableBytes.create(32);
    Bytes bytes = Bytes.wrap(dest, Bytes.fromHexString("0x4567"));
    int hashCode = bytes.hashCode();
    dest.set(1, (byte) 123);
    assertEquals(hashCode, bytes.hashCode());
  }

  @Test
  void shouldUpdateMessageDigest() {
    Bytes value1 = Bytes.fromHexString("0x01234567");
    Bytes value2 = Bytes.fromHexString("0x89ABCDEF");
    Bytes value3 = Bytes.fromHexString("0x01234567");
    Bytes bytes = Bytes.wrap(value1, value2, value3);
    MessageDigest digest = mock(MessageDigest.class);
    bytes.update(digest);

    final InOrder inOrder = inOrder(digest);
    inOrder.verify(digest).update(value1.toArrayUnsafe(), 0, 4);
    inOrder.verify(digest).update(value2.toArrayUnsafe(), 0, 4);
    inOrder.verify(digest).update(value3.toArrayUnsafe(), 0, 4);
  }

  @Test
  void and() {
    Bytes part1 = Bytes.of(0xaa, 0xaa);
    Bytes part2 = Bytes.of(0xbb, 0xbb);
    Bytes expected = Bytes.fromHexString("0x000000000000aaaabbbb");

    Bytes concat = ConcatenatedBytes.create(part1, part2);
    MutableBytes mutableBytes = MutableBytes.fromHexString("0xffffffffffffffffffff");
    assertThat(mutableBytes.and(concat)).isEqualTo(expected);
  }

  @Test
  void or() {
    Bytes part1 = Bytes.of(0xaa, 0xaa);
    Bytes part2 = Bytes.of(0xbb, 0xbb);
    Bytes expected = Bytes.fromHexString("0xffffffffffffffffbbbb");

    Bytes concat = ConcatenatedBytes.create(part1, part2);
    MutableBytes mutableBytes = MutableBytes.fromHexString("0xffffffffffffffff0000");
    assertThat(mutableBytes.or(concat)).isEqualTo(expected);
  }

  @Test
  void xor() {
    Bytes part1 = Bytes.of(0xaa, 0xaa);
    Bytes part2 = Bytes.of(0xbb, 0xbb);
    Bytes expected = Bytes.fromHexString("0xffffffffffff55554444");

    Bytes concat = ConcatenatedBytes.create(part1, part2);
    MutableBytes mutableBytes = MutableBytes.fromHexString("0xffffffffffffffffffff");
    assertThat(mutableBytes.xor(concat)).isEqualTo(expected);
  }
}
