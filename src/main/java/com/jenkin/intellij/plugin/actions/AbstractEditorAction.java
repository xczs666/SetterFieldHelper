package com.jenkin.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.jenkin.intellij.plugin.data.Context;
import com.jenkin.intellij.plugin.handler.AbstractEditorWriteActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
      &&  StdFileTypes.JAVA.equals(psiFile.getFileType())) {
      final PsiClass targetClass = getTargetClass(editor, psiFile);
      if (null != targetClass) {
        final PsiElement elementAtCaret = PsiUtilBase.getElementAtCaret(editor);
        final PsiElement psiParent = PsiTreeUtil.getParentOfType(elementAtCaret,
          PsiLocalVariable.class, PsiMethod.class);
        final Context.ContextBuilder builder = Context.builder()
        .elementAtCaret(elementAtCaret)
        .psiClass(targetClass)
        .psiFile(psiFile)
        .psiParent(psiParent);
        if (psiParent instanceof PsiLocalVariable) {
         builder.variable((PsiLocalVariable) psiParent)
         .targetClass(PsiTypesUtil.getPsiClass(((PsiLocalVariable) psiParent).getType()));

        }else if (psiParent instanceof PsiMethod) {
          builder.psiMethod((PsiMethod) psiParent)
          .targetClass(PsiTypesUtil.getPsiClass(((PsiMethod) psiParent).getReturnType()));
        }

      }
    }
    return new Pair<>(false, null);
  }

  protected void executeMyWriteAction(Editor editor, final DataContext dataContext, final Context additionalParam) {
    editor.getCaretModel().runForEachCaret(caret -> executeMyWriteActionPerCaret(caret.getEditor(), caret, dataContext, additionalParam));
  }



  protected void executeMyWriteActionPerCaret(Editor editor, Caret caret, DataContext dataContext, Context additionalParam) {


    final SelectionModel selectionModel = editor.getSelectionModel();
    String selectedText = selectionModel.getSelectedText();

    if (selectedText == null) {
      selectSomethingUnderCaret(editor, dataContext, selectionModel);
      selectedText = selectionModel.getSelectedText();

      if (selectedText == null) {
        return;
      }
    }

    String s = transformSelection(editor, dataContext, selectedText, additionalParam);
    s = s.replace("\r\n", "\n");
    s = s.replace("\r", "\n");
    editor.getDocument().replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), s);
  }

  protected String transformSelection(Editor editor, DataContext dataContext, String selectedText, Context additionalParam) {
    String[] textParts = selectedText.split("\n");


    return "aaaaa";
  }

  protected boolean selectSomethingUnderCaret(Editor editor, DataContext dataContext, SelectionModel selectionModel) {
    selectionModel.selectLineAtCaret();
    String selectedText = selectionModel.getSelectedText();
    if (selectedText != null && selectedText.endsWith("\n")) {
      selectionModel.setSelection(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd() - 1);
    }
    return true;
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
