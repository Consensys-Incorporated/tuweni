// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.devp2p.v5.encrypt

import org.apache.tuweni.v2.bytes.Bytes

internal data class SessionKey(
  val initiatorKey: Bytes,
  val recipientKey: Bytes,
  val authRespKey: Bytes,
)
