package com.jenkin.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;

public class SetFieldsAction extends AbstractEditorAction {

  public SetFieldsAction() {
    new Object();
  }

  public SetFieldsAction(boolean setupHandler) {
    super(setupHandler);
  }
}
