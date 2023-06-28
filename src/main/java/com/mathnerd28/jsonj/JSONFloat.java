package com.mathnerd28.jsonj;

import java.util.function.DoubleSupplier;

public final class JSONFloat implements JSONBase, DoubleSupplier {
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

  @Override
  public boolean equals(Object o) {
    return (o == this) || (o instanceof JSONFloat && ((JSONFloat) o).value == this.value);
  }

  @Override
  public int hashCode() {
    return Double.hashCode(value);
  }
}
