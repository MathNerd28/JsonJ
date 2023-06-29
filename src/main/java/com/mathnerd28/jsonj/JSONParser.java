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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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

  private class Token {

    final TokenType type;
    final String str;
    final int line;
    final int col;

    Token(TokenType type) {
      this(type, null);
    }

    Token(TokenType type, String str) {
      this.type = type;
      this.str = str;
      this.line = JSONParser.this.line;
      this.col = JSONParser.this.col;
    }

    @Override
    public String toString() {
      switch (type) {
        case STRING:
        case INTEGER:
        case FLOAT:
          return type + " = " + str;
        default:
          return type.toString();
      }
    }
  }

  private static final Pattern INTEGER = Pattern.compile("-?(?:0|[1-9]\\d*)");
  private static final Pattern FLOAT = Pattern.compile(
    "-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[Ee][+-]?(?:0|[1-9]\\d*))?"
  );

  private Reader reader;
  private ListIterator<Token> tokens;
  private int line;
  private int col;
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
    tokens = null;
    line = 1;
    col = 0;
    tokens = tokenize().listIterator();

    Token t = tokens.next();
    if (t.type == TokenType.LEFT_BRACE) {
      return processObj();
    } else if (t.type == TokenType.LEFT_BRACKET) {
      return processArr();
    } else {
      throw new JSONParseException("Unexpected token " + t.type, line, col);
    }
  }

  private int read() throws IOException {
    int c = reader.read();
    if (c == '\n') {
      col = 0;
      line++;
    } else {
      col++;
    }
    return c;
  }

  private List<Token> tokenize() throws IOException, JSONParseException {
    int c = read();
    if (c == -1) {
      termination();
    }

    List<Token> tokens = new ArrayList<>();
    StringBuilder builder = new StringBuilder();
    boolean noRead = true;
    while (noRead || (c = read()) != -1) {
      noRead = false;

      // These don't require the buffer, fastpath
      switch (c) {
        case ' ':
        case '\n':
        case '\t':
        case '\r':
          // whitespace
          continue;
        case ',':
          tokens.add(new Token(TokenType.COMMA));
          continue;
        case ':':
          tokens.add(new Token(TokenType.COLON));
          continue;
        case '{':
          tokens.add(new Token(TokenType.LEFT_BRACE));
          continue;
        case '[':
          tokens.add(new Token(TokenType.LEFT_BRACKET));
          continue;
        case '}':
          tokens.add(new Token(TokenType.RIGHT_BRACE));
          continue;
        case ']':
          tokens.add(new Token(TokenType.RIGHT_BRACKET));
          continue;
        case 't':
          if (
            // prettier-ignore
            (c = read()) == 'r' &&
            (c = read()) == 'u' &&
            (c = read()) == 'e'
          ) {
            tokens.add(new Token(TokenType.TRUE));
          } else if (c == -1) {
            termination();
          } else {
            throw new JSONParseException("Expected keyword 'true'", line, col);
          }
          continue;
        case 'f':
          if (
            // prettier-ignore
            (c = read()) == 'a' &&
            (c = read()) == 'l' &&
            (c = read()) == 's' &&
            (c = read()) == 'e'
          ) {
            tokens.add(new Token(TokenType.FALSE));
          } else if (c == -1) {
            termination();
          } else {
            throw new JSONParseException("Expected keyword 'false'", line, col);
          }
          continue;
        case 'n':
          if (
            // prettier-ignore
            (c = read()) == 'u' &&
            (c = read()) == 'l' &&
            (c = read()) == 'l'
          ) {
            tokens.add(new Token(TokenType.NULL));
          } else if (c == -1) {
            termination();
          } else {
            throw new JSONParseException("Expected keyword 'null'", line, col);
          }
          continue;
        default:
        // no fastpath
      }

      // Clear buffer for operation
      builder.delete(0, builder.length());

      if (c == '"') {
        // String
        boolean escaped = false;
        loop:for (;;) {
          c = read();
          if (c == -1) {
            termination();
          } else if (c < 0x0020) {
            throw new JSONParseException("Unescaped control character", line, col);
          }
          switch (c) {
            case '"':
              if (escaped) {
                escaped = false;
                builder.append('"');
              } else {
                break loop;
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
              builder.append((char) c);
              break;
            case 'f':
              if (escaped) {
                escaped = false;
                c = '\f';
              }
              builder.append((char) c);
              break;
            case 'n':
              if (escaped) {
                escaped = false;
                c = '\n';
              }
              builder.append((char) c);
              break;
            case 'r':
              if (escaped) {
                escaped = false;
                c = '\r';
              }
              builder.append((char) c);
              break;
            case 't':
              if (escaped) {
                escaped = false;
                c = '\t';
              }
              builder.append((char) c);
              break;
            case 'u':
              if (escaped) {
                escaped = false;
                int unicode = 0;
                for (int i = 0; i < 4; i++) {
                  unicode <<= 4;
                  c = read();
                  if ((c >= '0' && c <= '9')) {
                    unicode += c - '0';
                  } else if (c >= 'a' && c <= 'f') {
                    unicode += c - ('a' - 10);
                  } else if (c >= 'A' && c <= 'F') {
                    unicode += c - ('A' - 10);
                  } else if (c == -1) {
                    termination();
                  } else {
                    throw new JSONParseException(
                      "Expected escaped Unicode BMP codepoint",
                      line,
                      col
                    );
                  }
                }
                c = (char) unicode;
              }
            // fallthrough
            default:
              builder.append((char) c);
              break;
          }
        }
        tokens.add(new Token(TokenType.STRING, builder.toString()));
      } else if ((c >= '0' && c <= '9') || c == '-') {
        // Number: grab all characters, then verify
        do {
          builder.append((char) c);
          c = read();
        } while (
          // prettier-ignore
          (c >= '0' && c <= '9') ||
          c == '.' ||
          c == 'e' ||
          c == 'E' ||
          c == '+' ||
          c == '-'
        );

        String str = builder.toString();
        if (INTEGER.matcher(str).matches()) {
          tokens.add(new Token(TokenType.INTEGER, str));
        } else if (FLOAT.matcher(str).matches()) {
          tokens.add(new Token(TokenType.FLOAT, str));
        } else {
          throw new JSONParseException("Expected number", line, col);
        }
        noRead = true;
      } else {
        throw new JSONParseException("Unknown pattern", line, col);
      }
    }
    reader.close();
    return tokens;
  }

  private JSONObject processObj() throws JSONParseException {
    JSONObject obj = new JSONObject();
    for (;;) {
      checkTermination();
      Token t = tokens.next();
      if (t.type == TokenType.RIGHT_BRACE) {
        return obj;
      } else {
        expect(t, TokenType.STRING);
        checkTermination();
      }

      Token t2 = tokens.next();
      if (t2.type != TokenType.COLON) {
        throw new JSONParseException("Expected colon instead of " + t2.type, t2.line, t2.col);
      } else if (!tokens.hasNext()) {
        throw new JSONParseException("Unexpected termination", t2.line, t2.col);
      }

      t2 = tokens.next();
      switch (t2.type) {
        case STRING:
          obj.put(t.str, new JSONString(t2.str));
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
            throw new JSONParseException("Invalid number " + t2.str, t2.line, t2.col);
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

      t = tokens.next();
      if (t.type == TokenType.RIGHT_BRACE) {
        return obj;
      } else {
        expect(t, TokenType.COMMA);
      }
    }
  }

  private JSONArray processArr() throws JSONParseException {
    JSONArray array = new JSONArray();
    for (;;) {
      checkTermination();
      Token t = tokens.next();
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
            throw new JSONParseException("Invalid number " + t.str, t.line, t.col);
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
      t = tokens.next();
      if (t.type == TokenType.RIGHT_BRACKET) {
        return array;
      } else {
        expect(t, TokenType.COMMA);
      }
    }
  }

  private void termination() throws JSONParseException {
    throw new JSONParseException("Unexpected termination", line, col);
  }

  private void badToken(Token t) throws JSONParseException {
    throw new JSONParseException("Unexpected token " + t.type, t.line, t.col);
  }

  private void expect(Token t, TokenType type) throws JSONParseException {
    if (t.type != type) {
      throw new JSONParseException("Expected " + type + " instead of " + t.type, t.line, t.col);
    }
  }

  private void checkTermination() throws JSONParseException {
    if (!tokens.hasNext()) {
      Token t = tokens.previous();
      throw new JSONParseException("Unexpected termination", t.line, t.col);
    }
  }

  private void duplicateKey(Token t) throws JSONParseException {
    if (!overwriteDuplicateKeys) {
      throw new JSONParseException("Duplicate key '" + t.str + "'", t.line, t.col);
    }
  }
}
