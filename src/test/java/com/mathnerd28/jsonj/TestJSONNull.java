package com.mathnerd28.jsonj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestJSONNull {

  @Test
  void testNotEqualsNull() {
    assertNotEquals(null, JSONElement.NULL);
  }

  @Test
  void testNotEqualsReplication() {
    JSONElement NULL = new JSONElement() {
      private static final long serialVersionUID = -4256104788023041722L;

      @Override
      public String toString() {
        return "null";
      }

      @Override
      public String toJSON(boolean compact) {
        return "null";
      }

      @Override
      public boolean equals(Object o) {
        return (o == this) || (o != null && o.getClass() == this.getClass());
      }
    };
    assertNotEquals(JSONElement.NULL, NULL);
  }

  @Test
  void testToString() {
    assertEquals("null", JSONElement.NULL.toString());
  }

  @Test
  void testToJSON() {
    assertEquals("null", JSONElement.NULL.toJSON());
  }
}
