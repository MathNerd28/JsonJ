package com.mathnerd28.jsonj;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class JSONParser {

  private static final Pattern INTEGER = Pattern.compile("-?(?:0|[1-9]\\d*)");
  private static final Pattern FLOAT = Pattern.compile(
    "-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[Ee][+-]?(?:0|[1-9]\\d*))?"
  );

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
      return parse(new StringReader(json));
    } catch (IOException e) {
      // can't occur
      throw new IllegalStateException(e);
    }
  }

  public JSONElement parse(File file) throws IOException, JSONParseException {
    return parse(new FileReader(file));
  }

  public JSONElement parse(InputStream stream) throws IOException, JSONParseException {
    return parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
  }

  public JSONElement parse(Reader reader) throws IOException, JSONParseException {
    if (reader == null) {
      throw new IllegalArgumentException("reader cannot be null");
    }

    Iterator<Token> tokens = tokenize(reader).iterator();
    Token t = tokens.next();
    if (t.type == Token.Type.LEFT_BRACE) {
      return processObj(tokens);
    } else if (t.type == Token.Type.LEFT_BRACKET) {
      return processArr(tokens);
    } else {
      throwIllegal();
      return null;
    }
  }

  private List<Token> tokenize(Reader reader) throws IOException, JSONParseException {
    List<Token> tokens = new ArrayList<>();
    int c = 0;
    StringBuilder builder = new StringBuilder();
    boolean noRead = false;

    while (noRead || (c = reader.read()) != -1) {
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
          tokens.add(Token.COMMA);
          continue;
        case ':':
          tokens.add(Token.COLON);
          continue;
        case '{':
          tokens.add(Token.LEFT_BRACE);
          continue;
        case '[':
          tokens.add(Token.LEFT_BRACKET);
          continue;
        case '}':
          tokens.add(Token.RIGHT_BRACE);
          continue;
        case ']':
          tokens.add(Token.RIGHT_BRACKET);
          continue;
        case 't':
          if (
            // prettier-ignore
            (c = reader.read()) == 'r' &&
            (c = reader.read()) == 'u' &&
            (c = reader.read()) == 'e'
          ) {
            tokens.add(Token.TRUE);
          } else if (c == -1) {
            throwEOF();
          } else {
            throwIllegal();
          }
          continue;
        case 'f':
          if (
            // prettier-ignore
            (c = reader.read()) == 'a' &&
            (c = reader.read()) == 'l' &&
            (c = reader.read()) == 's' &&
            (c = reader.read()) == 'e'
          ) {
            tokens.add(Token.FALSE);
          } else if (c == -1) {
            throwEOF();
          } else {
            throwIllegal();
          }
          continue;
        case 'n':
          if (
            // prettier-ignore
            (c = reader.read()) == 'u' &&
            (c = reader.read()) == 'l' &&
            (c = reader.read()) == 'l'
          ) {
            tokens.add(Token.NULL);
          } else if (c == -1) {
            throwEOF();
          } else {
            throwIllegal();
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
          if (c == -1) {
            throwEOF();
          } else if (c < 0x0020) {
            throwIllegal();
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
                  c = reader.read();
                  if ((c >= '0' && c <= '9')) {
                    unicode += c - '0';
                  } else if (c >= 'a' && c <= 'f') {
                    unicode += c - ('a' - 10);
                  } else if (c >= 'A' && c <= 'F') {
                    unicode += c - ('A' - 10);
                  } else if (c == -1) {
                    throwEOF();
                  } else {
                    throwIllegal();
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
        tokens.add(new Token(Token.Type.STRING, builder.toString()));
      } else if ((c >= '0' && c <= '9') || c == '-') {
        // Number: grab all characters, then verify
        do {
          builder.append((char) c);
          c = reader.read();
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
          tokens.add(new Token(Token.Type.INTEGER, str));
        } else if (FLOAT.matcher(str).matches()) {
          tokens.add(new Token(Token.Type.FLOAT, str));
        } else {
          throwIllegal();
        }
        noRead = true;
      } else {
        throwIllegal();
      }
    }
    reader.close();
    return tokens;
  }

  private JSONObject processObj(Iterator<Token> tokens) throws JSONParseException {
    JSONObject obj = new JSONObject();
    while (tokens.hasNext()) {
      Token t = tokens.next();
      if (t.type == Token.Type.RIGHT_BRACE) {
        return obj;
      } else if (
        t.type != Token.Type.STRING ||
        !tokens.hasNext() ||
        tokens.next().type != Token.Type.COLON ||
        !tokens.hasNext()
      ) {
        throwIllegal();
      }
      String key = t.str;

      t = tokens.next();
      switch (t.type) {
        case STRING:
          obj.put(key, new JSONString(t.str));
          break;
        case INTEGER:
          try {
            long value = Long.parseLong(t.str);
            throwIfExists(obj, key, new JSONInteger(value));
            break;
          } catch (NumberFormatException e) {
            // fallthrough
          }
        case FLOAT:
          try {
            double value = Double.parseDouble(t.str);
            throwIfExists(obj, key, new JSONFloat(value));
          } catch (NumberFormatException e) {
            throwIllegal();
          }
          break;
        case TRUE:
          throwIfExists(obj, key, JSONBoolean.TRUE);
          break;
        case FALSE:
          throwIfExists(obj, key, JSONBoolean.FALSE);
          break;
        case NULL:
          throwIfExists(obj, key, JSONElement.NULL);
          break;
        case LEFT_BRACKET:
          throwIfExists(obj, key, processArr(tokens));
          break;
        case LEFT_BRACE:
          throwIfExists(obj, key, processObj(tokens));
          break;
        default:
          throwIllegal();
      }
      t = tokens.next();
      if (t.type == Token.Type.RIGHT_BRACE) {
        return obj;
      } else if (t.type != Token.Type.COMMA) {
        throwIllegal();
      }
    }
    throwIllegal();
    return null;
  }

  private void throwIfExists(JSONObject obj, String key, JSONElement value) throws JSONParseException {
    if (obj.put(key, value) != null && !overwriteDuplicateKeys) {
      throw new JSONParseException("Duplicate key '" + key + "'");
    }
  }

  private JSONArray processArr(Iterator<Token> tokens) throws JSONParseException {
    JSONArray array = new JSONArray();
    while (tokens.hasNext()) {
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
            throwIllegal();
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
          array.add(processArr(tokens));
          break;
        case LEFT_BRACE:
          array.add(processObj(tokens));
          break;
        default:
          throwIllegal();
      }
      t = tokens.next();
      if (t.type == Token.Type.RIGHT_BRACKET) {
        return array;
      } else if (t.type != Token.Type.COMMA) {
        throwIllegal();
      }
    }
    throwIllegal();
    return null;
  }

  private static void throwEOF() throws JSONParseException {
    throw new JSONParseException("Unexpected termination");
  }

  private static void throwIllegal() throws JSONParseException {
    throw new JSONParseException("Illegal character");
  }

  private static class Token {

    enum Type {
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

    static final Token TRUE = new Token(Type.TRUE, null);
    static final Token FALSE = new Token(Type.FALSE, null);
    static final Token NULL = new Token(Type.NULL, null);
    static final Token COMMA = new Token(Type.COMMA, null);
    static final Token COLON = new Token(Type.COLON, null);
    static final Token LEFT_BRACE = new Token(Type.LEFT_BRACE, null);
    static final Token LEFT_BRACKET = new Token(Type.LEFT_BRACKET, null);
    static final Token RIGHT_BRACE = new Token(Type.RIGHT_BRACE, null);
    static final Token RIGHT_BRACKET = new Token(Type.RIGHT_BRACKET, null);

    final Type type;
    final String str;

    Token(Type type, String str) {
      this.type = type;
      this.str = str;
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
}
