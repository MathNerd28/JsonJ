package com.mathnerd28.jsonj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestJSONString {

  @Test
  void testNullException() {
    assertThrows(NullPointerException.class, () -> new JSONString(null));
  }

  @Test
  void testNullToJSON() {
    assertEquals("\"null\"", new JSONString("null").toJSON());
  }

  @Test
  void testEscapeControlCharsToJSON() {
    assertEquals(
      "\"\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\b\\t\\n\\u000b\\f\\r\\u000e\\u000f\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001a\\u001b\\u001c\\u001d\\u001e\\u001f\"",
      new JSONString(
        "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f"
      )
        .toJSON()
    );
  }

  @Test
  void testEscapeQuotesToJSON() {
    assertEquals("\"\\\"\"", new JSONString("\"").toJSON());
  }

  @Test
  void testEscapeSolidusToJSON() {
    assertEquals("\"\\\\\"", new JSONString("\\").toJSON());
  }

  @Test
  void testGet() {
    String s = "testString";
    assertEquals(s, new JSONString(s).get());
  }

  @Test
  void testEqualsSelf() {
    JSONString s = new JSONString("some string here");
    assertEquals(s, s);
  }

  @Test
  void testEqualsSame() {
    assertEquals(new JSONString(new String("str")), new JSONString(new String("str")));
  }

  @Test
  void testNotEqualsDifferent() {
    assertNotEquals(new JSONString("str"), new JSONString("stf"));
  }

  @Test
  void testNotEqualsWrongType() {
    assertNotEquals(new JSONString("str"), new String("str"));
  }

  @Test
  void testHashCodeSame() {
    JSONString s = new JSONString("str");
    assertEquals(s.hashCode(), s.hashCode());
  }

  @Test
  void testHashCodeEquals() {
    assertEquals(
      new JSONString(new String("str")).hashCode(),
      new JSONString(new String("str")).hashCode()
    );
  }

  @Test
  void testToString() {
    String s = "testString";
    assertEquals(s, new JSONString(s).toString());
  }
}
