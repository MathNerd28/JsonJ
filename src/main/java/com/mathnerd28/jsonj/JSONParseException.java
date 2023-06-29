package com.mathnerd28.jsonj;

public class JSONParseException extends Exception {

  private static final long serialVersionUID = -5710080026111384881L;

  public JSONParseException(String msg, int line, int col) {
    super(msg + " at line " + line + ", column " + col);
  }
}
