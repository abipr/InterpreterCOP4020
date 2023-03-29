package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

//names scopes and bindings //leblanc cook table each scope has unique identification
//hashmap of linked lists for symbol table //store stack where each entry is the id of the specific scope

public class Visitor implements ASTVisitor{
    private void check(boolean condition, String message)
            throws TypeCheckException {
        if (! condition) { throw new TypeCheckException(message); }
    }

    public class SymbolTable {
        public class node{
            NameDef def;
            Integer scopeID;
            node(NameDef nameDef, Integer ID){
                def = nameDef;
                scopeID = ID;
            }
        }
        Stack<Integer> scope = new Stack<Integer>();
        Integer currentScope = 0;
        HashMap<String, List<node>> entries = new HashMap<>();
        //returns true if name successfully inserted in symbol table, false if already present
        public boolean insert(String name, NameDef def){
            if(entries.get(name)==null){
                entries.put(name,new ArrayList<node>());
            }
            if(notSameScope(name)){
                node n = new node(def,scope.peek());
                entries.get(name).add(n);
                return true;
            }
            else{
                return false;
            }

            //return (entries.putIfAbsent(name,def) == null);
        }
        //returns NameDef if present, or null if name not declared.
        public NameDef lookup(String name) {
            List<node> list = entries.get(name);
            if(list != null){
                for(Integer i=scope.size()-1;i>=0;i--){
                    for (node n:list) {
                        if (scope.get(i) == n.scopeID) {
                            return n.def;
                        }
                    }
                }
            }
            return null;
        }
        //returns true if not the same scope
        public boolean notSameScope(String name){
            List<node> list = entries.get(name);
            if(list != null){
                for (node n:list) {
                    if(scope.peek()== n.scopeID){
                        return false;
                    }
                }
            }
            return true;
        }

