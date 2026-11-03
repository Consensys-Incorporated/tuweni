// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import java.nio.ByteBuffer;

class ByteBufferBytesTest extends CommonBytesTests {

  @Override
  Bytes h(String hex) {
    return Bytes.wrapByteBuffer(ByteBuffer.wrap(Bytes.fromHexString(hex).toArrayUnsafe()));
  }

  @Override
  MutableBytes m(int size) {
    return MutableBytes.fromByteBuffer(ByteBuffer.allocate(size));
  }

  @Override
  Bytes w(byte[] bytes) {
    return Bytes.wrapByteBuffer(ByteBuffer.wrap(Bytes.of(bytes).toArrayUnsafe()));
  }

  @Override
  Bytes of(int... bytes) {
    return Bytes.wrapByteBuffer(ByteBuffer.wrap(Bytes.of(bytes).toArrayUnsafe()));
  }
}
