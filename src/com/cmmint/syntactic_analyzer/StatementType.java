package com.cmmint.syntactic_analyzer;

public class StatementType {
    public static final int NONE = 0;
    public static final int IF_ST = 1;
    public static final int ELSE_ST = 2;
    public static final int WHILE_ST = 3;
    public static final int SCAN_ST = 4;
    public static final int PRINT_ST = 5;
    public static final int DEC_ST = 6;  // 声明语句
    public static final int ASSIGN_ST = 7;// 赋值语句

    public static final int VAR = 8; //变量
    public static final int EXP = 9;  //表达式
    public static final int FACTOR = 10; //因子
    public static final int STBLOCK = 11; // 语句块
    public static final int OPR = 12; //操作符
    public static final int NULL = 13; //NULL
    public static final int VALUE = 14;
    public static final int MORE_FACTOR = 15;

    public static final int BREAK = 16;
    public static final int CONTINUE = 17;
    public static final int MORE_LOGIC_EXP = 18;
    public static final int MORE_ADD_EXP = 19;
    public static final int INIT = 20;// 初始化语句
    public static final int VALUE_LIST = 21;
    public static final int MORE_VALUE = 22;
    public static final int MORE_TERM = 23;
    public static final int TERM = 24;
    public static final int JUMP_ST = 25;
    public static final int PROGRAM = 26;
    public static final int STMT_SEQ = 27;
    public static final int IF_BLOCK = 28;
    public static final int VAR_LIST = 29;
    public static final int MORE_VAR = 30;
    public static final int ARRAY_DIM = 31;
    public static final int MORE_COMP_EXP = 32;
    public static final int ID = 33;
}
