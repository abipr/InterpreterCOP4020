package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

public class Parser implements IParser {
    public AST parse() throws PLCException {
        return expr();
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
                || k == IToken.Kind.NUM_LIT || k == IToken.Kind.IDENT || k == IToken.Kind.LPAREN||k == IToken.Kind.RES_Z ||k == IToken.Kind.RES_rand){
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
        return primary_expr();
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
            case LPAREN -> {
                consume();
                Expr e = expr();
                if(token.getKind() == IToken.Kind.RPAREN){
                    consume();
                    return e;
                }
                throw new SyntaxException("Paren is illegal");
            }
            /*case RES_x -> {
                consume();
                return new ;//what is the ast type to return for x,y,a,r?
            }*/
            default -> {
                throw new SyntaxException("Primary expr is illegal");
            }
        }
    }/*
    Expr Type() throws PLCException{
        switch(token.getKind()){
            case RES_image -> {
                consume();
                return Type.IMAGE;
            }
    }*/

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
