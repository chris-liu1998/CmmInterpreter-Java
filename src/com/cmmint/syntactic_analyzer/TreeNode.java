package com.cmmint.syntactic_analyzer;

import com.cmmint.lexical_analyser.Token;

import java.util.ArrayList;

public class TreeNode {
    private int type;
    private int dataType = 100;
    private String value;
    private TreeNode leftN;
    private TreeNode rightN;
    private TreeNode middleN;
    private TreeNode nextStmtN;
    public ArrayList<TreeNode> children = new ArrayList<>(0);


    public TreeNode(int type) {
        this.type = type;
    }

    public int getDataType() {
        return this.dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public TreeNode getLeftN() {
        return this.leftN;
    }

    public void setLeftN(TreeNode leftN) {
        this.leftN = leftN;
    }

    public TreeNode getRightN() {
        return this.rightN;
    }

    public void setRightN(TreeNode rightN) {
        this.rightN = rightN;
    }

    public TreeNode getMiddleN() {
        return this.middleN;
    }

    public void setMiddleN(TreeNode middleN) {
        this.middleN = middleN;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TreeNode getNextStmtN() {
        return this.nextStmtN;
    }

    public void setNextStmtN(TreeNode nextStmtN) {
        this.nextStmtN = nextStmtN;
    }

    public String typeToString() {
        switch (this.getType()) {
            case StatementType.IF_ST:
                return "IF_STATEMENT";
            case StatementType.ELSE_ST:
                return "ELSE_STATEMENT";
            case StatementType.WHILE_ST:
                return "WHILE_STATEMENT";
            case StatementType.PRINT_ST:
                return "PRINT_STATEMENT";
            case StatementType.SCAN_ST:
                return "SCAN_STATEMENT";
            case StatementType.INIT:
                return "INITIAL_STATEMENT";
            case StatementType.DEC_ST:
                return "DEC_STATEMENT";
            case StatementType.STBLOCK:
                return "STATEMENT_BLOCK";
            case StatementType.ASSIGN_ST:
                return "ASSIGN_STATEMENT";
            case StatementType.JUMP_ST:
                return "JUMP_STATEMENT";
            case StatementType.VALUE_LIST:
                return "VALUE_LIST";
            case StatementType.VAR_LIST:
                return "VARIABLE_LIST";
            case StatementType.ID:
                return "IDENTIFIER";
            case StatementType.MORE_VALUE:
                return "MORE_VALUE";
            case StatementType.EXP:
                return "EXP";
            case StatementType.BREAK:
                return "BREAK";
            case StatementType.CONTINUE:
                return "CONTINUE";
            case StatementType.OPR:
                return "OPERATOR";
            case StatementType.VAR:
                return "VAR";
            case StatementType.FACTOR:
                return "FACTOR";
            case StatementType.MORE_FACTOR:
                return "MORE_FACTOR";
            case StatementType.MORE_TERM:
                return "MORE_TERM";
            case StatementType.TERM:
                return "TERM";
            case StatementType.MORE_ADD_EXP:
                return "MORE_ADDITIVE_EXP";
            case StatementType.MORE_LOGIC_EXP:
                return "MORE_EXP";
            case StatementType.VALUE:
                return "VALUE";
            case StatementType.PROGRAM:
                return "PROGRAM";
            case StatementType.STMT_SEQ:
                return "STATEMENT_SEQ";
            case StatementType.NONE:
                return "NONE";
            case StatementType.NULL:
                return "NULL";
            default:
                return "UNKNOWN";
        }
    }

    private String printNode(TreeNode node, String space) {
        boolean hasVal = false;
        StringBuilder strb = new StringBuilder();
        strb.append(space).append("<").append(node.typeToString()).append(" : ").append(new Token(node.getDataType()).typeToString()).append(">\n");
        if (node.getValue() != null) {
            strb.append("\t\t").append(space).append(node.getValue()).append("\n");
        }
        if (node.getLeftN() != null)
            strb.append(printNode(node.getLeftN(), "\t" + space));
        if (node.getMiddleN() != null)
            strb.append(printNode(node.getMiddleN(), "\t" + space));
        if (node.getRightN() != null)
            strb.append(printNode(node.getRightN(), "\t" + space));
        if (node.getNextStmtN() != null)
            strb.append(printNode(node.getNextStmtN(), "\t" + space));
        //strb.append(space + "</" + node.typeToString() + ">\n");
        return strb.toString();
    }

    @Override
    public String toString() {
        return printNode(this, "");
    }

}
