

### 工具方法
- 代码插入
```java
Editor.getDocument().insertString(int offset,String str);
```
- 光标移动
```java
Editor.getCaretModel().moveToOffset(int offset);
```
- 编辑器事件传递
```java
EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_ENTER).execute(editor, caret, dataContext);
```
- 代码格式化
```java
/**
 * 封装后的方法，格式化光标所在行代码
 */
public static void formatCurrentLine(Editor editor, DataContext dataContext){
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project!=null){
            PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            editor.getSelectionModel().selectLineAtCaret();
            if (file!=null) {
                ReformatCodeProcessor processor = new ReformatCodeProcessor(file,editor.getSelectionModel());
                processor.runWithoutProgress();
                editor.getSelectionModel().removeSelection();
            }
        }
    }
```
