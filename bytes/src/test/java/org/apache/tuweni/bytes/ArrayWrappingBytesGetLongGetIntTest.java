// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteOrder;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks that the {@link ArrayWrappingBytes} overrides of {@code getLong} and {@code getInt} return
 * the same values and fail with the same exceptions as the default {@link Bytes} implementations
 * they replace.
 */
class ArrayWrappingBytesGetLongGetIntTest {

  /**
   * The orders passed to the {@link ByteOrder} overloads. The default implementations compare the
   * order with {@code BIG_ENDIAN} and treat anything else, {@code null} included, as little-endian;
   * the overrides must do the same.
   */
  private static final ByteOrder[] ORDERS = {BIG_ENDIAN, LITTLE_ENDIAN, null};

  /** A backing array long enough for every view below, with high bits set on many bytes. */
  private static byte[] pattern(int size) {
    byte[] bytes = new byte[size];
    for (int i = 0; i < size; i++) {
      bytes[i] = (byte) ((i * 37 + 11) ^ ((i & 1) == 0 ? 0x80 : 0x00));
    }
    return bytes;
  }

  /**
   * A {@link Bytes} that overrides nothing beyond the abstract methods, so {@code getLong} and
   * {@code getInt} run the default interface implementations.
   */
  private static final class DefaultReadBytes extends AbstractBytes {
    private final byte[] bytes;
    private final int offset;
    private final int length;

    DefaultReadBytes(byte[] bytes, int offset, int length) {
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
      Checks.checkElementIndex(i, length);
      return bytes[offset + i];
    }

    @Override
    public Bytes slice(int i, int length) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Bytes copy() {
      throw new UnsupportedOperationException();
    }

    @Override
    public MutableBytes mutableCopy() {
      throw new UnsupportedOperationException();
    }
  }

  /** An array-backed view under test, paired with a reference reading the same bytes. */
  private static final class View {
    private final String name;
    private final Bytes actual;
    private final Bytes reference;

    View(String name, Bytes actual, Bytes reference) {
      this.name = name;
      this.actual = actual;
      this.reference = reference;
    }

    Bytes actual() {
      return actual;
    }

