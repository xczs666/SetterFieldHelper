package com.jenkin.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.util.PsiTreeUtil;
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
      this.setupHandler(new AbstractEditorWriteActionHandler<T>(getClass()) {
        @NotNull
        protected Pair<Boolean, T> beforeWriteAction(Editor editor, DataContext dataContext) {
          return AbstractEditorAction.this.beforeWriteAction(editor, dataContext);
        }

        protected void executeWriteAction(Editor editor, @Nullable Caret caret, final DataContext dataContext, final T additionalParam) {
          executeMyWriteAction(editor, dataContext, additionalParam);
        }

      });
    }
    this.setupHandler = setupHandler;
  }

  @NotNull
  protected abstract Pair<Boolean, T> beforeWriteAction(Editor editor, DataContext dataContext);

  protected void executeMyWriteAction(Editor editor, final DataContext dataContext, final T additionalParam) {
    editor.getCaretModel().runForEachCaret(caret -> executeMyWriteActionPerCaret(caret.getEditor(), caret, dataContext, additionalParam));
  }


  protected abstract void executeMyWriteActionPerCaret(Editor editor, Caret caret, DataContext dataContext, T context);

  @Nullable
  protected PsiClass getTargetClass(Editor editor, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    if (element == null) {
      return null;
    }
    final PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
    return target instanceof SyntheticElement ? null : target;
  }
}
