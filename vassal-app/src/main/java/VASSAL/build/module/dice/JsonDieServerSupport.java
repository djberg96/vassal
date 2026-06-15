package VASSAL.build.module.dice;

import java.io.IOException;

final class JsonDieServerSupport {
  private JsonDieServerSupport() {
  }

  static int[] intArrayProperty(String json, String property) throws IOException {
    final int propertyStart = json.indexOf(quoted(property));
    if (propertyStart < 0) {
      throw new IOException("Dice server response did not contain '" + property + "'.");
    }

    final int arrayStart = json.indexOf('[', propertyStart);
    final int arrayEnd = json.indexOf(']', arrayStart);
    if (arrayStart < 0 || arrayEnd < 0) {
      throw new IOException("Dice server response contained an invalid '" + property + "' array.");
    }

    final String body = json.substring(arrayStart + 1, arrayEnd).trim();
    if (body.isEmpty()) {
      return new int[0];
    }

    final String[] values = body.split(",");
    final int[] results = new int[values.length];
    for (int i = 0; i < values.length; i++) {
      try {
        results[i] = Integer.parseInt(values[i].trim());
      }
      catch (NumberFormatException e) {
        throw new IOException("Dice server response contained a non-integer result.", e);
      }
    }

    return results;
  }

  static String stringProperty(String json, String property) throws IOException {
    final int propertyStart = json.indexOf(quoted(property));
    if (propertyStart < 0) {
      throw new IOException("Dice server response did not contain '" + property + "'.");
    }

    final int colon = json.indexOf(':', propertyStart);
    final int valueStart = json.indexOf('"', colon + 1);
    if (colon < 0 || valueStart < 0) {
      throw new IOException("Dice server response contained an invalid '" + property + "' value.");
    }

    final StringBuilder value = new StringBuilder();
    boolean escaped = false;
    for (int i = valueStart + 1; i < json.length(); i++) {
      final char c = json.charAt(i);
      if (escaped) {
        value.append(c);
        escaped = false;
      }
      else if (c == '\\') {
        escaped = true;
      }
      else if (c == '"') {
        return value.toString();
      }
      else {
        value.append(c);
      }
    }

    throw new IOException("Dice server response contained an unterminated '" + property + "' value.");
  }

  static void requireNoError(String json) throws IOException {
    final int errorStart = json.indexOf(quoted("error"));
    if (errorStart < 0) {
      return;
    }

    final int colon = json.indexOf(':', errorStart);
    if (colon < 0) {
      throw new IOException("Dice server response contained an invalid error field.");
    }

    final int valueStart = nextNonWhitespace(json, colon + 1);
    if (valueStart >= 0 && json.startsWith("null", valueStart)) {
      return;
    }

    final String message = stringProperty(json.substring(errorStart), "message");
    throw new IOException("Dice server returned an error: " + message);
  }

  static String jsonString(String value) {
    final StringBuilder escaped = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      appendJsonChar(escaped, c);
    }
    return '"' + escaped.toString() + '"';
  }

  private static void appendJsonChar(StringBuilder escaped, char c) {
    if (c == '"') {
      escaped.append("\\\"");
    }
    else if (c == '\\') {
      escaped.append("\\\\");
    }
    else if (c == '\b') {
      escaped.append("\\b");
    }
    else if (c == '\f') {
      escaped.append("\\f");
    }
    else if (c == '\n') {
      escaped.append("\\n");
    }
    else if (c == '\r') {
      escaped.append("\\r");
    }
    else if (c == '\t') {
      escaped.append("\\t");
    }
    else if (c < 0x20) {
      escaped.append(String.format("\\u%04x", (int) c));
    }
    else {
      escaped.append(c);
    }
  }

  private static int nextNonWhitespace(String json, int start) {
    for (int i = start; i < json.length(); i++) {
      if (!Character.isWhitespace(json.charAt(i))) {
        return i;
      }
    }
    return -1;
  }

  private static String quoted(String value) {
    return '"' + value + '"';
  }
}
