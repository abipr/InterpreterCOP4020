package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import java.util.List;
import static edu.ufl.cise.plcsp23.ast.Type.*;

public class codeGenerator implements ASTVisitor {
    StringBuilder output;
    String packageName;
    codeGenerator(){
        output = new StringBuilder();
    }
    codeGenerator(String packageName){
        output = new StringBuilder();
        this.packageName = packageName;
    }
    String type_to_string(Type type){
        String javaType = "";
        switch(type){
            case INT ->{
                javaType = "Integer";
            }
            case STRING->{
                javaType = "String";
            }
            case VOID->{
                javaType = "void";
            }
            case IMAGE->{
                throw new UnsupportedOperationException("IMAGE");
            }
            case PIXEL->{
                throw new UnsupportedOperationException("PIXEL");
            }
        }
        return javaType;
    }
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        LValue lValue = statementAssign.getLv();
        Expr expr = statementAssign.getE();
        lValue.visit(this,arg);
        output.append("=");
        if(lValue.type != STRING){
            expr.visit(this,arg);
        }else{
            if(expr.getType() == INT){
                expr.visit(this,arg);
                output.append(".toString()");
            }else{
                expr.visit(this,arg);
            }
        }
        return null;
    }
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        output.append("(");
        Expr e0 = binaryExpr.getLeft();
        Expr e1 = binaryExpr.getRight();
        switch(binaryExpr.getOp()){
            case PLUS -> {
                e0.visit(this, arg);
                output.append(" + ");
                e1.visit(this, arg);
            }
            case MINUS -> {
                e0.visit(this, arg);
                output.append(" - ");
                e1.visit(this, arg);
            }
            case TIMES -> {
                e0.visit(this, arg);
                output.append(" * ");
                e1.visit(this, arg);
            }
            case DIV -> {
                e0.visit(this, arg);
                output.append(" / ");
                e1.visit(this, arg);
            }
            case MOD -> {
                e0.visit(this, arg);
                output.append(" % ");
                e1.visit(this, arg);
            }
            case LT -> {
                output.append("((");
                e0.visit(this, arg);
                output.append("<");
                e1.visit(this, arg);
                output.append(") ? 1 : 0)");
            }
            case GT -> {
                output.append("((");
                e0.visit(this, arg);
                output.append(">");
                e1.visit(this, arg);
                output.append(") ? 1 : 0)");
            }
            case LE -> {
                output.append("((");
                e0.visit(this, arg);
                output.append("<=");
                e1.visit(this, arg);
                output.append(") ? 1 : 0)");
            }
            case GE -> {
                output.append("((");
                e0.visit(this, arg);
                output.append(">=");
                e1.visit(this, arg);
                output.append(") ? 1 : 0)");
            }
            case EQ -> {
                output.append("((");
                e0.visit(this, arg);
                output.append("==");
                e1.visit(this, arg);
                output.append(") ? 1 : 0)");
            }
            case OR -> {
                output.append("(((");
                e0.visit(this, arg);
                output.append("!= 0)");
                output.append("|| (");
                e1.visit(this, arg);
                output.append("!= 0)");
                output.append(") ? 1 : 0)");
            }
            case AND -> {
                output.append("(((");
                e0.visit(this, arg);
                output.append("!= 0)");
                output.append("&& (");
                e1.visit(this, arg);
                output.append("!= 0)");
                output.append(") ? 1 : 0)");
            }
            case BITOR -> {
                e0.visit(this, arg);
                output.append(" | ");
                e1.visit(this, arg);
            }
            case BITAND -> {
                e0.visit(this, arg);
                output.append(" & ");
                e1.visit(this, arg);
            }
            case EXP -> {
                output.append("(int) Math.pow(");
                e0.visit(this, arg);
                output.append(", ");
                e1.visit(this, arg);
                output.append(")");
            }
        }
        output.append(")");
        return null;
    }
    public Object visitBlock(Block block, Object arg) throws PLCException {
        //visit Declarations
        for (Declaration dec:block.getDecList()) {
            dec.visit(this,arg);
            output.append(";\n");
        }
        //visit Statements
        for (Statement statement:block.getStatementList()) {
            statement.visit(this,arg);
            if(statement.toString().charAt(0) != 'W'){
                output.append(";\n");
            }
        }
        return null;
    }
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Expr e0,e1,e2;
        e0 = conditionalExpr.getGuard();
        e1 = conditionalExpr.getTrueCase();
        e2 = conditionalExpr.getFalseCase();

        output.append("((");
        e0.visit(this,arg);
        output.append("!=0)");
        output.append(" ? ");
        e1.visit(this,arg);
        output.append(" : ");
        e2.visit(this,arg);
        output.append(")");
        return null;
    }
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef nameDef = declaration.getNameDef();
        nameDef.visit(this,arg);
        Expr expr = declaration.getInitializer();
        if(expr != null){
            output.append("=");
            expr.visit(this,arg);
            if(expr.getType() == INT && nameDef.getType() == STRING){
                output.append(".toString()");
            }
        }
        return null;
    }
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        //do not implement in Assignment 5
        throw new UnsupportedOperationException("visitDimension");
    }
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        //do not implement in Assignment 5
        throw new UnsupportedOperationException("visitExpandedPixelExpr");
    }
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        output.append(identExpr.getName());
        if(identExpr.getNameDef() != null){
        output.append(identExpr.getNameDef().getScopeID());}
        return null;
    }
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        output.append(lValue.getIdent().getName());
        output.append(lValue.nameDef.getScopeID());
        //only handle the case where there is no PixelSelector and no ChannelSelector
        return null;
    }
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        output.append(type_to_string(nameDef.getType()));
        output.append(" ");
        output.append(nameDef.getIdent().getName());
        output.append(nameDef.scopeID);
        return null;
    }
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        output.append(numLitExpr.getValue());
        return null;
    }
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        //do not implement in Assignment 5
        throw new UnsupportedOperationException("visitPixelFuncExpr");
    }
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        //do not implement in Assignment 5
        throw new UnsupportedOperationException("visitPixelSelector");
    }
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        //do not implement in Assignment 5
        throw new UnsupportedOperationException("visitPredeclaredVarExpr");
    }
    public Object visitProgram(Program program, Object arg) throws PLCException {
        if(packageName != ""){
            output.append("package ");
            output.append(packageName);
            output.append(";\n");
        }
        output.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO;\n");
        output.append("public class ");
        output.append(program.getIdent().getName());
        output.append(" {\npublic static ");
        output.append(type_to_string(program.getType()));
        output.append(" apply(");
        //params
        List<NameDef> paramList = program.getParamList();
        int size = paramList.size();
        for(int i = 0; i < size-1; i++){
            paramList.get(i).visit(this,arg);
            output.append(",");
        }
        if(size > 0){
        paramList.get(size - 1).visit(this,arg);}
        output.append(") {\n");
        //blocks
        program.getBlock().visit(this,arg);
        output.append("}\n}");//the second } was not in documentation but is necessary to be a valid java class
        return output.toString();
    }
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        //generate code for a random int [0,265)
        output.append("(int) Math.floor(Math.random() * 256)");
        return null;
    }
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        output.append("return ");
        returnStatement.getE().visit(this,arg);
        return null;
    }
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        //added quotes around the strings
        output.append("\"");
        output.append(stringLitExpr.getValue());
        output.append("\"");
        return null;
    }
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        //do not implement in Assignment 5
        throw new UnsupportedOperationException("visitUnaryExpr");
    }
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        //do not implement in Assignment 5
        throw new UnsupportedOperationException("visitUnaryExprPostFix");
    }
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        Expr expr = whileStatement.getGuard();
        Block block = whileStatement.getBlock();
        output.append("while (");
        expr.visit(this,arg);
        output.append(" != 0");
        output.append(") {\n");
        block.visit(this,arg);
        output.append("}");
        return null;
    }
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        output.append("ConsoleIO.write(");
        statementWrite.getE().visit(this,arg);
        output.append(")");
        return null;
    }
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        output.append(zExpr.getValue());
        return zExpr.getValue();
    }
}
