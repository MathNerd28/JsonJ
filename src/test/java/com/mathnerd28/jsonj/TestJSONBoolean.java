package com.mathnerd28.jsonj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestJSONBoolean {

  @Test
  void testGetAsBoolean() {
    assertTrue(JSONBoolean.TRUE.getAsBoolean());
    assertFalse(JSONBoolean.FALSE.getAsBoolean());
  }

  @Test
  void testToString() {
    assertEquals("true", JSONBoolean.TRUE.toString());
    assertEquals("false", JSONBoolean.FALSE.toString());
  }

  @Test
  void testToJSON() {
    assertEquals("true", JSONBoolean.TRUE.toJSON());
    assertEquals("false", JSONBoolean.FALSE.toJSON());
  }

  @Test
  void testEqualsSelf() {
    assertEquals(JSONBoolean.TRUE, JSONBoolean.TRUE);
    assertEquals(JSONBoolean.FALSE, JSONBoolean.FALSE);
  }

  @Test
  void testNotEqualsOther() {
    assertNotEquals(JSONBoolean.TRUE, JSONBoolean.FALSE);
    assertNotEquals(JSONBoolean.FALSE, JSONBoolean.TRUE);
  }

  @Test
  void testHashCodeSame() {
    assertEquals(JSONBoolean.TRUE.hashCode(), JSONBoolean.TRUE.hashCode());
    assertEquals(JSONBoolean.FALSE.hashCode(), JSONBoolean.FALSE.hashCode());
  }

  @Test
  void testDifferentHashCodes() {
    assertNotEquals(JSONBoolean.TRUE.hashCode(), JSONBoolean.FALSE.hashCode());
  }

  @Test
  void testValueOf() {
    assertSame(JSONBoolean.TRUE, JSONBoolean.valueOf(true));
    assertSame(JSONBoolean.FALSE, JSONBoolean.valueOf(false));
  }
}
