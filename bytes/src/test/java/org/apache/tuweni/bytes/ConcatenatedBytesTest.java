// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes;

import static org.apache.tuweni.bytes.Bytes.fromHexString;
import static org.apache.tuweni.bytes.Bytes.wrap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;
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
    byte[] res = wrap(wrap(first), wrap(second)).toArray();
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
    Bytes res = wrap(wrap(first), wrap(second));
    assertArrayEquals(res.toArray(), expected1);

    first[1] = 42;
    second[0] = 42;
    byte[] expected2 = new byte[] {1, 42, 3, 42, 5};
    assertArrayEquals(res.toArray(), expected2);
  }

  @Test
  void shouldReadConcatenatedValue() {
    Bytes bytes = wrap(fromHexString("0x01234567"), fromHexString("0x89ABCDEF"));
    assertEquals(8, bytes.size());
    assertEquals("0x0123456789abcdef", bytes.toHexString());
  }

  @Test
  void shouldSliceConcatenatedValue() {
    Bytes bytes =
        wrap(
            fromHexString("0x01234567"),
            fromHexString("0x89ABCDEF"),
            fromHexString("0x01234567"),
            fromHexString("0x89ABCDEF"));
    assertEquals("0x", bytes.slice(4, 0).toHexString());
    assertEquals("0x0123456789abcdef0123456789abcdef", bytes.slice(0, 16).toHexString());
    assertEquals("0x01234567", bytes.slice(0, 4).toHexString());
    assertEquals("0x0123", bytes.slice(0, 2).toHexString());
    assertEquals("0x6789", bytes.slice(3, 2).toHexString());
    assertEquals("0x89abcdef", bytes.slice(4, 4).toHexString());
    assertEquals("0xabcd", bytes.slice(5, 2).toHexString());
    assertEquals("0xef012345", bytes.slice(7, 4).toHexString());
    assertEquals("0x01234567", bytes.slice(8, 4).toHexString());
    assertEquals("0x456789abcdef", bytes.slice(10, 6).toHexString());
    assertEquals("0x89abcdef", bytes.slice(12, 4).toHexString());
  }

  @Test
  void shouldSliceEndingPartwayThroughLaterValue() {
    Bytes part = fromHexString("0x00010203040506070809");
    Bytes slice = wrap(part, part, part).slice(5, 22);
    assertEquals(22, slice.size());
    assertEquals("0x05060708090001020304050607080900010203040506", slice.toHexString());
    assertArrayEquals(slice.toArray(), slice.copy().toArrayUnsafe());
  }

  @Test
  void shouldNotBeAffectedByReplacingWrappedArrayElements() {
    Bytes[] values = new Bytes[] {fromHexString("0x0102"), fromHexString("0x0304")};
    Bytes bytes = wrap(values);
    values[0] = fromHexString("0x01");
    values[1] = fromHexString("0x020304");
    assertEquals(2, bytes.get(1));
    assertEquals("0x0203", bytes.slice(1, 2).toHexString());
    assertArrayEquals(new byte[] {1, 2, 3, 4}, bytes.toArray());
  }

  @Test
  void shouldHandleEmptyValues() {
    Bytes inner = wrap(fromHexString("0x0102"), fromHexString("0x0304"));
    Bytes bytes = wrap(Bytes.EMPTY, inner, Bytes.EMPTY, fromHexString("0x05"));
    assertEquals(5, bytes.size());
    for (int i = 0; i < 5; i++) {
      assertEquals(i + 1, bytes.get(i));
    }
    assertEquals("0x0304", bytes.slice(2, 2).toHexString());
    assertEquals("0x020304", bytes.slice(1, 3).toHexString());
    assertEquals(fromHexString("0x0102030405"), bytes);
  }

  @Test
  void shouldMatchFlatValue() {
    Random random = new Random(42);
    Bytes[] parts = new Bytes[50];
    for (int k = 0; k < parts.length; k++) {
      parts[k] = Bytes.random(random.nextInt(5), random);
    }
    Bytes concatenated = wrap(parts);
    Bytes flat = Bytes.concatenate(parts);

    assertEquals(flat, concatenated);
    assertEquals(concatenated, flat);
    assertEquals(flat.hashCode(), concatenated.hashCode());
    for (int i = 0; i < flat.size(); i++) {
      assertEquals(flat.get(i), concatenated.get(i));
    }
    for (int n = 0; n < 1000; n++) {
      int start = random.nextInt(flat.size());
      int length = random.nextInt(flat.size() - start + 1);
      Bytes slice = concatenated.slice(start, length);
      assertEquals(flat.slice(start, length), slice);
      assertArrayEquals(flat.slice(start, length).toArray(), slice.toArray());
    }

    MutableBytes different = flat.mutableCopy();
    different.set(flat.size() - 1, (byte) (flat.get(flat.size() - 1) + 1));
    assertNotEquals(different, concatenated);
    assertNotEquals(concatenated, different);
  }

  @Test
  void shouldReadDeepConcatenatedValue() {
    Bytes bytes =
        wrap(
            wrap(fromHexString("0x01234567"), fromHexString("0x89ABCDEF")),
            wrap(fromHexString("0x01234567"), fromHexString("0x89ABCDEF")),
            fromHexString("0x01234567"),
            fromHexString("0x89ABCDEF"));
    assertEquals(24, bytes.size());
    assertEquals("0x0123456789abcdef0123456789abcdef0123456789abcdef", bytes.toHexString());
  }

  @Test
  void testCopy() {
    Bytes bytes = wrap(fromHexString("0x01234567"), fromHexString("0x89ABCDEF"));
    assertEquals(bytes, bytes.copy());
    assertEquals(bytes, bytes.mutableCopy());
  }

  @Test
  void testCopyTo() {
    Bytes bytes = wrap(fromHexString("0x0123"), fromHexString("0x4567"));
    MutableBytes dest = MutableBytes.create(32);
    bytes.copyTo(dest, 10);
    assertEquals(
        Bytes.fromHexString("0x0000000000000000000001234567000000000000000000000000000000000000"),
        dest);
  }

  @Test
  void testHashcodeUpdates() {
    MutableBytes dest = MutableBytes.create(32);
    Bytes bytes = wrap(dest, fromHexString("0x4567"));
    int hashCode = bytes.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, bytes.hashCode());
  }

  @Test
  void shouldUpdateMessageDigest() {
    Bytes value1 = fromHexString("0x01234567");
    Bytes value2 = fromHexString("0x89ABCDEF");
    Bytes value3 = fromHexString("0x01234567");
    Bytes bytes = wrap(value1, value2, value3);
    MessageDigest digest = mock(MessageDigest.class);
    bytes.update(digest);

    final InOrder inOrder = inOrder(digest);
    inOrder.verify(digest).update(value1.toArrayUnsafe(), 0, 4);
    inOrder.verify(digest).update(value2.toArrayUnsafe(), 0, 4);
    inOrder.verify(digest).update(value3.toArrayUnsafe(), 0, 4);
  }
}
