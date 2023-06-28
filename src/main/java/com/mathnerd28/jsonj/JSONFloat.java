package com.mathnerd28.jsonj;

import java.util.function.DoubleSupplier;

public class JSONFloat implements JSONBase, DoubleSupplier {
  private final double value;

  public JSONFloat(double value) {
    this.value = value;
  }

  @Override
  public double getAsDouble() {
    return value;
  }

  @Override
  public String toJSON() {
    return toString();
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }
}
