// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.ssz;

import org.apache.tuweni.ssz.SSZType;

public interface SSZReadable extends SSZType {
  void populateFromReader(SSZReader reader);
}
