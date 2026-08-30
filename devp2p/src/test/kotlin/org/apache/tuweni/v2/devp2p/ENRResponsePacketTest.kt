// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.devp2p

import org.apache.tuweni.junit.BouncyCastleExtension
import org.apache.tuweni.v2.bytes.Bytes32
import org.apache.tuweni.v2.crypto.SECP256K1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.InetAddress

@ExtendWith(BouncyCastleExtension::class)
internal class ENRResponsePacketTest {

  @Test
  fun shouldEncodeThenDecodePacket() {
    val keyPair = SECP256K1.KeyPair.random()

    val requestHash = Bytes32.fromRandom()
    val enr = EthereumNodeRecord.toRLP(
      SECP256K1.KeyPair.random(),
      2,
      emptyMap(),
      emptyMap(),
      InetAddress.getByName("localhost"),
      3000,
      12000,
    )
    val now = System.currentTimeMillis()
    val pong = ENRResponsePacket.create(keyPair, now, requestHash, enr)

    val datagram = pong.encode()
    val packet = Packet.decodeFrom(datagram)
    assertTrue(packet is ENRResponsePacket)

    val responsePacket = packet as ENRResponsePacket
    assertEquals(keyPair.publicKey(), responsePacket.nodeId)
    assertEquals(enr, responsePacket.enr)
    assertEquals(requestHash, responsePacket.requestHash)
    assertEquals(((now + PACKET_EXPIRATION_PERIOD_MS + 999) / 1000) * 1000, responsePacket.expiration)
  }
}
