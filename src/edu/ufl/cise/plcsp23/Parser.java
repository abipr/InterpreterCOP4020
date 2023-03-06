package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.ArrayList;
import java.util.List;
//look for off by 1 error, use debugger
public class Parser implements IParser {
    public AST parse() throws PLCException {
        return program();
    }
    Program program() throws PLCException{
        IToken first = token;
        Type t = Type();
        Ident i = new Ident(token);
        consume();
        ArrayList<NameDef> params = new ArrayList<NameDef>();
        if(token.getKind() == IToken.Kind.LPAREN){
            //consume();
            params = ParamList();

        }
        else{
            new SyntaxException("missing ( before ParamList");
        }

        Block block = block_method();
        return new Program(first, t,i,params,block);
    }
    ArrayList<NameDef> ParamList() throws PLCException{
        consume();//lparen
        ArrayList<NameDef> params = new ArrayList<NameDef>();
        boolean in_param_list = true;
        IToken.Kind k = token.getKind();
        if(k == IToken.Kind.RES_image || k == IToken.Kind.RES_pixel ||k == IToken.Kind.RES_int||k == IToken.Kind.RES_string||k == IToken.Kind.RES_void){
            params.add(name_def());
            while(token.getKind() == IToken.Kind.COMMA){
                consume();
                k = token.getKind();
                if(k == IToken.Kind.RES_image || k == IToken.Kind.RES_pixel ||k == IToken.Kind.RES_int||k == IToken.Kind.RES_string||k == IToken.Kind.RES_void){
                    params.add(name_def());
                }else{
                    throw new SyntaxException("missing nameDef after ,");
                }
            }

        }
        if(token.getKind() == IToken.Kind.RPAREN){
            consume();
        }
        else{
            new SyntaxException("missing ) after ParamList");
        }
        return params;
    }
    NameDef name_def() throws PLCException{
        IToken first = token;
        Type t = Type();
        Dimension d = null;
        if(token.getKind() == IToken.Kind.LSQUARE){
            d = dimension();
        }
        Ident i = new Ident(token);
        consume();
        return new NameDef(first,t,d,i);
    }
    Block block_method() throws PLCException {
        IToken first = token;
        Declaration d = null;
        Statement s = null;
        List<Declaration> decList = new ArrayList<Declaration>();
        List<Statement> statementList = new ArrayList<Statement>();
        boolean in_decList = true;
        boolean in_statementList = true;
        if(token.getKind() == IToken.Kind.LCURLY){
            consume();
            //declaration starts with NameDef which starts with Type
            while(in_decList){
                IToken.Kind k = token.getKind();
                if(k == IToken.Kind.RES_image || k == IToken.Kind.RES_pixel ||k == IToken.Kind.RES_int||k == IToken.Kind.RES_string||k == IToken.Kind.RES_void){
                    d = declaration();
                    if(token.getKind() == IToken.Kind.DOT){
                        consume();
                        decList.add(d);
                    }else{
                        in_decList = false;
                        throw new SyntaxException("missing . after declaration");
                    }
                }else{
                    in_decList = false;
                }
            }
            while(in_statementList){
                IToken.Kind k = token.getKind();
                if(k == IToken.Kind.RES_write || k == IToken.Kind.RES_while || k == IToken.Kind.IDENT){
                    s = statement();

                    if(token.getKind() == IToken.Kind.DOT){
                        consume();
                        statementList.add(s);
                    }else{
                        in_statementList = false;
                        throw new SyntaxException("missing . after statement");
                    }
                }else{
                    in_statementList = false;
                }
            }
            if(token.getKind() == IToken.Kind.RCURLY){
                consume();
                return new Block(first,decList, statementList);
            }

        }else{
            throw new SyntaxException("block missing {");
        }

        return new Block(first,decList, statementList);
    }
    Statement statement() throws PLCException {
        IToken first = token;
        Expr ex;
        switch (token.getKind()) {
            case IDENT -> {
                LValue left = lValue();
                if(token.getKind() == IToken.Kind.ASSIGN){
                    consume();
                    ex = expr();
                    return new AssignmentStatement(first,left,ex);
                }
                else{
                    throw new SyntaxException("missing = in statement");
                }
            }
            case RES_write -> {
                consume();
                ex = expr();
                return new WriteStatement(first,ex);
            }
            case RES_while -> {
                consume();
                ex = expr();
                Block b = block_method();
                if(token.getKind() == IToken.Kind.RCURLY){
                    throw new SyntaxException("rcurly was not consumed");
                }
                return new WhileStatement(first,ex, b);
            }
            default -> {
                throw new SyntaxException("invalid statement");
            }
        }
    }
    Declaration declaration() throws PLCException{
        IToken first = token;
        NameDef name = name_def();
        Expr initializer = null;
        if(token.getKind() == IToken.Kind.ASSIGN){
            consume();
            initializer = expr();
        }
        return new Declaration(first, name, initializer);
    }
    Dimension dimension() throws PLCException{
        IToken first = token;
        if(token.getKind() == IToken.Kind.LSQUARE){
            consume();
            Expr width = expr();
            if(token.getKind() == IToken.Kind.COMMA){
                consume();
                Expr height = expr();
                if(token.getKind() == IToken.Kind.RSQUARE){
                    consume();
                    return new Dimension(first, width, height);
                }else{
                    throw new SyntaxException("missing ]");
                }
            }else{
                throw new SyntaxException("missing comma");
            }
        }else{
            throw new SyntaxException("missing [  check if it was consumed where dimension is called");
        }
    }
    IScanner scanner;
    IToken token, temp;
    Parser(IScanner scanner) throws LexicalException{
        this.scanner = scanner;
        this.token = this.scanner.next();
    }
    void consume() throws PLCException{
        temp = token;
        token = scanner.next();
        if(token == null){
            throw new LexicalException("token is null in consume");
        }
    }
    Expr expr() throws PLCException {
        //check if in predit set of conditional
        IToken.Kind k = token.getKind();
        if(k == IToken.Kind.RES_if){
            return conditional_expr();
        }
        //check if in predict set of or_expr
        if(k == IToken.Kind.BANG || k == IToken.Kind.MINUS || k == IToken.Kind.RES_sin || k == IToken.Kind.RES_cos||k == IToken.Kind.RES_atan || k == IToken.Kind.STRING_LIT
                || k == IToken.Kind.NUM_LIT || k == IToken.Kind.IDENT || k == IToken.Kind.LPAREN||k == IToken.Kind.RES_Z ||k == IToken.Kind.RES_rand || k == IToken.Kind.RES_x
        ||k == IToken.Kind.RES_y|| k == IToken.Kind.RES_a|| k == IToken.Kind.RES_r|| k == IToken.Kind.LSQUARE|| k == IToken.Kind.RES_x_cart|| k == IToken.Kind.RES_y_cart
                || k == IToken.Kind.RES_a_polar|| k == IToken.Kind.RES_r_polar){
            return or_expr();
        }
        else{
            throw new SyntaxException("token null in expr");
        }

    }
    Expr conditional_expr() throws PLCException {
        //call checking tokens from left to right, verify order
        IToken toke = token;
        Expr guard, t,f;
        if(token.getKind() == IToken.Kind.RES_if){
            consume();//if
            guard = expr();//consume happens internally
            if(token.getKind() == IToken.Kind.QUESTION){
                consume();//?
                t = expr();//consume happens internally
                if(token.getKind() == IToken.Kind.QUESTION){
                    consume();//?
                    f = expr();
                    return new ConditionalExpr(toke,guard,t,f);
                }
            }
        }
        throw new SyntaxException("Conditional expr error");
    }
    Expr or_expr() throws PLCException {
        IToken t = token;
        Expr left = and_expr();
        while(token.getKind() == IToken.Kind.BITOR || token.getKind() == IToken.Kind.OR){
            IToken tok= token;
            consume();
            Expr right = and_expr();
            left = new BinaryExpr(t,left,tok.getKind(),right);
        }
        return left;
    }
    Expr and_expr() throws PLCException {
        IToken t = token;
        Expr left = comparison_expr();
        while(token.getKind() == IToken.Kind.BITAND || token.getKind() == IToken.Kind.AND){
            IToken tok= token;
            consume();
            Expr right = comparison_expr();
            left = new BinaryExpr(t,left,tok.getKind(),right);
        }
        return left;
    }
    Expr comparison_expr() throws PLCException {
        IToken t = token;
        Expr left = power_expr();
        while(token.getKind() == IToken.Kind.LT ||token.getKind() == IToken.Kind.GT || token.getKind() == IToken.Kind.EQ || token.getKind() == IToken.Kind.LE|| token.getKind() == IToken.Kind.GE){
            IToken tok= token;
            consume();
            Expr right = power_expr();
            left = new BinaryExpr(t,left,tok.getKind(),right);
        }
        return left;
    }
    Expr power_expr() throws PLCException {
//simalar to unary
        IToken t = token;
        Expr ex = additive_expr();
        IToken.Kind k = token.getKind();

        if(k == IToken.Kind.EXP){
            consume();
            Expr right = power_expr();
            return new BinaryExpr(t,ex,k,right);
        }else{
            return ex;
        }

    }
    Expr additive_expr() throws PLCException {
        IToken t = token;
        Expr left = multiplicative_expr();
        while(token.getKind() == IToken.Kind.PLUS || token.getKind() == IToken.Kind.MINUS){
            IToken tok= token;
            consume();
            Expr right = multiplicative_expr();
            left = new BinaryExpr(t,left,tok.getKind(),right);
        }
        return left;
    }
    Expr multiplicative_expr() throws PLCException {
        IToken t = token;
        Expr left = unary_expr();
        while(token.getKind() == IToken.Kind.TIMES || token.getKind() == IToken.Kind.DIV || token.getKind() == IToken.Kind.MOD){
            IToken tok= token;
            consume();
            Expr right = unary_expr();
            left = new BinaryExpr(t,left,tok.getKind(),right);
        }
        return left;
    }
    Expr unary_expr() throws PLCException {
        IToken.Kind k = token.getKind();
        IToken t = token;
        if(k == IToken.Kind.BANG ||k == IToken.Kind.MINUS ||k == IToken.Kind.RES_sin ||k == IToken.Kind.RES_cos ||k == IToken.Kind.RES_atan){
            consume();
            Expr right = unary_expr();
            return new UnaryExpr(t,k,right);
        }
        return unary_expr_postfix();
    }
    Expr unary_expr_postfix() throws PLCException {
        IToken first = token;
        Expr primary = primary_expr();
        PixelSelector p = null;
        if(token.getKind() == IToken.Kind.LSQUARE){
            //consume();
            p = Pixel_Selector();
        }
        ColorChannel color = null;
        if(token.getKind() == IToken.Kind.COLON){
            color = ChannelSelector();
        }
        if(p == null && color == null){
            return primary;
        }
        return new UnaryExprPostfix(first, primary, p, color);
    }
    LValue lValue() throws PLCException {
        IToken first = token;
        Ident i = new Ident(token);
        consume(); //verify
        PixelSelector p = null;
        if(token.getKind() == IToken.Kind.LSQUARE){
            //consume();
            p = Pixel_Selector();
        }
        ColorChannel color = null;
        if(token.getKind() == IToken.Kind.COLON){
            //consume();
            color = ChannelSelector();
        }
        return new LValue(first, i, p, color);
    }
    ColorChannel ChannelSelector() throws PLCException{
        consume();
        switch(token.getKind()){
            case RES_red -> {
                return ColorChannel.red;
            }
            case RES_grn -> {
                return ColorChannel.grn;
            }
            case RES_blu -> {
                return ColorChannel.blu;
            }
            default -> {
                throw new SyntaxException("expected ColorChannel");
            }
        }
    }
    Expr primary_expr() throws PLCException {
        switch(token.getKind()){
            case STRING_LIT -> {
                consume();
                return new StringLitExpr(temp);
            }
            case NUM_LIT -> {
                consume();
                return new NumLitExpr(temp);
            }
            case IDENT -> {
                consume();
                return new IdentExpr(temp);
            }
            case RES_rand -> {
                consume();
                return new RandomExpr(temp);
            }
            case RES_Z -> {
                consume();
                return new ZExpr(temp);
            }
            //handle lparen inside of production for selector and expanded
            case LPAREN -> {
                consume();
                Expr e = expr();
                if(token.getKind() == IToken.Kind.RPAREN){
                    consume();
                    return e;
                }
                throw new SyntaxException("Paren is illegal");
            }
            case RES_x, RES_y, RES_a, RES_r-> {
                consume();
                return new PredeclaredVarExpr(temp);
            }
            case LSQUARE -> {
                //consume();
                return ExpandedPixel();
            }
            case RES_x_cart, RES_y_cart, RES_a_polar,RES_r_polar -> {
                consume();
                IToken first = temp;
                PixelSelector p = Pixel_Selector();
                return new PixelFuncExpr(first, first.getKind(),p);
            }
            default -> {
                throw new SyntaxException("Primary expr is illegal");
            }
        }
    }
    Expr ExpandedPixel() throws PLCException{
        IToken first = token;
        consume();
        Expr e1 = expr();
        if(token.getKind() == IToken.Kind.COMMA){
            consume();
        }else{
            throw new SyntaxException("Missing comma");
        }
        Expr e2 = expr();
        if(token.getKind() == IToken.Kind.COMMA){
            consume();
        }else{
            throw new SyntaxException("Missing comma");
        }
        Expr e3 = expr();
        if(token.getKind() == IToken.Kind.RSQUARE){
            consume();
        }else{
            throw new SyntaxException("Missing ]");
        }
        return new ExpandedPixelExpr(first, e1, e2, e3);
    }
    PixelSelector Pixel_Selector() throws PLCException{
        IToken first = token;
        consume();
        Expr e1 = expr();
        if(token.getKind() == IToken.Kind.COMMA){
            consume();
        }else{
            throw new SyntaxException("Missing comma");
        }
        Expr e2 = expr();
        if(token.getKind() == IToken.Kind.RSQUARE){
            consume();
        }else{
            throw new SyntaxException("Missing ]");
        }
        return new PixelSelector(first, e1, e2);
    }
    Type Type() throws PLCException{
        IToken.Kind k = token.getKind();
        if(k == IToken.Kind.RES_image || k == IToken.Kind.RES_pixel ||k == IToken.Kind.RES_int||k == IToken.Kind.RES_string||k == IToken.Kind.RES_void){
            consume();
            return Type.getType(temp);
        }else{
            throw new SyntaxException("invalid type");
        }
    }
    /*
    from grammer, 1 method for each rule
    check token type, if matches grammer consume, else error
    return AST else error


    match(c);
    //where c is a Token
    match(c){
        if ( current token == c )
        {  get next token from scanner;}
        else error
    }
    Top-down parsing
 Start with start  symbol in language
 At each step, replace a nonterminal symbol with one of its productions,
trying to match prefixes in the sentence
 Remove matching prefixes
 If the resulting string is empty, then the sentence is in the grammar

Feb1,6
if it is too hard, it is probably not LL(1)
be sus if there are more than 3 if else or while blocks in each method
*/
}