    Bytes reference() {
      return reference;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static View view(String name, byte[] array, int offset, int length, Bytes actual) {
    assertInstanceOf(ArrayWrappingBytes.class, actual, name);
    assertEquals(length, actual.size(), name);
    return new View(name, actual, new DefaultReadBytes(array, offset, length));
  }

  static Stream<View> views() {
    byte[] array = pattern(96);
    byte[] three = new byte[] {(byte) 0x81, 0x02, (byte) 0xFF};
    byte[] mutable = pattern(40);
    return Stream.of(
        view("whole array", array, 0, 96, Bytes.wrap(array)),
        view("slice at offset 8", array, 8, 24, Bytes.wrap(array, 8, 24)),
        view("slice at offset 3", array, 3, 24, Bytes.wrap(array, 3, 24)),
        view("slice at offset 5", array, 5, 24, Bytes.wrap(array, 5, 24)),
        view("slice at offset 7", array, 7, 24, Bytes.wrap(array, 7, 24)),
        view("sub-slice of a slice", array, 3 + 5, 20, Bytes.wrap(array, 3, 40).slice(5, 20)),
        view("Bytes32 at offset 5", array, 5, 32, Bytes32.wrap(array, 5)),
        view("Bytes48 at offset 3", array, 3, 48, Bytes48.wrap(array, 3)),
        view("MutableBytes", mutable, 0, 40, MutableBytes.wrap(mutable)),
        view("mutable slice at offset 5", mutable, 5, 20, MutableBytes.wrap(mutable, 5, 20)),
        view("Bytes.EMPTY", new byte[0], 0, 0, Bytes.EMPTY),
        view("3-byte value", three, 0, 3, Bytes.wrap(three)));
  }

  @ParameterizedTest
  @MethodSource("views")
  void getLongMatchesDefaultImplementation(View view) {
    for (int i = -1; i <= view.actual().size() + 1; i++) {
      int index = i;
      for (ByteOrder order : ORDERS) {
        assertSameOutcome(
            () -> view.reference().getLong(index, order),
            () -> view.actual().getLong(index, order),
            view + " getLong(" + index + ", " + order + ")");
      }
      assertSameOutcome(
          () -> view.reference().getLong(index),
          () -> view.actual().getLong(index),
          view + " getLong(" + index + ")");
    }
  }

  @ParameterizedTest
  @MethodSource("views")
  void getIntMatchesDefaultImplementation(View view) {
    for (int i = -1; i <= view.actual().size() + 1; i++) {
      int index = i;
      for (ByteOrder order : ORDERS) {
        assertSameOutcome(
            () -> view.reference().getInt(index, order),
            () -> view.actual().getInt(index, order),
            view + " getInt(" + index + ", " + order + ")");
      }
      assertSameOutcome(
          () -> view.reference().getInt(index),
          () -> view.actual().getInt(index),
          view + " getInt(" + index + ")");
    }
  }

  @Test
  void highBitValues() {
    Bytes allOnes = Bytes.wrap(new byte[] {-1, -1, -1, -1, -1, -1, -1, -1});
    assertEquals(-1L, allOnes.getLong(0, BIG_ENDIAN));
    assertEquals(-1L, allOnes.getLong(0, LITTLE_ENDIAN));
    assertEquals(-1, allOnes.getInt(0, BIG_ENDIAN));
    assertEquals(-1, allOnes.getInt(0, LITTLE_ENDIAN));

    Bytes highFirst = Bytes.wrap(new byte[] {(byte) 0x80, 0, 0, 0, 0, 0, 0, 0});
    assertEquals(Long.MIN_VALUE, highFirst.getLong(0, BIG_ENDIAN));
    assertEquals(128L, highFirst.getLong(0, LITTLE_ENDIAN));
    assertEquals(Integer.MIN_VALUE, highFirst.getInt(0, BIG_ENDIAN));
    assertEquals(128, highFirst.getInt(0, LITTLE_ENDIAN));

    Bytes highLast = Bytes.wrap(new byte[] {0, 0, 0, 0, 0, 0, 0, (byte) 0x80});
    assertEquals(128L, highLast.getLong(0, BIG_ENDIAN));
    assertEquals(Long.MIN_VALUE, highLast.getLong(0, LITTLE_ENDIAN));
    assertEquals(128, highLast.getInt(4, BIG_ENDIAN));
    assertEquals(Integer.MIN_VALUE, highLast.getInt(4, LITTLE_ENDIAN));

    // A single 0xFF byte must not sign-extend into its neighbours.
    Bytes oneByteSet = Bytes.wrap(new byte[] {0, 0, 0, (byte) 0xFF, 0, 0, 0, 0});
    assertEquals(0x000000FF00000000L, oneByteSet.getLong(0, BIG_ENDIAN));
    assertEquals(0x00000000FF000000L, oneByteSet.getLong(0, LITTLE_ENDIAN));
    assertEquals(0x000000FF, oneByteSet.getInt(0, BIG_ENDIAN));
    assertEquals(0xFF000000, oneByteSet.getInt(0, LITTLE_ENDIAN));
  }

  @Test
  void readBackAfterMutation() {
    MutableBytes bytes = MutableBytes.create(16);
    assertEquals(0L, bytes.getLong(3, BIG_ENDIAN));
    bytes.set(10, (byte) 0xAB);
    // Index 10 is the last byte of the long starting at 3.
    assertEquals(0xABL, bytes.getLong(3, BIG_ENDIAN));
    assertEquals(0xABL << 56, bytes.getLong(3, LITTLE_ENDIAN));
    // ... the third byte of the long starting at 8 ...
    assertEquals(0xABL << 40, bytes.getLong(8, BIG_ENDIAN));
    assertEquals(0xABL << 16, bytes.getLong(8, LITTLE_ENDIAN));
    // ... and the first byte of the int starting at 10.
    assertEquals(0xAB000000, bytes.getInt(10, BIG_ENDIAN));
    assertEquals(0xAB, bytes.getInt(10, LITTLE_ENDIAN));

    MutableBytes slice = bytes.mutableSlice(4, 12);
    slice.set(0, (byte) 0x7F);
    assertEquals(0x7F000000_0000AB00L, bytes.getLong(4, BIG_ENDIAN));
    assertEquals(0x7F000000_0000AB00L, slice.getLong(0, BIG_ENDIAN));
    assertEquals(0x00AB0000_0000007FL, slice.getLong(0, LITTLE_ENDIAN));
    assertEquals(0x7F000000, slice.getInt(0, BIG_ENDIAN));
    assertEquals(0x0000007F, slice.getInt(0, LITTLE_ENDIAN));
  }

  @Test
  void boundaryIndexesOnASlice() {
    // The backing array has plenty of bytes after the slice: reads must still stop at the slice.
    byte[] array = pattern(64);
    Bytes slice = Bytes.wrap(array, 5, 16);
    int size = slice.size();

    assertEquals(new DefaultReadBytes(array, 5, 16).getLong(size - 8), slice.getLong(size - 8));
    assertEquals(
        new DefaultReadBytes(array, 5, 16).getLong(size - 8, LITTLE_ENDIAN),
        slice.getLong(size - 8, LITTLE_ENDIAN));
    assertEquals(new DefaultReadBytes(array, 5, 16).getInt(size - 4), slice.getInt(size - 4));
    assertEquals(
        new DefaultReadBytes(array, 5, 16).getInt(size - 4, LITTLE_ENDIAN),
        slice.getInt(size - 4, LITTLE_ENDIAN));

    IndexOutOfBoundsException e =
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getLong(size - 7));
    assertEquals(
        "Value of size 16 has not enough bytes to read a 8 bytes long from index 9",
        e.getMessage());
    e = assertThrows(IndexOutOfBoundsException.class, () -> slice.getLong(size - 7, LITTLE_ENDIAN));
    assertEquals(
        "Value of size 16 has not enough bytes to read a 8 bytes long from index 9",
        e.getMessage());
    e = assertThrows(IndexOutOfBoundsException.class, () -> slice.getInt(size - 3));
    assertEquals(
        "Value of size 16 has not enough bytes to read a 4 bytes int from index 13",
        e.getMessage());
    e = assertThrows(IndexOutOfBoundsException.class, () -> slice.getInt(size - 3, LITTLE_ENDIAN));
    assertEquals(
        "Value of size 16 has not enough bytes to read a 4 bytes int from index 13",
        e.getMessage());

    e = assertThrows(IndexOutOfBoundsException.class, () -> slice.getLong(-1));
    assertEquals("index is out of bounds", e.getMessage());
    e = assertThrows(IndexOutOfBoundsException.class, () -> slice.getLong(size, LITTLE_ENDIAN));
    assertEquals("index is out of bounds", e.getMessage());
    e = assertThrows(IndexOutOfBoundsException.class, () -> slice.getInt(-1, BIG_ENDIAN));
    assertEquals("index is out of bounds", e.getMessage());
    e = assertThrows(IndexOutOfBoundsException.class, () -> slice.getInt(size));
    assertEquals("index is out of bounds", e.getMessage());
  }

