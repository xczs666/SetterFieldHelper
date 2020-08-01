package com.jenkin.intellij.plugin.actions;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.jenkin.intellij.plugin.data.Context;
import com.jenkin.intellij.plugin.handler.ExpressionBuilder;
import com.jenkin.intellij.plugin.support.ParameterClass;
import com.jenkin.intellij.plugin.support.TargetClass;
import com.jenkin.intellij.plugin.support.ThisClass;
import com.jenkin.intellij.plugin.utils.NameUtil;
import org.bouncycastle.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class SetFieldsAction extends AbstractEditorAction<Context> {

  @NotNull
  public Pair<Boolean, Context> beforeWriteAction(Editor editor, DataContext dataContext) {
    if (editor == null || editor.getProject() == null) {
      return new Pair<>(false, null);
    }

    final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());
    if (null != psiFile
      && psiFile.isWritable()
      && !psiFile.isDirectory()
      && JavaFileType.INSTANCE.equals(psiFile.getFileType())) {
      final PsiClass thisPsiClass = getTargetClass(editor, psiFile);
      PsiElement elementAtCaret = PsiUtilBase.getElementAtCaret(editor);
      PsiLocalVariable variable = PsiTreeUtil.getParentOfType(elementAtCaret, PsiLocalVariable.class);
      if (variable == null) {
        editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - 1);
        elementAtCaret = PsiUtilBase.getElementAtCaret(editor);
        variable = PsiTreeUtil.getParentOfType(elementAtCaret, PsiLocalVariable.class);
      }
      final PsiMethod method = PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod.class);
      if (thisPsiClass != null && method != null) {
        final Context.ContextBuilder builder = Context.builder()
          .elementAtCaret(elementAtCaret)
          .thisClass(ThisClass.create(thisPsiClass))
          .method(method);

        if (variable != null) {
          final PsiClass targetClass = PsiTypesUtil.getPsiClass(variable.getType());
          builder.targetClass(TargetClass.createUseVariable(targetClass, variable.getName()));
        } else {
          final PsiClass targetClass = PsiTypesUtil.getPsiClass(method.getReturnType());
          if (targetClass == null) {
            builder.targetClass(TargetClass.createUseMethod(thisPsiClass, "this", true));
          } else {
            builder.targetClass(TargetClass.createUseMethod(targetClass, NameUtil.variableName(targetClass.getName()), false));
          }
        }
        Context context = builder.parameters(ParameterClass.create(method.getParameterList().getParameters())).build();
        if (!context.getThisClass().isSame(context.getTargetClass())) {
          context = builder.parameter(ParameterClass.create(context.getThisClass().getPsiClass(), "this")).build();
        }
        return new Pair<>(true, context);
      }
    }
    return new Pair<>(false, null);
  }

  protected void executeMyWriteActionPerCaret(Editor editor, Caret caret, DataContext dataContext, Context context) {
//    context.getTargetClass().getAnnotation()
    String expression = ExpressionBuilder.of(context.getTargetClass(), context.getParameters()).buildExpression();

    int offset = editor.getCaretModel().getOffset();
    if (context.getTargetClass().isUseVariable()) {// 已經有 Object obj
      CharSequence charSequence = editor.getDocument().getImmutableCharSequence();
      char[] breakChar = new char[]{' ', '\t', '\n', '=', '}', '\r'};
      for (; offset < charSequence.length(); offset++) {
        char c = charSequence.charAt(offset);
        if (Arrays.contains(breakChar, c)) {
          break;
        }
      }
    }

    editor.getDocument().insertString(offset, expression);

    PsiDocumentManager.getInstance(editor.getProject()).commitDocument(editor.getDocument());
    CodeStyleManager.getInstance(editor.getProject()).reformatRange(context.getMethod(), offset, offset + expression.length());
//    final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());
//    JavaCodeStyleManager.getInstance(editor.getProject()).optimizeImports(psiFile);
//    UndoUtil.markPsiFileForUndo(psiFile);
  }
}
