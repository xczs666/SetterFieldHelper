package com.jenkin.intellij.plugin.support;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PropertyUtil;
import com.jenkin.intellij.plugin.utils.NameUtil;

import java.util.ArrayList;
import java.util.List;

public class TargetClass extends AbstractHelperClass {
  private boolean useVariable;
  private boolean useMethod;
  private boolean methodReturnTypeIsNon;
  private List<FieldHelper> fields;

  public static TargetClass createUseMethod(PsiClass psiClass, String varName, boolean methodReturnTypeIsNon) {
    return new TargetClass(psiClass, varName, false, true, methodReturnTypeIsNon);
  }

  public static TargetClass createUseVariable(PsiClass psiClass, String varName) {
    return new TargetClass(psiClass, varName, true, false, false);
  }

  private TargetClass(PsiClass psiClass, String varName, boolean useVariable, boolean useMethod, boolean methodReturnTypeIsNon) {
    super(psiClass, varName == null ? NameUtil.variableName(psiClass.getName()) : varName);
    this.useVariable = useVariable;
    this.useMethod = useMethod;
    this.methodReturnTypeIsNon = methodReturnTypeIsNon;
    buildFields(psiClass);
  }

  private void buildFields(final PsiClass psiClass) {
    final PsiField[] allFields = psiClass.getAllFields();
    this.fields = new ArrayList<>(allFields.length);
    boolean clzSetter = false;
    if (psiClass.hasAnnotation("lombok.Data")) {
      clzSetter = true;
    }
    if (!clzSetter && psiClass.hasAnnotation("lombok.Setter")) {
      clzSetter = true;
    }
    boolean hasBuilder = false;
    if (psiClass.hasAnnotation("lombok.Builder") || psiClass.hasAnnotation("lombok.experimental.SuperBuilder")) {
      hasBuilder = true;
    }
    for (PsiField psiField : allFields) {
      if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
        continue;
      }
      boolean fieldSetter = clzSetter || hasBuilder || psiField.hasAnnotation("lombok.Setter");
      fieldSetter = fieldSetter || PropertyUtil.findPropertySetter(psiClass, psiField.getName(), false, true) != null;
      this.fields.add(FieldHelper.create(psiField, fieldSetter, null));
    }
  }

  public List<FieldHelper> getFields() {
    return fields;
  }

  public boolean isUseVariable() {
    return useVariable;
  }

  public boolean isUseMethod() {
    return useMethod;
  }

  public boolean isMethodReturnTypeIsNon() {
    return methodReturnTypeIsNon;
  }
}
