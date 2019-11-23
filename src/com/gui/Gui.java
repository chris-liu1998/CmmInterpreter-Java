package com.gui;

import com.Exception.ParserException;
import com.cmmint.LexicalAnalyser.Lexer;
import com.cmmint.SyntacticParser.Parser;
import com.cmmint.LexicalAnalyser.Token;
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
    //private static JavaLineStyler lineStyler = new JavaLineStyler();
    private static int flag = 0;
    private static boolean isParseError = false;
    protected static Token currentToken = null;

    private static void createMenu(Shell shell, StyledText code_area, JavaLineStyler lineStyler) {
        final Menu main_menu = new Menu(shell, SWT.BAR);
        final String[] exfilter = {"*.cmm", "*.txt", "*.*"};
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
                    MenuItem new_proj_item = new MenuItem(new_file_menu, SWT.PUSH);
                    new_proj_item.setText("项目...&(Ctrl+Shfit+N)");
                    new_proj_item.setAccelerator(SWT.CTRL + SWT.SHIFT + 'N');
                    new_proj_item.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {    //测试菜单功能

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
                        code_file = open_file.open();
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
                                display.asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        code_area.setText(strb.toString());
                                        isSaved = true;
                                    }
                                });
                                lineStyler.parseBlockComments(strb.toString());
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }

                        }

                    }
                });

                //保存
                MenuItem save_file_item = new MenuItem(file_menu, SWT.CASCADE);
                save_file_item.setText("保存...&(S)");
                save_file_item.setAccelerator(SWT.CTRL + 'S');
                save_file_item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (code_file == null) { //当要保存未保存过的代码
                            FileDialog fd = new FileDialog(shell, SWT.SAVE);
                            fd.setText("保存");
                            fd.setFilterExtensions(exfilter);
                            code_file = fd.open();
                        } else {
                            File file = new File(code_file);
                            try {
                                if (!file.exists()) {   //防止意外删除
                                    file.createNewFile();
                                }
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                                bw.write(code_area.getText());
                                bw.close();
                                isSaved = true;
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
                        if (!shell.isDisposed())
                            shell.dispose();
                    }
                });

            }
            MenuItem edit_item = new MenuItem(main_menu, SWT.CASCADE);
            edit_item.setText("编辑&(E)");
            Menu edit_menu = new Menu(shell, SWT.DROP_DOWN);
            edit_item.setMenu(edit_menu);
            {

            }

        }
    }

    private static StyledText createCodeArea(Shell shell, JavaLineStyler lineStyler) {

        final StyledText code_text = new StyledText(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        code_text.addLineStyleListener(new LineStyleListener() {   //显示行号
            @Override
            public void lineGetStyle(LineStyleEvent event) {
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
            }
        });
        code_text.addLineStyleListener(lineStyler);
        code_text.addModifyListener(new ModifyListener() {   //更新行号
            @Override
            public void modifyText(ModifyEvent e) {
                // For line number redrawing.
                isParseError = false;
                currentToken = null;
                code_text.redraw();
                isSaved = false;
                lineStyler.parseBlockComments(code_text.getText());
            }
        });
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                lineStyler.disposeColors();
                code_text.removeLineStyleListener(lineStyler);
            }
        });
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 30);
        formData.left = new FormAttachment(0, 0);
        formData.bottom = new FormAttachment(70, -1);
        formData.right = new FormAttachment(100, 0);
        //code_text.setBounds(0,10,100,100);
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

    private static void createButtons(Shell shell, StyledText code_area, int flag, Display display, StyledText result_area) {

        //Display display = new Display();
        final ToolBar toolBar = new ToolBar(shell, SWT.HORIZONTAL);
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
            //boolean parse_error = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.setFocus();
                Lexer.errorInfoStrb = new StringBuilder();
                //result_area.setText(code_area.getText());
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
//                        //JavaLineStyler lineStyler = new JavaLineStyler();
                        isParseError = true;
                        currentToken = Parser.getCurrentToken();
//
//                        //code_area.addLineStyleListener(lineStyler);
//                        //lineStyler.setCurrentToken(currentToken);
                        error.append(e1.getMessage() + "\n");
                        result_area.setText(error.toString() + " (" + currentToken.getValue() + ")");
                    }
                } else {

                }

            }
        });
        buttonSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.setFocus();
            }
        });
        buttonStop.addSelectionListener(new SelectionAdapter() {
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
        JavaLineStyler lineStyler = new JavaLineStyler();
        FormLayout formLayout = new FormLayout();
        formLayout.marginLeft = 5;
        formLayout.marginTop = 0;
        formLayout.marginRight = 5;
        formLayout.marginBottom = 10;

        shell.setLayout(formLayout);
        shell.setText("CMMInterpreter");
        Color color = new Color(Display.getCurrent(), 240, 237, 220);
        shell.setBackground(color);
        StyledText code_area = createCodeArea(shell, lineStyler);

        StyledText result_area = createResultArea(shell, code_area);
        createMenu(shell, code_area, lineStyler);
        createButtons(shell, code_area, flag, display, result_area);
        shell.setBounds(600, 100, 600, 900);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
