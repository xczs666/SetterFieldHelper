package com.jenkin.intellij.plugin.handler;

import com.jenkin.intellij.plugin.support.FieldHelper;
import com.jenkin.intellij.plugin.support.TargetClass;

class NewTypeSetExpression extends TargetExpression {
  protected NewTypeSetExpression(TargetClass targetClass) {
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
    builder.append(' ')
      .append('=')
      .append(' ')
      .append("new ")
      .append(targetClass.getPsiClass().getName())
      .append("();\n");
  }

  @Override
  public void buildSetExpression(FieldHelper fieldHelper, CharSequence str) {
    if (fieldHelper.isHasSetter()) {
      builder.append(targetClass.getVarName())
        .append(".set")
        .append(fieldHelper.getFirstUpperName())
        .append('(')
        .append(str)
        .append(')')
        .append(';')
        .append('\n');
    }
  }
}
