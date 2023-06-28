package com.mathnerd28.json;

public class JSONParseException extends Exception {
  public JSONParseException() {
    super("");
  }

  public JSONParseException(String msg) {
    super(msg);
  }

  public JSONParseException(Throwable cause) {
    super(cause);
  }
}
