package com.mathnerd28.jsonj;

import java.util.function.BooleanSupplier;

public class JSONBoolean implements JSONBase, BooleanSupplier {
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

  public static JSONBoolean valueOf(boolean b) {
    return b ? TRUE : FALSE;
  }
}
