package com.mathnerd28.jsonj;

public class JSONParseException extends Exception {

  private static final long serialVersionUID = -5710080026111384881L;

  private final int line;
  private final int col;

  public JSONParseException(String msg, int line, int col) {
    super(msg + " at line " + line + ", column " + col);
    this.line = line;
    this.col = col;
  }
}
