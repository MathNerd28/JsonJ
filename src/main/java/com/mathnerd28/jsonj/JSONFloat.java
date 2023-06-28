package com.mathnerd28.jsonj;

import java.util.function.DoubleSupplier;

public final class JSONFloat implements JSONElement, DoubleSupplier {

  private static final long serialVersionUID = -1001481974395547082L;

  private final double value;

  public JSONFloat(double value) {
    this.value = value;
  }

  @Override
  public double getAsDouble() {
    return value;
  }

  @Override
  public String toJSON(boolean compact) {
    return Double.toString(value);
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }

  @Override
  public boolean equals(Object o) {
    return (
      (o == this) ||
      (o != null && o.getClass() == this.getClass() && ((JSONFloat) o).value == this.value)
    );
  }

  @Override
  public int hashCode() {
    return Double.hashCode(value);
  }
}
