// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.ssz;

import org.apache.tuweni.v2.bytes.Bytes;

import java.nio.ByteBuffer;

final class ByteBufferSSZWriter implements SSZWriter {

  private ByteBuffer buffer;

  ByteBufferSSZWriter(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public void writeSSZ(Bytes value) {
    buffer.put(value.toArrayUnsafe());
  }

  @Override
  public void writeSSZ(byte[] value) {
    buffer.put(value);
  }
}
