package com.cmmint.LexicalAnalyser;

public class Token {
    private String value;
    private int type;
    private int lineNo;

    public Token(int type) {
        this.type = type;
    }

    public Token(String value, int type, int lineNo) {
        this.value = value;
        this.type = type;
        this.lineNo = lineNo;
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
            case TypeEncoding.INTVAL:
                return "INT_VALUE";
            case TypeEncoding.REAL:
                return "REAL";
            case TypeEncoding.REALVAL:
                return "REAL_VALUE";
            case TypeEncoding.ID:
                return "标识符";
            case TypeEncoding.END:
                return ";";
            case TypeEncoding.ASSIGN:
                return "=";
            case TypeEncoding.LEFTP:
                return "(";
            case TypeEncoding.RIGHTP:
                return ")";
            case TypeEncoding.RIGHTBRA:
                return "}";
            case TypeEncoding.LEFTBRA:
                return "{";
            case TypeEncoding.LEFTBRK:
                return "[";
            case TypeEncoding.RIGHTBRK:
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
            case TypeEncoding.GREATEREQ:
                return "GREATER_EQ";
            case TypeEncoding.LESSEQ:
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
            case TypeEncoding.NULL:
                return "NULL";
            default:
                return "NONE";
        }
    }

    @Override
    public String toString() {
        return ("<value: '" + this.getValue() + "', " + "type: " + this.typeToString() + " (" + this.getType() + ")" +  ", " + "lineNo: " + this.getLineNo() + ">\n");
    }
}
