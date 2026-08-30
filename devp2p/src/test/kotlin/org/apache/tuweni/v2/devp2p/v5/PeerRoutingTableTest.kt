// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.devp2p.v5

import org.apache.tuweni.junit.BouncyCastleExtension
import org.apache.tuweni.v2.bytes.Bytes
import org.apache.tuweni.v2.crypto.SECP256K1
import org.apache.tuweni.v2.devp2p.DevP2PPeerRoutingTable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BouncyCastleExtension::class)
class PeerRoutingTableTest {

  @Test
  fun invalidPublicKey() {
    val routingTable = DevP2PPeerRoutingTable(SECP256K1.KeyPair.random().publicKey())
    val invalidPublicKey = SECP256K1.PublicKey.fromBytes(Bytes.repeat(0, 64))
    val peers = routingTable.nearest(invalidPublicKey, 3)
    assertEquals(0, peers.size)
  }
}
