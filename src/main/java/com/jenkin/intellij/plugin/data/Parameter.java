package com.jenkin.intellij.plugin.data;

import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Parameter {
  private final boolean ths;
  private final String name;
  private final PsiClass psiClass;
  private final List<Field> fields;

  Parameter(boolean ths, String name, PsiClass psiClass, List<Field> fields) {
    this.ths = ths;
    this.name = name;
    this.psiClass = psiClass;
    this.fields = fields;
  }

  public static ParameterBuilder builder() {
    return new ParameterBuilder();
  }

  public boolean isThs() {
    return this.ths;
  }

  public String getName() {
    return this.name;
  }

  public PsiClass getPsiClass() {
    return this.psiClass;
  }

  public List<Field> getFields() {
    return this.fields;
  }

  public static class Field {
    private final String fieldName;
    private String lowerFiledName;
    private final String getMethodName;
    private final String setMethodName;
    private final PsiField field;

    Field(String fieldName, String lowerFiledName, String getMethodName, String setMethodName, PsiField field) {
      this.fieldName = fieldName;
      this.lowerFiledName = lowerFiledName;
      this.getMethodName = getMethodName;
      this.setMethodName = setMethodName;
      this.field = field;
    }

    public static FieldBuilder builder() {
      return new FieldBuilder();
    }

    public String getLowerFiledName() {
      if (lowerFiledName == null) {
        this.lowerFiledName = fieldName.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
      }
      return lowerFiledName;
    }

    public String getFieldName() {
      return this.fieldName;
    }

    public String getGetMethodName() {
      return this.getMethodName;
    }

    public String getSetMethodName() {
      return this.setMethodName;
    }

    public PsiField getField() {
      return this.field;
    }

    public static class FieldBuilder {
      private String fieldName;
      private String lowerFiledName;
      private String getMethodName;
      private String setMethodName;
      private PsiField field;

      FieldBuilder() {
      }

      public Field.FieldBuilder fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
      }

      public Field.FieldBuilder lowerFiledName(String lowerFiledName) {
        this.lowerFiledName = lowerFiledName;
        return this;
      }

      public Field.FieldBuilder getMethodName(String getMethodName) {
        this.getMethodName = getMethodName;
        return this;
      }

      public Field.FieldBuilder setMethodName(String setMethodName) {
        this.setMethodName = setMethodName;
        return this;
      }

      public Field.FieldBuilder field(PsiField field) {
        this.field = field;
        return this;
      }

      public Field build() {
        return new Field(fieldName, lowerFiledName, getMethodName, setMethodName, field);
      }

      public String toString() {
        return "Parameter.Field.FieldBuilder(fieldName=" + this.fieldName + ", lowerFiledName=" + this.lowerFiledName + ", getMethodName=" + this.getMethodName + ", setMethodName=" + this.setMethodName + ", field=" + this.field + ")";
      }
    }
  }


  public static List<Parameter> create(@NotNull final PsiParameter[] psiParameters, PsiClass thisClass) {
    final List<Parameter> list = new ArrayList<>(psiParameters.length + 1);
    for (PsiParameter psiParameter : psiParameters) {
      list.add(create(psiParameter));
    }
    if (thisClass != null) {
      list.add(createThis(thisClass));
    }
    return list;
  }

  public static Parameter createThis(@NotNull final PsiClass psiClass) {
    return create(psiClass, "this", true);
  }

  public static Parameter create(@NotNull final PsiClass psiClass, String name) {
    return create(psiClass, name, false);
  }

  public static Parameter create(@NotNull final PsiClass psiClass, String name, boolean isThis) {
    return Parameter.builder()
      .name(name)
      .ths(isThis)
      .psiClass(psiClass)
      .fields(of(psiClass))
      .build();
  }

  public static Parameter create(@NotNull final PsiParameter in) {
    final PsiClass psiClass = PsiTypesUtil.getPsiClass(in.getType());
    assert psiClass != null;
    return Parameter.builder()
      .name(in.getName())
      .psiClass(psiClass)
      .fields(of(psiClass))
      .build();
  }

  private static List<Field> of(@NotNull final PsiClass psiClass) {
    final PsiField[] allFields = psiClass.getAllFields();
    final List<Field> fields = new ArrayList<>(allFields.length);
    boolean clzSetter = false;
    boolean clzGetter = false;
    if (psiClass.hasAnnotation("lombok.Data")) {
      clzGetter = true;
      clzSetter = true;
    }
    if (!clzGetter && psiClass.hasAnnotation("lombok.Getter")) {
      clzGetter = true;
    }
    if (!clzSetter && psiClass.hasAnnotation("lombok.Setter")) {
      clzSetter = true;
    }
    boolean hasBuilder = false;
    if (psiClass.hasAnnotation("lombok.Builder") || psiClass.hasAnnotation("lombok.experimental.SuperBuilder")) {
      hasBuilder = true;
    }
    for (PsiField psiField : allFields) {
      String setName = clzSetter || psiField.hasAnnotation("lombok.Setter") ? "set" + buildMethodName(psiField.getName()) : null;
      if (setName == null && hasBuilder) {
        setName = psiField.getName();
      }
      if (setName == null) {
        PsiMethod setter = PropertyUtil.findPropertySetter(psiClass, psiField.getName(), psiField.hasModifierProperty(PsiModifier.STATIC), true);
        if (setter != null) {
          setName = setter.getName();
        }
      }
      String getName = clzGetter || psiField.hasAnnotation("lombok.Getter") ? "get" + buildMethodName(psiField.getName()) : null;
      if (getName == null) {
        PsiMethod getter = PropertyUtil.findPropertyGetter(psiClass, psiField.getName(), psiField.hasModifierProperty(PsiModifier.STATIC), true);
        if (getter != null) {
          getName = getter.getName();
        }
      }
      if (null != getName || null != setName) {
        fields.add(Field.builder().field(psiField).fieldName(psiField.getName())
          .setMethodName(setName)
          .getMethodName(getName)
          .build());
      }
    }
    return fields;
  }

  @NotNull
  public static String buildParamName(String fullName) {
    int u = fullName.indexOf("<");
    return extractLowerName(u == -1 ? fullName : fullName.substring(0, u));
  }

  private static String extractLowerName(String fullName) {
    fullName = fullName.substring(fullName.lastIndexOf(".") + 1);
    return fullName.substring(0, 1).toLowerCase() + fullName.substring(1);
  }

  @NotNull
  public static String buildMethodName(String fullName) {
    int u = fullName.indexOf("<");
    return extractUpperName(u == -1 ? fullName : fullName.substring(0, u));
  }

  private static String extractUpperName(String fullName) {
    fullName = fullName.substring(fullName.lastIndexOf(".") + 1);
    return fullName.substring(0, 1).toUpperCase() + fullName.substring(1);
  }

  public static class ParameterBuilder {
    private boolean ths;
    private String name;
    private PsiClass psiClass;
    private List<Field> fields;

    ParameterBuilder() {
    }

    public Parameter.ParameterBuilder ths(boolean ths) {
      this.ths = ths;
      return this;
    }

    public Parameter.ParameterBuilder name(String name) {
      this.name = name;
      return this;
    }

    public Parameter.ParameterBuilder psiClass(PsiClass psiClass) {
      this.psiClass = psiClass;
      return this;
    }

    public Parameter.ParameterBuilder fields(List<Field> fields) {
      this.fields = fields;
      return this;
    }

    public Parameter build() {
      return new Parameter(ths, name, psiClass, fields);
    }

    public String toString() {
      return "Parameter.ParameterBuilder(ths=" + this.ths + ", name=" + this.name + ", psiClass=" + this.psiClass + ", fields=" + this.fields + ")";
    }
  }
}
