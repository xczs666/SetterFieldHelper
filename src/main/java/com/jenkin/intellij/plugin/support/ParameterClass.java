package com.jenkin.intellij.plugin.support;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ParameterClass extends AbstractHelperClass {
  @NotNull
  private Map<String, FieldHelper> fieldNameMap;
  @NotNull
  private Map<String, List<FieldHelper>> fuzzyNameMap;

  public static List<ParameterClass> create(PsiParameter[] psiParameters) {
    return Arrays.stream(psiParameters).map(ParameterClass::new).collect(Collectors.toList());
  }

  public static ParameterClass create(PsiParameter psiParameter) {
    return new ParameterClass(psiParameter);
  }

  public static ParameterClass create(PsiClass psiClass, String varName) {
    return new ParameterClass(psiClass, varName);
  }

  public FieldHelper findField(String varName) {
    return fieldNameMap.get(varName);
  }

  public List<FieldHelper> findFieldByFuzzyName(String varName) {
    return fuzzyNameMap.get(varName);
  }

  private ParameterClass(PsiParameter psiParameter) {
    super(psiParameter);
    buildFields(PsiTypesUtil.getPsiClass(psiParameter.getType()));
  }

  private ParameterClass(PsiClass psiClass, String varName) {
    super(psiClass, varName);
    buildFields(psiClass);
  }

  private void buildFields(@Nullable final PsiClass psiClass) {
    if (psiClass == null) {
      fieldNameMap = Collections.emptyMap();
      fuzzyNameMap = Collections.emptyMap();
      return;
    }
    final PsiField[] allFields = psiClass.getAllFields();
    List<FieldHelper> fields = new ArrayList<>(allFields.length);
    boolean clzGetter = false;
    if (psiClass.hasAnnotation("lombok.Data")) {
      clzGetter = true;
    }
    if (!clzGetter && psiClass.hasAnnotation("lombok.Getter")) {
      clzGetter = true;
    }

    for (PsiField psiField : allFields) {
      boolean fieldGetter = clzGetter || psiField.hasAnnotation("lombok.Getter");
      fieldGetter = fieldGetter || PropertyUtil.findPropertyGetter(psiClass, psiField.getName(), psiField.hasModifierProperty(PsiModifier.STATIC), true) != null;
      fields.add(FieldHelper.create(psiField, null, fieldGetter));
    }
    fieldNameMap = new HashMap<>(fields.size());
    fields.forEach(o -> fieldNameMap.put(o.getFieldName(), o));
    fuzzyNameMap = fields.stream().collect(Collectors.groupingBy(FieldHelper::getFuzzyName));
  }

  public Map<String, FieldHelper> getFieldNameMap() {
    return fieldNameMap;
  }

  public Map<String, List<FieldHelper>> getFuzzyNameMap() {
    return fuzzyNameMap;
  }
}
