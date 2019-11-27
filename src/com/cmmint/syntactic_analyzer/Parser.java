package com.cmmint.syntactic_analyzer;

import com.exception.ParserException;
import com.cmmint.lexical_analyser.Token;
import com.cmmint.lexical_analyser.TypeEncoding;

import java.util.LinkedList;
import java.util.ListIterator;

public class Parser {
    private static Token currentToken = null;
    private static ListIterator<Token> iterator = null;
    private static int blockLevel = 0;
    private static StringBuilder errorInfo;
    //public static int dims;

    public static Token getCurrentToken() {
        return currentToken;
    }

    private static String getErrorInfo() {
        return errorInfo.toString();
    }

    public static TreeNode syntaxAnalyse(LinkedList<Token> tokens) throws ParserException {  //语法分析主程序
        TreeNode syntaxTree = new TreeNode(StatementType.PROGRAM);
        errorInfo = new StringBuilder();
        iterator = tokens.listIterator();
        blockLevel = 0;
        syntaxTree.setLeftN(pStmtSeq());
        if (errorInfo.length() != 0)
            throw new ParserException(getErrorInfo());
        return syntaxTree;  //返回语法树
    }

    private static TreeNode pStmtSeq() {
        if (iterator.hasNext()) {
            if ((blockLevel == 0) || (blockLevel > 0 && !checkNextTokenType(TypeEncoding.RIGHT_BRA))) {
                TreeNode node = new TreeNode(StatementType.STMT_SEQ);
                node.setLeftN(pStmt());
                node.setMiddleN(pStmtSeq());
                return node;
            } else
                return null;
        } else
            return null;
    }

    private static TreeNode pStmt() { //处理语句或语句块
        switch (getNextTokenType()) {
            case TypeEncoding.IF:
                return pIfStmt();  //转向处理if语句
            case TypeEncoding.END:
                consumeNextToken(TypeEncoding.END);
                return null;  //转向处理空语句
            case TypeEncoding.WHILE:
                return pWhileStmt();  //转向处理while语句
            case TypeEncoding.INT:
            case TypeEncoding.REAL:
            case TypeEncoding.CHAR:
                return pDeclareStmt();  //转向处理声明语句
            case TypeEncoding.PRINT:
                return pPrintStmt();  //转向处理打印语句
            case TypeEncoding.SCAN:
                return pScanStmt();   //转向处理扫描语句
            case TypeEncoding.LEFT_BRA:
                return pStmtBlock();  //转向处理语句块
            case TypeEncoding.BREAK:
            case TypeEncoding.CONTINUE:
                return pJumpStmt();  //转向处理跳转语句
            default:
                return pAssignStmt();  //转向处理赋值语句
        }
    }

    private static TreeNode pStmtBlock() {
        TreeNode node = new TreeNode(StatementType.STBLOCK);
        consumeNextToken(TypeEncoding.LEFT_BRA);
        blockLevel++;
        node.setLeftN(pStmtSeq());
        consumeNextToken(TypeEncoding.RIGHT_BRA);
        blockLevel--;
        return node;
    }

    private static TreeNode pIfStmt() {
        TreeNode node = new TreeNode(StatementType.IF_ST);
        consumeNextToken(TypeEncoding.IF);
        consumeNextToken(TypeEncoding.LEFT_P);
        node.setLeftN(pAssignExp());
        consumeNextToken(TypeEncoding.RIGHT_P);
        node.setMiddleN(pIfBlock());
        return node;
    }

    private static TreeNode pIfBlock() {
        if (checkNextTokenType(TypeEncoding.END)) {
            consumeNextToken(TypeEncoding.END);
            return null;
        } else if (checkNextTokenType(TypeEncoding.ELSE)) {
            return pElseStmt();
        } else {
            TreeNode node;
            node = new TreeNode(StatementType.IF_BLOCK);
            node.setLeftN(pStmt());
            node.setMiddleN(pElseStmt());
            return node;
        }
    }

