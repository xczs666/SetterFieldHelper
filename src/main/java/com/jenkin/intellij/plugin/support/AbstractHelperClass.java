package com.jenkin.intellij.plugin.support;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractHelperClass {
  @Nullable
  private PsiClass psiClass;
  @NotNull
  private PsiType psiType;
  @NotNull
  private String varName;

  protected AbstractHelperClass(PsiClass psiClass, String varName) {
    this.psiClass = psiClass;
    this.psiType = PsiTypesUtil.getClassType(psiClass);
    this.varName = varName;
  }

  protected AbstractHelperClass(PsiParameter psiParameter) {
    this.psiType = psiParameter.getType();
    this.psiClass = PsiTypesUtil.getPsiClass(psiParameter.getType());
    this.varName = psiParameter.getName();
  }

  public PsiClass getPsiClass() {
    return psiClass;
  }

  public String getVarName() {
    return varName;
  }

  public boolean isThis() {
    return "this" .equals(varName);
  }

  public PsiType getPsiType() {
    return psiType;
  }
}
