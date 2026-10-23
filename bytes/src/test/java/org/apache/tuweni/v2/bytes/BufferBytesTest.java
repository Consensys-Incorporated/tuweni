// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import io.vertx.core.buffer.Buffer;

class BufferBytesTest extends CommonBytesTests {

  @Override
  Bytes h(String hex) {
    return Bytes.wrapBuffer(Buffer.buffer(Bytes.fromHexString(hex).toArrayUnsafe()));
  }

  @Override
  MutableBytes m(int size) {
    return MutableBytes.fromBuffer(Buffer.buffer(new byte[size]));
  }

  @Override
  Bytes w(byte[] bytes) {
    return Bytes.wrapBuffer(Buffer.buffer(Bytes.of(bytes).toArrayUnsafe()));
  }

  @Override
  Bytes of(int... bytes) {
    return Bytes.wrapBuffer(Buffer.buffer(Bytes.of(bytes).toArrayUnsafe()));
  }
}
