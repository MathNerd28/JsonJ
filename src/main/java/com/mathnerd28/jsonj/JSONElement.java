package com.mathnerd28.jsonj;

import java.io.Serializable;

public interface JSONElement extends Serializable {
  JSONElement NULL = new JSONElement() {
    private static final long serialVersionUID = -4256104788023041722L;

    @Override
    public String toString() {
      return "NULL";
    }

    @Override
    public String toJSON() {
      return "null";
    }

    @Override
    public boolean equals(Object o) {
      return (o == this) || (o != null && o.getClass() == this.getClass());
    }
  };

  String toJSON();

  default String toJSONCompact() {
    return toJSON();
  }
}