package com.mathnerd28.jsonj;

import java.util.function.BooleanSupplier;

public final class JSONBoolean implements JSONBase, BooleanSupplier {
  private static final long serialVersionUID = -8286974874362307637L;
  
  public static final JSONBoolean TRUE  = new JSONBoolean(true);
  public static final JSONBoolean FALSE = new JSONBoolean(false);

  private final boolean value;

  private JSONBoolean(boolean value) {
    this.value = value;
  }

  public boolean getAsBoolean() {
    return value;
  }

  @Override
  public String toJSON() {
    return toString();
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }

  @Override
  public boolean equals(Object o) {
    // Only two instances allowed
    return o == this;
  }

  @Override
  public int hashCode() {
    return Boolean.hashCode(value);
  }

  public static JSONBoolean valueOf(boolean b) {
    return b ? TRUE : FALSE;
  }
}
