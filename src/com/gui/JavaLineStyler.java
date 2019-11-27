package com.gui;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

class JavaLineStyler implements LineStyleListener {
    private JavaScanner scanner = new JavaScanner();
    private int[] tokenColors;
    //    private boolean isError = false;
//    private boolean isReal = false;
    private Color[] colors;
    private Vector<int[]> blockComments = new Vector<>();
    private StringBuilder strb;
    private static final int EOF = -1;
    private static final int EOL = 10;

    private static final int WORD = 0;
    private static final int WHITE = 1;
    private static final int KEY = 2;
    private static final int COMMENT = 3;
    private static final int STRING = 5;
    private static final int OTHER = 6;
    private static final int NUMBER = 7;
    private static final int ERROR = 8;

    private static final int MAXIMUM_TOKEN = 9;

    JavaLineStyler() {
        initializeColors();
        scanner = new JavaScanner();
    }

//    public void setCurrentToken(Token token){
//        this.currentToken = token;
//    }

    private Color getColor(int type) {
        if (type < 0 || type >= tokenColors.length) {
            return null;
        }
        return colors[tokenColors[type]];
    }

    private boolean inBlockComment(int start, int end) {
        for (int i = 0; i < blockComments.size(); i++) {
            int[] offsets = blockComments.elementAt(i);
            // start of comment in the line
            if ((offsets[0] >= start) && (offsets[0] <= end)) return true;
            // end of comment in the line
            if ((offsets[1] >= start) && (offsets[1] <= end)) return true;
            if ((offsets[0] <= start) && (offsets[1] >= end)) return true;
        }
        return false;
    }

    private void initializeColors() {
        Display display = Display.getDefault();
        colors = new Color[]{
                new Color(display, new RGB(0, 0, 0)),        // black
                new Color(display, new RGB(255, 0, 0)),    // red
                new Color(display, new RGB(0, 255, 0)),    // green
                new Color(display, new RGB(43, 125, 159)),    // blue
                new Color(display, new RGB(127, 0, 85)),    //reserved keyword
                new Color(display, new RGB(231, 165, 99)),  //id
                new Color(display, new RGB(209, 210, 215))    //comment
        };
        tokenColors = new int[MAXIMUM_TOKEN];
        tokenColors[WORD] = 5;
        tokenColors[WHITE] = 0;
        tokenColors[KEY] = 4;
        tokenColors[COMMENT] = 6;
        tokenColors[STRING] = 2;
        tokenColors[OTHER] = 0;
        tokenColors[NUMBER] = 3;
        tokenColors[ERROR] = 1;
    }

    void disposeColors() {
        for (Color color : colors) {
            color.dispose();
        }
    }

    /**
     * Event.detail            line start offset (input)
     * Event.text             line text (input)
     * LineStyleEvent.styles     Enumeration of StyleRanges, need to be in order. (output)
     * LineStyleEvent.background     line background color (output)
     */
    public void lineGetStyle(LineStyleEvent event) {
        Vector<StyleRange> styles = new Vector<>();
        int token;
        StyleRange lastStyle;
        // If the line is part of a block comment, create one style for the entire line.
        if (inBlockComment(event.lineOffset, event.lineOffset + event.lineText.length())) {
            styles.addElement(new StyleRange(event.lineOffset, event.lineText.length(), getColor(COMMENT), null));
            event.styles = new StyleRange[styles.size()];
            styles.copyInto(event.styles);
            return;
        }
        Color defaultFgColor = ((Control) event.widget).getForeground();
        scanner.setRange(event.lineText);
        token = scanner.nextToken();
        while (token != EOF) {
            if (token == OTHER) {
                // do nothing for non-colored tokens
            } else if (token != WHITE) {
                Color color = getColor(token);
                // Only create a style if the token color is different than the
                // widget's default foreground color and the token's style is not
                // bold.  Keywords are bolded.
                if (color != null && ((!color.equals(defaultFgColor)) || (token == KEY))) {
                    StyleRange style = new StyleRange(scanner.getStartOffset() + event.lineOffset, scanner.getLength(), color, null);
                    if (token == KEY) {
                        style.fontStyle = SWT.BOLD;
                    }
                    if (styles.isEmpty()) {
                        styles.addElement(style);
                    } else {
                        // Merge similar styles.  Doing so will improve performance.
                        lastStyle = styles.lastElement();
                        if (lastStyle.similarTo(style) && (lastStyle.start + lastStyle.length == style.start)) {
                            lastStyle.length += style.length;
                        } else {
                            styles.addElement(style);
                        }
                    }
                }
            } else if ((!styles.isEmpty()) && (styles.lastElement().fontStyle == SWT.BOLD)) {
                int start = scanner.getStartOffset() + event.lineOffset;
                lastStyle = styles.lastElement();
                // A font style of SWT.BOLD implies that the last style
                // represents a java keyword.
                if (lastStyle.start + lastStyle.length == start) {
                    // Have the white space take on the style before it to
                    // minimize the number of style ranges created and the
                    // number of font style changes during rendering.
                    lastStyle.length += scanner.getLength();
                }
            }
            token = scanner.nextToken();
        }
        event.styles = new StyleRange[styles.size()];
        styles.copyInto(event.styles);
    }

