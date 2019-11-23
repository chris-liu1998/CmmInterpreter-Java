package com.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.security.PublicKey;

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

    public static int n = 5;
    public static void print(String space, String s) {
        System.out.println(space + s);
        if(n > 0){
            n--;
            print(" " + space, s);
        }else
            return;

    }

    public static void main(String[] args) {
//        char[] ch = {','};
//        if (ch[1] == )
        String space = "";
        String s = "123";
        print(space, s);

    }
}
