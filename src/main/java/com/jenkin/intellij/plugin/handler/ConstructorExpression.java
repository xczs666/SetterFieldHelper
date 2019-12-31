package com.jenkin.intellij.plugin.handler;

import com.jenkin.intellij.plugin.support.FieldHelper;
import com.jenkin.intellij.plugin.support.TargetClass;

class ConstructorExpression extends TargetExpression {
  protected ConstructorExpression(TargetClass targetClass) {
    super(targetClass);
  }

  @Override
  public void buildSetExpression(FieldHelper fieldHelper, CharSequence str) {
    builder.append(targetClass.getVarName())
      .append('.')
      .append(fieldHelper.getFieldName())
      .append(' ')
      .append('=')
      .append(' ')
      .append(str)
      .append(';')
      .append('\n');
  }
}
