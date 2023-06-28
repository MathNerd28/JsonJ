package com.mathnerd28.json;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public class JSONInteger implements JSONBase, LongSupplier, IntSupplier {
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
}