  @Test
  void tooShortValues() {
    Bytes seven = Bytes.wrap(pattern(7));
    IndexOutOfBoundsException e =
        assertThrows(IndexOutOfBoundsException.class, () -> seven.getLong(0, LITTLE_ENDIAN));
    assertEquals(
        "Value of size 7 has not enough bytes to read a 8 bytes long from index 0", e.getMessage());
    assertEquals(new DefaultReadBytes(seven.toArrayUnsafe(), 0, 7).getInt(3), seven.getInt(3));

    Bytes three = Bytes.wrap(pattern(3));
    e = assertThrows(IndexOutOfBoundsException.class, () -> three.getInt(0, BIG_ENDIAN));
    assertEquals(
        "Value of size 3 has not enough bytes to read a 4 bytes int from index 0", e.getMessage());

    e = assertThrows(IndexOutOfBoundsException.class, () -> Bytes.EMPTY.getLong(0));
    assertEquals("index is out of bounds", e.getMessage());
    e = assertThrows(IndexOutOfBoundsException.class, () -> Bytes.EMPTY.getInt(0));
    assertEquals("index is out of bounds", e.getMessage());
  }

  private interface Read<T> {
    T read();
  }

  /**
   * Asserts that {@code actual} produces the same value as {@code expected}, or throws an exception
   * of the same class with the same message.
   */
  private static <T> void assertSameOutcome(Read<T> expected, Read<T> actual, String description) {
    T expectedValue;
    try {
      expectedValue = expected.read();
    } catch (RuntimeException expectedException) {
      RuntimeException actualException =
          assertThrows(RuntimeException.class, actual::read, description);
      assertEquals(expectedException.getClass(), actualException.getClass(), description);
      assertEquals(expectedException.getMessage(), actualException.getMessage(), description);
      return;
    }
    T actualValue;
    try {
      actualValue = actual.read();
    } catch (RuntimeException e) {
      fail(description + " threw " + e, e);
      return;
    }
    assertEquals(expectedValue, actualValue, description);
  }
}