    void parseBlockComments(String text) {
        blockComments = new Vector<>();
        StringReader buffer = new StringReader(text);
        int ch;
        boolean blkComment = false;
        int cnt = 0;
        int[] offsets = new int[2];
        boolean done = false;

        try {
            while (!done) {
                switch (buffer.read()) {
                    case -1: {
                        if (blkComment) {
                            offsets[1] = cnt;
                            blockComments.addElement(offsets);
                        }
                        done = true;
                        break;
                    }
                    case '/': {
                        ch = buffer.read();
                        if ((ch == '*') && (!blkComment)) {
                            offsets = new int[2];
                            offsets[0] = cnt;
                            blkComment = true;
                            cnt++;
                        } else {
                            cnt++;
                        }
                        cnt++;
                        break;
                    }
                    case '*': {
                        if (blkComment) {
                            ch = buffer.read();
                            cnt++;
                            if (ch == '/') {
                                blkComment = false;
                                offsets[1] = cnt;
                                blockComments.addElement(offsets);
                            }
                        }
                        cnt++;
                        break;
                    }
                    default: {
                        cnt++;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            // ignore errors
        }
    }

    /**
     * A simple fuzzy scanner for Java
     */
    public class JavaScanner {

        Hashtable<String, Integer> fgKeys = null;
        StringBuffer fBuffer = new StringBuffer();
        String fDoc;
        int fPos;
        int fEnd;
        int fStartToken;

        private String[] fgKeywords = {
                "abstract",
                "bool", "break", "byte",
                "case", "catch", "char", "class", "continue",
                "default", "do", "double",
                "else", "extends",
                "false", "final", "finally", "float", "for",
                "if", "implements", "import", "instanceof", "int", "interface",
                "long",
                "native", "new", "NULL",
                "package", "private", "protected", "public", "print",
                "return",
                "short", "static", "super", "switch", "synchronized", "scan",
                "this", "throw", "throws", "transient", "true", "try",
                "void", "volatile",
                "while",
                "write", "read", "real"
        };

        JavaScanner() {
            initialize();
        }

        /**
         * Returns the ending location of the current token in the document.
         */
        final int getLength() {
            return fPos - fStartToken;
        }

        /**
         * Initialize the lookup table.
         */
        void initialize() {
            fgKeys = new Hashtable<>();
            Integer k = KEY;
            for (String word : fgKeywords)
                fgKeys.put(word, k);
        }

        /**
         * Returns the starting location of the current token in the document.
         */
        final int getStartOffset() {
            return fStartToken;
        }

        /**
         * Returns the next lexical token in the document.
         */
        int nextToken() {
            int c;
            boolean isError = false;
            boolean isReal = false;
            strb = new StringBuilder();
            fStartToken = fPos;
            while (true) {
                switch (c = read()) {
                    case EOF:
                        return EOF;
                    case '/':    // comment
                        c = read();
                        if (c == '/') {
                            while (true) {
                                c = read();
                                if ((c == EOF) || (c == EOL)) {
                                    unread(c);
                                    return COMMENT;
                                }
                            }
                        }
                        unread(c);
                        return OTHER;
                    case '\'':    // char const
                        while (true) {
                            c = read();
                            switch (c) {
                                case '\'':
                                    return STRING;
                                case EOF:
                                    unread(c);
                                    return STRING;
                                case '\\':
                                    c = read();
                                    if (c == '\'') {
                                        isError = true;
                                    }
                                    if (isError)
                                        return ERROR;
                                    break;
                            }
                        }

                    case '"':    // string
                        while (true) {
                            c = read();
                            switch (c) {
                                case '"':
                                    return STRING;
                                case EOF:
                                    unread(c);
                                    return STRING;
                                case '\\':
                                    c = read();
                                    if (c == '"') {
                                        isError = true;
                                    }
                                    if (isError)
                                        return ERROR;
                                    break;
                            }
                        }
                    case '。':
                    case '#':
                    case '【':
                    case '】':
                    case '《':
                    case '》':
                    case '’':
                    case '‘':
                    case '“':
                    case '”':
                    case '：':
                    case '；':
                    case '，':
                    case '？':
                    case '！':
                    case '、':
                    case '·':
                    case '（':
                    case '）':
                    case '…':
                    case '@':
                        return ERROR;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '.':
                        if (c == '.') {
                            isReal = true;
                        }
                        do {
                            c = read();
                            if (c == '.')
                                if (!isReal)
                                    isReal = true;
                                else
                                    isError = true;

                        } while (Character.isDigit((char) c) || c == '.');
                        unread(c);
                        if (isError)
                            return ERROR;
                        return NUMBER;
                    default:
                        if (Character.isWhitespace((char) c)) {
                            do {
                                c = read();
                            } while (Character.isWhitespace((char) c));
                            unread(c);
                            return WHITE;
                        }
                        if (Character.isJavaIdentifierStart((char) c)) {
                            fBuffer.setLength(0);
                            do {
                                fBuffer.append((char) c);
                                c = read();
                                if (fBuffer.length() > 64) {
                                    isError = true;
                                }
                            } while (Character.isJavaIdentifierPart((char) c));
                            unread(c);
                            Integer i = fgKeys.get(fBuffer.toString());
                            if (i != null)
                                return i;
                            if (isError)
                                return ERROR;
                            return WORD;
                        }
                        return OTHER;
                }

            }

        }

        /**
         * Returns next character.
         */
        int read() {
            if (fPos <= fEnd) {
                char ch = fDoc.charAt(fPos++);
                strb.append(ch);

                return ch;
                //return fDoc.charAt(fPos++);
            }
            return EOF;
        }

        void setRange(String text) {
            fDoc = text;
            fPos = 0;
            fEnd = fDoc.length() - 1;
        }

        void unread(int c) {
            if (c != EOF) {
                fPos--;
                strb.deleteCharAt(strb.length() - 1);
            }
        }
    }


}
