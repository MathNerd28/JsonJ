package com.mathnerd28.jsonj;

import java.util.Objects;
import java.util.function.Supplier;

public final class JSONString implements JSONElement, Supplier<String> {

  private static final long serialVersionUID = 7777030346963177423L;

  private final String str;

  public JSONString(String str) {
    this.str = Objects.requireNonNull(str);
  }

  public String get() {
    return str;
  }

  @Override
  public boolean equals(Object o) {
    return (
      (o == this) ||
      (o != null && o.getClass() == this.getClass() && this.str.equals(((JSONString) o).str))
    );
  }

  @Override
  public int hashCode() {
    return str.hashCode();
  }

  @Override
  public String toString() {
    return str;
  }

  @Override
  public String toJSON() {
    StringBuilder builder = new StringBuilder(str.length() + 2);
    builder.append('"');
    for (char c : str.toCharArray()) {
      if (c < 0x0020) {
        if (c == '\b') {
          builder.append("\\b");
        } else if (c == '\f') {
          builder.append("\\f");
        } else if (c == '\n') {
          builder.append("\\n");
        } else if (c == '\r') {
          builder.append("\\r");
        } else if (c == '\t') {
          builder.append("\\t");
        } else {
          builder.append(c < 0x0010 ? "000" : "00").append(Integer.toString(c, 16));
        }
      } else if (c == '"') {
        builder.append("\\\"");
      } else if (c == '\\') {
        builder.append("\\\\");
      } else {
        builder.append(c);
      }
    }
    return builder.append('"').toString();
  }
}
