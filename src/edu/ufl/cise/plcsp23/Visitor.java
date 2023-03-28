package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.List;
//names scopes and bindings //leblanc cook table each scope has unique identification
//hashmap of linked lists for symbol table //store stack where each entry is the id of the specific scope

public class Visitor implements ASTVisitor{
    //implement the type checking for the language ignoring scoping

    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        return null;
    }

    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitBlock(Block block, Object arg) throws PLCException {
        return null;
    }

    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        return null;
    }

    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }

    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }

    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        return null;
    }

    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitProgram(Program program, Object arg) throws PLCException {
        //enterScope
        List<NameDef> nameDefList;
        //All NameDef objects are properly typed
        nameDefList = program.getParamList();//gets list of NameDef Objects

        for (NameDef n:nameDefList) {//verify this works as intended
            n.visit(this,arg);
        }

        //Block is properly typed
        program.getBlock().visit(this,arg);


        //leaveScope
        return null;
    }

    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        return null;
    }

    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        return null;
    }

    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        return null;
    }

    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        return null;
    }

    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        return null;
    }

    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return null;
    }
}
