package com.cmmint.LexicalAnalyser;

public class TypeEncoding {
    public static final int ERROR = -1;
    public static final int NULL = 0;
    public static final int INT = 1;
    public static final int REAL = 2;
    public static final int IF = 3;
    public static final int ELSE = 4;
    public static final int WHILE = 5;
    public static final int PLUS = 6;  //+
    public static final int MINUS = 7; //-
    public static final int DIV = 8; // /
    public static final int MUL = 9; //*
    public static final int ASSIGN = 10; //=
    public static final int EQ = 11; //==
    public static final int LEFTP = 12;  //(
    public static final int RIGHTP = 13; //)
    public static final int LESS = 14; //<
    public static final int GREATER = 15;  //>
    public static final int NEQ = 16; //<>
    public static final int END = 17; //;
    public static final int LCOMMENT = 18; // //
    public static final int BCOMMENT = 19; // /* */
    public static final int LEFTBRK = 20; // [
    public static final int RIGHTBRK = 21; // ]
    public static final int LEFTBRA = 23; // {
    public static final int RIGHTBRA = 24; // }
    public static final int ID = 25; // 标识符
    public static final int INTVAL = 26; // 整型值
    public static final int REALVAL = 27; // 浮点型值
    public static final int LESSEQ = 28; // <=
    public static final int GREATEREQ = 29; // >=
    public static final int AND = 30; // &&
    public static final int OR = 31; // ||
    public static final int CHAR = 32; // char
    public static final int FOR = 33; // for
    public static final int BREAK = 34; // break
    public static final int CONTINUE = 35; // continue
    public static final int PRINT = 36; // continue
    public static final int SCAN = 37; // continue
    public static final int COMMA = 38; // ,
    public static final int NOT = 39; //!
}
