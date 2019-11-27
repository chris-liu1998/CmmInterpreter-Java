package com.cmmint.lexical_analyser;

public class Token {
    private String value;
    private int type;
    private int lineNo;
    private int startPos;

    public Token(int type) {
        this.type = type;
    }

    Token(String value, int type, int lineNo) {
        this.value = value;
        this.type = type;
        this.lineNo = lineNo;
    }

    public int getStartPos() {
        return this.startPos;
    }

    void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {   //设置种别码
        this.type = type;
    }

    public String getValue() {   //设置token值
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLineNo() {
        return this.lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String typeToString() {
        switch (this.getType()) {
            case TypeEncoding.INT:
                return "INT";
            case TypeEncoding.INT_VAL:
                return "INT_VALUE";
            case TypeEncoding.REAL:
                return "REAL";
            case TypeEncoding.REAL_VAL:
                return "REAL_VALUE";
            case TypeEncoding.ID:
                return "Identifier";
            case TypeEncoding.END:
                return ";";
            case TypeEncoding.ASSIGN:
                return "=";
            case TypeEncoding.LEFT_P:
                return "(";
            case TypeEncoding.RIGHT_P:
                return ")";
            case TypeEncoding.RIGHT_BRA:
                return "}";
            case TypeEncoding.LEFT_BRA:
                return "{";
            case TypeEncoding.LEFT_BRK:
                return "[";
            case TypeEncoding.RIGHT_BRK:
                return "]";
            case TypeEncoding.WHILE:
                return "WHILE";
            case TypeEncoding.IF:
                return "IF";
            case TypeEncoding.ELSE:
                return "ELSE";
            case TypeEncoding.NEQ:
                return "NOT_EQ";
            case TypeEncoding.EQ:
                return "EQ";
            case TypeEncoding.MOD:
                return "MOD";
            case TypeEncoding.MOD_ASSIGN:
                return "MOD_ASSIGN";
            case TypeEncoding.MUL_ASSIGN:
                return "MUL_ASSIGN";
            case TypeEncoding.DIV_ASSIGN:
                return "DIV_ASSIGN";
            case TypeEncoding.PLUS_ASSIGN:
                return "PLUS_ASSIGN";
            case TypeEncoding.MINUS_ASSIGN:
                return "MINUS_ASSIGN";
            case TypeEncoding.GREATER_EQ:
                return "GREATER_EQ";
            case TypeEncoding.LESS_EQ:
                return "LESS_EQ";
            case TypeEncoding.LESS:
                return "LESS";
            case TypeEncoding.GREATER:
                return "GREATER";
            case TypeEncoding.BREAK:
                return "BREAK";
            case TypeEncoding.CONTINUE:
                return "CONTINUE";
            case TypeEncoding.PLUS:
                return "PLUS";
            case TypeEncoding.MINUS:
                return "MINUS";
            case TypeEncoding.MUL:
                return "MULTIPLY";
            case TypeEncoding.DIV:
                return "DIVIDE";
            case TypeEncoding.AND:
                return "AND";
            case TypeEncoding.OR:
                return "OR";
            case TypeEncoding.NOT:
                return "NOT";
            case TypeEncoding.PLUS_PLUS:
                return "SELF_INC";
            case TypeEncoding.MINUS_MINUS:
                return "SELF_DEC";
            case TypeEncoding.NULL:
                return "NULL";
            case TypeEncoding.ERROR:
                return "ERROR";
            default:
                return "NONE";
        }
    }

    @Override
    public String toString() {
        return ("起始位置：" + this.getStartPos() + "\t<value: '" + this.getValue() + "', " + "type: " + this.typeToString() + " (" + this.getType() + ")" + ", " + "lineNo: " + this.getLineNo() + ">\n");
    }
}
