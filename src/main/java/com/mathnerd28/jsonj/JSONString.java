package com.mathnerd28.jsonj;

import java.util.function.Supplier;

public class JSONString implements JSONBase, Supplier<String> {
  private final String str;

  public JSONString(String str) {
    this.str = str;
  }

  public String get() {
    return str;
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
          builder.append(c < 0x0010 ? "000" : "00")
                 .append(Integer.toString(c, 16));
        }
      } else if (c == '"') {
        builder.append("\\\"");
      } else if (c == '\\') {
        builder.append("\\\\");
      } else {
        builder.append(c);
      }
    }
    return builder.append('"')
                  .toString();
  }
}
