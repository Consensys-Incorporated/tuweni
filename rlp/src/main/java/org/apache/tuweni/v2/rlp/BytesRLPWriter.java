// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.rlp;

import org.apache.tuweni.v2.bytes.Bytes;

import java.util.Deque;

final class BytesRLPWriter extends DelegatingRLPWriter<AccumulatingRLPWriter> {

  BytesRLPWriter() {
    super(new AccumulatingRLPWriter());
  }

  Bytes toBytes() {
    Deque<byte[]> values = delegate.values();
    if (values.isEmpty()) {
      return Bytes.EMPTY;
    }
    return Bytes.wrap(values.stream().map(Bytes::wrap).toArray(Bytes[]::new));
  }
}
