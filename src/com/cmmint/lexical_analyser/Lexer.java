package com.cmmint.lexical_analyser;

import java.util.LinkedList;

public class Lexer {
    private static String[] keyWords = {"if", "else", "while", "int", "real", "NULL",
            "char", "for", "break", "continue", "print", "scan"};
    private static LinkedList<Token> words;
    public static StringBuilder errorInfoStrb = new StringBuilder();

    private static boolean isDigit(char dig) {    //判断是否为数字
        return (dig >= '0' && dig <= '9');
    }

    private static boolean isSpaceOrLine(char sp) {  //判断是否为空格、回车、换行等
        return (sp == ' ' || sp == '\t' || sp == '\n' || sp == '\r');
    }

    private static boolean isLetter(char let) {   //判断是否为字母
        return ((let >= 'a' && let <= 'z') || (let >= 'A' && let <= 'Z'));
    }

    private static boolean isKey(String str) {   //判断单词是否为关键字
        for (String key : keyWords) {
            if (key.equals(str))
                return true;
        }
        return false;
    }

    public static String printTokens() {
        StringBuilder strb = new StringBuilder();
        for (Token token : words) {
            strb.append(token.toString());
        }
        return strb.toString();
    }

    public static LinkedList<Token> lexAnalyze(char[] chars) {
        words = new LinkedList<>();  //用来存储分析出来的token
        char oneChar;
        int lineNo = 1;
//        boolean is_pos = false;
//        boolean is_neg = false;
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            int startPos = i;
            word.delete(0, word.length());
            oneChar = chars[i];
            if (isSpaceOrLine(oneChar)) {
                if (oneChar == '\n') {
                    lineNo++; //检测换行符，改变行号
                }
            } else if (isLetter(oneChar) || oneChar == '_') {  // _或字母开头
                while (isLetter(oneChar) || isDigit(oneChar) || oneChar == '_') {  //后面可以有数字
                    try {
                        word.append(oneChar);
                        oneChar = chars[++i];
                    } catch (ArrayIndexOutOfBoundsException e) {  //如果读取的是最后一个字符会有越界异常
                        break;
                    }
                }
                i--; //多进了一位，需要回退
                if (isKey(word.toString())) {
                    switch (word.toString()) {
                        case "int":
                            words.add(new Token(word.toString(), TypeEncoding.INT, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "if":
                            words.add(new Token(word.toString(), TypeEncoding.IF, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "else":
                            words.add(new Token(word.toString(), TypeEncoding.ELSE, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "real":
                            words.add(new Token(word.toString(), TypeEncoding.REAL, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "while":
                            words.add(new Token(word.toString(), TypeEncoding.WHILE, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "NULL":
                            words.add(new Token(word.toString(), TypeEncoding.NULL, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "char":
                            words.add(new Token(word.toString(), TypeEncoding.CHAR, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "for":
                            words.add(new Token(word.toString(), TypeEncoding.FOR, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "break":
                            words.add(new Token(word.toString(), TypeEncoding.BREAK, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "continue":
                            words.add(new Token(word.toString(), TypeEncoding.CONTINUE, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "print":
                            words.add(new Token(word.toString(), TypeEncoding.PRINT, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        case "scan":
                            words.add(new Token(word.toString(), TypeEncoding.SCAN, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        default:
                            break;
                    }
                } //是关键字
                else {  //是普通标识符
                    if (word.length() <= 64) {
                        words.add(new Token(word.toString(), TypeEncoding.ID, lineNo));// 自定义标识符不能超过64个字符
                        words.getLast().setStartPos(startPos);
                    } else {
                        words.add(new Token(word.toString(), TypeEncoding.ERROR, lineNo));
                        words.getLast().setStartPos(startPos);
                        errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 标识符不得超过64个字符. ").append("(").append(word.toString()).append(")\n");
                    }
                }
            } else if (isDigit(oneChar) || (oneChar == '.')) {   //如果是数字
                boolean real_flag = false; //判断是否为REAL类型
                boolean is_error = false;
//                if (is_pos) { //判断是否带有符号
//                    word.append('+');
//                } else if (is_neg) {
//                    word.append('-');
//                }
                while (isDigit(oneChar) || (oneChar == '.')) {
                    if (oneChar == '.') {
                        if (real_flag) {
                            is_error = true;
                        } else {
                            real_flag = true;
                        }
                    }
                    try {
                        word.append(oneChar);
                        oneChar = chars[++i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        break;
                    }
                }
                i--;
//                is_pos = false;
//                is_neg = false;
                if (is_error) {
                    words.add(new Token(word.toString(), TypeEncoding.ERROR, lineNo));
                    words.getLast().setStartPos(startPos);
                    errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 非法数字. ").append("(").append(word.toString()).append(")\n");
                } else {
                    if (real_flag) {
                        words.add(new Token(word.toString(), TypeEncoding.REAL_VAL, lineNo));  //实型
                        words.getLast().setStartPos(startPos);
                    } else {
                        words.add(new Token(word.toString(), TypeEncoding.INT_VAL, lineNo));// 整型
                        words.getLast().setStartPos(startPos);
                    }
                }

            } else {
                switch (oneChar) {  //各种符号
                    case '%':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '=') {   //检测%=
                                words.add(new Token("%=", TypeEncoding.MOD_ASSIGN, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {
                                words.add(new Token("%", TypeEncoding.MOD, lineNo));
                                words.getLast().setStartPos(startPos);
                                i--;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("%", TypeEncoding.MOD, lineNo));
                            words.getLast().setStartPos(startPos);
                        }
                        break;
                    case '+':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '+') {   //检测++
                                words.add(new Token("++", TypeEncoding.PLUS_PLUS, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else if (oneChar == '=') {//检测+=
                                words.add(new Token("+=", TypeEncoding.PLUS_ASSIGN, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {
                                words.add(new Token("+", TypeEncoding.PLUS, lineNo));
                                words.getLast().setStartPos(startPos);
                                //errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 非法字符'+'. ").append("(").append(word.toString()).append(")\n");
                                i--;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("+", TypeEncoding.PLUS, lineNo));
                            words.getLast().setStartPos(startPos);
                        }
                        break;
                    case '-':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '-') {   //检测--
                                words.add(new Token("--", TypeEncoding.MINUS_MINUS, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else if (oneChar == '=') { //检测-=
                                words.add(new Token("-=", TypeEncoding.MINUS_ASSIGN, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {
                                words.add(new Token("-", TypeEncoding.MINUS, lineNo));
                                words.getLast().setStartPos(startPos);
                                //errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 非法字符'-'. ").append("(").append(word.toString()).append(")\n");
                                i--;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("-", TypeEncoding.MINUS, lineNo));
                            words.getLast().setStartPos(startPos);
                        }
                        break;
                    case '*':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '=') {   //检测*=
                                words.add(new Token("*=", TypeEncoding.MUL_ASSIGN, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {
                                words.add(new Token("*", TypeEncoding.MUL, lineNo));
                                words.getLast().setStartPos(startPos);
                                i--;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("*", TypeEncoding.MUL, lineNo));
                            words.getLast().setStartPos(startPos);
                        }
                        break;
                    case '(':
                        words.add(new Token("(", TypeEncoding.LEFT_P, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case ')':
                        words.add(new Token(")", TypeEncoding.RIGHT_P, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case '[':
                        words.add(new Token("[", TypeEncoding.LEFT_BRK, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case ']':
                        words.add(new Token("]", TypeEncoding.RIGHT_BRK, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case '{':
                        words.add(new Token("{", TypeEncoding.LEFT_BRA, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case '!':
                        words.add(new Token("!", TypeEncoding.NOT, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case '}':
                        words.add(new Token("}", TypeEncoding.RIGHT_BRA, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case ';':
                        words.add(new Token(";", TypeEncoding.END, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case ',':
                        words.add(new Token(",", TypeEncoding.COMMA, lineNo));
                        words.getLast().setStartPos(startPos);
                        break;
                    case '&':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '&') {   //检测&&
                                words.add(new Token("&&", TypeEncoding.AND, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {
                                words.add(new Token("&", TypeEncoding.ERROR, lineNo));
                                words.getLast().setStartPos(startPos);
                                errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 非法字符'&'. ").append("(").append(word.toString()).append(")\n");
                                i--;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("&", TypeEncoding.ERROR, lineNo));
                            words.getLast().setStartPos(startPos);
                            errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 非法字符'&'. ").append("(").append(word.toString()).append(")\n");
                        }
                        break;
                    case '|':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '|') {  //检测||
                                words.add(new Token("||", TypeEncoding.OR, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {
                                words.add(new Token("|", TypeEncoding.ERROR, lineNo));
                                words.getLast().setStartPos(startPos);
                                errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 非法字符'|'. ").append("(").append(word.toString()).append(")\n");
                                i--;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("|", TypeEncoding.ERROR, lineNo));
                            words.getLast().setStartPos(startPos);
                        }
                        break;
                    case '<':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '>')   //不等于
                            {
                                words.add(new Token("<>", TypeEncoding.NEQ, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else if (oneChar == '=') { //小于等于
                                words.add(new Token("<=", TypeEncoding.LESS_EQ, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {  //小于
                                words.add(new Token("<", TypeEncoding.LESS, lineNo));
                                words.getLast().setStartPos(startPos);
                                i--;
                            }
                            break;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("<", TypeEncoding.LESS, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        }

                    case '>':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '=') { //大于等于
                                words.add(new Token(">=", TypeEncoding.GREATER_EQ, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {   //大于
                                words.add(new Token(">", TypeEncoding.GREATER, lineNo));
                                words.getLast().setStartPos(startPos);
                                i--;
                            }
                            break;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token(">", TypeEncoding.GREATER, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        }
                    case '=':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '=') {  //等于
                                words.add(new Token("==", TypeEncoding.EQ, lineNo));
                                words.getLast().setStartPos(startPos);
                            } else {  //赋值
                                words.add(new Token("=", TypeEncoding.ASSIGN, lineNo));
                                words.getLast().setStartPos(startPos);
                                i--;
                            }
                            break;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("=", TypeEncoding.ASSIGN, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        }
                    case '/':
                        try {
                            oneChar = chars[++i];
                            if (oneChar == '/') {  //行级注释
                                while (oneChar != '\n') {
                                    try {
                                        oneChar = chars[++i];
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        break;
                                    }
                                }
                                lineNo++;  //检测到换行符，行号加1

                            } else if (oneChar == '*') { //块级注释
                                try {
                                    oneChar = chars[++i];
                                    while (!((oneChar == '*') && (chars[++i] == '/'))) {
                                        if (oneChar == '\n') {  //检测换行符
                                            lineNo++;
                                        }
                                        oneChar = chars[++i];
                                    }
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 块级注释未闭合.\n");
                                    break;
                                }

                            } else if (oneChar == '=') {
                                words.add(new Token("/=", TypeEncoding.DIV_ASSIGN, lineNo)); //检测/*
                                words.getLast().setStartPos(startPos);
                            } else {  //除法
                                words.add(new Token("/", TypeEncoding.DIV, lineNo));
                                words.getLast().setStartPos(startPos);
                                i--;
                            }
                            break;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            words.add(new Token("/", TypeEncoding.DIV, lineNo));
                            words.getLast().setStartPos(startPos);
                            break;
                        }
                    default:
                        words.add(new Token(word.append(oneChar).toString(), TypeEncoding.ERROR, lineNo));
                        words.getLast().setStartPos(startPos);
                        errorInfoStrb.append("ERROR : Line: ").append(lineNo).append(" 非法字符'").append(word.toString()).append("'. ").append("(").append(word.toString()).append(")\n");
                        break;
                }
            }
        }
        return words;
    }
}
