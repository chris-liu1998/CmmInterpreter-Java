package com.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Font;

/**
 * ZetCode Java SWT tutorial
 * <p>
 * This program creates a simple toolbar.
 * <p>
 * Author: Jan Bodnar
 * Website: zetcode.com
 * Last modified: June 2015
 */


public class Test {
    Color orange, blue, red, green;

    public void run() {
        Display display = new Display();
        Shell shell = new Shell(display);

        orange = new Color(display, 255, 127, 0);
        blue = display.getSystemColor(SWT.COLOR_BLUE);
        red = display.getSystemColor(SWT.COLOR_RED);
        green = display.getSystemColor(SWT.COLOR_GREEN);

        create(shell);
        shell.setSize(430, 100);
        shell.setText("Styled Text");
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private void create(final Shell shell) {
        shell.setLayout(new FillLayout());
        final StyledText styledText = new StyledText(shell, SWT.BORDER);
        Button button = new Button(shell, SWT.PUSH);
        button.setText("1");
        StyleRange[] ranges = new StyleRange[7];
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                //styledText.replaceStyleRanges(0, 48, ranges);
                styledText.setStyleRange(ranges[0]);
            }
        });
        styledText.setText("Java is an Object Oriented Programming Language.");
        Font font = new Font(shell.getDisplay(), "Courier", 10, SWT.NORMAL);
        styledText.setFont(font);


        ranges[0] = new StyleRange(0, 4, orange, null);
        ranges[0].underline = true;
        ranges[0].underlineStyle = 3;
        ranges[0].background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
        ranges[0].fontStyle = SWT.BOLD;

        ranges[1] = new StyleRange(5, 2, blue, null);
        ranges[1].background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);

        ranges[2] = new StyleRange(8, 2, red, null);
        ranges[2].background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);

        ranges[3] = new StyleRange(11, 6, green, null);
        ranges[3].underline = true;
        ranges[3].background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
        ranges[3].fontStyle = SWT.BOLD;

        ranges[4] = new StyleRange(18, 8, orange, null);
        ranges[4].underline = true;
        ranges[4].background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
        ranges[4].fontStyle = SWT.BOLD;

        ranges[5] = new StyleRange(27, 11, blue, null);
        ranges[5].background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);

        ranges[6] = new StyleRange(39, 9, red, null);
        ranges[6].background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);

    }

    public static void main(String[] args) {
        new Test().run();
    }
}

