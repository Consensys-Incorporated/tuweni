// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.benchmark;

import org.apache.tuweni.v2.bytes.Bytes;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(value = Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(value = TimeUnit.NANOSECONDS)
public class BytesMegamorphicBenchmarkV2 {
  private static final int N = 4;
  private static final int FACTOR = 1_000;
  private static final Random RANDOM = new Random(23L);
  Bytes[] bytesV2;

  @Param({"mono", "mega"})
  private String mode;

  @Setup
  public void setup() {
    bytesV2 = new Bytes[N * FACTOR];
    for (int i = 0; i < N * FACTOR; i += N) {
      bytesV2[i] = Bytes.wrap(getBytes(32));
      bytesV2[i + 1] = "mega".equals(mode) ? Bytes.wrap(getBytes(48)) : Bytes.wrap(getBytes(32));
      bytesV2[i + 2] =
          "mega".equals(mode) ? Bytes.repeat((byte) 0x09, 16) : Bytes.wrap(getBytes(32));
      bytesV2[i + 3] =
          "mega".equals(mode) ? Bytes.wrap(bytesV2[i], bytesV2[i + 1]) : Bytes.wrap(getBytes(32));
    }
  }

  private static byte[] getBytes(final int size) {
    byte[] b = new byte[size];
    RANDOM.nextBytes(b);
    return b;
  }

  @Benchmark
  @OperationsPerInvocation(N * FACTOR)
  public void test() {
    for (Bytes b : bytesV2) {
      b.slice(1);
    }
  }
}
