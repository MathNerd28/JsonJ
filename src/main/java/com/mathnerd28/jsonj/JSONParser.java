package com.mathnerd28.jsonj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class JSONParser {

  enum TokenType {
    STRING,
    INTEGER,
    FLOAT,
    TRUE,
    FALSE,
    NULL,
    COMMA,
    COLON,
    LEFT_BRACE,
    LEFT_BRACKET,
    RIGHT_BRACE,
    RIGHT_BRACKET,
  }

  private static class Token {

    static final Token TRUE = new Token(TokenType.TRUE, JSONBoolean.TRUE);
    static final Token FALSE = new Token(TokenType.FALSE, JSONBoolean.FALSE);
    static final Token NULL = new Token(TokenType.NULL, JSONElement.NULL);

    static final Token COMMA = new Token(TokenType.COMMA);
    static final Token COLON = new Token(TokenType.COLON);
    static final Token LEFT_BRACE = new Token(TokenType.LEFT_BRACE);
    static final Token LEFT_BRACKET = new Token(TokenType.LEFT_BRACKET);
    static final Token RIGHT_BRACE = new Token(TokenType.RIGHT_BRACE);
    static final Token RIGHT_BRACKET = new Token(TokenType.RIGHT_BRACKET);

    final TokenType type;
    final JSONElement data;

    Token(TokenType type) {
      this(type, null);
    }

    Token(TokenType type, JSONElement data) {
      this.type = type;
      this.data = data;
    }
  }

  private static final Pattern INTEGER = Pattern.compile("-?(?:0|[1-9]\\d*)");
  private static final Pattern FLOAT = Pattern.compile(
    "-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[Ee][+-]?(?:0|[1-9]\\d*))?"
  );

  private Reader reader;
  private StringBuilder builder;

  private int carryC;
  private int line;
  private int col;
  private int tokenLine;
  private int tokenCol;

  private boolean allowDuplicateKeys;

  public JSONParser() {
    builder = new StringBuilder();
    carryC = -2;
    allowDuplicateKeys = false;
  }

  public JSONParser overwritingDuplicateKeys() {
    allowDuplicateKeys = true;
    return this;
  }

  public JSONParser exceptingDuplicateKeys() {
    allowDuplicateKeys = false;
    return this;
  }

  public JSONElement parse(String json) throws JSONParseException {
    // BUG: exception in try-with-resources causes self-suppression???
    Reader reader = new StringReader(json);
    try {
      // don't need to buffer
      return parseRaw(reader);
    } catch (IOException e) {
      throw new AssertionError("IOException from StringReader", e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        throw new AssertionError("IOException from StringReader", e);
      }
    }
  }

  public JSONElement parse(File file) throws IOException, JSONParseException {
    try (InputStream stream = new FileInputStream(file)) {
      return parse(stream);
    }
  }

  public JSONElement parse(InputStream stream) throws IOException, JSONParseException {
    // Don't want to close
    return parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
  }

  public JSONElement parse(Reader reader) throws IOException, JSONParseException {
    // Don't want to close
    return parseRaw(reader instanceof BufferedReader ? reader : new BufferedReader(reader, 4096));
  }

  private JSONElement parseRaw(Reader r) throws IOException, JSONParseException {
    reader = r;
    line = 1;
    col = 0;

    Token t = nextToken();
    return getData(t);
  }

  private boolean hasChar() throws IOException {
    return carryC >= 0 || (carryC = reader.read()) != -1;
  }

  private char nextChar() throws IOException, JSONParseException {
    if (carryC != -2) {
      int c = carryC;
      carryC = -2;
      return (char) c;
    }

    int c = reader.read();
    if (c == -1) {
      throw new JSONParseException("Unexpected termination", line, col);
    } else if (c == '\n') {
      col = 0;
      line++;
    } else {
      col++;
    }
    return (char) c;
  }

  private Token nextToken() throws IOException, JSONParseException {
    char c;
    do {
      c = nextChar();
    } while (c == ' ' || c == '\n' || c == '\t' || c == '\r');

    tokenLine = line;
    tokenCol = col;

    // These don't require the buffer, fastpath
    switch (c) {
      case ',':
        return Token.COMMA;
      case ':':
        return Token.COLON;
      case '{':
        return Token.LEFT_BRACE;
      case '[':
        return Token.LEFT_BRACKET;
      case '}':
        return Token.RIGHT_BRACE;
      case ']':
        return Token.RIGHT_BRACKET;
      case 't':
        if (
          // prettier-ignore
          (c = nextChar()) == 'r' &&
          (c = nextChar()) == 'u' &&
          (c = nextChar()) == 'e'
        ) {
          return Token.TRUE;
        } else {
          throw new JSONParseException("Expected keyword 'true'", line, col);
        }
      case 'f':
        if (
          // prettier-ignore
          (c = nextChar()) == 'a' &&
          (c = nextChar()) == 'l' &&
          (c = nextChar()) == 's' &&
          (c = nextChar()) == 'e'
        ) {
          return Token.FALSE;
        } else {
          throw new JSONParseException("Expected keyword 'false'", line, col);
        }
      case 'n':
        if (
          // prettier-ignore
          (c = nextChar()) == 'u' &&
          (c = nextChar()) == 'l' &&
          (c = nextChar()) == 'l'
        ) {
          return Token.NULL;
        } else {
          throw new JSONParseException("Expected keyword 'null'", line, col);
        }
      default:
      // no fastpath
    }

    // Clear buffer for operation
    builder.delete(0, builder.length());

    if (c == '"') {
      // String
      boolean escaped = false;
      for (;;) {
        c = nextChar();
        if (c < 0x0020) {
          throw new JSONParseException("Unescaped control character", line, col);
        }
        switch (c) {
          case '"':
            if (escaped) {
              escaped = false;
              builder.append('"');
            } else {
              return new Token(TokenType.STRING, new JSONString(builder.toString()));
            }
            break;
          case '\\':
            if (escaped) {
              builder.append('\\');
            }
            escaped = !escaped;
            break;
          case '/':
            escaped = false;
            builder.append('/');
            break;
          case 'b':
            if (escaped) {
              escaped = false;
              c = '\b';
            }
            builder.append(c);
            break;
          case 'f':
            if (escaped) {
              escaped = false;
              c = '\f';
            }
            builder.append(c);
            break;
          case 'n':
            if (escaped) {
              escaped = false;
              c = '\n';
            }
            builder.append(c);
            break;
          case 'r':
            if (escaped) {
              escaped = false;
              c = '\r';
            }
            builder.append(c);
            break;
          case 't':
            if (escaped) {
              escaped = false;
              c = '\t';
            }
            builder.append(c);
            break;
          case 'u':
            if (escaped) {
              escaped = false;
              int unicode = 0;
              for (int i = 0; i < 4; i++) {
                unicode <<= 4;
                c = nextChar();
                if ((c >= '0' && c <= '9')) {
                  unicode += c - '0';
                } else if (c >= 'a' && c <= 'f') {
                  unicode += c - ('a' - 10);
                } else if (c >= 'A' && c <= 'F') {
                  unicode += c - ('A' - 10);
                } else {
                  throw new JSONParseException("Expected escaped Unicode BMP codepoint", line, col);
                }
              }
              c = (char) unicode;
            }
          // fallthrough
          default:
            builder.append(c);
            break;
        }
      }
    } else if ((c >= '0' && c <= '9') || c == '-') {
      // Number: grab all characters, then verify
      do {
        builder.append(c);
        if (!hasChar()) {
          break;
        }
        c = nextChar();
      } while (
        // prettier-ignore
        (c >= '0' && c <= '9') ||
        c == '.' ||
        c == 'e' ||
        c == 'E' ||
        c == '+' ||
        c == '-'
      );

      carryC = c;
      String str = builder.toString();
      if (INTEGER.matcher(str).matches()) {
        try {
          return new Token(TokenType.INTEGER, new JSONInteger(Long.parseLong(str)));
        } catch (NumberFormatException e) {
          // fallthrough
        }
      }
      if (FLOAT.matcher(str).matches()) {
        try {
          return new Token(TokenType.FLOAT, new JSONFloat(Double.parseDouble(str)));
        } catch (NumberFormatException e) {
          // fallthrough
        }
      }
      throw new JSONParseException("Invalid number '" + str + "'", tokenLine, tokenCol);
    } else {
      throw new JSONParseException("Unknown pattern", line, col);
    }
  }

  private JSONElement getData(Token t) throws IOException, JSONParseException {
    if (t.data != null) {
      // Value literal
      return t.data;
    } else if (t.type == TokenType.LEFT_BRACE) {
      return processObj();
    } else if (t.type == TokenType.LEFT_BRACKET) {
      return processArr();
    } else {
      throw new JSONParseException(
        "Unexpected " + t.type + " token '" + t.data + "'",
        tokenLine,
        tokenCol
      );
    }
  }

  private JSONObject processObj() throws IOException, JSONParseException {
    JSONObject obj = new JSONObject();
    Token t = nextToken();
    if (t.type == TokenType.RIGHT_BRACE) {
      return obj;
    }
    for (;;) {
      expect(t, TokenType.STRING);
      String key = ((JSONString) t.data).get();
      if (obj.containsKey(key)) {
        if (allowDuplicateKeys) {
          obj.remove(key);
        } else {
          throw new JSONParseException("Duplicate key '" + key + "'", tokenLine, tokenCol);
        }
      }
      expect(nextToken(), TokenType.COLON);

      Token t2 = nextToken();
      obj.put(key, getData(t2));

      t = nextToken();
      if (t.type != TokenType.COMMA) {
        expect(t, TokenType.RIGHT_BRACE);
        return obj;
      }
      t = nextToken();
    }
  }

  private JSONArray processArr() throws IOException, JSONParseException {
    JSONArray array = new JSONArray();
    Token t = nextToken();
    if (t.type == TokenType.RIGHT_BRACKET) {
      return array;
    }
    for (;;) {
      array.add(getData(t));
      t = nextToken();
      if (t.type != TokenType.COMMA) {
        expect(t, TokenType.RIGHT_BRACKET);
        return array;
      }
      t = nextToken();
    }
  }

  private void expect(Token t, TokenType type) throws JSONParseException {
    if (t.type != type) {
      throw new JSONParseException(
        "Expected " + type + " instead of " + t.type,
        tokenLine,
        tokenCol
      );
    }
  }
}
