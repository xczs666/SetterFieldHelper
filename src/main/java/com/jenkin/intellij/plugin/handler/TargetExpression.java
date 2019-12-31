package com.jenkin.intellij.plugin.handler;

import com.jenkin.intellij.plugin.support.FieldHelper;
import com.jenkin.intellij.plugin.support.TargetClass;

abstract class TargetExpression {
  protected TargetClass targetClass;
  protected StringBuilder builder;

  protected TargetExpression(TargetClass targetClass) {
    this.targetClass = targetClass;
  }

  public static TargetExpression create(TargetClass targetClass) {
    if (targetClass.isUseMethod() && targetClass.isMethodReturnTypeIsNon()) {
      return new ConstructorExpression(targetClass);
    }
    if (targetClass.getPsiClass().hasAnnotation("lombok.Builder")
      || targetClass.getPsiClass().hasAnnotation("lombok.experimental.SuperBuilder")){
      return new BuilderSetExpression(targetClass);
    }
    return new NewTypeSetExpression(targetClass);
  }

  public void buildHeadExpression() {
    this.builder = new StringBuilder();
  }

  public abstract void buildSetExpression(FieldHelper fieldHelper, CharSequence str);

  public String buildTailExpression() {
    return builder.toString();
  }

  public TargetClass getTargetClass() {
    return targetClass;
  }
}