        void enterScope(){
            scope.push(currentScope++);//puts 0 then implements
        }
        void leaveScope(){
            scope.pop();
        }
    }
    SymbolTable symbolTable = new SymbolTable();
    Type programType;
    boolean assignmentCompatible(Type lhsType, Type rhsType){
        boolean bool = false;
        switch(lhsType){
            case IMAGE -> {
                bool = (rhsType == Type.IMAGE || rhsType == Type.PIXEL || rhsType == Type.STRING);
            }
            case PIXEL,INT -> {
                bool = (rhsType == Type.PIXEL || rhsType == Type.INT);
            }
            case STRING -> {
                bool = (rhsType == Type.IMAGE || rhsType == Type.PIXEL || rhsType == Type.STRING || rhsType == Type.INT);
            }
        }
        return bool;
    }
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        //LValue is properly typed
        LValue lvalue = statementAssign.getLv();
        Type l = (Type)lvalue.visit(this,arg);
        //Expr is properly typed
        Expr expr = statementAssign.getE();
        expr.visit(this,arg);
        //LValue.type is assignment compatible with Expr.type
        Type e = expr.getType();
        //Type l = lvalue.getIdent().getDef().getType();
        System.out.println(statementAssign.getFirstToken().getSourceLocation());
        check(assignmentCompatible(l,e),"not assignment compatible");
        return null;
    }
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        Expr e1, e2;
        Type resultType = null;
        Type t1, t2;
        e1 = binaryExpr.getLeft();//gotten child of binexpr
        e1.visit(this, arg);
        e2 = binaryExpr.getRight();
        e2.visit(this, arg);
        t1 = e1.getType();
        t2 = e2.getType();
        //check left and right
        //if they are correct set the type
        switch(binaryExpr.getOp()){
            case BITOR,BITAND ->{// | &
                if(t1 == Type.PIXEL && t2 == Type.PIXEL){
                    resultType = Type.PIXEL;
                }
            }
            case OR,AND, LT, GT, LE, GE -> {// || && < > <= >=
                if(t1 == Type.INT && t2 == Type.INT){
                    resultType = Type.INT;
                }
            }
            case EQ -> {// ==
                if(t1 == Type.INT && t2 == Type.INT){
                    resultType = Type.INT;
                }
                else if(t1 == Type.PIXEL && t2 == Type.PIXEL){
                    resultType = Type.INT;
                }
                else if(t1 == Type.IMAGE && t2 == Type.IMAGE){
                    resultType = Type.INT;
                }
                else if(t1 == Type.STRING && t2 == Type.STRING){
                    resultType = Type.INT;
                }
            }
            case EXP -> {//**
                if(t1 == Type.INT && t2 == Type.INT){
                    resultType = Type.INT;
                }
                if(t1 == Type.PIXEL && t2 == Type.INT){
                    resultType = Type.PIXEL;
                }
            }
            case PLUS -> {// +
                if(t1 == Type.INT && t2 == Type.INT){
                    resultType = Type.INT;
                }
                else if(t1 == Type.PIXEL && t2 == Type.PIXEL){
                    resultType = Type.PIXEL;
                }
                else if(t1 == Type.IMAGE && t2 == Type.IMAGE){
                    resultType = Type.IMAGE;
                }
                else if(t1 == Type.STRING && t2 == Type.STRING){
                    resultType = Type.STRING;
                }
            }
            case MINUS -> {// -
                if(t1 == Type.INT && t2 == Type.INT){
                    resultType = Type.INT;
                }
                else if(t1 == Type.PIXEL && t2 == Type.PIXEL){
                    resultType = Type.PIXEL;
                }
                else if(t1 == Type.IMAGE && t2 == Type.IMAGE){
                    resultType = Type.IMAGE;
                }
            }
            case TIMES,DIV,MOD -> {// * / %
                if(t1 == Type.INT && t2 == Type.INT){
                    resultType = Type.INT;
                }
                else if(t1 == Type.PIXEL && t2 == Type.PIXEL){
                    resultType = Type.PIXEL;
                }
                else if(t1 == Type.IMAGE && t2 == Type.IMAGE){
                    resultType = Type.IMAGE;
                }
                else if(t1 == Type.PIXEL && t2 == Type.INT){
                    resultType = Type.PIXEL;
                }
                else if(t1 == Type.IMAGE && t2 == Type.INT){
                    resultType = Type.IMAGE;
                }
            }
        }
        binaryExpr.setType(resultType);

        return resultType;
    }
    public Object visitBlock(Block block, Object arg) throws PLCException {
        //DecList is properly typed
        for (Declaration dec:block.getDecList()) {
            dec.visit(this,arg);
        }
        //StatementList is properly typed
        for (Statement statement:block.getStatementList()) {
            statement.visit(this,arg);
        }
        return null;
    }
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Expr e0,e1,e2;
        e0 = conditionalExpr.getGuard();
        e1 = conditionalExpr.getTrueCase();
        e2 = conditionalExpr.getFalseCase();
        //• Expr0, Expr1, and Expr2 are properly typed
        e0.visit(this,arg);
        e1.visit(this,arg);
        e2.visit(this,arg);

        //• Expr0.type == int
        check((e0.getType() == Type.INT),"guard incorrectly typed in conditional expression");

        //• Expr1.type == Expr2.type
        check((e1.getType() == e2.getType()),"true case and false case are not of the same type in conditional expression");

        //ConditionalExpr.type ← Expr1.type
        conditionalExpr.setType(e1.getType());
        return null;
    }
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        //If present, Expr.type must be properly typed and assignment compatible with NameDef.type.
        Expr expr = declaration.getInitializer();
        NameDef nameDef = declaration.getNameDef();
        if(expr != null){
            expr.visit(this,arg);
            check(assignmentCompatible(nameDef.getType(),expr.getType()),"declaration not type compatible");
        }
        //NameDef is properly Typed
        nameDef.visit(this,arg);

        //• If NameDef.Type == image then
        //either it has an initializer (Expr != null)
        //or NameDef.dimension != null, or both.
        if(nameDef.getType() == Type.IMAGE){
            check((expr!= null || nameDef.getDimension() !=null),"nameDef type == image, but initializer and dimension are both null");

        }

        // It is not allowed to refer to the name being defined.
        //should be covered because of ordering

        return null;
    }
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        Expr width = dimension.getWidth();
        Expr height = dimension.getHeight();

        //expr0 and expr1 are properly typed
        width.visit(this, arg);
        height.visit(this,arg);
        Type t0 = width.getType();
        Type t1 = height.getType();
        //expr0 type == int
        //expr1 type == int
        check((t0 == Type.INT && t1 == Type.INT),"incorrect types in dimension");
        return null;
    }
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        Expr red = expandedPixelExpr.getRedExpr();
        Expr grn = expandedPixelExpr.getGrnExpr();
        Expr blu = expandedPixelExpr.getBluExpr();

        red.visit(this,arg);
        grn.visit(this,arg);
        blu.visit(this,arg);

        check((red.getType()==Type.INT && grn.getType()==Type.INT && blu.getType()==Type.INT),"ExpandedPixelExpr type error");
        expandedPixelExpr.setType(Type.PIXEL);
        return null;
    }

    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        //Constraints:IdentExpr.name has been defined and is visible in this scope
        NameDef nameDef = symbolTable.lookup(identExpr.getName());
        check((nameDef != null),"undefined variable: " + identExpr.getName());
        //IdentExpr.type ← NameDef.type
        identExpr.setType(nameDef.getType());
        return null;
    }

    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        //constraint:
        //• Ident has been declared and is visible
        //in this scope.
        String name = lValue.getIdent().getName();
        NameDef nameDef = symbolTable.lookup(name);
        check((nameDef != null),"undefined variable: " + name);
        boolean pixel,channel;
        pixel = (lValue.getPixelSelector() != null);//yes=true
        channel = (lValue.getColor() != null);//yes=true
        //See table
        switch(nameDef.getType()){
            case INT -> {
                //pixelSelector null
                if(!pixel && !channel){
                    return Type.INT;
                }else{
                    throw new TypeCheckException("lvalue has a pixel or channel selector");
                }
            }
            case STRING -> {
                //pixelSelector null
                if(!pixel && !channel){
                    return Type.STRING;
                }else{
                    throw new TypeCheckException("lvalue has a pixel or channel selector");
                }
            }
            case PIXEL -> {
                //pixelSelector null
                if(!pixel && !channel){
                    return Type.PIXEL;
                }
                else if (!pixel && channel) {
                    return Type.INT;
                } else{
                    throw new TypeCheckException("lvalue has a pixel or channel selector");
                }
            }
            case IMAGE -> {
                if(!pixel){
                    return Type.IMAGE;
                }else{
                    if(!channel){
                        return Type.PIXEL;
                    }else{
                        return Type.INT;
                    }
                }
            }
        }
        return null;
    }

    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        //If (Dimension != ε) Type == image
        //If (Dimension != ε) Dimension is properly typed
        Dimension dim = nameDef.getDimension();
        if(dim != null){
            check((nameDef.getType()== Type.IMAGE),"expected type IMAGE");
            dim.visit(this,arg);
        }
        String identName = nameDef.getIdent().getName();
        //Type != void
        if(nameDef.getType() != Type.VOID){
            //Ident.name has not been previously declared in this scope.
            //Insert (name, NameDef) into symbol table
            //if identName has been previously declared then an error will be thrown
            check(symbolTable.insert(identName,nameDef),identName+" has been previously declared in this scope");
        }else{
            throw new TypeCheckException("Type is void");
        }
        return null;
    }

    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        pixelFuncExpr.getSelector().visit(this,arg);
        pixelFuncExpr.setType(Type.INT);
        return null;
    }
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        Expr x = pixelSelector.getX();
        Expr y = pixelSelector.getY();
        x.visit(this,arg);
        y.visit(this,arg);
        check((x.getType()==Type.INT && y.getType()==Type.INT), "pixelSelector type error");
        return null;
    }
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }
    public Object visitProgram(Program program, Object arg) throws PLCException {
        //enterScope
        symbolTable.enterScope();
        programType = program.getType();
        List<NameDef> nameDefList;
        //All NameDef objects are properly typed
        nameDefList = program.getParamList();//gets list of NameDef Objects
        for (NameDef n:nameDefList) {//visits each object in list
            n.visit(this,arg);
        }
        //Block is properly typed
        program.getBlock().visit(this,arg);
        //save program type for use in visitReturnStatement

        //leaveScope
        symbolTable.leaveScope();
        return null;
    }
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        randomExpr.setType(Type.INT);
        return Type.INT;
    }
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        Expr expr = returnStatement.getE();
        //expr is properly typed
        expr.visit(this,arg);
        Type type = expr.getType();
        //expr type is assignment compatible with program.type
        check(assignmentCompatible(programType,type),"not assignment compatible");
        return null;
    }
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        Expr expr = unaryExpr.getE();
        expr.visit(this,arg);
        IToken.Kind op = unaryExpr.getOp();
        Type exprType = expr.getType();
        switch(op){
            case BANG -> {
                if(exprType == Type.INT){
                    unaryExpr.setType(Type.INT);
                }else if(exprType == Type.PIXEL){
                    unaryExpr.setType(Type.PIXEL);
                }else{
                    throw new TypeCheckException("unexpected expr Type for ! operator");
                }
            }
            case MINUS,RES_cos,RES_sin,RES_atan -> {
                if(exprType == Type.INT){
                    unaryExpr.setType(Type.INT);
                }else{
                    throw new TypeCheckException("unexpected expr Type");
                }
            }
            default -> {
                throw new TypeCheckException("unexpected unary operator");
            }
        }
        return null;
    }
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        Expr primary = unaryExprPostfix.getPrimary();
        PixelSelector pixel = unaryExprPostfix.getPixel();
        ColorChannel color = unaryExprPostfix.getColor();
        primary.visit(this,arg);
        if(pixel != null){
            unaryExprPostfix.getPixel().visit(this,arg);
            if(primary.getType() == Type.IMAGE){
                if(color != null){
                    unaryExprPostfix.setType(Type.INT);
                }
                else{
                    unaryExprPostfix.setType(Type.PIXEL);
                }
            }else{
                throw new TypeCheckException("unexpected primaryExpr type");
            }
        }else{//pixelSelector is not present
            if(color != null){
                if(primary.getType() == Type.PIXEL){
                    unaryExprPostfix.setType(Type.INT);
                }
                else if(primary.getType() == Type.IMAGE){
                    unaryExprPostfix.setType(Type.IMAGE);
                }
                else{
                    throw new TypeCheckException("unexpected primaryExpr type");
                }
            }
            else{//color and pixel selector are not present
                throw new TypeCheckException("missing channelSelector and PixelSelector");
            }
        }


        return null;
    }
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        Expr expr = whileStatement.getGuard();
        Block block = whileStatement.getBlock();
        //expr is properly typed
        expr.visit(this,arg);
        //expr.type == int
        check((expr.getType()==Type.INT),"whileStatement type error");
        symbolTable.enterScope();
        block.visit(this,arg);
        symbolTable.leaveScope();
        return null;
    }
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        statementWrite.getE().visit(this,arg);
        return null;
    }
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        return Type.INT;
    }
}
