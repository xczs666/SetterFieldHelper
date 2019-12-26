package com.jenkin.intellij.plugin.data;

import com.intellij.psi.*;


public class Context {
  private final PsiFile psiFile;
  private final PsiClass psiClass;
  private final PsiElement elementAtCaret;
  private final PsiElement psiParent;
  private final PsiLocalVariable variable;
  private final PsiMethod psiMethod;
  private final PsiClass targetClass;

  Context(PsiFile psiFile, PsiClass psiClass, PsiElement elementAtCaret, PsiElement psiParent, PsiLocalVariable variable, PsiMethod psiMethod, PsiClass targetClass) {
    this.psiFile = psiFile;
    this.psiClass = psiClass;
    this.elementAtCaret = elementAtCaret;
    this.psiParent = psiParent;
    this.variable = variable;
    this.psiMethod = psiMethod;
    this.targetClass = targetClass;
  }

  public static ContextBuilder builder() {
    return new ContextBuilder();
  }

  public static class ContextBuilder {
    private PsiFile psiFile;
    private PsiClass psiClass;
    private PsiElement elementAtCaret;
    private PsiElement psiParent;
    private PsiLocalVariable variable;
    private PsiMethod psiMethod;
    private PsiClass targetClass;

    ContextBuilder() {
    }

    public ContextBuilder psiFile(PsiFile psiFile) {
      this.psiFile = psiFile;
      return this;
    }

    public ContextBuilder psiClass(PsiClass psiClass) {
      this.psiClass = psiClass;
      return this;
    }

    public ContextBuilder elementAtCaret(PsiElement elementAtCaret) {
      this.elementAtCaret = elementAtCaret;
      return this;
    }

    public ContextBuilder psiParent(PsiElement psiParent) {
      this.psiParent = psiParent;
      return this;
    }

    public ContextBuilder variable(PsiLocalVariable variable) {
      this.variable = variable;
      return this;
    }

    public ContextBuilder psiMethod(PsiMethod psiMethod) {
      this.psiMethod = psiMethod;
      return this;
    }

    public ContextBuilder targetClass(PsiClass targetClass) {
      this.targetClass = targetClass;
      return this;
    }

    public Context build() {
      return new Context(psiFile, psiClass, elementAtCaret, psiParent, variable, psiMethod, targetClass);
    }

    public String toString() {
      return "Context.ContextBuilder(psiFile=" + this.psiFile + ", psiClass=" + this.psiClass + ", elementAtCaret=" + this.elementAtCaret + ", psiParent=" + this.psiParent + ", variable=" + this.variable + ", psiMethod=" + this.psiMethod + ", targetClass=" + this.targetClass + ")";
    }
  }
}