    private static TreeNode pElseStmt() {
        if (checkNextTokenType(TypeEncoding.ELSE)) {
            TreeNode node = new TreeNode(StatementType.ELSE_ST);
            consumeNextToken(TypeEncoding.ELSE);
            node.setLeftN(pStmt());
            return node;
        } else
            return null;
    }

    private static TreeNode pWhileStmt() {
        TreeNode node = new TreeNode(StatementType.WHILE_ST);
        consumeNextToken(TypeEncoding.WHILE);
        consumeNextToken(TypeEncoding.LEFT_P);
        node.setLeftN(pAssignExp());
        consumeNextToken(TypeEncoding.RIGHT_P);
        node.setMiddleN(pStmt());
        return node;
    }

    private static TreeNode pAssignStmt() {
        TreeNode node = pAssignExp();
        consumeNextToken(TypeEncoding.END);
        return node;
    }

    private static TreeNode pAssignExp() {
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pLogicOrExp());
        node.setMiddleN(pMoreLogicOrExp());
        return node;
    }

    private static TreeNode pMoreLogicOrExp() {
        if (checkNextTokenType(TypeEncoding.ASSIGN)) {
            TreeNode node = new TreeNode(StatementType.MORE_LOGIC_EXP);
            node.setLeftN(getAssignOp());
            node.setMiddleN(pLogicOrExp());
            node.setRightN(pMoreLogicOrExp());
            return node;
        } else
            return null;
    }

    private static TreeNode pJumpStmt() {   //处理跳转语句
        currentToken = iterator.next();
        TreeNode node = new TreeNode(StatementType.JUMP_ST);
        int type = currentToken.getType();
        node.setDataType(type);
        node.setValue(currentToken.getValue());
        consumeNextToken(TypeEncoding.END);
        return node;
    }

    private static TreeNode pScanStmt() { //处理scan语句
        TreeNode node = new TreeNode(StatementType.SCAN_ST);
        consumeNextToken(TypeEncoding.SCAN);
        consumeNextToken(TypeEncoding.LEFT_P);
        node.setLeftN(pVariable());
        consumeNextToken(TypeEncoding.RIGHT_P);
        consumeNextToken(TypeEncoding.END);
        return node;
    }

    private static TreeNode pPrintStmt() { //处理print语句
        TreeNode node = new TreeNode(StatementType.PRINT_ST);
        consumeNextToken(TypeEncoding.PRINT);
        consumeNextToken(TypeEncoding.LEFT_P);
        node.setLeftN(pLogicOrExp());
        consumeNextToken(TypeEncoding.RIGHT_P);
        consumeNextToken(TypeEncoding.END);
        return node;
    }

    private static TreeNode pDeclareStmt() {
        TreeNode node = new TreeNode(StatementType.DEC_ST);
        int type = getNextTokenType();
        if (type == TypeEncoding.INT || type == TypeEncoding.REAL || type == TypeEncoding.CHAR) {
            consumeNextToken(type);
            node.setDataType(type);
        }
        node.setLeftN(pVariableList());
        consumeNextToken(TypeEncoding.END);
        return node;
    }

    private static TreeNode pVariableList() {
        TreeNode node = new TreeNode(StatementType.VAR_LIST);
        node.setLeftN(pVariable());
        node.setMiddleN(pInitializer());
        node.setRightN(pMoreVars());
        return node;
    }

    private static TreeNode pVariable() {
        TreeNode node = new TreeNode(StatementType.VAR);
        node.setLeftN(getIdentifier());
        node.setMiddleN(pArrayDim());
        return node;
    }

    private static TreeNode pArrayDim() {
        if (checkNextTokenType(TypeEncoding.LEFT_BRK)) {
            TreeNode node = new TreeNode(StatementType.ARRAY_DIM);
            consumeNextToken(TypeEncoding.LEFT_BRK);
            node.setLeftN(pLogicOrExp());
            consumeNextToken(TypeEncoding.RIGHT_BRK);
            node.setMiddleN(pArrayDim());
            return node;
        } else
            return null;
    }

    private static TreeNode pLogicOrExp() {
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pLogicAndExp());
        node.setMiddleN(pMoreLogicAndExp());
        return node;
    }

    private static TreeNode pLogicAndExp() {
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pCompEqExp());
        node.setMiddleN(pMoreCompEqExp());
        return node;
    }

    private static TreeNode pMoreCompEqExp() {
        if (checkNextTokenType(TypeEncoding.AND)) {
            TreeNode node = new TreeNode(StatementType.MORE_COMP_EXP);
            node.setLeftN(getLogicOp());
            node.setMiddleN(pCompEqExp());
            node.setRightN(pMoreCompEqExp());
            return node;
        } else
            return null;
    }

    private static TreeNode pCompEqExp() {
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pCompExp());
        node.setMiddleN(pMoreCompExp());
        return node;
    }

    private static TreeNode pMoreCompExp() {
        if (checkNextTokenType(TypeEncoding.EQ, TypeEncoding.NEQ)) {
            TreeNode node = new TreeNode(StatementType.MORE_COMP_EXP);
            node.setLeftN(getCompOp());
            node.setMiddleN(pCompExp());
            node.setRightN(pMoreCompExp());
            return node;
        } else
            return null;
    }

    private static TreeNode pCompExp() {
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pAdditiveExp());
        node.setRightN(pMoreAdditiveExp());
        return node;
    }

    private static TreeNode pMoreLogicAndExp() {
        if (checkNextTokenType(TypeEncoding.OR)) {
            TreeNode node = new TreeNode(StatementType.MORE_LOGIC_EXP);
            node.setLeftN(getLogicOp());
            node.setMiddleN(pLogicAndExp());
            node.setRightN(pMoreLogicAndExp());
            return node;
        } else
            return null;
    }

    private static TreeNode pMoreVars() {
        if (checkNextTokenType(TypeEncoding.COMMA)) {
            TreeNode node = new TreeNode(StatementType.MORE_VAR);
            consumeNextToken(TypeEncoding.COMMA);
            node.setLeftN(pVariable());
            node.setMiddleN(pInitializer());
            node.setRightN(pMoreVars());
            return node;
        } else
            return null;
    }

    private static TreeNode pInitializer() {
        if (checkNextTokenType(TypeEncoding.ASSIGN)) {
            TreeNode node = new TreeNode(StatementType.INIT);
            node.setLeftN(getAssignOp());
            node.setMiddleN(pValue());
            return node;
        } else
            return null;
    }

    private static TreeNode pValue() {
        if (checkNextTokenType(TypeEncoding.LEFT_BRA)) {
            consumeNextToken(TypeEncoding.LEFT_BRA);
            TreeNode node = pValueList();
            consumeNextToken(TypeEncoding.RIGHT_BRA);
            return node;
        } else {
            return pLogicOrExp();
        }
    }

    private static TreeNode pValueList() {    //赋值列表
        TreeNode node = new TreeNode(StatementType.VALUE_LIST);
        node.setLeftN(pValue());
        node.setMiddleN(pMoreValue());
        return node;
    }

    private static TreeNode pMoreValue() {
        if (checkNextTokenType(TypeEncoding.COMMA)) {
            TreeNode node = new TreeNode(StatementType.MORE_VALUE);
            consumeNextToken(TypeEncoding.COMMA);
            node.setLeftN(pValue());
            node.setMiddleN(pMoreValue());
            return node;
        } else
            return null;
    }

    private static TreeNode getIdentifier() {
        if (iterator.hasNext()) {
            if (checkNextTokenType(TypeEncoding.ID)) {
                currentToken = iterator.next();
                TreeNode node = new TreeNode(StatementType.ID);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少标识符.\n");
        return null;
    }

    private static TreeNode getLogicOp() {  //处理逻辑操作符
        if (iterator.hasNext()) {
            if (checkNextTokenType(TypeEncoding.AND, TypeEncoding.OR, TypeEncoding.NOT)) {
                currentToken = iterator.next();
                int type = currentToken.getType();
                TreeNode node = new TreeNode(StatementType.OPR);
                node.setDataType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少逻辑运算符.\n");
        return null;
    }

    private static TreeNode pMoreAdditiveExp() {
        TreeNode node = new TreeNode(StatementType.MORE_ADD_EXP);
        if (checkNextTokenType(TypeEncoding.GREATER,
                TypeEncoding.GREATER_EQ, TypeEncoding.LESS,
                TypeEncoding.LESS_EQ)) {
            node.setLeftN(getCompOp());
            node.setMiddleN(pAdditiveExp());
            node.setRightN(pMoreAdditiveExp());
            return node;
        } else
            return null;
    }

    private static TreeNode getCompOp() {  //逻辑运算符
        if (iterator.hasNext()) {
            if (checkNextTokenType(TypeEncoding.LESS_EQ, TypeEncoding.LESS,
                    TypeEncoding.EQ, TypeEncoding.NEQ,
                    TypeEncoding.GREATER_EQ, TypeEncoding.GREATER)) {
                currentToken = iterator.next();
                int type = currentToken.getType();
                TreeNode node = new TreeNode(StatementType.OPR);
                node.setDataType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少关系运算符.\n");
        return null;
    }

    private static TreeNode pAdditiveExp() {    //多项式
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pTerm());
        node.setMiddleN(pMoreTerm());
        return node;
    }

    private static TreeNode pMoreTerm() {
        TreeNode node = new TreeNode(StatementType.MORE_TERM);
        if (checkNextTokenType(TypeEncoding.PLUS, TypeEncoding.MINUS)) {
            node.setLeftN(getAlgOp());
            node.setMiddleN(pAdditiveExp());
            node.setRightN(pMoreTerm());
            return node;
        } else
            return null;
    }

    private static TreeNode getAlgOp() {  //处理多项式算术运算符
        if (iterator.hasNext()) {
            if (checkNextTokenType(TypeEncoding.PLUS, TypeEncoding.MINUS, TypeEncoding.MUL, TypeEncoding.DIV)) {
                currentToken = iterator.next();
                int type = currentToken.getType();
                TreeNode node = new TreeNode(StatementType.OPR);
                node.setDataType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少算术运算符.\n");
        return null;
    }

    private static TreeNode getAssignOp() {  //处理多项式算术运算符
        if (iterator.hasNext()) {
            if (checkNextTokenType(TypeEncoding.ASSIGN, TypeEncoding.DIV_ASSIGN,
                    TypeEncoding.MUL_ASSIGN, TypeEncoding.PLUS_ASSIGN,
                    TypeEncoding.MINUS_ASSIGN, TypeEncoding.MOD_ASSIGN)) {
                currentToken = iterator.next();
                int type = currentToken.getType();
                TreeNode node = new TreeNode(StatementType.OPR);
                node.setDataType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少赋值运算符.\n");
        return null;
    }

    private static TreeNode pTerm() {  //处理项
        TreeNode node = new TreeNode(StatementType.TERM);
        node.setLeftN(pFactor());
        node.setMiddleN(pMoreFactor());
        return node;
    }

    private static TreeNode pFactor() {
        TreeNode node = new TreeNode(StatementType.FACTOR);
        if (checkNextTokenType(TypeEncoding.NOT)) {
            node.setLeftN(getLogicOp());
            node.setMiddleN(pFactor());
        } else if (checkNextTokenType(TypeEncoding.PLUS_PLUS)) {
            node.setLeftN(getIncDecOp());
            node.setMiddleN(pFactor());
        } else if (checkNextTokenType(TypeEncoding.PLUS, TypeEncoding.MINUS)) {
            node.setLeftN(getAlgOp());
            node.setMiddleN(pFactor());
        } else {
            node.setLeftN(pSpecific());
            node.setMiddleN(getPossibleIncDecOp());
        }
        return node;
    }

    private static TreeNode getPossibleIncDecOp() {
        if (checkNextTokenType(TypeEncoding.PLUS_PLUS, TypeEncoding.MINUS_MINUS)) {
            return getIncDecOp();
        } else
            return null;
    }

    private static TreeNode pSpecific() {
        if (iterator.hasNext()) {
            TreeNode node;
            int type = getNextTokenType();
            switch (type) {
                case TypeEncoding.LEFT_P:
                    consumeNextToken(TypeEncoding.LEFT_P);
                    node = pAssignExp();
                    consumeNextToken(TypeEncoding.RIGHT_P);
                    return node;
                case TypeEncoding.INT_VAL:
                case TypeEncoding.REAL_VAL:
                    node = getNumValue();
                    return node;
                case TypeEncoding.NULL:
                    currentToken = iterator.next();
                    node = new TreeNode(StatementType.NULL);
                    node.setDataType(TypeEncoding.NULL);
                    node.setValue(currentToken.getValue());
                    return node;
                case TypeEncoding.PLUS:
                    currentToken = iterator.next();
                    node = pTerm();
                    return node;
                case TypeEncoding.MINUS:
                    currentToken = iterator.next();
                    node = pTerm();
                    node.setDataType(TypeEncoding.MINUS);
                    return node;
                case TypeEncoding.SCAN:
                    consumeNextToken(TypeEncoding.SCAN);
                    consumeNextToken(TypeEncoding.LEFT_P);
                    node = pVariable();
                    consumeNextToken(TypeEncoding.RIGHT_P);
                    return node;
                case TypeEncoding.ID:
                    node = pVariable();
                    return node;
                default:
                    break;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少标识符或数值或表达式.\n");
        return null;
    }

    private static TreeNode getIncDecOp() {
        if (iterator.hasNext()) {
            if (checkNextTokenType(TypeEncoding.PLUS_PLUS, TypeEncoding.MINUS_MINUS)) {
                currentToken = iterator.next();
                int type = currentToken.getType();
                TreeNode node = new TreeNode(StatementType.OPR);
                node.setDataType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少自增/自减运算符.\n");
        return null;
    }

    private static TreeNode pMoreFactor() {
        TreeNode node = new TreeNode(StatementType.MORE_FACTOR);
        if (checkNextTokenType(TypeEncoding.MUL, TypeEncoding.DIV)) {
            node.setLeftN(getAlgOp());
            node.setMiddleN(pTerm());
            return node;
        } else
            return null;
    }

    private static TreeNode getNumValue() {    //获取具体的值
        if (iterator.hasNext()) {
            if (checkNextTokenType(TypeEncoding.INT_VAL, TypeEncoding.REAL_VAL)) {
                currentToken = iterator.next();
                int type = currentToken.getType();
                TreeNode node = new TreeNode(StatementType.VALUE);
                node.setDataType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少值类型.\n");
        return null;
    }

    private static boolean checkNextTokenType(int... types) {  //检查下一个token的类型
        if (iterator.hasNext()) {
            int type = iterator.next().getType();
            iterator.previous();
            for (int t : types) {
                if (t == type)
                    return true;
            }
        }
        return false;
    }

    private static int getNextTokenType() {    //获取下一个token类型
        if (iterator.hasNext()) {
            int type = iterator.next().getType();
            iterator.previous();
            return type;
        }
        return TypeEncoding.NULL;
    }

    private static void consumeNextToken(int type) {   //消耗掉无用的token
        if (iterator.hasNext()) {
            if (checkNextTokenType(type)) {
                currentToken = iterator.next();
                return;
            }
        }
        errorInfo.append("ERROR : " + "line: ").append(currentToken.getLineNo()).append(" 此处缺少").append(new Token(type).typeToString()).append("（类型）.\n");
    }
}
