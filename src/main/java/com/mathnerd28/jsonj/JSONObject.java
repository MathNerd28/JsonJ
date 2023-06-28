package com.mathnerd28.jsonj;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class JSONObject extends LinkedHashMap<String, JSONBase> implements JSONBase {
  public JSONObject getObject(String key) {
    return (JSONObject) get(key);
  }

  public JSONArray getArray(String key) {
    return (JSONArray) get(key);
  }

  public String getString(String key) {
    return ((JSONString) get(key)).get();
  }

  public boolean getBoolean(String key) {
    return ((JSONBoolean) get(key)).getAsBoolean();
  }

  public long getLong(String key) {
    return ((JSONInteger) get(key)).getAsLong();
  }

  public int getInt(String key) {
    return ((JSONInteger) get(key)).getAsInt();
  }

  public double getDouble(String key) {
    JSONBase val = get(key);
    if (val instanceof JSONFloat) {
      return ((JSONFloat) val).getAsDouble();
    } else {
      return ((JSONInteger) get(key)).getAsLong();
    }
  }

  public JSONBase putString(String key, String value) {
    return put(key, new JSONString(value));
  }

  public JSONBase putBoolean(String key, boolean value) {
    return put(key, JSONBoolean.valueOf(value));
  }

  public JSONBase putLong(String key, long value) {
    return put(key, new JSONInteger(value));
  }

  public JSONBase putInteger(String key, int value) {
    return put(key, new JSONInteger(value));
  }

  public JSONBase putDouble(String key, double value) {
    return put(key, new JSONFloat(value));
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
      return "{}";
    }
    Iterator<Entry<String, JSONBase>> iterator = entrySet().iterator();
    StringBuilder builder = new StringBuilder();
    builder.append('{');
    for (;;) {
      Entry<String, JSONBase> entry = iterator.next();
      String key = entry.getKey();
      JSONBase value = entry.getValue();
      builder.append(new JSONString(key).toJSON())
             .append(':');
      if (!compact) {
        builder.append(' ');
      }
      if (value == this) {
        builder.append("(this object)");
      } else {
        builder.append(compact ? value.toJSONCompact() : value.toJSON());
      }
      if (!iterator.hasNext()) {
        return builder.append('}')
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
      return "{}";
    }
    String indentation = " ".repeat(indent);
    Iterator<Entry<String, JSONBase>> iterator = entrySet().iterator();
    StringBuilder builder = new StringBuilder();
    builder.append("{\n");
    for (;;) {
      Entry<String, JSONBase> entry = iterator.next();
      String key = entry.getKey();
      JSONBase value = entry.getValue();
      builder.append(indentation)
             .append(new JSONString(key).toJSON())
             .append(": ");
      if (value == this) {
        builder.append("(this object)");
      } else if (value instanceof JSONObject) {
        builder.append(((JSONObject) value).toJSONFormatted(indent + 2));
      } else if (value instanceof JSONArray) {
        builder.append(((JSONArray) value).toJSONFormatted(indent + 2));
      } else {
        builder.append(value.toJSON());
      }
      if (!iterator.hasNext()) {
        builder.append('\n');
        if (indent > 2) {
          builder.append(" ".repeat(indent - 2));
        }
        return builder.append('}')
                      .toString();
      }
      builder.append(",\n");
    }
  }
}