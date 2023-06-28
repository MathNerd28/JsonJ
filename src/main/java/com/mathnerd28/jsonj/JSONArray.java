package com.mathnerd28.jsonj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class JSONArray extends ArrayList<JSONBase> implements JSONBase {
  public JSONArray() {
    super();
  }

  public JSONArray(Collection<JSONBase> c) {
    super(c);
  }

  @Override
  public String toJSON() {
    return toJSON(false);
  }

  @Override
  public String toJSONCompact() {
    return toJSON(true);
  }

  private String toJSON(boolean compact) {
    if (isEmpty()) {
      return "[]";
    }
    Iterator<JSONBase> iterator = iterator();
    StringBuilder builder = new StringBuilder();
    builder.append('[');
    for (;;) {
      JSONBase item = iterator.next();
      builder.append(item == this ? "(this array)" : item);
      if (!iterator.hasNext()) {
        return builder.append(']')
                      .toString();
      }
      builder.append(',');
      if (!compact) {
        builder.append(' ');
      }
    }
  }

  @Override
  public String toJSONFormatted() {
    return toJSONFormatted(2);
  }

  String toJSONFormatted(int indent) {
    if (isEmpty()) {
      return "[]";
    }
    String indentation = " ".repeat(indent);
    Iterator<JSONBase> iterator = iterator();
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");
    for (;;) {
      builder.append(indentation);
      JSONBase item = iterator.next();
      if (item == this) {
        builder.append("(this array)");
      } else if (item instanceof JSONObject) {
        builder.append(((JSONObject) item).toJSONFormatted(indent + 2));
      } else if (item instanceof JSONArray) {
        builder.append(((JSONArray) item).toJSONFormatted(indent + 2));
      } else {
        builder.append(item.toJSON());
      }
      if (!iterator.hasNext()) {
        builder.append('\n');
        if (indent > 2) {
          builder.append(" ".repeat(indent - 2));
        }
        return builder.append(']')
                      .toString();
      }
      builder.append(",\n");
    }
  }
}
