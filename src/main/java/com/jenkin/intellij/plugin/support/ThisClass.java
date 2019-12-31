package com.jenkin.intellij.plugin.support;

import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PsiTypesUtil;

public class ThisClass extends AbstractHelperClass {

  public static ThisClass create(PsiClass psiClass) {
    return new ThisClass(psiClass);
  }

  private ThisClass(PsiClass psiClass) {
    super(psiClass, "this");
  }

  public boolean isSame(AbstractHelperClass helperClass) {
    return PsiTypesUtil.compareTypes(getPsiType(), helperClass.getPsiType(), true);
  }
}
