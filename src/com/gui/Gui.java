package com.gui;

import com.Exception.ParserException;
import com.cmmint.LexicalAnalyser.Lexer;
import com.cmmint.SyntacticParser.Parser;
import com.cmmint.LexicalAnalyser.Token;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

import java.io.*;
import java.util.LinkedList;


public class Gui {
    private static boolean isSaved = false;
    private static String code_file = null;
    private static String file_name = null;
    private static int flag = 0;
    //private static boolean isParseError = false;
    private static Token currentToken = null;
    private static String[] exfilter = {"*.cmm", "*.txt", "*.*"};

    private static String getFileName(String code_file, String file_name) {
        if (code_file == null)
            file_name = "未命名";
        return file_name;
    }

    private static void saveFile(String[] exfilter, Shell shell, StyledText code_area) {
        if (code_file == null) { //当要保存未保存过的代码
            FileDialog fd = new FileDialog(shell, SWT.SAVE);
            fd.setText("保存");
            fd.setFilterExtensions(exfilter);
            fd.setOverwrite(true);
            code_file = fd.open();
            file_name = fd.getFileName();
        }
        if (code_file != null) {
            File file = new File(code_file);
            try {
                if (!file.exists()) {   //防止意外删除
                    file.createNewFile();
                }
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
                bw.write(code_area.getText());
                bw.close();
                isSaved = true;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        file_name = getFileName(code_file, file_name);
    }

    private static void createMenu(Shell shell, StyledText code_area, JavaLineStyler lineStyler, StyledText result_area) {
        final Menu main_menu = new Menu(shell, SWT.BAR);

        shell.setMenuBar(main_menu);
        {
            MenuItem file_item = new MenuItem(main_menu, SWT.CASCADE);
            file_item.setText("文件&(F)");
            Menu file_menu = new Menu(shell, SWT.DROP_DOWN);
            file_item.setMenu(file_menu);
            {
                MenuItem new_file_item = new MenuItem(file_menu, SWT.CASCADE);
                new_file_item.setText("新建&(N)");
                Menu new_file_menu = new Menu(shell, SWT.DROP_DOWN);
                new_file_item.setMenu(new_file_menu);

                {
                    MenuItem new_codeFile_item = new MenuItem(new_file_menu, SWT.PUSH);
                    new_codeFile_item.setText("文件...&(Ctrl+Shift+N)");
                    new_codeFile_item.setAccelerator(SWT.CTRL + SWT.SHIFT + 'N');
                    new_codeFile_item.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {    //新建
                            MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                            //设置对话框的标题
                            box.setText("新建文件");
                            //设置对话框显示的消息
                            if (!isSaved) {
                                box.setMessage("当前文件未保存，是否保存？");
                                int choice = box.open();
                                if (choice == SWT.YES) {
                                    saveFile(exfilter, shell, code_area);
                                    code_area.setText("");
                                } else if (choice == SWT.NO)
                                    code_area.setText("");
                            } else
                                code_area.setText("");
                            code_file = null;
                            file_name = getFileName(code_file, file_name);
                            shell.setText("CMM解释器—" + file_name + "*");
                        }
                    });
                }

                //打开
                MenuItem open_file_item = new MenuItem(file_menu, SWT.CASCADE);
                open_file_item.setText("打开...&(O)");
                open_file_item.setAccelerator(SWT.CTRL + 'O');
                open_file_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        FileDialog open_file = new FileDialog(shell, SWT.OPEN);
                        open_file.setText("打开");
                        open_file.setFilterExtensions(exfilter);
                        String last_file_name = file_name;
                        code_file = open_file.open();
                        file_name = open_file.getFileName();
                        if (code_file != null) {
                            try {
                                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(code_file), "UTF-8"));
                                StringBuilder strb = new StringBuilder();
                                String content;
                                while ((content = br.readLine()) != null) {
                                    strb.append(content);
                                    strb.append(System.getProperty("line.separator"));
                                }
                                br.close();
                                Display display = code_area.getDisplay();
                                display.asyncExec(() -> {
                                            code_area.setText(strb.toString());
                                            isSaved = true;
                                        }
                                );
                                lineStyler.parseBlockComments(strb.toString());
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }

                        }
                        file_name = getFileName(code_file, file_name);
                        if (code_file != null)
                            isSaved = true;
                        else
                            file_name = last_file_name;  //防止关闭“打开”窗口时文件名发生更改

                        shell.setText("CMM解释器—" + file_name);
                    }
                });

                //保存
                MenuItem save_file_item = new MenuItem(file_menu, SWT.CASCADE);
                save_file_item.setText("保存...&(S)");
                save_file_item.setAccelerator(SWT.CTRL + 'S');
                save_file_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        saveFile(exfilter, shell, code_area);
                        shell.setText("CMM解释器—" + file_name);
                    }
                });

                MenuItem saveAs_file_item = new MenuItem(file_menu, SWT.CASCADE);
                saveAs_file_item.setText("另存为...&(Ctrl+Shift+S)");
                saveAs_file_item.setAccelerator(SWT.CTRL + SWT.SHIFT + 'S');
                saveAs_file_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        FileDialog fd = new FileDialog(shell, SWT.SAVE);
                        fd.setText("另存为");
                        fd.setOverwrite(true);
                        fd.setFilterExtensions(exfilter);
                        String filename = fd.open();
                        if (filename != null) {
                            File file = new File(filename);
                            try {
                                if (!file.exists()) {
                                    file.createNewFile();
                                }
                                FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
                                BufferedWriter bw = new BufferedWriter(fw);
                                bw.write(code_area.getText());
                                bw.close();
                                fw.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });

                //退出
                MenuItem exit_item = new MenuItem(file_menu, SWT.CASCADE);
                exit_item.setText("退出&(E)");
                exit_item.setAccelerator(SWT.CTRL + 'E');
                exit_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (!isSaved) {
                            MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                            box.setText("退出");
                            box.setMessage("文件尚未保存，是否退出？");
                            int choice = box.open();
                            if (choice == SWT.YES) {
                                if (!shell.isDisposed())
                                    shell.dispose();
                            }
                        }
                    }
                });

            }
            MenuItem edit_item = new MenuItem(main_menu, SWT.CASCADE);
            edit_item.setText("编辑&(E)");
            Menu edit_menu = new Menu(shell, SWT.DROP_DOWN);
            edit_item.setMenu(edit_menu);
            {
                MenuItem select_all_item = new MenuItem(edit_menu, SWT.CASCADE);
                select_all_item.setText("全选&(A)");
                select_all_item.setAccelerator(SWT.CTRL + 'A');
                select_all_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (code_area.isFocusControl())
                            code_area.selectAll();
                        if (result_area.isFocusControl())
                            result_area.selectAll();
                    }
                });
                MenuItem copy_item = new MenuItem(edit_menu, SWT.CASCADE);
                copy_item.setText("复制&(C)");
                copy_item.setAccelerator(SWT.CTRL + 'C');
                copy_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (code_area.isFocusControl())
                            code_area.copy();
                        if (result_area.isFocusControl())
                            result_area.copy();
                    }
                });
                MenuItem cut_item = new MenuItem(edit_menu, SWT.CASCADE);
                cut_item.setText("剪切&(X)");
                cut_item.setAccelerator(SWT.CTRL + 'X');
                cut_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        code_area.cut();
                    }
                });
                MenuItem paste_item = new MenuItem(edit_menu, SWT.CASCADE);
                paste_item.setText("粘贴&(V)");
                paste_item.setAccelerator(SWT.CTRL + 'V');
                paste_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        code_area.paste();
                    }
                });
                MenuItem find_item = new MenuItem(edit_menu, SWT.CASCADE);
                find_item.setText("查找...&(F)");
                find_item.setAccelerator(SWT.CTRL + 'F');
                find_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
