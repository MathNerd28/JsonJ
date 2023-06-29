package com.mathnerd28.jsonj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestJSONInteger {

  static final int[] ints = { 0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE };
  // prettier-ignore
  static final long[] longs = { 0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE };

  @Test
  void testGetAsInt() {
    for (int i : ints) {
      assertEquals(i, new JSONInteger(i).getAsInt());
    }
  }

  @Test
  void testGetAsIntException() {
    for (long l : longs) {
      assertDoesNotThrow(() -> new JSONInteger(l).getAsInt());
    }
  }

  @Test
  void testGetAsLong() {
    for (long l : longs) {
      assertEquals(l, new JSONInteger(l).getAsLong());
    }
  }

  @Test
  void testToJSON() {
    for (long l : longs) {
      assertEquals(Long.toString(l), new JSONInteger(l).toString());
    }
  }

  @Test
  void testToString() {
    for (long l : longs) {
      assertEquals(Long.toString(l), new JSONInteger(l).toJSON());
    }
  }

  @Test
  void testEqualsSelf() {
    for (long l : longs) {
      JSONInteger i = new JSONInteger(l);
      assertEquals(i, i);
    }
  }

  @Test
  void testEqualsSame() {
    for (long l : longs) {
      assertEquals(new JSONInteger(l), new JSONInteger(l));
    }
  }

  @Test
  void testNotEqualsDifferent() {
    for (int i = 0; i < longs.length; i++) {
      for (int j = 0; j < longs.length; j++) {
        if (i != j) {
          assertNotEquals(new JSONInteger(longs[i]), new JSONInteger(longs[j]));
        }
      }
    }
  }

  @Test
  void testHashCodeSame() {
    for (long l : longs) {
      assertEquals(new JSONInteger(l).hashCode(), new JSONInteger(l).hashCode());
    }
  }
}
