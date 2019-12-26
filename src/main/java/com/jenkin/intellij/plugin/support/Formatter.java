package com.jenkin.intellij.plugin.support;

import org.apache.commons.lang.StringUtils;

public class Formatter {
  public static final String RET = "<ret>";
  public static final String RET_CLASS = "<retClass>";
  public static final String RET_FIELD_NAME = "<retFieldName>";
  public static final String RET_GET_METHOD_NAME = "<retGetMethodName>";
  public static final String RET_SET_METHOD_NAME = "<retSetMethodName>";
  public static final String ARG = "<arg>";
  public static final String ARG_FIELD_NAME = "<argFieldName>";
  public static final String ARG_GET_METHOD_NAME = "<argGetMethodName>";
  public static final String ARG_SET_METHOD_NAME = "<argSetMethodName>";
  private String prefix;
  private String line;
  private String emptyLine;
  private String suffix;

  Formatter(String prefix, String line, String emptyLine, String suffix) {
    this.prefix = StringUtils.defaultString(prefix);
    this.line = line;
    this.emptyLine = emptyLine;
    this.suffix = StringUtils.defaultString(suffix);
  }

  public static FormatterBuilder builder() {
    return new FormatterBuilder();
  }

  public String getPrefix() {
    return this.prefix;
  }

  public String getLine() {
    return this.line;
  }

  public String getEmptyLine() {
    return this.emptyLine;
  }

  public String getSuffix() {
    return this.suffix;
  }

  public static class FormatterBuilder {
    private String prefix;
    private String line;
    private String emptyLine;
    private String suffix;

    FormatterBuilder() {
    }

    public Formatter.FormatterBuilder prefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Formatter.FormatterBuilder line(String line) {
      this.line = line;
      return this;
    }

    public Formatter.FormatterBuilder emptyLine(String emptyLine) {
      this.emptyLine = emptyLine;
      return this;
    }

    public Formatter.FormatterBuilder suffix(String suffix) {
      this.suffix = suffix;
      return this;
    }

    public Formatter build() {
      return new Formatter(prefix, line, emptyLine, suffix);
    }

    public String toString() {
      return "Formatter.FormatterBuilder(prefix=" + this.prefix + ", line=" + this.line + ", emptyLine=" + this.emptyLine + ", suffix=" + this.suffix + ")";
    }
  }
}
