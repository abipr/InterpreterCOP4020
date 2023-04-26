package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.runtime.*;

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
                javaType = "BufferedImage";
            }
            case PIXEL->{
                javaType = "int";
            }
        }
        return javaType;
    }
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        LValue lValue = statementAssign.getLv();
        Expr expr = statementAssign.getE();


        if(lValue.nameDef.getType() == STRING){
            if(expr.getType() == INT){
                lValue.visit(this,arg);
                output.append(" = ");
                expr.visit(this,arg);
                output.append(".toString()");
            }else{
                lValue.visit(this,arg);
                output.append(" = ");
                expr.visit(this,arg);
            }
        }else if(lValue.nameDef.getType() == PIXEL){
            if(expr.getType() == INT){
                lValue.visit(this,arg);
                output.append(" = ");
                output.append("PixelOps.pack(");
                expr.visit(this,arg);
                output.append(",");
                expr.visit(this,arg);
                output.append(",");
                expr.visit(this,arg);
                output.append(")");
            }
            else{
                lValue.visit(this,arg);
                output.append(" = ");
                expr.visit(this, arg);
            }
        }else if(lValue.nameDef.getType() == IMAGE){
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
                            output.append("ImageOps.copyInto(");
                            expr.visit(this,arg);
                            output.append(", ");
                            lValue.visit(this,arg);
                            output.append(")");
                        }
                        case PIXEL -> {
                            output.append("ImageOps.setAllPixels(");
                            output.append(lValue.nameDef.getIdent().getName());
                            output.append(lValue.nameDef.scopeID);
                            output.append(",");
                            expr.visit(this,arg);
                            output.append(")");
                        }
                        default -> {
                            throw new PLCRuntimeException("unexpected expr type");
                        }
                    }
                }else{//no pixel selector, but has color channel
                    //throw new PLCRuntimeException("no pixel selector, but has color channel");
                }
            }else{
                if(color != null){//Variable type is image with pixel selector and color channel
                    output.append("for (int y = 0; y != ");
                    String name = lValue.getIdent().getName();
                    name += lValue.nameDef.scopeID;

                    output.append(name);
                    output.append(".getHeight(); y++) {\n");
                    output.append("for (int x = 0; x != ");
                    output.append(name);
                    output.append(".getWidth(); x++) {\n");
                    output.append("ImageOps.setRGB(");
                    output.append(name);
                    output.append(",x,y, ");
                    if(color.name() == "red"){
                        output.append("PixelOps.setRed(");
                    }else if(color.name() == "grn"){
                        output.append("PixelOps.setGrn(");
                    }else if(color.name() == "blu"){
                        output.append("PixelOps.setBlu(");
                    }
                    output.append("ImageOps.getRGB(");
                    output.append(name);
                    output.append(", x, y), ");
                    expr.visit(this,arg);

                    output.append("));\n}\n}");

                }else{//Variable type is image with pixel selector, no color channel
                    Dimension dimension = lValue.nameDef.getDimension();
                    String name = lValue.getIdent().getName();
                    name += lValue.nameDef.scopeID;
                    output.append("for (int y = 0; y != ");
                    output.append(name);
                    output.append(".getHeight(); y++) {\n");
                    output.append("for (int x = 0; x != ");
                    output.append(name);
                    output.append(".getWidth(); x++) {\n");
                    output.append("ImageOps.setRGB(");
                    output.append(name);
                    output.append(",x,y");
                    if(expr.getType() == PIXEL){
                        output.append(", ");
                        expr.visit(this,arg);
                    }else{
                        output.append(", ImageOps.getRGB(");
                        output.append(name);
                        output.append(", x, y)");
                    }
                    output.append(");\n}}");
                }
            }
        }
        return null;
    }

    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        output.append("(");
        Expr e0 = binaryExpr.getLeft();
        Expr e1 = binaryExpr.getRight();
        Type t0 = e0.getType();
        Type t1 = e1.getType();
        String op;
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
            default -> {
                 op = "invalid op";
            }

        }



        switch(binaryExpr.getOp()){
            case PLUS -> {
                if(t0==IMAGE){
                    if(t1==IMAGE){
                        output.append("ImageOps.binaryImageImageOp(" +op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else if(t1 == INT){
                        output.append("ImageOps.binaryImageScalarOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");

                    }else if(t1 == PIXEL){
                        output.append("ImageOps.binaryImagePixelOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else{
                        throw new PLCRuntimeException("e0 is a Image and e1 is incompatible");
                    }
                }else if(t0 == PIXEL) {
                    if (t1 == PIXEL) {
                        output.append("ImageOps.binaryPackedPixelPixelOp(" + op + ", ");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else if (t1 == INT) {
                        output.append("ImageOps.binaryPackedPixelIntOp(" + op + ",");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else {
                        throw new PLCRuntimeException("e0 is a Pixel and e1 is incompatible");
                    }
                }
                else{
                    e0.visit(this, arg);
                    output.append(" + ");
                    e1.visit(this, arg);
                }
            }
            case MINUS -> {
                if(t0==IMAGE){
                    if(t1==IMAGE){
                        output.append("ImageOps.binaryImageImageOp(" +op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else if(t1 == INT){
                        output.append("ImageOps.binaryImageScalarOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");

                    }else if(t1 == PIXEL){
                        output.append("ImageOps.binaryImagePixelOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else{
                        throw new PLCRuntimeException("e0 is a Image and e1 is incompatible");
                    }
                }else if(t0 == PIXEL) {
                    if (t1 == PIXEL) {
                        output.append("ImageOps.binaryPackedPixelPixelOp(" + op + ", ");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else if (t1 == INT) {
                        output.append("ImageOps.binaryPackedPixelIntOp(" + op + ",");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else {
                        throw new PLCRuntimeException("e0 is a Pixel and e1 is incompatible");
                    }
                }else {
                    e0.visit(this, arg);
                    output.append(" - ");
                    e1.visit(this, arg);
                }
            }
            case TIMES -> {
                if(t0==IMAGE){
                    if(t1==IMAGE){
                        output.append("ImageOps.binaryImageImageOp(" +op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else if(t1 == INT){
                        output.append("ImageOps.binaryImageScalarOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");

                    }else if(t1 == PIXEL){
                        output.append("ImageOps.binaryImagePixelOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else{
                        throw new PLCRuntimeException("e0 is a Image and e1 is incompatible");
                    }
                }else if(t0 == PIXEL) {
                    if (t1 == PIXEL) {
                        output.append("ImageOps.binaryPackedPixelPixelOp(" + op + ", ");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else if (t1 == INT) {
                        output.append("ImageOps.binaryPackedPixelIntOp(" + op + ",");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else {
                        throw new PLCRuntimeException("e0 is a Pixel and e1 is incompatible");
                    }
                }else{
                    e0.visit(this, arg);
                    output.append(" * ");
                    e1.visit(this, arg);
                }
            }
            case DIV -> {
                if(t0==IMAGE){
                    if(t1==IMAGE){
                        output.append("ImageOps.binaryImageImageOp(" +op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else if(t1 == INT){
                        output.append("ImageOps.binaryImageScalarOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");

                    }else if(t1 == PIXEL){
                        output.append("ImageOps.binaryImagePixelOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else{
                        throw new PLCRuntimeException("e0 is a Image and e1 is incompatible");
                    }
                }else if(t0 == PIXEL) {
                    if (t1 == PIXEL) {
                        output.append("ImageOps.binaryPackedPixelPixelOp(" + op + ", ");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else if (t1 == INT) {
                        output.append("ImageOps.binaryPackedPixelIntOp(" + op + ",");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else {
                        throw new PLCRuntimeException("e0 is a Pixel and e1 is incompatible");
                    }
                }else {
                    e0.visit(this, arg);
                    output.append(" / ");
                    e1.visit(this, arg);
                }
            }
            case MOD -> {
                if(t0==IMAGE){
                    if(t1==IMAGE){
                        output.append("ImageOps.binaryImageImageOp(" +op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else if(t1 == INT){
                        output.append("ImageOps.binaryImageScalarOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");

                    }else if(t1 == PIXEL){
                        output.append("ImageOps.binaryImagePixelOp(" + op+", ");
                        e0.visit(this,arg);
                        output.append(", ");
                        e1.visit(this,arg);
                        output.append(")");
                    }else{
                        throw new PLCRuntimeException("e0 is a Image and e1 is incompatible");
                    }
                }else if(t0 == PIXEL) {
                    if (t1 == PIXEL) {
                        output.append("ImageOps.binaryPackedPixelPixelOp(" + op + ", ");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else if (t1 == INT) {
                        output.append("ImageOps.binaryPackedPixelIntOp(" + op + ",");
                        e0.visit(this, arg);
                        output.append(", ");
                        e1.visit(this, arg);
                        output.append(")");
                    } else {
                        throw new PLCRuntimeException("e0 is a Pixel and e1 is incompatible");
                    }
                }else{
                    e0.visit(this, arg);
                    output.append(" % ");
                    e1.visit(this, arg);
                }

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
                if(t0 == IMAGE && t1 == IMAGE) {
                    output.append("ImageOps.equalsForCodeGen(");
                    e0.visit(this, arg);
                    output.append(", ");
                    e1.visit(this, arg);
                    output.append(")");
                }
                else{
                    output.append("((");
                    e0.visit(this, arg);
                    output.append("==");
                    e1.visit(this, arg);
                    output.append(") ? 1 : 0)");
                }

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

        Expr expr = declaration.getInitializer();
        Type nameType = nameDef.getType();

        Dimension dimension = nameDef.getDimension();
        if(expr != null){
            Type exprType = expr.getType();

            if(exprType == INT && nameType == STRING){
                nameDef.visit(this,arg);
                output.append(" = ");
                expr.visit(this,arg);
                output.append(".toString()");
            }
            else if(nameType == IMAGE){
                if(dimension != null){
                    nameDef.visit(this,arg);
                    output.append(" = ");
                    output.append("ImageOps.makeImage(");
                    dimension.visit(this,arg);
                    output.append(");\n");


                    if(exprType == STRING){
                        output.append(nameDef.getIdent().getName());
                        output.append(nameDef.scopeID);
                        output.append(" = ");
                        output.append("FileURLIO.readImage(");
                        expr.visit(this,arg);
                        output.append(", ");
                        dimension.visit(this,arg);
                        output.append(")");
                    }
                    else if(exprType == IMAGE){
                        output.append(nameDef.getIdent().getName());
                        output.append(nameDef.scopeID);
                        output.append(" = ");
                        output.append("ImageOps.copyAndResize(");
                        expr.visit(this,arg);
                        output.append(",");
                        dimension.visit(this,arg);
                        output.append(")");
                    }
                    else if(exprType == PIXEL){
                        output.append(nameDef.getIdent().getName());
                        output.append(nameDef.scopeID);
                        output.append(" = ");
                        output.append("ImageOps.setAllPixels(");
                        output.append(nameDef.getIdent().getName());
                        output.append(nameDef.scopeID);
                        output.append(" , ");
                        expr.visit(this,arg);
                        output.append(")");

                    }
                    else{

                    }
                }else{//dimension == null
                    if(exprType == STRING){
                        nameDef.visit(this,arg);
                        output.append(" = ");
                        output.append("FileURLIO.readImage(");
                        expr.visit(this,arg);
                        output.append(")");
                    }
                    else if(exprType == IMAGE){
                        nameDef.visit(this,arg);
                        output.append(" = ");
                        output.append("ImageOps.cloneImage(");

                        expr.visit(this,arg);
                        output.append(")");
                    }
                }
            }
            else if(nameType == PIXEL ){
                nameDef.visit(this,arg);
                output.append(" = ");
                expr.visit(this, arg);
                /*if(exprType == PIXEL){
                    nameDef.visit(this,arg);
                    output.append(" = ");
                    expr.visit(this,arg);//expect visitexpandedpixelexpr
                }else if(exprType == INT){
                    nameDef.visit(this,arg);
                    output.append(" = ");
                    expr.visit(this, arg);
                }*/

            }
            else{
                nameDef.visit(this,arg);
                output.append(" = ");
                expr.visit(this,arg);
            }
        }else{
            nameDef.visit(this,arg);
            if(nameType == IMAGE && dimension != null){
                output.append(" = ");
                output.append("ImageOps.makeImage(");
                dimension.visit(this,arg);
                output.append(")");
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

        output.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO;\n" +
                "import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n" +
                "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" +
                "import edu.ufl.cise.plcsp23.runtime.PixelOps;\n");
        output.append("import java.awt.image.BufferedImage;\n");
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
        Expr primary = unaryExprPostfix.getPrimary();
        PixelSelector pixel = unaryExprPostfix.getPixel();
        ColorChannel color = unaryExprPostfix.getColor();
        Boolean p = false;
        Boolean c = false;
        if(pixel != null){
            p = true;
        }
        if(color != null){
            c = true;
        }
        if(primary.getType() == IMAGE){
            if(p && !c){
                output.append("ImageOps.getRGB(");
                primary.visit(this,arg);
                output.append(", ");
                pixel.visit(this,arg);
                output.append(")");
            }else if(p && c){
                if(color.name() == "red"){
                    output.append("PixelOps.red(");
                }else if(color.name() == "grn"){
                    output.append("PixelOps.grn(");
                }else if(color.name() == "blu"){
                    output.append("PixelOps.blu(");
                }
                output.append("ImageOps.getRGB(");
                primary.visit(this,arg);
                output.append(", ");
                pixel.visit(this,arg);
                output.append(")");

                output.append(")");
            }else if(c){
                if(color.name() == "red"){
                    output.append("ImageOps.extractRed(");
                    primary.visit(this,arg);
                    output.append(")");
                }else if(color.name() == "grn"){
                    output.append("ImageOps.extractGrn(");
                    primary.visit(this,arg);
                    output.append(")");
                }else if(color.name() == "blu"){
                    output.append("ImageOps.extractBlu(");
                    primary.visit(this,arg);
                    output.append(")");
                }

            }else{
                primary.visit(this,arg);
            }
        }else if(primary.getType() == PIXEL && c){
            //check if pixel selector is null?
            if(color.name() == "red"){
                output.append("PixelOps.red(");
            }else if(color.name() == "grn"){
                output.append("PixelOps.grn(");
            }else if(color.name() == "blu"){
                output.append("PixelOps.blu(");
            }
            primary.visit(this,arg);
            output.append(")");
        }
        return null;
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
