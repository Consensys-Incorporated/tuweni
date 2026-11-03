// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

class DelegateBytesTest extends CommonBytesTests {

  @Override
  Bytes h(String hex) {
    Bytes bytes = Bytes.fromHexString(hex);
    return new DelegatingBytes(bytes, bytes.size());
  }

  @Override
  MutableBytes m(int size) {
    // no-op
    return MutableBytes.create(size);
  }

  @Override
  Bytes w(byte[] bytes) {
    Bytes bytesValue = Bytes.wrap(bytes);
    return new DelegatingBytes(bytesValue, bytesValue.size());
  }

  @Override
  Bytes of(int... bytes) {
    Bytes bytesValue = Bytes.of(bytes);
    return new DelegatingBytes(bytesValue, bytesValue.size());
  }
}
