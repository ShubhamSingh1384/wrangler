package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Represents a duration of time parsed from strings like "500ms", "2s", "3.5m", or "1h".
 */
public class TimeDuration implements Token {
  private final long milliseconds;
  private final String original;

  public TimeDuration(String value) {
    this.original = value;
    this.milliseconds = parseToMilliseconds(value.trim().toLowerCase());
  }

  private long parseToMilliseconds(String value) {
    if (value.endsWith("ms")) {
      return (long) Double.parseDouble(value.replace("ms", ""));
    } else if (value.endsWith("s")) {
      return (long) (Double.parseDouble(value.replace("s", "")) * 1000);
    } else if (value.endsWith("m")) {
      return (long) (Double.parseDouble(value.replace("m", "")) * 60 * 1000);
    } else if (value.endsWith("h")) {
      return (long) (Double.parseDouble(value.replace("h", "")) * 60 * 60 * 1000);
    } else {
      throw new IllegalArgumentException("Invalid time duration: " + value);
    }
  }

  public long getMilliseconds() {
    return milliseconds;
  }

  public double getSeconds() {
    return milliseconds / 1000.0;
  }

  @Override
  public Object value() {
    return milliseconds;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION; // 👈 Add this to your enum
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(milliseconds);
  }

  @Override
  public String toString() {
    return milliseconds + "ms";
  }

  public String getOriginal() {
    return original;
  }
}
