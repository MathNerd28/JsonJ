package com.mathnerd28.jsonj;

public class JSONParseException extends Exception {
  private static final long serialVersionUID = -5710080026111384881L;

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
