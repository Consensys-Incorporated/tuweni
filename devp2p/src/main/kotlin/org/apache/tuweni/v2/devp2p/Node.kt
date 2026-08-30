// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.devp2p

import org.apache.tuweni.v2.crypto.SECP256K1
import org.apache.tuweni.v2.rlp.RLPReader
import org.apache.tuweni.v2.rlp.RLPWriter

internal data class Node(
  val endpoint: Endpoint,
  val nodeId: SECP256K1.PublicKey,
) {

  companion object {
    fun readFrom(reader: RLPReader): Node {
      val endpoint = Endpoint.readFrom(reader)
      val nodeId = SECP256K1.PublicKey.fromBytes(reader.readValue())
      return Node(endpoint, nodeId)
    }
  }

  internal fun writeTo(writer: RLPWriter) {
    endpoint.writeTo(writer)
    writer.writeValue(nodeId.bytes())
  }

  internal fun rlpSize(): Int = 1 + endpoint.rlpSize() + 3 + 64
}

internal fun Peer.toNode(): Node =
  Node(endpoint, nodeId)
