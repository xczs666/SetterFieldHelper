package com.jenkin.intellij.plugin.data;

import com.intellij.psi.*;

import java.util.List;


public class Context {
  private final PsiFile psiFile;
  private final PsiClass psiClass;
  private final PsiElement elementAtCaret;
  private final PsiLocalVariable variable;
  private final PsiMethod method;
  // 返回类型
  private final PsiClass targetClass;
  // 返回类型的域
  private final Parameter targetFields;
  // 入参
  private final List<Parameter> parameters;

  Context(PsiFile psiFile, PsiClass psiClass, PsiElement elementAtCaret, PsiLocalVariable variable, PsiMethod method, PsiClass targetClass, Parameter targetFields, List<Parameter> parameters) {
    this.psiFile = psiFile;
    this.psiClass = psiClass;
    this.elementAtCaret = elementAtCaret;
    this.variable = variable;
    this.method = method;
    this.targetClass = targetClass;
    this.targetFields = targetFields;
    this.parameters = parameters;
  }

  public static ContextBuilder builder() {
    return new ContextBuilder();
  }

  public PsiFile getPsiFile() {
    return this.psiFile;
  }

  public PsiClass getPsiClass() {
    return this.psiClass;
  }

  public PsiElement getElementAtCaret() {
    return this.elementAtCaret;
  }

  public PsiLocalVariable getVariable() {
    return this.variable;
  }

  public PsiMethod getMethod() {
    return this.method;
  }

  public PsiClass getTargetClass() {
    return this.targetClass;
  }

  public Parameter getTargetFields() {
    return this.targetFields;
  }

  public List<Parameter> getParameters() {
    return this.parameters;
  }

  public static class ContextBuilder {
    private PsiFile psiFile;
    private PsiClass psiClass;
    private PsiElement elementAtCaret;
    private PsiLocalVariable variable;
    private PsiMethod method;
    private PsiClass targetClass;
    private Parameter targetFields;
    private List<Parameter> parameters;

    ContextBuilder() {
    }

    public Context.ContextBuilder psiFile(PsiFile psiFile) {
      this.psiFile = psiFile;
      return this;
    }

    public Context.ContextBuilder psiClass(PsiClass psiClass) {
      this.psiClass = psiClass;
      return this;
    }

    public Context.ContextBuilder elementAtCaret(PsiElement elementAtCaret) {
      this.elementAtCaret = elementAtCaret;
      return this;
    }

    public Context.ContextBuilder variable(PsiLocalVariable variable) {
      this.variable = variable;
      return this;
    }

    public Context.ContextBuilder method(PsiMethod method) {
      this.method = method;
      return this;
    }

    public Context.ContextBuilder targetClass(PsiClass targetClass) {
      this.targetClass = targetClass;
      return this;
    }

    public Context.ContextBuilder targetFields(Parameter targetFields) {
      this.targetFields = targetFields;
      return this;
    }

    public Context.ContextBuilder parameters(List<Parameter> parameters) {
      this.parameters = parameters;
      return this;
    }

    public Context build() {
      return new Context(psiFile, psiClass, elementAtCaret, variable, method, targetClass, targetFields, parameters);
    }

    public String toString() {
      return "Context.ContextBuilder(psiFile=" + this.psiFile + ", psiClass=" + this.psiClass + ", elementAtCaret=" + this.elementAtCaret + ", variable=" + this.variable + ", method=" + this.method + ", targetClass=" + this.targetClass + ", targetFields=" + this.targetFields + ", parameters=" + this.parameters + ")";
    }
  }
}
