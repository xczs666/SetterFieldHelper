package com.jenkin.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.*;
import com.jenkin.intellij.plugin.data.Context;
import com.jenkin.intellij.plugin.data.Parameter;
import com.jenkin.intellij.plugin.handler.AbstractEditorWriteActionHandler;
import com.jenkin.intellij.plugin.support.Formatter;
import com.jenkin.intellij.plugin.support.Strategy;
import com.jenkin.intellij.plugin.utils.PsiAnnotationSearchUtil;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;

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
          .method(method);

        if (variable != null) {
          final PsiClass targetClass = PsiTypesUtil.getPsiClass(variable.getType());
          assert targetClass != null;
          builder.targetClass(targetClass)
            .targetFields(Parameter.create(targetClass, variable.getName()))
            .parameters(Parameter.create(method.getParameterList().getParameters(), myClass));
        } else if (method != null) {
          if (method.isConstructor()) {
            builder.targetClass(myClass)
              .targetFields(Parameter.createThis(myClass))
              .parameters(Parameter.create(method.getParameterList().getParameters(), null));
            ;
          } else {
            final PsiClass targetClass = PsiTypesUtil.getPsiClass(method.getReturnType());
            builder.targetClass(targetClass)
              .targetFields(targetClass != null ? Parameter.create(targetClass, Parameter.buildParamName(targetClass.getName())) : Parameter.createThis(myClass))
              .parameters(Parameter.create(method.getParameterList().getParameters(), null));
            ;
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
    final Strategy strategy;
//    context.getTargetClass().getAnnotation()
    final Parameter targetFields = context.getTargetFields();
    if (!targetFields.isThs() && context.getTargetClass() != null
      && (context.getTargetClass().hasAnnotation("lombok.Builder")
      || context.getTargetClass().hasAnnotation("lombok.experimental.SuperBuilder"))) {
      strategy = Strategy.BUILDER;
    } else {
      strategy = Strategy.NORMAL;
    }

    final StringBuilder data = new StringBuilder();
    int offset = editor.getCaretModel().getOffset();
    // 需要new
    if (context.getVariable() == null && !context.getTargetFields().isThs()) {
      data.append(targetFields.getPsiClass().getName() + " " + targetFields.getName() + " = ");
    } else if (context.getVariable() != null && !context.getMethod().isConstructor()) {// 已經有 Object obj
      CharSequence charSequence = editor.getDocument().getImmutableCharSequence();
      char[] breakChar = new char[]{' ', '\t', '\n', '=', '}', '\r'};
      for (; offset < charSequence.length(); offset++) {
        char c = charSequence.charAt(offset);
        if (Arrays.contains(breakChar, c)) {
          break;
        }
      }
      // 已存在Object obj
      data.append(" = ");
    }
    final Formatter formatter;
    if (targetFields.isThs()) {
      formatter = strategy.getOutFieldArgMethod();
    } else if (context.getMethod().hasParameters()) {
      formatter = strategy.getOutMethodArgMethod();
    } else {
      formatter = strategy.getOutMethodArgField();
    }

    data.append(formatter.getPrefix().replaceAll(Formatter.RET_CLASS, targetFields.getPsiClass().getName()));

    for (Parameter.Field targetField : targetFields.getFields()) {
      Parameter tag = null;
      Parameter.Field tagField = null;
      String fn = targetField.getFieldName();
      for (Parameter parameter : context.getParameters()) {
        for (Parameter.Field field : parameter.getFields()) {
          if (fn.equals(field.getFieldName())) {
            tagField = field;
            break;
          }
        }
        if (tagField != null) {
          tag = parameter;
          break;
        }
      }
      if (tagField == null) {
        fn = fn.replaceAll("[^A-Za-z0-9]", "");
        for (Parameter parameter : context.getParameters()) {
          for (Parameter.Field field : parameter.getFields()) {
            if (fn.equals(field.getLowerFiledName())) {
              tagField = field;
              break;
            }
          }
          if (tagField != null) {
            tag = parameter;
            break;
          }
        }
      }
      if (tagField == null) {
        data.append(formatter.getEmptyLine().replaceAll(Formatter.RET, targetFields.getName())
          .replaceAll(Formatter.RET_FIELD_NAME, targetField.getFieldName())
          .replaceAll(Formatter.RET_SET_METHOD_NAME, targetField.getSetMethodName())
          .replaceAll(Formatter.RET_GET_METHOD_NAME, targetField.getGetMethodName()));
      } else {
        data.append(formatter.getLine().replaceAll(Formatter.RET, targetFields.getName())
          .replaceAll(Formatter.RET_FIELD_NAME, targetField.getFieldName())
          .replaceAll(Formatter.RET_SET_METHOD_NAME, targetField.getSetMethodName())
          .replaceAll(Formatter.RET_GET_METHOD_NAME, targetField.getGetMethodName())
          .replaceAll(Formatter.ARG, tag.getName())
          .replaceAll(Formatter.ARG_FIELD_NAME, tagField.getFieldName())
          .replaceAll(Formatter.ARG_GET_METHOD_NAME, tagField.getGetMethodName())
          .replaceAll(Formatter.ARG_SET_METHOD_NAME, tagField.getSetMethodName()));
      }
    }
    data.append(formatter.getSuffix());


    editor.getDocument().insertString(offset, data.toString());
//    editor.getDocument().insertString(context.getMethod().getBody().getTextOffset() + 1, data.toString());

    PsiDocumentManager.getInstance(editor.getProject()).commitDocument(editor.getDocument());
    CodeStyleManager.getInstance(editor.getProject()).reformatRange(context.getMethod(), offset, offset + data.length());
//    reformatMethod.getBody().getText();
//    final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());
//    JavaCodeStyleManager.getInstance(editor.getProject()).optimizeImports(psiFile);
//    UndoUtil.markPsiFileForUndo(psiFile);
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

  @NotNull
  private static String extractSplitText(PsiMethod method,
                                         Document document) {
    int startOffset = method.getTextRange().getStartOffset();
    int lastLine = startOffset - 1;
    String text = document.getText(new TextRange(lastLine, lastLine + 1));
    boolean isTable = false;
    while (!text.equals("\n")) {
      if (text.equals('\t')) {
        isTable = true;
      }
      lastLine--;
      text = document.getText(new TextRange(lastLine, lastLine + 1));
    }
    lastLine++;// skip \n
    String methodStartToLastLineText = document
      .getText(new TextRange(lastLine, startOffset));
    String splitText = null;
    if (isTable) {
      splitText += methodStartToLastLineText + "\t";
    } else {
      splitText = methodStartToLastLineText + "    ";
    }
    return splitText;
  }
}
