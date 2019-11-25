package com.cmmint.SyntacticParser;

import com.Exception.ParserException;
import com.cmmint.LexicalAnalyser.Token;
import com.cmmint.LexicalAnalyser.TypeEncoding;

import java.util.LinkedList;
import java.util.ListIterator;

public class Parser {
    private static Token currentToken = null;
    private static ListIterator<Token> iterator = null;
    private static LinkedList<Token> tokensList = null;
    private static boolean allowNull = false;  //允许空语句
    public static int dims;

    public static Token getCurrentToken() {
        return currentToken;
    }

    public static LinkedList<TreeNode> syntaxAnalyse(LinkedList<Token> tokens) throws ParserException {  //语法分析主程序
        LinkedList<TreeNode> syntaxTree = new LinkedList<>();
        tokensList = tokens;
        iterator = tokensList.listIterator();
        while (iterator.hasNext()) {
            syntaxTree.add(pStatement());
        }
        return syntaxTree;  //返回语法树
    }

    public static String printTree(LinkedList<TreeNode> tree) {  //打印语法树
        StringBuilder strb = new StringBuilder();
        for (TreeNode node : tree) {
            strb.append(node.toString());
        }
        return strb.toString();
    }

    private static TreeNode pExpression() throws ParserException {  //处理表达式
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pCondExp());
        return node;
    }

    private static TreeNode pStatement() throws ParserException { //处理语句或语句块
        switch (getNextTokenType()) {
            case TypeEncoding.IF:
                return pIfStatement();  //转向处理if语句
            case TypeEncoding.WHILE:
                return pWhileStatement();  //转向处理while语句
            case TypeEncoding.ID:
                return pAssignStatement();  //转向处理赋值语句
            case TypeEncoding.INT:
            case TypeEncoding.REAL:
                return pDeclareStatement();  //转向处理声明语句
            case TypeEncoding.PRINT:
                return pPrintStatement();  //转向处理打印语句
            case TypeEncoding.SCAN:
                return pScanStatement();   //转向处理扫描语句
            case TypeEncoding.LEFTBRA:
                return pStBlockStatement();  //转向处理语句块
            case TypeEncoding.BREAK:
            case TypeEncoding.CONTINUE:
                return pJumpStatement();  //转向处理跳转语句
            default:
                if (allowNull)    //判断当前位置是否允许为空语句
                    consumeNextToken(StatementType.NONE);
                else if (currentToken != null)
                    throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处需要有单个语句或者语句块或';'.");
                else {
                    currentToken = iterator.next();
                    throw new ParserException("ERROR : " + "line: 1" + " 此处需要有单个语句或者语句块或';'.");
                }

        }
        TreeNode node = new TreeNode(StatementType.NONE);
        node.setdType(TypeEncoding.NULL);
        return node;
    }

    private static TreeNode pJumpStatement() throws ParserException {   //处理跳转语句
        if (checkNextTokenType(TypeEncoding.BREAK, TypeEncoding.CONTINUE)) {
            currentToken = iterator.next();
            TreeNode node = new TreeNode(StatementType.JUMP_ST);
            int type = currentToken.getType();
            node.setdType(type);
            node.setValue(currentToken.getValue());
            consumeNextToken(TypeEncoding.END);
            return node;
        }
        throw new ParserException();
    }


    private static TreeNode pWhileStatement() throws ParserException {    //处理while语句
        TreeNode node = new TreeNode(StatementType.WHILE_ST);
        consumeNextToken(TypeEncoding.WHILE);
        consumeNextToken(TypeEncoding.LEFTP);
        try {
            TreeNode left_node = pCondExp();
            node.setLeftN(left_node);
            consumeNextToken(TypeEncoding.RIGHTP);
        } catch (ParserException e) {
            throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " WHILE括号内应为逻辑表达式或数值类型 或 缺少')'.");
        }
        if (checkNextTokenType(TypeEncoding.END)) {
            consumeNextToken(TypeEncoding.END);
            return node;
        } else if (checkNextTokenType(TypeEncoding.LEFTBRA)) {
            allowNull = true;
            node.setMiddleN(pStatement());
            allowNull = false;
        } else {
            allowNull = false;
            node.setMiddleN(pStatement());
        }
        return node;
    }

    private static TreeNode pStBlockStatement() throws ParserException {  //处理语句块
        TreeNode node = new TreeNode(StatementType.STBLOCK);
        TreeNode head_node = node;
        consumeNextToken(TypeEncoding.LEFTBRA);

        while (!checkNextTokenType(TypeEncoding.RIGHTBRA) && iterator.hasNext()) {
            allowNull = false;
            TreeNode temp = pStatement();
            node.setNextStmtN(temp);
            node = temp;
        }
        consumeNextToken(TypeEncoding.RIGHTBRA);
        return head_node;
    }

    private static TreeNode pDeclareStatement() throws ParserException {  //处理声明语句
        TreeNode node = new TreeNode(StatementType.DEC_ST);
        TreeNode var_node = new TreeNode(StatementType.VAR);
        if (checkNextTokenType(TypeEncoding.INT, TypeEncoding.REAL)) {   //检查下一个token是否是int或者real
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == TypeEncoding.INT) {
                var_node.setdType(TypeEncoding.INT);
            } else {
                var_node.setdType(TypeEncoding.REAL);
            }
        } else {
            throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少INT或REAL类型.");
        }
        if (checkNextTokenType(TypeEncoding.ID)) {
            currentToken = iterator.next();
            var_node.setValue(currentToken.getValue());
        } else {
            throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少标识符.");
        }
        if (checkNextTokenType(TypeEncoding.ASSIGN)) {    //是否变为初始化语句
            node.setType(StatementType.INIT_ST);
            consumeNextToken(TypeEncoding.ASSIGN);
            node.setMiddleN(pExpression());
        } else if (checkNextTokenType(TypeEncoding.LEFTBRK)) {
            consumeNextToken(TypeEncoding.LEFTBRK);
            var_node.setLeftN(pExpression());
            consumeNextToken(TypeEncoding.RIGHTBRK);
            if (checkNextTokenType(TypeEncoding.ASSIGN)) {
                node.setType(StatementType.INIT_ST);
                consumeNextToken(TypeEncoding.ASSIGN);
                consumeNextToken(TypeEncoding.LEFTBRA);
                if (!checkNextTokenType(TypeEncoding.RIGHTBRA))
                    node.setMiddleN(pValueList());
                else {
                    TreeNode val_node = new TreeNode(StatementType.VALUE_LIST);
                    node.setMiddleN(val_node);
                }
                consumeNextToken(TypeEncoding.RIGHTBRA);
            }
        }
        consumeNextToken(TypeEncoding.END);
        node.setLeftN(var_node);
        return node;
    }

    private static TreeNode pValueList() throws ParserException {    //赋值列表
        TreeNode node = new TreeNode(StatementType.VALUE_LIST);
        node.setLeftN(pExpression());
        node.setMiddleN(pMoreValue());
        return node;
    }

    private static TreeNode pMoreValue() throws ParserException {
        TreeNode node = new TreeNode(StatementType.MORE_VALUE);
        if (checkNextTokenType(TypeEncoding.COMMA)) {
            consumeNextToken(TypeEncoding.COMMA);
            node.setLeftN(pExpression());
            node.setMiddleN(pMoreValue());
            return node;
        } else
            return null;
    }

    private static TreeNode pAssignStatement() throws ParserException {  //处理赋值语句
        TreeNode node = new TreeNode(StatementType.ASSIGN_ST);
        node.setLeftN(getVariable());
        consumeNextToken(TypeEncoding.ASSIGN);
        node.setMiddleN(pExpression());
        consumeNextToken(TypeEncoding.END);
        return node;
    }

    private static TreeNode pPrintStatement() throws ParserException { //处理print语句
        TreeNode node = new TreeNode(StatementType.PRINT_ST);
        consumeNextToken(TypeEncoding.PRINT);
        consumeNextToken(TypeEncoding.LEFTP);
        node.setLeftN(pExpression());
        consumeNextToken(TypeEncoding.RIGHTP);
        consumeNextToken(TypeEncoding.END);
        return node;
    }

    private static TreeNode pScanStatement() throws ParserException { //处理scan语句
        TreeNode node = new TreeNode(StatementType.SCAN_ST);
        consumeNextToken(TypeEncoding.SCAN);
        consumeNextToken(TypeEncoding.LEFTP);
        node.setLeftN(getVariable());
        consumeNextToken(TypeEncoding.RIGHTP);
        consumeNextToken(TypeEncoding.END);
        return node;
    }

    private static TreeNode pIfStatement() throws ParserException {    //解析if语句
        TreeNode node = new TreeNode(StatementType.IF_ST);
        consumeNextToken(TypeEncoding.IF);
        consumeNextToken(TypeEncoding.LEFTP);
        try {
            TreeNode left_node = pCondExp();
            node.setLeftN(left_node);
            consumeNextToken(TypeEncoding.RIGHTP);
        } catch (ParserException e) {
            throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " IF括号内应为逻辑表达式或者数值类型 或 缺少')'.");
        }
        if (checkNextTokenType(TypeEncoding.END)) {
            consumeNextToken(TypeEncoding.END);
            return node;
        } else if (checkNextTokenType(TypeEncoding.LEFTBRA)) {
            allowNull = true;
            node.setMiddleN(pStatement());
            allowNull = false;
        } else if (checkNextTokenType(TypeEncoding.ELSE)) {
            node.setMiddleN(new TreeNode(StatementType.NONE));
            consumeNextToken(TypeEncoding.ELSE);
            TreeNode else_node = new TreeNode(StatementType.ELSE_ST);
            node.setRightN(else_node);
            if (checkNextTokenType(TypeEncoding.END)) {
                consumeNextToken(TypeEncoding.END);
                return node;
            } else if (checkNextTokenType(TypeEncoding.LEFTBRA)) {
                allowNull = true;
                else_node.setLeftN(pStatement());
                allowNull = false;
            } else {
                allowNull = false;
                else_node.setLeftN(pStatement());
            }
            return node;
        } else {
            allowNull = false;
            node.setMiddleN(pStatement());
        }

        if (getNextTokenType() == TypeEncoding.ELSE) { //如果后面有else
            consumeNextToken(TypeEncoding.ELSE);
            TreeNode else_node = new TreeNode(StatementType.ELSE_ST);
            node.setRightN(else_node);
//            allowNull = true;
//            else_node.setLeftN(pStatement());
//            allowNull = false;
            if (checkNextTokenType(TypeEncoding.END)) {
                else_node.setLeftN(new TreeNode(StatementType.NONE));
                consumeNextToken(TypeEncoding.END);
                return node;
            } else if (checkNextTokenType(TypeEncoding.LEFTBRA)) {
                allowNull = true;
                else_node.setLeftN(pStatement());
                allowNull = false;
            } else {
                allowNull = false;
                else_node.setLeftN(pStatement());
            }
        }
        return node;
    }

    private static TreeNode pCondExp() throws ParserException {  //处理条件表达式
        TreeNode node = new TreeNode(StatementType.EXP);
        if (checkNextTokenType(TypeEncoding.NOT)) {
            node.setLeftN(pLogicalOP());
            node.setMiddleN(pCondExp());
            return node;
        } else {
            node.setLeftN(pGeneralExp());
            node.setMiddleN(pMoreExp());
        }
        return node;
    }

    private static TreeNode pMoreExp() throws ParserException {
        TreeNode node = new TreeNode(StatementType.MORE_EXP);
        if (checkNextTokenType(TypeEncoding.AND, TypeEncoding.OR)) {
            node.setLeftN(pLogicalOP());
            node.setMiddleN(pExpression());
            node.setRightN(pMoreExp());
            return node;
        } else
            return null;
    }

    private static TreeNode pLogicalOP() throws ParserException {  //处理逻辑操作符
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == TypeEncoding.AND || type == TypeEncoding.OR || type == TypeEncoding.NOT) {
                TreeNode node = new TreeNode(StatementType.OPR);
                node.setdType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少逻辑运算符.");
    }

    private static TreeNode pGeneralExp() throws ParserException {  //解析表达式
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pAdditiveExp());
        node.setMiddleN(pMoreAdditiveExp());
        return node;
    }

    private static TreeNode pMoreAdditiveExp() throws ParserException {
        TreeNode node = new TreeNode(StatementType.MORE_ADDEXP);
        int[] types = {TypeEncoding.EQ, TypeEncoding.NEQ,
                TypeEncoding.GREATER, TypeEncoding.GREATEREQ,
                TypeEncoding.LESS, TypeEncoding.LESSEQ};
        if (checkNextTokenType(types)) {
            node.setLeftN(pCompareOP());
            node.setMiddleN(pAdditiveExp());
            node.setRightN(pMoreAdditiveExp());
            return node;
        } else
            return null;
    }

    private static TreeNode pCompareOP() throws ParserException {  //逻辑运算符
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == TypeEncoding.LESSEQ || type == TypeEncoding.LESS
                    || type == TypeEncoding.EQ || type == TypeEncoding.NEQ
                    || type == TypeEncoding.GREATEREQ || type == TypeEncoding.GREATER) {
                TreeNode node = new TreeNode(StatementType.OPR);
                node.setdType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少关系运算符.");
    }

    private static TreeNode pAdditiveExp() throws ParserException {    //多项式
        TreeNode node = new TreeNode(StatementType.EXP);
        node.setLeftN(pTerm());
        node.setMiddleN(pMoreTerm());
        return node;
    }

    private static TreeNode pMoreTerm() throws ParserException {
        TreeNode node = new TreeNode(StatementType.MORE_TERM);
        if (checkNextTokenType(TypeEncoding.PLUS, TypeEncoding.MINUS, TypeEncoding.MUL, TypeEncoding.DIV)) {
            node.setLeftN(pAlgOp());
            node.setMiddleN(pAdditiveExp());
            node.setRightN(pMoreTerm());
            return node;
        } else
            return null;
    }

    private static TreeNode pAlgOp() throws ParserException {  //处理多项式算术运算符
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == TypeEncoding.PLUS
                    || type == TypeEncoding.MINUS
                    || type == TypeEncoding.MUL
                    || type == TypeEncoding.DIV) {
                TreeNode node = new TreeNode(StatementType.OPR);
                node.setdType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少算术运算符.");
    }

    private static TreeNode pTerm() throws ParserException {  //处理项
        TreeNode node = new TreeNode((StatementType.TERM));
        node.setLeftN(pFactorExp());
        node.setMiddleN(pMoreFactor());
        return node;
    }

    private static TreeNode pMoreFactor() throws ParserException {
        TreeNode node = new TreeNode(StatementType.MORE_FACTOR);
        if (checkNextTokenType(TypeEncoding.PLUS, TypeEncoding.MINUS, TypeEncoding.MUL, TypeEncoding.DIV)) {
            node.setLeftN(pAlgOp());
            node.setMiddleN(pTerm());
            return node;
        } else
            return null;
    }

    private static TreeNode pFactorExp() throws ParserException {  //处理因子
        if (iterator.hasNext()) {
            TreeNode node = new TreeNode(StatementType.FACTOR);
            int type = getNextTokenType();
            switch (type) {
                case TypeEncoding.LEFTP:
                    consumeNextToken(TypeEncoding.LEFTP);
                    node.setLeftN(pExpression());
                    consumeNextToken(TypeEncoding.RIGHTP);
                    break;
                case TypeEncoding.INTVAL:
                case TypeEncoding.REALVAL:
                    node.setLeftN(getNumValue());
                    break;
                case TypeEncoding.NULL:
                    currentToken = iterator.next();
                    TreeNode leftnode = new TreeNode(StatementType.NULL);
                    leftnode.setdType(TypeEncoding.NULL);
                    leftnode.setValue(currentToken.getValue());
                    node.setLeftN(leftnode);
                    break;
                case TypeEncoding.PLUS:
                    currentToken = iterator.next();
                    node.setLeftN(pTerm());
                    break;
                case TypeEncoding.MINUS:
                    currentToken = iterator.next();
                    node.setdType(TypeEncoding.MINUS);
                    node.setLeftN(pTerm());
                    break;
                default:
                    node.setLeftN(getVariable());
            }
            return node;
        }
        throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少因子.");
    }

    private static TreeNode getVariable() throws ParserException {  //获取变量名，可能为数组的形式
        TreeNode node = new TreeNode(StatementType.VAR);
        if (checkNextTokenType(TypeEncoding.ID)) {
            currentToken = iterator.next();
            node.setValue(currentToken.getValue());
        } else {
            throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少变量或常量.");
        }
        if (checkNextTokenType(TypeEncoding.LEFTBRK)) {
            consumeNextToken(TypeEncoding.LEFTBRK);
            node.setLeftN(pExpression());
            consumeNextToken(TypeEncoding.RIGHTBRK);
        }
        return node;
    }

    private static TreeNode getNumValue() throws ParserException {    //获取具体的值
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            TreeNode node = new TreeNode(StatementType.VALUE);
            if (type == TypeEncoding.INTVAL || type == TypeEncoding.REALVAL) {
                node.setdType(type);
                node.setValue(currentToken.getValue());
                return node;
            }
        }
        throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少值类型.");
    }

    private static boolean checkNextTokenType(int... types) {  //检查下一个token的类型
        if (iterator.hasNext()) {
            int tokentype = iterator.next().getType();
            iterator.previous();
            for (int type : types) {
                if (type == tokentype)
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

    private static void consumeNextToken(int type) throws ParserException {   //消耗掉无用的token
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            if (currentToken.getType() == type) return;
            else {
                if (type == 0)
                    return;
                throw new ParserException("ERROR : " + "line: " + currentToken.getLineNo() + " 此处缺少" + new Token(type).typeToString() + "（类型）.");
            }

        } else if (type == 0)
            return;
        throw new ParserException("ERROR : " + "line: " + tokensList.getLast().getLineNo() + " 此处缺少" + new Token(type).typeToString() + "（类型）.");
    }
}
