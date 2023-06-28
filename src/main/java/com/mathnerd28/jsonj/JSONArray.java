package com.mathnerd28.jsonj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class JSONArray extends ArrayList<JSONElement> implements JSONElement {

  private static final long serialVersionUID = 1637774693420351464L;

  public JSONArray() {
    super();
  }

  public JSONArray(Collection<JSONElement> c) {
    super(c);
  }

  public JSONObject getObject(int index) {
    return (JSONObject) get(index);
  }

  public JSONArray getArray(int index) {
    return (JSONArray) get(index);
  }

  public String getString(int index) {
    return ((JSONString) get(index)).get();
  }

  public boolean getBoolean(int index) {
    return ((JSONBoolean) get(index)).getAsBoolean();
  }

  public long getLong(int index) {
    return ((JSONInteger) get(index)).getAsLong();
  }

  public int getInt(int index) {
    return ((JSONInteger) get(index)).getAsInt();
  }

  public double getDouble(int index) {
    JSONElement val = get(index);
    if (val instanceof JSONFloat) {
      return ((JSONFloat) val).getAsDouble();
    } else {
      return ((JSONInteger) get(index)).getAsLong();
    }
  }

  public void addString(String str) {
    add(new JSONString(str));
  }

  public void addBoolean(boolean b) {
    add(JSONBoolean.valueOf(b));
  }

  public void addLong(long l) {
    add(new JSONInteger(l));
  }

  public void addInt(int i) {
    add(new JSONInteger(i));
  }

  public void addDouble(double d) {
    add(new JSONFloat(d));
  }

  public void addString(int index, String str) {
    add(index, new JSONString(str));
  }

  public void addBoolean(int index, boolean b) {
    add(index, JSONBoolean.valueOf(b));
  }

  public void addLong(int index, long l) {
    add(index, new JSONInteger(l));
  }

  public void addInt(int index, int i) {
    add(index, new JSONInteger(i));
  }

  public void addDouble(int index, double d) {
    add(index, new JSONFloat(d));
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
    Iterator<JSONElement> iterator = iterator();
    StringBuilder builder = new StringBuilder();
    builder.append('[');
    for (;;) {
      JSONElement item = iterator.next();
      builder.append(item == this ? "(this array)" : item);
      if (!iterator.hasNext()) {
        return builder.append(']').toString();
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
      return "[]";
    }
    Iterator<JSONElement> iterator = iterator();
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");
    for (;;) {
      builder.append(indentation);
      JSONElement item = iterator.next();
      if (item == this) {
        builder.append("(this array)");
      } else if (item instanceof JSONObject) {
        builder.append(((JSONObject) item).toJSONFormatted(indentation + "  "));
      } else if (item instanceof JSONArray) {
        builder.append(((JSONArray) item).toJSONFormatted(indentation + "  "));
      } else {
        builder.append(item.toJSON());
      }
      if (!iterator.hasNext()) {
        builder.append('\n');
        if (indentation.length() > 2) {
          builder.append(indentation.substring(0, indentation.length() - 2));
        }
        return builder.append(']').toString();
      }
      builder.append(",\n");
    }
  }

  @Override
  public boolean equals(Object o) {
    return (this == o) || (o != null && o.getClass() == this.getClass() && super.equals(o));
  }
}
