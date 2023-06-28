package com.mathnerd28.jsonj;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class JSONObject extends LinkedHashMap<String, JSONElement> implements JSONElement {

  private static final long serialVersionUID = -7400546217746697748L;

  public JSONObject() {
    super();
  }

  public JSONObject(int capacity) {
    super(capacity);
  }

  public JSONObject(Map<String, JSONElement> map) {
    super(map);
  }

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
    JSONElement val = get(key);
    if (val instanceof JSONFloat) {
      return ((JSONFloat) val).getAsDouble();
    } else {
      return ((JSONInteger) get(key)).getAsLong();
    }
  }

  public JSONElement putString(String key, String value) {
    return put(key, new JSONString(value));
  }

  public JSONElement putBoolean(String key, boolean value) {
    return put(key, JSONBoolean.valueOf(value));
  }

  public JSONElement putLong(String key, long value) {
    return put(key, new JSONInteger(value));
  }

  public JSONElement putInteger(String key, int value) {
    return put(key, new JSONInteger(value));
  }

  public JSONElement putDouble(String key, double value) {
    return put(key, new JSONFloat(value));
  }

  @Override
  public String toJSON(boolean compact) {
    if (isEmpty()) {
      return "{}";
    }
    Iterator<Entry<String, JSONElement>> iterator = entrySet().iterator();
    StringBuilder builder = new StringBuilder();
    builder.append('{');
    for (;;) {
      Entry<String, JSONElement> entry = iterator.next();
      String key = entry.getKey();
      JSONElement value = entry.getValue();
      builder.append(new JSONString(key).toJSON()).append(':');
      if (!compact) {
        builder.append(' ');
      }
      if (value == this) {
        builder.append("(this object)");
      } else {
        builder.append(value.toJSON(compact));
      }
      if (!iterator.hasNext()) {
        return builder.append('}').toString();
      }
      builder.append(',');
      if (!compact) {
        builder.append(' ');
      }
    }
  }

  public String toJSONFormatted() {
    return toJSONFormatted("  ");
  }

  String toJSONFormatted(String indentation) {
    if (isEmpty()) {
      return "{}";
    }
    Iterator<Entry<String, JSONElement>> iterator = entrySet().iterator();
    StringBuilder builder = new StringBuilder();
    builder.append("{\n");
    for (;;) {
      Entry<String, JSONElement> entry = iterator.next();
      String key = entry.getKey();
      JSONElement value = entry.getValue();
      builder.append(indentation).append(new JSONString(key).toJSON()).append(": ");
      if (value == this) {
        builder.append("(this object)");
      } else if (value instanceof JSONObject) {
        builder.append(((JSONObject) value).toJSONFormatted(indentation + "  "));
      } else if (value instanceof JSONArray) {
        builder.append(((JSONArray) value).toJSONFormatted(indentation + "  "));
      } else {
        builder.append(value.toJSON());
      }
      if (!iterator.hasNext()) {
        builder.append('\n');
        if (indentation.length() > 2) {
          builder.append(indentation.substring(0, indentation.length() - 2));
        }
        return builder.append('}').toString();
      }
      builder.append(",\n");
    }
  }

  @Override
  public boolean equals(Object o) {
    return (this == o) || (o != null && o.getClass() == this.getClass() && super.equals(o));
  }
}
