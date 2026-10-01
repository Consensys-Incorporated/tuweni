// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes;

import static org.apache.tuweni.bytes.Checks.checkArgument;
import static org.apache.tuweni.bytes.Checks.checkElementIndex;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

final class ConcatenatedBytes extends AbstractBytes {

  private final Bytes[] values;
  // ends[k] is the exclusive end offset of values[k] within this value
  private final int[] ends;
  private final int size;

  private ConcatenatedBytes(Bytes[] values, int totalSize) {
    this.values = values;
    this.size = totalSize;
    this.ends = new int[values.length];
    int end = 0;
    for (int k = 0; k < values.length; k++) {
      end += values[k].size();
      ends[k] = end;
    }
  }

  static Bytes wrap(Bytes... values) {
    if (values.length == 0) {
      return EMPTY;
    }
    if (values.length == 1) {
      return values[0];
    }

    int count = 0;
    int totalSize = 0;

    for (Bytes value : values) {
      int size = value.size();
      try {
        totalSize = Math.addExact(totalSize, size);
      } catch (ArithmeticException e) {
        throw new IllegalArgumentException(
            "Combined length of values is too long (> Integer.MAX_VALUE)");
      }
      if (value instanceof ConcatenatedBytes) {
        count += ((ConcatenatedBytes) value).values.length;
      } else if (size != 0) {
        count += 1;
      }
    }

    if (count == 0) {
      return Bytes.EMPTY;
    }
    if (count == values.length) {
      // Copy, as part offsets are cached and the caller may replace elements of its array
      return new ConcatenatedBytes(values.clone(), totalSize);
    }

    Bytes[] concatenated = new Bytes[count];
    int i = 0;
    for (Bytes value : values) {
      if (value instanceof ConcatenatedBytes) {
        Bytes[] subvalues = ((ConcatenatedBytes) value).values;
        System.arraycopy(subvalues, 0, concatenated, i, subvalues.length);
        i += subvalues.length;
      } else if (value.size() != 0) {
        concatenated[i++] = value;
      }
    }
    return new ConcatenatedBytes(concatenated, totalSize);
  }

  static Bytes wrap(List<Bytes> values) {
    if (values.size() == 0) {
      return EMPTY;
    }
    if (values.size() == 1) {
      return values.get(0);
    }

    int count = 0;
    int totalSize = 0;

    for (Bytes value : values) {
      int size = value.size();
      try {
        totalSize = Math.addExact(totalSize, size);
      } catch (ArithmeticException e) {
        throw new IllegalArgumentException(
            "Combined length of values is too long (> Integer.MAX_VALUE)");
      }
      if (value instanceof ConcatenatedBytes) {
        count += ((ConcatenatedBytes) value).values.length;
      } else if (size != 0) {
        count += 1;
      }
    }

    if (count == 0) {
      return Bytes.EMPTY;
    }
    if (count == values.size()) {
      return new ConcatenatedBytes(values.toArray(new Bytes[0]), totalSize);
    }

    Bytes[] concatenated = new Bytes[count];
    int i = 0;
    for (Bytes value : values) {
      if (value instanceof ConcatenatedBytes) {
        Bytes[] subvalues = ((ConcatenatedBytes) value).values;
        System.arraycopy(subvalues, 0, concatenated, i, subvalues.length);
        i += subvalues.length;
      } else if (value.size() != 0) {
        concatenated[i++] = value;
      }
    }
    return new ConcatenatedBytes(concatenated, totalSize);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public byte get(int i) {
    checkElementIndex(i, size);
    int k = partIndex(i);
    return values[k].get(i - partStart(k));
  }

  @Override
  public Bytes slice(int i, final int length) {
    if (i == 0 && length == size) {
      return this;
    }
    if (length == 0) {
      return Bytes.EMPTY;
    }

    checkElementIndex(i, size);
    checkArgument(
        (i + length) <= size,
        "Provided length %s is too large: the value has size %s and has only %s bytes from %s",
        length,
        size,
        size - i,
        i);

    int first = partIndex(i);
    int last = partIndex(i + length - 1);
    int firstOffset = i - partStart(first);
    if (first == last) {
      return values[first].slice(firstOffset, length);
    }

    Bytes[] combined = Arrays.copyOfRange(values, first, last + 1);
    combined[0] = values[first].slice(firstOffset);
    combined[combined.length - 1] = values[last].slice(0, i + length - partStart(last));
    return new ConcatenatedBytes(combined, length);
  }

  /** Index of the part containing byte {@code i}. */
  private int partIndex(int i) {
    int k = Arrays.binarySearch(ends, i);
    if (k < 0) {
      return -k - 1;
    }
    // An exact match means i is the first byte of a later part. Empty parts share the same end
    // offset, so skip past them.
    while (ends[k] <= i) {
      k++;
    }
    return k;
  }

  private int partStart(int k) {
    return k == 0 ? 0 : ends[k - 1];
  }

  @Override
  public Bytes copy() {
    return mutableCopy();
  }

  @Override
  public MutableBytes mutableCopy() {
    if (size == 0) {
      return MutableBytes.EMPTY;
    }
    MutableBytes result = MutableBytes.create(size);
    copyToUnchecked(result, 0);
    return result;
  }

  @Override
  public void copyTo(MutableBytes destination, int destinationOffset) {
    if (size == 0) {
      return;
    }

    checkElementIndex(destinationOffset, destination.size());
    checkArgument(
        destination.size() - destinationOffset >= size,
        "Cannot copy %s bytes, destination has only %s bytes from index %s",
        size,
        destination.size() - destinationOffset,
        destinationOffset);

    copyToUnchecked(destination, destinationOffset);
  }

  @Override
  public void update(MessageDigest digest) {
    for (Bytes value : values) {
      value.update(digest);
    }
  }

  @Override
  public byte[] toArray() {
    if (size == 0) {
      return new byte[0];
    }

    MutableBytes result = MutableBytes.create(size);
    copyToUnchecked(result, 0);
    return result.toArrayUnsafe();
  }

  private void copyToUnchecked(MutableBytes destination, int destinationOffset) {
    int offset = 0;
    for (Bytes value : values) {
      int vSize = value.size();
      if ((offset + vSize) > size) {
        throw new IllegalStateException("element sizes do not match total size");
      }
      value.copyTo(destination, destinationOffset);
      offset += vSize;
      destinationOffset += vSize;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Bytes)) {
      return false;
    }
    Bytes other = (Bytes) obj;
    if (other.size() != size) {
      return false;
    }
    int offset = 0;
    for (Bytes value : values) {
      int vSize = value.size();
      for (int j = 0; j < vSize; j++) {
        if (value.get(j) != other.get(offset + j)) {
          return false;
        }
      }
      offset += vSize;
    }
    return true;
  }

  @Override
  protected int computeHashcode() {
    int result = 1;
    for (Bytes value : values) {
      int vSize = value.size();
      for (int j = 0; j < vSize; j++) {
        result = 31 * result + value.get(j);
      }
    }
    return result;
  }

  @Override
  public int hashCode() {
    // Not cached, as the wrapped values may be mutable
    return computeHashcode();
  }
}
