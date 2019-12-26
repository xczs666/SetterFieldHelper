package com.jenkin.intellij.plugin.support;

public class Formatter {
  public static final String OBJ = "${obj}";
  public static final String ARG = "${arg}";
  public static final String FIELD_NAME = "${fieldName}";
  public static final String GET_METHOD_NAME = "${getMethodName}";
  public static final String SET_METHOD_NAME = "${setMethodName}";
  private String prefix = "";
  private String line;
  private String suffix = "";

  Formatter(String prefix, String line, String suffix) {
    this.prefix = prefix;
    this.line = line;
    this.suffix = suffix;
  }

  public static FormatterBuilder builder() {
    return new FormatterBuilder();
  }

  public static class FormatterBuilder {
    private String prefix;
    private String line;
    private String suffix;

    FormatterBuilder() {
    }

    public FormatterBuilder prefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public FormatterBuilder line(String line) {
      this.line = line;
      return this;
    }

    public FormatterBuilder suffix(String suffix) {
      this.suffix = suffix;
      return this;
    }

    public Formatter build() {
      return new Formatter(prefix, line, suffix);
    }

    public String toString() {
      return "Formatter.FormatterBuilder(prefix=" + this.prefix + ", line=" + this.line + ", suffix=" + this.suffix + ")";
    }
  }
}
