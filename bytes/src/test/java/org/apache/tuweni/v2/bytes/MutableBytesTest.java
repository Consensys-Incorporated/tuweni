// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MutableBytesTest {

  @Test
  void testMutableBytesWrap() {
    MutableBytes b = MutableBytes.fromArray(Bytes.fromHexString("deadbeef").toArrayUnsafe(), 1, 3);
    assertEquals(Bytes.fromHexString("adbeef"), b);
  }

  @Test
  void testClear() {
    MutableBytes b = MutableBytes.fromHexString("deadbeef");
    assertEquals(Bytes.fromHexString("00000000"), b.clear());
  }

  @Test
  void testFill() {
    MutableBytes b = MutableBytes.create(2);
    assertEquals(Bytes.fromHexString("0x2222"), b.fill((byte) 34));
  }

  @Test
  void testDecrementAndIncrement() {
    MutableBytes b = MutableBytes.create(2);
    assertEquals(Bytes.fromHexString("0x0001"), b.increment());
    assertEquals(Bytes.fromHexString("0x0000"), b.decrement());

    assertEquals(Bytes.fromHexString("0xFFFE"), b.fill((byte) 0xFF).decrement());

    b = MutableBytes.fromHexString("0x00FF");
    assertEquals(Bytes.fromHexString("0x0100"), b.increment());
  }

  @Test
  void setLong() {
    MutableBytes b = MutableBytes.create(8);
    b.setLong(0, 256);
    assertEquals(Bytes.fromHexString("0x0000000000000100"), b);
  }

  @Test
  void setInt() {
    MutableBytes b = MutableBytes.create(4);
    b.setInt(0, 256);
    assertEquals(Bytes.fromHexString("0x00000100"), b);
  }

  @Test
  void setIntOverflow() {
    MutableBytes b = MutableBytes.create(2);
    assertThrows(IndexOutOfBoundsException.class, () -> b.setInt(0, 18096));
  }

  @Test
  void setLongOverflow() {
    MutableBytes b = MutableBytes.create(6);
    assertThrows(IndexOutOfBoundsException.class, () -> b.setLong(0, Long.MAX_VALUE));
  }

  @Test
  void wrapEmpty() {
    MutableBytes b = MutableBytes.fromBuffer(Buffer.buffer());
    assertEquals(b.size(), 0);
    b = MutableBytes.fromByteBuf(Unpooled.buffer(0));
    assertEquals(b.size(), 0);
  }

  @Test
  void testHashcodeUpdates() {
    MutableBytes dest = MutableBytes.create(32);
    int hashCode = dest.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, dest.hashCode());
  }

  @Test
  void testHashcodeUpdatesBuffer() {
    MutableBytes dest = MutableBytes.fromBuffer(Buffer.buffer(new byte[4]));
    int hashCode = dest.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, dest.hashCode());
  }

  @Test
  void testHashcodeUpdatesByteBuffer() {
    MutableBytes dest = MutableBytes.fromByteBuffer(ByteBuffer.wrap(new byte[4]));
    int hashCode = dest.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, dest.hashCode());
  }

  @Test
  void testHashcodeUpdatesByteBuf() {
    MutableBytes dest = MutableBytes.fromByteBuf(Unpooled.buffer(4));
    int hashCode = dest.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, dest.hashCode());
  }

  @ParameterizedTest
  @CsvSource({
    "0x01, 0x00, 1",
    "0x02, 0x01, 1",
    "0x04, 0x01, 2",
    "0x08, 0x01, 3",
    "0x10, 0x01, 4",
    "0xFF, 0x0F, 4",
    "0xFFFF, 0x00FF, 8",
    "0x1234, 0x0123, 4",
    "0x8000, 0x0001, 15",
    "0x321243, 0x000000, 25",
    "0x213211AD, 0x00000213, 20",
    "0x7FFFFFFF, 0x3FFFFFFF, 1",
    "0xFFFFFFFF, 0x0FFFFFFF, 4",
    "0xABCDEF, 0x00ABCD, 8",
    "0x12345678, 0x01234567, 4",
    "0x00, 0x00, 1",
    "0x01, 0x01, 0",
    "0xAA55, 0x0552, 5",
    "0x01000001, 0x00400000, 2"
  })
  void shiftRight(String bytesValue, String expected, int shiftBits) {
    MutableBytes value = MutableBytes.fromHexString(bytesValue);
    assertEquals(Bytes.fromHexString(expected), value.shiftRight(shiftBits));
  }

  @ParameterizedTest
  @CsvSource({
    "0x80, 0x00, 1",
    "0x40, 0x80, 1",
    "0x20, 0x80, 2",
    "0x10, 0x80, 3",
    "0x08, 0x80, 4",
    "0xFF, 0xF0, 4",
    "0xFFFF, 0xFF00, 8",
    "0x1234, 0x2340, 4",
    "0x0001, 0x8000, 15",
    "0x321243, 0x000000, 25",
    "0x213211AD, 0x1AD00000, 20",
    "0xFFFFFFFE, 0xFFFFFFFC, 1",
    "0xFFFFFFFF, 0xFFFFFFF0, 4",
    "0xABCDEF, 0xCDEF00, 8",
    "0x12345678, 0x23456780, 4",
    "0x00, 0x00, 1",
    "0x80, 0x80, 0",
    "0xAA55, 0x4AA0, 5",
    "0xAA, 0x40, 5",
    "0x01000001, 0x04000004, 2"
  })
  void shiftLeft(String bytesValue, String expected, int shiftBits) {
    MutableBytes value = MutableBytes.fromHexString(bytesValue);
    assertEquals(Bytes.fromHexString(expected), value.shiftLeft(shiftBits));
  }

  @ParameterizedTest
  @CsvSource({
    "0x0102, 0x00000102, 4",
    "0x0102, 0x0102, 2",
    "0x0102, 0x0102, 0",
    "0x, 0x000000, 3",
    "0x, 0x, 0",
    "0x01, 0x000000000001, 6",
    "0xFF, 0x00FF, 2",
    "0x0000, 0x00000000, 4",
    "0x000000000000000000000000000000000000000000000000000000, 0x0000000000000000000000000000000000000000000000000000000000, 29",
    "0xE000000000E000000000E000000000, 0x0000000000000000000000000000000000E000000000E000000000E000000000, 32",
    "0x123456789ABCDEF0, 0x123456789ABCDEF0, 4"
  })
  void leftPad(String bytesValue, String expected, int size) {
    MutableBytes value = MutableBytes.fromHexString(bytesValue);
    assertEquals(Bytes.fromHexString(expected), value.leftPad(size));
  }

  @ParameterizedTest
  @CsvSource({
    "0x0102, 0x01020000, 4",
    "0x0102, 0x0102, 2",
    "0x0102, 0x0102, 0",
    "0x, 0x000000, 3",
    "0x, 0x, 0",
    "0x01, 0x010000000000, 6",
    "0xFF, 0xFF00, 2",
    "0x0000, 0x00000000, 4",
    "0x000000000000000000000000000000000000000000000000000000, 0x0000000000000000000000000000000000000000000000000000000000, 29",
    "0xE000000000E000000000E000000000, 0xE000000000E000000000E0000000000000000000000000000000000000000000, 32",
    "0x123456789ABCDEF0, 0x123456789ABCDEF0, 4"
  })
  void rightPad(String bytesValue, String expected, int size) {
    MutableBytes value = MutableBytes.fromHexString(bytesValue);
    assertEquals(Bytes.fromHexString(expected), value.rightPad(size));
  }

  @ParameterizedTest
  @CsvSource({"0x000102030405, 0x050403020100", "0x, 0x"})
  void reverse(String bytesValue, String expectedValue) {
    MutableBytes bytes = MutableBytes.fromHexString(bytesValue);
    assertEquals(MutableBytes.fromHexString(expectedValue), bytes.reverse());
  }

  @ParameterizedTest
  @CsvSource({"0x01, 0x02", "0x01FF, 0x0200", "0xFFFFFF, 0x000000"})
  void increment(String bytesValue, String expectedBytes) {
    MutableBytes one = MutableBytes.fromHexString(bytesValue);
    one.increment();
    assertEquals(MutableBytes.fromHexString(expectedBytes), one);
  }

  @ParameterizedTest
  @CsvSource({"0x02, 0x01", "0x0100, 0x00FF", "0x000000, 0xFFFFFF"})
  void decrement(String bytesValue, String expectedBytes) {
    MutableBytes one = MutableBytes.fromHexString(bytesValue);
    one.decrement();
    assertEquals(MutableBytes.fromHexString(expectedBytes), one);
  }

  @ParameterizedTest
  @CsvSource({
    "0x0F, 0xF0",
    "0xA5, 0x5A",
    "0xFF, 0x00",
    "0xAA, 0x55",
    "0x, 0x",
    "0x123456, 0xEDCBA9",
    "0xABCDEF, 0x543210",
    "DEADBEEF, 21524110",
    "0x000000, 0xFFFFFF",
    "0x0100, 0xFEFF",
    "0x01000001, 0xfefffffe"
  })
  void not(String bytesValue, String expectedBytes) {
    MutableBytes value = MutableBytes.fromHexString(bytesValue).not();
    assertEquals(MutableBytes.fromHexString(expectedBytes), value);
  }

  @ParameterizedTest
  @CsvSource({
    "0x0F, 0xF0, 0x00",
    "0xA5, 0x5A, 0x00",
    "0xFF, 0xFF, 0xFF",
    "0x00, 0x00, 0x00",
    "0xAA, 0x55, 0x00",
    "0x, 0x, 0x",
    "0x123456, 0x654321, 0x000000",
    "0xABCDEF, 0x123456, 0x020446",
    "0xDEADBEEF, 0xCAFEBABE, 0xCAACBAAE",
    "0x01000001, 0x01000000, 0x01000000",
    "0xFF, 0xF012, 0x0012",
    "0xF012, 0xFF, 0x0012",
    "0xFF, 0x, 0x00",
    "0x, 0xFF, 0x00",
    "0xDEADBEEF, 0x0000BABE, 0x0000BAAE",
    "0x0000BEEF, 0xCAFEBABE, 0x0000BAAE"
  })
  void and(String bytes1, String bytes2, String expectedResult) {
    MutableBytes value = MutableBytes.fromHexString(bytes1).and(Bytes.fromHexString(bytes2));
    assertEquals(Bytes.fromHexString(expectedResult), value);
  }

  @ParameterizedTest
  @CsvSource({
    "0, 4, 0xCAACBAAE",
    "1, 3, 0x00ACBAAE",
    "0, 3, 0x00DEA8BE",
    "0, 2, 0x00009AAC",
    "2, 1, 0x000000BE",
    "2, 2, 0x0000BAAE"
  })
  void andOffsetLengthFirstOperand(int offset, int length, String expectedResult) {
    Bytes firstOperand = Bytes.fromHexString("0xDEADBEEF");
    Bytes secondOperand = Bytes.fromHexString("0xCAFEBABE");
    firstOperand = firstOperand.slice(offset, length);
    assertEquals(
        Bytes.fromHexString(expectedResult), firstOperand.mutableCopy().and(secondOperand));
  }

  @ParameterizedTest
  @CsvSource({
    "0, 4, 0xCAACBAAE",
    "1, 3, 0x00ACBAAE",
    "0, 3, 0x0088BEAA",
    "0, 2, 0x00008AEE",
    "2, 1, 0x000000AA",
    "2, 2, 0x0000BAAE"
  })
  void andOffsetLengthSecondOperand(int offset, int length, String expectedResult) {
    Bytes firstOperand = Bytes.fromHexString("0xDEADBEEF");
    Bytes secondOperand = Bytes.fromHexString("0xCAFEBABE");
    secondOperand = secondOperand.slice(offset, length);
    assertEquals(
        Bytes.fromHexString(expectedResult), firstOperand.mutableCopy().and(secondOperand));
  }

  @ParameterizedTest
  @CsvSource({
    "0x0F, 0xF0, 0xFF",
    "0xA5, 0x5A, 0xFF",
    "0xFF, 0xFF, 0xFF",
    "0x00, 0x00, 0x00",
    "0xAA, 0x55, 0xFF",
    "0x, 0x, 0x",
    "0x123456, 0x654321, 0x777777",
    "0xABCDEF, 0x123456, 0xBBFDFF",
    "0xDEADBEEF, 0xCAFEBABE, 0xDEFFBEFF",
    "0x01000001, 0x01000000, 0x01000001",
    "0x0F, 0xF0F0, 0xF0FF",
    "0xF0F0, 0x0F, 0xF0FF",
    "0xFF, 0x, 0xFF",
    "0x, 0xFF, 0xFF",
    "0xDEADBEEF, 0x0000BABE, 0xDEADBEFF",
    "0x0000BEEF, 0xCAFEBABE, 0xCAFEBEFF"
  })
  void or(String bytes1, String bytes2, String expectedResult) {
    MutableBytes value = MutableBytes.fromHexString(bytes1).or(Bytes.fromHexString(bytes2));
    assertEquals(Bytes.fromHexString(expectedResult), value);
  }

  @ParameterizedTest
  @CsvSource({
    "0, 4, 0xDEFFBEFF",
    "1, 3, 0xCAFFBEFF",
    "0, 3, 0xCAFEBFBE",
    "0, 2, 0xCAFEFEBF",
    "2, 1, 0xCAFEBABE",
    "2, 2, 0xCAFEBEFF"
  })
  void orOffsetLengthFirstOperand(int offset, int length, String expectedResult) {
    Bytes firstOperand = Bytes.fromHexString("0xDEADBEEF");
    Bytes secondOperand = Bytes.fromHexString("0xCAFEBABE");
    firstOperand = firstOperand.slice(offset, length);
    assertEquals(Bytes.fromHexString(expectedResult), firstOperand.mutableCopy().or(secondOperand));
  }

  @ParameterizedTest
  @CsvSource({
    "0, 4, 0xDEFFBEFF",
    "1, 3, 0xDEFFBEFF",
    "0, 3, 0xDEEFFEFF",
    "0, 2, 0xDEADFEFF",
    "2, 1, 0xDEADBEFF",
    "2, 2, 0xDEADBEFF"
  })
  void orOffsetLengthSecondOperand(int offset, int length, String expectedResult) {
    Bytes firstOperand = Bytes.fromHexString("0xDEADBEEF");
    Bytes secondOperand = Bytes.fromHexString("0xCAFEBABE");
    secondOperand = secondOperand.slice(offset, length);
    assertEquals(Bytes.fromHexString(expectedResult), firstOperand.mutableCopy().or(secondOperand));
  }

  @ParameterizedTest
  @CsvSource({
    "0x0F, 0xF0, 0xFF",
    "0xA5, 0x5A, 0xFF",
    "0xFF, 0xFF, 0x00",
    "0x00, 0x00, 0x00",
    "0xAA, 0x55, 0xFF",
    "0x, 0x, 0x",
    "0x123456, 0x654321, 0x777777",
    "0xABCDEF, 0x123456, 0xB9F9B9",
    "0xDEADBEEF, 0xCAFEBABE, 0x14530451",
    "0x01000001, 0x01000000, 0x00000001",
    "0x0F, 0xF0F0, 0xF0FF",
    "0xF0F0, 0x0F, 0xF0FF",
    "0xFF, 0x, 0xFF",
    "0x, 0xFF, 0xFF",
    "0x0000BEEF, 0xCAFEBABE, 0xCAFE0451",
    "0xDEADBEEF, 0x0000BABE, 0xDEAD0451"
  })
  void xor(String bytes1, String bytes2, String expectedResult) {
    MutableBytes value = MutableBytes.fromHexString(bytes1).xor(Bytes.fromHexString(bytes2));
    assertEquals(Bytes.fromHexString(expectedResult), value);
  }

  @ParameterizedTest
  @CsvSource({
    "0, 4, 0x14530451",
    "1, 3, 0xCA530451",
    "0, 3, 0xCA201700",
    "0, 2, 0xCAFE6413",
    "2, 1, 0xCAFEBA00",
    "2, 2, 0xCAFE0451"
  })
  void xorOffsetLengthFirstOperand(int offset, int length, String expectedResult) {
    Bytes firstOperand = Bytes.fromHexString("0xDEADBEEF");
    Bytes secondOperand = Bytes.fromHexString("0xCAFEBABE");
    firstOperand = firstOperand.slice(offset, length);
    assertEquals(
        Bytes.fromHexString(expectedResult), firstOperand.mutableCopy().xor(secondOperand));
  }

  @ParameterizedTest
  @CsvSource({
    "0, 4, 0x14530451",
    "1, 3, 0xDE530451",
    "0, 3, 0xDE674055",
    "0, 2, 0xDEAD7411",
    "2, 1, 0xDEADBE55",
    "2, 2, 0xDEAD0451"
  })
  void xorOffsetLengthSecondOperand(int offset, int length, String expectedResult) {
    Bytes firstOperand = Bytes.fromHexString("0xDEADBEEF");
    Bytes secondOperand = Bytes.fromHexString("0xCAFEBABE");
    secondOperand = secondOperand.slice(offset, length);
    assertEquals(
        Bytes.fromHexString(expectedResult), firstOperand.mutableCopy().xor(secondOperand));
  }
}
