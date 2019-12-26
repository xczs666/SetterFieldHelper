package com.jenkin.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.*;
import com.jenkin.intellij.plugin.data.Context;
import com.jenkin.intellij.plugin.data.Parameter;
import com.jenkin.intellij.plugin.handler.AbstractEditorWriteActionHandler;
import com.jenkin.intellij.plugin.support.Strategy;
import com.jenkin.intellij.plugin.utils.PsiAnnotationSearchUtil;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public abstract class AbstractEditorAction<T> extends EditorAction {
  protected final boolean setupHandler;

  protected AbstractEditorAction() {
    this(true);
  }

  private AbstractEditorAction(EditorActionHandler defaultHandler) {
    super(null);
    this.setupHandler = true;
  }

  protected AbstractEditorAction(boolean setupHandler) {
    super(null);
    if (setupHandler) {
      this.setupHandler(new AbstractEditorWriteActionHandler<Context>(getClass()) {
        @NotNull
        protected Pair<Boolean, Context> beforeWriteAction(Editor editor, DataContext dataContext) {
          return AbstractEditorAction.this.beforeWriteAction(editor, dataContext);
        }

        protected void executeWriteAction(Editor editor, @Nullable Caret caret, final DataContext dataContext, final Context additionalParam) {
          executeMyWriteAction(editor, dataContext, additionalParam);
        }

      });
    }
    this.setupHandler = setupHandler;
  }

  protected Class getActionClass() {
    return getClass();
  }

  @NotNull
  public Pair<Boolean, Context> beforeWriteAction(Editor editor, DataContext dataContext) {
    if (editor == null || editor.getProject() == null) {
      return new Pair<>(false, null);
    }

    final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());
    if (null != psiFile
      && psiFile.isWritable()
      && !psiFile.isDirectory()
      && StdFileTypes.JAVA.equals(psiFile.getFileType())) {
      final PsiClass myClass = getTargetClass(editor, psiFile);
      final PsiElement elementAtCaret = PsiUtilBase.getElementAtCaret(editor);
      final PsiLocalVariable variable = PsiTreeUtil.getParentOfType(elementAtCaret, PsiLocalVariable.class);
      final PsiMethod method = PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod.class);
      if (myClass != null && method != null) {
        final Context.ContextBuilder builder = Context.builder()
          .elementAtCaret(elementAtCaret)
          .psiClass(myClass)
          .psiFile(psiFile)
          .variable(variable)
          .method(method)
          .parameters(Parameter.create(method.getParameterList().getParameters(), myClass));
        if (variable != null) {
          final PsiClass targetClass = PsiTypesUtil.getPsiClass(variable.getType());
          assert targetClass != null;
          builder.targetClass(targetClass)
            .targetFields(Parameter.create(targetClass, variable.getName()));
        } else if (method != null) {
          if (method.isConstructor()) {
            builder.targetClass(myClass)
              .targetFields(Parameter.createThis(myClass));
          } else {
            final PsiClass targetClass = PsiTypesUtil.getPsiClass(method.getReturnType());
            builder.targetClass(targetClass)
              .targetFields(targetClass != null ? Parameter.create(targetClass, Parameter.buildParamName(targetClass.getName())) : Parameter.createThis(myClass));
          }
        }
        return new Pair<>(true, builder.build());
      }
    }
    return new Pair<>(false, null);
  }

  protected void executeMyWriteAction(Editor editor, final DataContext dataContext, final Context additionalParam) {
    editor.getCaretModel().runForEachCaret(caret -> executeMyWriteActionPerCaret(caret.getEditor(), caret, dataContext, additionalParam));
  }


  protected void executeMyWriteActionPerCaret(Editor editor, Caret caret, DataContext dataContext, Context context) {
    Strategy strategy;


//    context.getTargetClass().getAnnotation()
    if (!context.getTargetFields().isThs() && context.getTargetClass() != null
      && (context.getTargetClass().hasAnnotation("lombok.Builder")
      || context.getTargetClass().hasAnnotation("lombok.experimental.SuperBuilder"))) {
      strategy = Strategy.BUILDER;
    } else {
      strategy = Strategy.NORMAL;
    }
    StringBuilder data = new StringBuilder();
    if (context.getVariable() == null) {
      data.append(context.getTargetFields().getPsiClass().getName() + " " + context.getTargetFields().getName() + "\n");
    } else {
      data.append(context.getTargetFields().getName() + "\n");
    }
//    editor.getDocument().replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), s);
    editor.getDocument().insertString(context.getMethod().getBody().getTextOffset() + 1, data.toString());
    final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());
    JavaCodeStyleManager.getInstance(editor.getProject()).optimizeImports(psiFile);
    UndoUtil.markPsiFileForUndo(psiFile);
  }

  @Nullable
  private PsiClass getTargetClass(Editor editor, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    if (element == null) {
      return null;
    }
    final PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
    return target instanceof SyntheticElement ? null : target;
  }

}
