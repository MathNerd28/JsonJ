package com.mathnerd28.jsonj;

public interface JSONBase {
  JSONBase NULL = new JSONBase() {
    @Override
    public String toString() {
      return "NULL";
    }

    @Override
    public String toJSON() {
      return "null";
    }
  };

  String toJSON();

  default String toJSONCompact() {
    return toJSON();
  }

  default String toJSONFormatted() {
    return toJSON();
  }
}
