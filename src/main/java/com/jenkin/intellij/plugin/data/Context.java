package com.jenkin.intellij.plugin.data;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.jenkin.intellij.plugin.support.ParameterClass;
import com.jenkin.intellij.plugin.support.TargetClass;
import com.jenkin.intellij.plugin.support.ThisClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Context {
  private final ThisClass thisClass;
  private final PsiElement elementAtCaret;
  private final PsiMethod method;
  // 返回类型
  private final TargetClass targetClass;
  // 入参
  private final List<ParameterClass> parameters;

  Context(ThisClass thisClass, PsiElement elementAtCaret, PsiMethod method, TargetClass targetClass, List<ParameterClass> parameters) {
    this.thisClass = thisClass;
    this.elementAtCaret = elementAtCaret;
    this.method = method;
    this.targetClass = targetClass;
    this.parameters = parameters;
  }

  public static ContextBuilder builder() {
    return new ContextBuilder();
  }

  public ThisClass getThisClass() {
    return this.thisClass;
  }

  public PsiElement getElementAtCaret() {
    return this.elementAtCaret;
  }

  public PsiMethod getMethod() {
    return this.method;
  }

  public TargetClass getTargetClass() {
    return this.targetClass;
  }

  public List<ParameterClass> getParameters() {
    return this.parameters;
  }

  public static class ContextBuilder {
    private ThisClass thisClass;
    private PsiElement elementAtCaret;
    private PsiMethod method;
    private TargetClass targetClass;
    private ArrayList<ParameterClass> parameters;

    ContextBuilder() {
    }

    public Context.ContextBuilder thisClass(ThisClass thisClass) {
      this.thisClass = thisClass;
      return this;
    }

    public Context.ContextBuilder elementAtCaret(PsiElement elementAtCaret) {
      this.elementAtCaret = elementAtCaret;
      return this;
    }

    public Context.ContextBuilder method(PsiMethod method) {
      this.method = method;
      return this;
    }

    public Context.ContextBuilder targetClass(TargetClass targetClass) {
      this.targetClass = targetClass;
      return this;
    }

    public Context.ContextBuilder parameter(ParameterClass parameter) {
      if (this.parameters == null) this.parameters = new ArrayList<ParameterClass>();
      this.parameters.add(parameter);
      return this;
    }

    public Context.ContextBuilder parameters(Collection<? extends ParameterClass> parameters) {
      if (this.parameters == null) this.parameters = new ArrayList<ParameterClass>();
      this.parameters.addAll(parameters);
      return this;
    }

    public Context.ContextBuilder clearParameters() {
      if (this.parameters != null)
        this.parameters.clear();
      return this;
    }

    public Context build() {
      List<ParameterClass> parameters;
      switch (this.parameters == null ? 0 : this.parameters.size()) {
        case 0:
          parameters = java.util.Collections.emptyList();
          break;
        case 1:
          parameters = java.util.Collections.singletonList(this.parameters.get(0));
          break;
        default:
          parameters = java.util.Collections.unmodifiableList(new ArrayList<ParameterClass>(this.parameters));
      }

      return new Context(thisClass, elementAtCaret, method, targetClass, parameters);
    }

    public String toString() {
      return "Context.ContextBuilder(thisClass=" + this.thisClass + ", elementAtCaret=" + this.elementAtCaret + ", method=" + this.method + ", targetClass=" + this.targetClass + ", parameters=" + this.parameters + ")";
    }
  }
}