//                        FindWindow fw = new FindWindow(shell,SWT.DIALOG_TRIM);
//                        fw.open();
                    }
                });
                MenuItem find_next_item = new MenuItem(edit_menu, SWT.CASCADE);
                find_next_item.setText("查找下一个...&(FN)");
                find_next_item.setAccelerator(SWT.CTRL + SWT.SHIFT + 'F');
                find_next_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
//                        FindWindow fw = new FindWindow(shell,SWT.DIALOG_TRIM);
//                        fw.open();
                    }
                });
                MenuItem replace_item = new MenuItem(edit_menu, SWT.CASCADE);
                replace_item.setText("替换...&(R)");
                replace_item.setAccelerator(SWT.CTRL + SWT.SHIFT + 'R');
                replace_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
//                        FindWindow fw = new FindWindow(shell,SWT.DIALOG_TRIM);
//                        fw.open();
                    }
                });
            }
            MenuItem help_item = new MenuItem(main_menu, SWT.CASCADE);
            help_item.setText("帮助&(H)");
            Menu help_menu = new Menu(shell, SWT.DROP_DOWN);
            help_item.setMenu(help_menu);
            {
                MenuItem help_info_item = new MenuItem(help_menu, SWT.CASCADE);
                help_info_item.setText("查看帮助...&(I)");
                help_info_item.setAccelerator(SWT.CTRL + 'H');
                help_info_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                    }
                });
                MenuItem about_item = new MenuItem(help_menu, SWT.CASCADE);
                about_item.setText("关于...&(A)");
                about_item.setAccelerator(SWT.CTRL + SWT.SHIFT + 'A');
                about_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        MessageBox box = new MessageBox(shell);
                        box.setText("关于");
                        box.setMessage("Copyright © 2019 lxl");
                        box.open();
                    }
                });

            }
        }
    }


    private static StyledText createCodeArea(Shell shell, JavaLineStyler lineStyler) {

        final StyledText code_text = new StyledText(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        code_text.addLineStyleListener((event) -> {   //显示行号
            StyleRange styleRange = new StyleRange();
            styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
            int maxLine = code_text.getLineCount();
            int bulletLength = Integer.toString(maxLine).length();
            // Width of number character is half the height in monospaced font, add 1 character width for right padding.
            int bulletWidth = (bulletLength + 1) * code_text.getLineHeight() / 2;
            styleRange.metrics = new GlyphMetrics(0, 0, bulletWidth);
            event.bullet = new Bullet(ST.BULLET_TEXT, styleRange);
            // getLineAtOffset() returns a zero-based line index.
            int bulletLine = code_text.getLineAtOffset(event.lineOffset) + 1;
            event.bullet.text = String.format("%" + bulletLength + "s", bulletLine);
        });
        code_text.addLineStyleListener(lineStyler);
        code_text.addModifyListener((e) -> { //更新行号
            // For line number redrawing.
            lineStyler.parseBlockComments(code_text.getText());
            //isParseError = false;
            currentToken = null;
            code_text.redraw();
            isSaved = false;
            shell.setText("CMM解释器—" + file_name + "*");


        });
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 30);
        formData.left = new FormAttachment(0, 0);
        formData.bottom = new FormAttachment(70, -1);
        formData.right = new FormAttachment(100, 0);
        code_text.setLayoutData(formData);
        code_text.setAlwaysShowScrollBars(false);
        return code_text;
    }

    private static StyledText createResultArea(Shell shell, StyledText code_area) {   //创建显示结果的区域
        final StyledText result_text = new StyledText(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_FOCUS);
        FormData formData = new FormData();
        formData.bottom = new FormAttachment(100, -10);
        formData.top = new FormAttachment(code_area, 5);
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        result_text.setLayoutData(formData);
        result_text.setEditable(false);
        result_text.setBackground(new Color(Display.getCurrent(), 254, 251, 234));
        result_text.setAlwaysShowScrollBars(false);
        return result_text;
    }

    private static void createButtons(Shell shell, StyledText code_area, int flag, Display display, StyledText
            result_area) {

        final ToolBar toolBar = new ToolBar(shell, SWT.HORIZONTAL);
        Color color = display.getSystemColor(SWT.COLOR_RED);

        FormData formData = new FormData();
        formData.bottom = new FormAttachment(code_area, 0);
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        toolBar.setLayoutData(formData);
        final Combo combo = new Combo(toolBar, SWT.NONE | SWT.NO_FOCUS | SWT.READ_ONLY);
        combo.setBounds(90, 0, 110, 15);
        combo.add("lex");
        combo.add("parser");
        combo.add("interpreter");
        combo.select(0);


        Button buttonSave = new Button(toolBar, SWT.NONE);
        buttonSave.setImage(new Image(display, "res" + File.separator + "Save.png"));
        buttonSave.setToolTipText("保存");
        Button buttonRun = new Button(toolBar, SWT.NONE);
        buttonRun.setImage(new Image(display, "res" + File.separator + "Play.png"));
        buttonRun.setToolTipText("运行");
        Button buttonStop = new Button(toolBar, SWT.NONE);
        buttonStop.setImage(new Image(display, "res" + File.separator + "Stop.png"));
        buttonStop.setToolTipText("停止");
        buttonRun.setLocation(205, 0);
        buttonSave.setLocation(0, 0);
        buttonStop.setLocation(35, 0);

        buttonRun.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.setFocus();
                Lexer.errorInfoStrb = new StringBuilder();
                if (combo.getSelectionIndex() == 0) {   //运行词法分析器
                    Lexer.lexAnalyze(code_area.getText().toCharArray());
                    result_area.setText(Lexer.printTokens() + "\n" + Lexer.errorInfoStrb.toString());
                } else if (combo.getSelectionIndex() == 1) {  //运行语法分析器
                    StringBuilder error = new StringBuilder();
                    try {
                        LinkedList<Token> tokens = Lexer.lexAnalyze(code_area.getText().toCharArray());
                        String result = Parser.printTree(Parser.syntaxAnalyse(tokens));
                        result_area.setText(result + "\n" + Lexer.errorInfoStrb.toString());
                    } catch (ParserException e1) {  //打印错误
                        //isParseError = true;
                        currentToken = Parser.getCurrentToken();
                        int start = currentToken.getStartPos();
                        int length = currentToken.getValue().length();
//                        code_area.addLineStyleListener((event) -> {
//                                    StyleRange styleRange = new StyleRange(start, length, color, null);
//                                    styleRange.underline = true;
//                                    styleRange.underlineStyle = SWT.COLOR_RED;
//                                    ArrayList<StyleRange> ranges = new ArrayList<>();
//                                    ranges.add(styleRange);
//                                    event.styles = ranges.toArray(new StyleRange[ranges.size()]);
//                                }
//                        );

                        // code_area.setStyleRange(styleRange);
                        error.append(e1.getMessage() + "\n");
                        result_area.setText(error.toString() + " (" + currentToken.getValue() + ")");
                    }
                } else {

                }

            }
        });
        buttonSave.addSelectionListener(new

                                                SelectionAdapter() {
                                                    @Override
                                                    public void widgetSelected(SelectionEvent e) {
                                                        shell.setFocus();
                                                        saveFile(exfilter, shell, code_area);
                                                        shell.setText("CMM解释器—" + file_name);
                                                    }
                                                });
        buttonStop.addSelectionListener(new

                                                SelectionAdapter() {
                                                    @Override
                                                    public void widgetSelected(SelectionEvent e) {
                                                        shell.setFocus();
                                                    }
                                                });
        buttonRun.pack();
        buttonSave.pack();
        buttonStop.pack();
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        Image image = new Image(shell.getDisplay(), "res" + File.separator + "C--.png");
        shell.setImage(image);
        JavaLineStyler lineStyler = new JavaLineStyler();
        //ArrayList<StyledText> code_areas = new ArrayList<>();
        FormLayout formLayout = new FormLayout();
        file_name = getFileName(code_file, file_name);

        formLayout.marginLeft = 5;
        formLayout.marginTop = 0;
        formLayout.marginRight = 5;
        formLayout.marginBottom = 10;


        shell.setLayout(formLayout);
        shell.setText("CMM解释器—" + file_name + "*");
        Color color = new Color(Display.getCurrent(), 240, 237, 220);
        shell.setBackground(color);
        StyledText code_area = createCodeArea(shell, lineStyler);
        StyledText result_area = createResultArea(shell, code_area);
        createMenu(shell, code_area, lineStyler, result_area);
        createButtons(shell, code_area, flag, display, result_area);

        shell.setBounds(600, 100, 600, 900);
        shell.open();
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                lineStyler.disposeColors();
                code_area.removeLineStyleListener(lineStyler);
            }
        });
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
