package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.runtime.*;

import java.util.List;

import static edu.ufl.cise.plcsp23.ast.Type.*;
/*
* Start with the most explicit ones
*
* BinaryExpr
* UnaryExprPostfix
* Declaration
*
*
*
* Handle Channel Selector in Assignment Statement for LValue
* Assignment Statement last
* */
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

        if(lValue.type == STRING){
            if(expr.getType() == INT){
                lValue.visit(this,arg);
                output.append("=");
                expr.visit(this,arg);
                output.append(".toString()");
            }else{
                lValue.visit(this,arg);
                output.append("=");
                expr.visit(this,arg);
            }
        }else if(lValue.type == PIXEL){
            lValue.visit(this,arg);
            output.append("=");
            expr.visit(this, arg);
        }else if(lValue.type == IMAGE){
            PixelSelector pixelSelector = lValue.getPixelSelector();
            ColorChannel color = lValue.getColor();
            if(pixelSelector == null){
                if(color == null){
                    switch(expr.getType()){
                        case STRING -> {
                            output.append("ImageOps.copyInto(");
                            output.append("FileURLIO.readImage(");
                            expr.visit(this, arg);
                            output.append("), ");
                            lValue.visit(this,arg);
                            output.append(")");
                        }
                        case IMAGE -> {
                            //get size of LValue image
                            //resize expr image to those dimensions
                            //ImageOps.copyInto()
                        }
                        case PIXEL -> {

                        }
                        default -> {
                            throw new PLCRuntimeException("unexpected expr type");
                        }
                    }
                }else{
                    throw new PLCRuntimeException("no pixel selector, but has color channel");
                }
            }else{
                if(color == null){//Variable type is image with pixel selector, no color channel
                    //3 lectures ago, April 12th

                }else{//Variable type is image with pixel selector and color channel

                }
            }
        }
        else{
            lValue.visit(this,arg);
            output.append("=");
            expr.visit(this,arg);
        }
        return null;
    }

    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        output.append("(");
        Expr e0 = binaryExpr.getLeft();
        Expr e1 = binaryExpr.getRight();
        Type t0 = e0.getType();
        Type t1 = e1.getType();
        String op = null;
        switch(binaryExpr.getOp()){
            case PLUS -> {
                op = "ImageOps.OP.PLUS";
            }
            case MINUS -> {
                op = "ImageOps.OP.MINUS";
            }
            case TIMES -> {
                op = "ImageOps.OP.TIMES";
            }
            case DIV -> {
                op = "ImageOps.OP.DIV";
            }
            case MOD -> {
                op = "ImageOps.OP.MOD";
            }
        }

        if(t0 == IMAGE){
            if(t1 == IMAGE){

                output.append("ImageOps.binaryImageImageOp(" +op+", ");
                e0.visit(this,arg);
                output.append(", ");
                e1.visit(this,arg);
                output.append(")");
            }
            else if(t1 == INT){
                output.append("ImageOps.binaryImageScalarOp(" + op+", ");
                e0.visit(this,arg);
                output.append(", ");
                e1.visit(this,arg);
                output.append(")");

            }else{
                throw new PLCRuntimeException("e0 is a Image and e1 is incompatible");
            }
        }else if(t0 == PIXEL){
            if(t1 == PIXEL){
                output.append("ImageOps.binaryImagePixelOp("+op+", ");
                e0.visit(this,arg);
                output.append(", ");
                e1.visit(this,arg);
                output.append(")");
            }else{
                throw new PLCRuntimeException("e0 is a Pixel and e1 is incompatible");
            }
        }
        else{
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
        dimension.getWidth().visit(this,arg);
        output.append(", ");
        dimension.getHeight().visit(this,arg);
        return null;
    }
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        output.append("PixelOps.pack(");
        expandedPixelExpr.getRedExpr().visit(this,arg);
        output.append(", ");
        expandedPixelExpr.getGrnExpr().visit(this,arg);
        output.append(", ");
        expandedPixelExpr.getBluExpr().visit(this,arg);
        output.append(")");
        return null;
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
        if(nameDef.getType() == PIXEL){
            output.append("int");
        }
        else if (nameDef.getType() == IMAGE) {
            output.append("BufferedImage");
        } else{
            output.append(type_to_string(nameDef.getType()));
        }
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
        pixelSelector.getX().visit(this,arg);
        output.append(", ");
        pixelSelector.getY().visit(this,arg);
        return null;
    }
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        //only outputs x and y
        IToken.Kind k = predeclaredVarExpr.getKind();
        if(k == IToken.Kind.RES_x){
            output.append("x");
            return null;
        }
        else if(k == IToken.Kind.RES_y){
            output.append("y");
            return null;
        }
        else{
            throw new UnsupportedOperationException("visitPredeclaredVarExpr");
        }
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
        //how does the case where it is an Ident with type int

        IToken.Kind op = unaryExpr.getOp();
        Expr expr = unaryExpr.getE();
        if(expr.getType() == INT){
            if(op == IToken.Kind.BANG){
                expr.visit(this,arg);
                output.append("==0 ? 1 : 0");
                return null;
            } else if (op == IToken.Kind.MINUS) {
                output.append("-");
                expr.visit(this,arg);
                return null;
            }else{
                throw new UnsupportedOperationException("visitUnaryExpr");
            }
        }else{
            throw new UnsupportedOperationException("visitUnaryExpr");
        }
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
        Expr expr = statementWrite.getE();
        if(expr.getType() == PIXEL){
            output.append("ConsoleIO.writePixel(");
        }
        else{
            output.append("ConsoleIO.write(");
        }
        expr.visit(this,arg);
        output.append(")");
        return null;
    }
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        output.append(zExpr.getValue());
        return zExpr.getValue();
    }
}
