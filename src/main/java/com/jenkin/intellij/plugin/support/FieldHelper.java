package com.jenkin.intellij.plugin.support;

import com.intellij.psi.PsiField;

public class FieldHelper {
  private PsiField psiField;
  private String fieldName;
  // 首字母大写，set/get 方法使用
  private String firstUpperName;
  // 去掉字幕数字之外字符且全部转为小写
  private String fuzzyName;
  private Boolean hasSetter;
  private Boolean hasGetter;

  static FieldHelper create(PsiField psiField, Boolean setter, Boolean getter) {
    return new FieldHelper(psiField, setter, getter);
  }

  private FieldHelper(PsiField psiField, Boolean setter, Boolean getter) {
    this.psiField = psiField;
    this.fieldName = psiField.getName();
    this.firstUpperName = this.fieldName.substring(0, 1).toUpperCase() + (this.fieldName.length() > 1 ? this.fieldName.substring(1) : "");
    this.fuzzyName = this.fieldName.replaceAll("[^A-Za-z0-9$_]", "").toLowerCase();
    this.hasSetter = setter;
    this.hasGetter = getter;
  }

  public PsiField getPsiField() {
    return psiField;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getFirstUpperName() {
    return firstUpperName;
  }

  public String getFuzzyName() {
    return fuzzyName;
  }

  public Boolean isHasSetter() {
    return hasSetter;
  }

  public Boolean isHasGetter() {
    return hasGetter;
  }
}
