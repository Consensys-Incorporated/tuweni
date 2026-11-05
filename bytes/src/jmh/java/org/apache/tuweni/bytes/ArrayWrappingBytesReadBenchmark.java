// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Cost of one {@code getLong} / {@code getInt} on a 4 KB array, read sequentially at a fixed stride
 * (each benchmark invocation performs a single read and advances the index, wrapping around at the
 * end).
 *
 * <ul>
 *   <li>{@code impl}: {@code varhandle} is {@link ArrayWrappingBytes} with its VarHandle overrides;
 *       {@code default} is {@link DefaultReadArrayBytes}, the inherited {@link Bytes}
 *       implementations over the same array access; {@code bytebuffer} is {@link
 *       ByteBufferWrappingBytes}, whose fast path is the order-less {@code getLong(int)} / {@code
 *       getInt(int)} (big-endian).
 *   <li>{@code layout}: {@code whole} wraps the full array; {@code slice} is a view starting at
 *       offset 5 (unaligned), as Teku's per-validator slices of a state buffer are.
 *   <li>{@code order}: {@code BE} / {@code LE} call the {@link ByteOrder} overload; {@code NONE}
 *       calls the order-less overload (big-endian).
 *   <li>{@code stride}: 8 (back-to-back longs) or 121 (Teku's validator record size).
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ArrayWrappingBytesReadBenchmark {

  private static final int ARRAY_SIZE = 4096;
  private static final int SLICE_OFFSET = 5;

  @Param({"varhandle", "default", "bytebuffer"})
  public String impl;

  @Param({"whole", "slice"})
  public String layout;

  @Param({"LE", "BE", "NONE"})
  public String order;

  @Param({"121", "8"})
  public int stride;

  private Bytes bytes;
  private ByteOrder byteOrder;
  private int lastLongIndex;
  private int lastIntIndex;
  private int longIndex;
  private int intIndex;

  @Setup
  public void setUp() {
    byte[] array = new byte[ARRAY_SIZE];
    new Random(42).nextBytes(array);
    int offset = layout.equals("whole") ? 0 : SLICE_OFFSET;
    int length = ARRAY_SIZE - offset;
    switch (impl) {
      case "varhandle":
        bytes = Bytes.wrap(array, offset, length);
        if (!(bytes instanceof ArrayWrappingBytes)) {
          throw new IllegalStateException("expected an ArrayWrappingBytes");
        }
        break;
      case "default":
        bytes = new DefaultReadArrayBytes(array, offset, length);
        break;
      case "bytebuffer":
        bytes = Bytes.wrapByteBuffer(ByteBuffer.wrap(array), offset, length);
        if (!(bytes instanceof ByteBufferWrappingBytes)) {
          throw new IllegalStateException("expected a ByteBufferWrappingBytes");
        }
        break;
      default:
        throw new IllegalArgumentException(impl);
    }
    switch (order) {
      case "LE":
        byteOrder = ByteOrder.LITTLE_ENDIAN;
        break;
      case "BE":
        byteOrder = ByteOrder.BIG_ENDIAN;
        break;
      case "NONE":
        byteOrder = null;
        break;
      default:
        throw new IllegalArgumentException(order);
    }
    lastLongIndex = length - 8;
    lastIntIndex = length - 4;
    longIndex = 0;
    intIndex = 0;
  }

  @Benchmark
  public long getLong() {
    int i = longIndex;
    int next = i + stride;
    longIndex = next > lastLongIndex ? 0 : next;
    if (byteOrder == null) {
      return bytes.getLong(i);
    }
    return bytes.getLong(i, byteOrder);
  }

  @Benchmark
  public int getInt() {
    int i = intIndex;
    int next = i + stride;
    intIndex = next > lastIntIndex ? 0 : next;
    if (byteOrder == null) {
      return bytes.getInt(i);
    }
    return bytes.getInt(i, byteOrder);
  }
}
