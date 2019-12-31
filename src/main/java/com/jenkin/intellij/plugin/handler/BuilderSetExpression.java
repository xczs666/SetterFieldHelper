package com.jenkin.intellij.plugin.handler;

import com.jenkin.intellij.plugin.support.FieldHelper;
import com.jenkin.intellij.plugin.support.TargetClass;

class BuilderSetExpression extends TargetExpression {
  protected BuilderSetExpression(TargetClass targetClass) {
    super(targetClass);
  }

  @Override
  public void buildHeadExpression() {
    this.builder = new StringBuilder();
    if (!targetClass.isUseVariable()) {
      builder.append(targetClass.getPsiClass().getName())
        .append(' ')
        .append(targetClass.getVarName());
    }
    builder.append(" = ")
      .append(targetClass.getPsiClass().getName())
      .append(".builder()\n");
  }

  @Override
  public void buildSetExpression(FieldHelper fieldHelper, CharSequence str) {
    if (fieldHelper.isHasSetter()) {
      builder.append('.')
        .append(fieldHelper.getFieldName())
        .append('(')
        .append(str)
        .append(')')
        .append('\n');
    }
  }

  @Override
  public String buildTailExpression() {
    return builder.append(".build();\n").toString();
  }
}
