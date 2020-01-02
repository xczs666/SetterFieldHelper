package com.jenkin.intellij.plugin.handler;

import com.intellij.psi.util.PsiTypesUtil;
import com.jenkin.intellij.plugin.support.AbstractHelperClass;
import com.jenkin.intellij.plugin.support.FieldHelper;
import com.jenkin.intellij.plugin.support.ParameterClass;
import com.jenkin.intellij.plugin.support.TargetClass;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

public class ExpressionBuilder {
  private TargetExpression targetExpression;
  private List<ParameterClass> parameters;

  public static ExpressionBuilder of(TargetClass targetClass, List<ParameterClass> parameters) {
    return new ExpressionBuilder(TargetExpression.create(targetClass), parameters);
  }

  private ExpressionBuilder(TargetExpression targetExpression, List<ParameterClass> parameters) {
    this.targetExpression = targetExpression;
    this.parameters = parameters;
  }

  public String buildExpression() {
    targetExpression.buildHeadExpression();
    final TargetClass targetClass = targetExpression.getTargetClass();
    for (FieldHelper field : targetClass.getFields()) {
      Optional<Pair<ParameterClass, FieldHelper>> first = parameters.stream().filter(o -> o.findField(field.getFieldName()) != null).map(o -> new Pair<>(o, o.findField(field.getFieldName()))).findFirst();
      if (first.isPresent()) {
        Pair<ParameterClass, FieldHelper> pair = first.get();
        // 字段名精确匹配
        // TODO 未做类型匹配喝类型转换
        targetExpression.buildSetExpression(field, buildGetExpression(pair.getKey(), pair.getValue()));
      } else {
        Optional<Pair<ParameterClass, List<FieldHelper>>> fuzzyOption = parameters.stream().filter(o -> o.findFieldByFuzzyName(field.getFuzzyName()) != null).map(o -> new Pair<>(o, o.findFieldByFuzzyName(field.getFuzzyName()))).findFirst();
        if (fuzzyOption.isPresent()) {
          Pair<ParameterClass, List<FieldHelper>> pair = fuzzyOption.get();
          // 字段名模糊匹配
          // TODO 未做类型匹配喝类型转换
          targetExpression.buildSetExpression(field, buildGetExpression(pair.getKey(), pair.getValue().get(0)));
        } else {
          Optional<ParameterClass> parameterOption = parameters.stream().filter(o -> field.getFieldName().equals(o.getVarName())
            && PsiTypesUtil.compareTypes(field.getPsiField().getType(), o.getPsiType(), true)).findFirst();
          if (parameterOption.isPresent()) {
            // 直接参数赋值
            // TODO 未做类型匹配喝类型转换
            targetExpression.buildSetExpression(field, parameterOption.get().getVarName());
          } else {
            // 未匹配到
            targetExpression.buildSetExpression(field, "");
          }
        }
      }
    }
    return targetExpression.buildTailExpression();
  }

  private StringBuilder buildGetExpression(AbstractHelperClass helperClass, FieldHelper fieldHelper) {
    StringBuilder builder = new StringBuilder();
    if (helperClass.isThis()) {
      builder.append(helperClass.getVarName())
        .append('.')
        .append(fieldHelper.getFieldName());
    } else {
      builder.append(helperClass.getVarName())
        .append(".get")
        .append(fieldHelper.getFirstUpperName())
        .append("()");
    }
    return builder;
  }

}
