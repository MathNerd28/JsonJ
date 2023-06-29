package com.mathnerd28.jsonj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestJSONFloat {

  // prettier-ignore
  static final double[] doubles = { 0, 1, -1, Double.MIN_VALUE, -Double.MIN_VALUE, Double.MIN_NORMAL, -Double.MIN_NORMAL, Double.MAX_VALUE, -Double.MAX_VALUE, -0 };
  // prettier-ignore
  static final double[] illegals = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };

  @Test
  void testGetAsDouble() {
    for (double d : doubles) {
      assertEquals(d, new JSONFloat(d).getAsDouble());
    }
  }

  @Test
  void testInvalidValues() {
    for (double d : illegals) {
      assertThrows(IllegalArgumentException.class, () -> new JSONFloat(d));
    }
  }

  @Test
  void testToJSON() {
    for (double d : doubles) {
      assertEquals(d, Double.parseDouble(new JSONFloat(d).toJSON()));
    }
  }

  @Test
  void testToString() {
    for (double d : doubles) {
      assertEquals(d, Double.parseDouble(new JSONFloat(d).toString()));
    }
  }

  @Test
  void testEqualsSelf() {
    for (double d : doubles) {
      JSONFloat f = new JSONFloat(d);
      assertEquals(f, f);
    }
  }

  @Test
  void testEqualsSame() {
    for (double d : doubles) {
      assertEquals(new JSONFloat(d), new JSONFloat(d));
    }
  }

  @Test
  void testNotEqualsDifferent() {
    // Skip -0, which is equal to 0
    for (int i = 1; i < doubles.length; i++) {
      for (int j = 1; j < doubles.length; j++) {
        if (i != j) {
          assertNotEquals(new JSONFloat(doubles[i]), new JSONFloat(doubles[j]));
        }
      }
    }
  }

  @Test
  void testHashCodeSame() {
    for (double d : doubles) {
      assertEquals(new JSONFloat(d).hashCode(), new JSONFloat(d).hashCode());
    }
  }
}
