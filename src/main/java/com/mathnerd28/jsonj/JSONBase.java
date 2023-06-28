package com.mathnerd28.jsonj;

import java.io.Serializable;

public interface JSONBase extends Serializable {
  JSONBase NULL = new JSONBase() {
    private static final long serialVersionUID = -4256104788023041722L;

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
