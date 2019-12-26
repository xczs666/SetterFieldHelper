package com.jenkin.intellij.plugin.support;

public class Strategy {
  private final Formatter outMethodArgMethod;
  private final Formatter outFieldArgMethod;
  private final Formatter outMethodArgField;
  public static final Strategy NORMAL = Strategy.builder()
    .outMethodArgMethod(Formatter.builder().prefix("new " + Formatter.RET_CLASS + "();\n")
      .line(String.format("%s.%s(%s.%s());\n", Formatter.RET, Formatter.RET_SET_METHOD_NAME, Formatter.ARG, Formatter.ARG_GET_METHOD_NAME))
      .emptyLine(String.format("%s.%s();\n", Formatter.RET, Formatter.RET_SET_METHOD_NAME))
      .build())
    .outMethodArgField(Formatter.builder().prefix("new " + Formatter.RET_CLASS + "();\n")
      .line(String.format("%s.%s(%s.%s);\n", Formatter.RET, Formatter.RET_SET_METHOD_NAME, Formatter.ARG, Formatter.ARG_FIELD_NAME))
      .emptyLine(String.format("%s.%s();\n", Formatter.RET, Formatter.RET_SET_METHOD_NAME))
      .build())
    .outFieldArgMethod(Formatter.builder()
      .line(String.format("%s.%s = %s.%s();\n", Formatter.RET, Formatter.RET_FIELD_NAME, Formatter.ARG, Formatter.ARG_GET_METHOD_NAME))
      .emptyLine(String.format("%s.%s = ;\n", Formatter.RET, Formatter.RET_FIELD_NAME))
      .build())
    .build();
  public static final Strategy BUILDER = Strategy.builder()
    .outMethodArgMethod(Formatter.builder().prefix(Formatter.RET_CLASS + ".builder()\n")
      .line(String.format(".%s(%s.%s())\n", Formatter.RET_FIELD_NAME, Formatter.ARG, Formatter.ARG_GET_METHOD_NAME))
      .emptyLine(String.format(".%s()\n", Formatter.RET_FIELD_NAME))
      .suffix(".build();").build())
    .outMethodArgField(Formatter.builder().prefix(Formatter.RET_CLASS + ".builder()\n")
      .line(String.format(".%s(%s.%s)\n", Formatter.RET_FIELD_NAME, Formatter.ARG, Formatter.ARG_FIELD_NAME))
      .emptyLine(String.format(".%s()\n", Formatter.RET_FIELD_NAME))
      .suffix(".build();").build())
    .outFieldArgMethod(Formatter.builder().prefix("new " + Formatter.RET_CLASS + "();\n")
      .line(String.format("%s.%s = %s.%s();\n", Formatter.RET, Formatter.RET_FIELD_NAME, Formatter.ARG, Formatter.ARG_GET_METHOD_NAME))
      .emptyLine(String.format("%s.%s = ;\n", Formatter.RET, Formatter.RET_FIELD_NAME))
      .build())
    .build();

  Strategy(Formatter outMethodArgMethod, Formatter outFieldArgMethod, Formatter outMethodArgField) {
    this.outMethodArgMethod = outMethodArgMethod;
    this.outFieldArgMethod = outFieldArgMethod;
    this.outMethodArgField = outMethodArgField;
  }

  public static StrategyBuilder builder() {
    return new StrategyBuilder();
  }

  public Formatter getOutMethodArgMethod() {
    return this.outMethodArgMethod;
  }

  public Formatter getOutFieldArgMethod() {
    return this.outFieldArgMethod;
  }

  public Formatter getOutMethodArgField() {
    return this.outMethodArgField;
  }

  public static class StrategyBuilder {
    private Formatter outMethodArgMethod;
    private Formatter outFieldArgMethod;
    private Formatter outMethodArgField;

    StrategyBuilder() {
    }

    public StrategyBuilder outMethodArgMethod(Formatter outMethodArgMethod) {
      this.outMethodArgMethod = outMethodArgMethod;
      return this;
    }

    public StrategyBuilder outFieldArgMethod(Formatter outFieldArgMethod) {
      this.outFieldArgMethod = outFieldArgMethod;
      return this;
    }

    public StrategyBuilder outMethodArgField(Formatter outMethodArgField) {
      this.outMethodArgField = outMethodArgField;
      return this;
    }

    public Strategy build() {
      return new Strategy(outMethodArgMethod, outFieldArgMethod, outMethodArgField);
    }

    public String toString() {
      return "Strategy.StrategyBuilder(outMethodArgMethod=" + this.outMethodArgMethod + ", outFieldArgMethod=" + this.outFieldArgMethod + ", outMethodArgField=" + this.outMethodArgField + ")";
    }
  }
}
