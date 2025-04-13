package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ByteSize implements Token {
  private final String original;
  private final long bytes;

  public ByteSize(String value) {
    this.original = value;
    this.bytes = parseToBytes(value);
  }

  private long parseToBytes(String str) {
    str = str.toUpperCase().trim();
    if (str.endsWith("KB")) {
      return (long) (Double.parseDouble(str.replace("KB", "")) * 1024);
    } else if (str.endsWith("MB")) {
      return (long) (Double.parseDouble(str.replace("MB", "")) * 1024 * 1024);
    } else if (str.endsWith("GB")) {
      return (long) (Double.parseDouble(str.replace("GB", "")) * 1024 * 1024 * 1024);
    } else if (str.endsWith("B")) {
      return Long.parseLong(str.replace("B", ""));
    } else {
      throw new IllegalArgumentException("Unsupported byte size format: " + str);
    }
  }

  @Override
  public Object value() {
    return bytes;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE; // Assuming you have this in your TokenType enum
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(bytes);
  }

  public long getBytes() {
    return bytes;
  }

  public String getOriginal() {
    return original;
  }
}
