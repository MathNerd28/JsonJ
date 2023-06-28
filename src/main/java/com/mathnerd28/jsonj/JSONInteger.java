package com.mathnerd28.jsonj;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public final class JSONInteger implements JSONBase, LongSupplier, IntSupplier {
  private static final long serialVersionUID = 3836778627312723823L;

  private final long value;

  public JSONInteger(long value) {
    this.value = value;
  }

  @Override
  public int getAsInt() {
    return (int) value;
  }

  @Override
  public long getAsLong() {
    return value;
  }

  @Override
  public String toJSON() {
    return toString();
  }

  @Override
  public String toString() {
    return Long.toString(value);
  }

  @Override
  public boolean equals(Object o) {
    return (this == o)
        || (o != null && o.getClass() == this.getClass() && ((JSONInteger) o).value == this.value);
  }

  @Override
  public int hashCode() {
    return Long.hashCode(value);
  }
}
