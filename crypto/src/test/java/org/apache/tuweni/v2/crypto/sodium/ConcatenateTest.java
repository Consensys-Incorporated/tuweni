// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.crypto.sodium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.apache.tuweni.v2.bytes.Bytes;
import org.apache.tuweni.v2.bytes.Bytes32;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConcatenateTest {

  @BeforeAll
  static void checkAvailable() {
    assumeTrue(Sodium.isAvailable(), "Sodium native library is not available");
  }

  @Test
  void testConcatenateTwoValues() {
    Concatenate concatenate = new Concatenate();
    Bytes random = Bytes32.fromRandom();

    concatenate.add(Signature.PublicKey.fromBytes(random));
    concatenate.add(Signature.PublicKey.fromBytes(random));

    Allocated result = concatenate.concatenate();

    assertEquals(Bytes.wrap(random, random), result.bytes());
  }
}
