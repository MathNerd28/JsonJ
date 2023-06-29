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
import java.nio.file.Files;
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

    static final Token TRUE = new Token(TokenType.TRUE);
    static final Token FALSE = new Token(TokenType.FALSE);
    static final Token NULL = new Token(TokenType.NULL);
    static final Token COMMA = new Token(TokenType.COMMA);
    static final Token COLON = new Token(TokenType.COLON);
    static final Token LEFT_BRACE = new Token(TokenType.LEFT_BRACE);
    static final Token LEFT_BRACKET = new Token(TokenType.LEFT_BRACKET);
    static final Token RIGHT_BRACE = new Token(TokenType.RIGHT_BRACE);
    static final Token RIGHT_BRACKET = new Token(TokenType.RIGHT_BRACKET);

    final TokenType type;
    final String str;

    Token(TokenType type) {
      this(type, null);
    }

    Token(TokenType type, String str) {
      this.type = type;
      this.str = str;
    }
  }

  private static final Pattern INTEGER = Pattern.compile("-?(?:0|[1-9]\\d*)");
  private static final Pattern FLOAT = Pattern.compile(
    "-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[Ee][+-]?(?:0|[1-9]\\d*))?"
  );

  private Reader reader;
  private StringBuilder builder = new StringBuilder();
  private int carryC = -2;
  private int line;
  private int col;
  private int tokenLine;
  private int tokenCol;

  private boolean overwriteDuplicateKeys = false;

  public JSONParser overwritingDuplicateKeys() {
    overwriteDuplicateKeys = true;
    return this;
  }

  public JSONParser exceptingDuplicateKeys() {
    overwriteDuplicateKeys = false;
    return this;
  }

  public JSONElement parse(String json) throws JSONParseException {
    try {
      // don't need to buffer
      return parse0(new StringReader(json));
    } catch (IOException e) {
      // can't occur
      throw new IllegalStateException(e);
    }
  }

  public JSONElement parse(File file) throws IOException, JSONParseException {
    return parse(new FileInputStream(file));
  }

  public JSONElement parse(InputStream stream) throws IOException, JSONParseException {
    return parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
  }

  public JSONElement parse(Reader reader) throws IOException, JSONParseException {
    return parse0(reader instanceof BufferedReader ? reader : new BufferedReader(reader, 4096));
  }

  private JSONElement parse0(Reader r) throws IOException, JSONParseException {
    reader = r;
    line = 1;
    col = 0;

    Token t = nextToken();
    switch (t.type) {
      case LEFT_BRACE:
        return processObj();
      case LEFT_BRACKET:
        return processArr();
      case STRING:
        return new JSONString(t.str);
      case TRUE:
        return JSONBoolean.TRUE;
      case FALSE:
        return JSONBoolean.FALSE;
      case NULL:
        return JSONElement.NULL;
      case INTEGER:
        try {
          long value = Long.parseLong(t.str);
          return new JSONInteger(value);
        } catch (NumberFormatException e) {
          // fallthrough
        }
      case FLOAT:
        try {
          double value = Double.parseDouble(t.str);
          return new JSONFloat(value);
        } catch (NumberFormatException e) {
          badNumber(t);
          return null;
        }
      default:
        badToken(t);
        return null;
    }
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
              return new Token(TokenType.STRING, builder.toString());
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
        return new Token(TokenType.INTEGER, str);
      } else if (FLOAT.matcher(str).matches()) {
        return new Token(TokenType.FLOAT, str);
      } else {
        throw new JSONParseException("Expected number", line, col);
      }
    } else {
      throw new JSONParseException("Unknown pattern", line, col);
    }
  }

  private JSONObject processObj() throws IOException, JSONParseException {
    JSONObject obj = new JSONObject();
    for (;;) {
      Token t = nextToken();
      if (t.type == TokenType.RIGHT_BRACE) {
        return obj;
      } else {
        expect(t, TokenType.STRING);
      }
      expect(nextToken(), TokenType.COLON);

      Token t2 = nextToken();
      switch (t2.type) {
        case STRING:
          if (obj.put(t.str, new JSONString(t2.str)) != null) {
            duplicateKey(t);
          }
          break;
        case INTEGER:
          try {
            long value = Long.parseLong(t2.str);
            if (obj.put(t.str, new JSONInteger(value)) != null) {
              duplicateKey(t);
            }
            break;
          } catch (NumberFormatException e) {
            // fallthrough
          }
        case FLOAT:
          try {
            double value = Double.parseDouble(t2.str);
            if (obj.put(t.str, new JSONFloat(value)) != null) {
              duplicateKey(t);
            }
          } catch (NumberFormatException e) {
            badNumber(t2);
          }
          break;
        case TRUE:
          if (obj.put(t.str, JSONBoolean.TRUE) != null) {
            duplicateKey(t);
          }
          break;
        case FALSE:
          if (obj.put(t.str, JSONBoolean.FALSE) != null) {
            duplicateKey(t);
          }
          break;
        case NULL:
          if (obj.put(t.str, JSONElement.NULL) != null) {
            duplicateKey(t);
          }
          break;
        case LEFT_BRACKET:
          if (obj.put(t.str, processArr()) != null) {
            duplicateKey(t);
          }
          break;
        case LEFT_BRACE:
          if (obj.put(t.str, processObj()) != null) {
            duplicateKey(t);
          }
          break;
        default:
          badToken(t2);
      }

      t = nextToken();
      if (t.type == TokenType.COMMA) {
        continue;
      }
      expect(t, TokenType.RIGHT_BRACE);
      return obj;
    }
  }

  private JSONArray processArr() throws IOException, JSONParseException {
    JSONArray array = new JSONArray();
    for (;;) {
      Token t = nextToken();
      switch (t.type) {
        case RIGHT_BRACKET:
          return array;
        case STRING:
          array.add(new JSONString(t.str));
          break;
        case INTEGER:
          try {
            long value = Long.parseLong(t.str);
            array.add(new JSONInteger(value));
            break;
          } catch (NumberFormatException e) {
            // fallthrough
          }
        case FLOAT:
          try {
            double value = Double.parseDouble(t.str);
            array.add(new JSONFloat(value));
          } catch (NumberFormatException e) {
            badNumber(t);
          }
          break;
        case TRUE:
          array.add(JSONBoolean.TRUE);
          break;
        case FALSE:
          array.add(JSONBoolean.FALSE);
          break;
        case NULL:
          array.add(JSONElement.NULL);
          break;
        case LEFT_BRACKET:
          array.add(processArr());
          break;
        case LEFT_BRACE:
          array.add(processObj());
          break;
        default:
          badToken(t);
      }

      t = nextToken();
      if (t.type == TokenType.COMMA) {
        continue;
      }
      expect(t, TokenType.RIGHT_BRACKET);
      return array;
    }
  }

  private void badToken(Token t) throws JSONParseException {
    throw new JSONParseException(
      "Unexpected " + t.type + " token '" + t.str + "'",
      tokenLine,
      tokenCol
    );
  }

  private void badNumber(Token t) throws JSONParseException {
    throw new JSONParseException("Invalid " + t.type + " '" + t.str + "'", tokenLine, tokenCol);
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

  private void duplicateKey(Token t) throws JSONParseException {
    if (!overwriteDuplicateKeys) {
      throw new JSONParseException("Duplicate key '" + t.str + "'", tokenLine, tokenCol);
    }
  }

  public static void main(String... args) throws IOException, JSONParseException {
    String str = new String(
      Files.readAllBytes(
        new File("/Users/xbhalla/Library/Application Support/Code/User/settings.json").toPath()
      )
    );
    System.out.println(((JSONObject) new JSONParser().parse(str)).toJSONFormatted());
  }
}
