package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
//import edu.ufl.cise.plcsp23.ast.IdentExpr;
//import edu.ufl.cise.plcsp23.ast.NumLitExpr;
//import edu.ufl.cise.plcsp23.ast.StringLitExpr;

public class Parser implements IParser {
    public AST parse() throws PLCException {
        return expr();
    }
    IScanner scanner;
    IToken token, temp;
    Parser(IScanner scanner){
        this.scanner = scanner;
    }
    void consume() throws PLCException{
        temp = token;
        token = scanner.next();
        if(token == null){
            throw new LexicalException("token is null in consume");
        }
    }
    Expr expr() throws PLCException {
        if(temp == null){
            token = scanner.next();
        }
        if(token != null){
            IToken.Kind k = token.getKind();
            if(k == IToken.Kind.RES_if){
                return conditional_expr();
            }
            if(k == IToken.Kind.STRING_LIT){
                consume();
                return new StringLitExpr(temp);
            }
            if(k == IToken.Kind.IDENT){
                consume();
                return new IdentExpr(temp);
            }
            if(k == IToken.Kind.NUM_LIT){
                consume();
                return new NumLitExpr(temp);
            }
            if(k == IToken.Kind.RES_rand){
                consume();
                return new RandomExpr(temp);
            }
            if(k == IToken.Kind.RES_Z){
                consume();
                return new ZExpr(temp);
            }
            if(k == IToken.Kind.LPAREN){
                primary_expr();
            }
            if(k == IToken.Kind.BANG ||k == IToken.Kind.MINUS ||k == IToken.Kind.RES_sin ||k == IToken.Kind.RES_cos ||k == IToken.Kind.RES_atan){
                unary_expr();
            }
            else{
                return or_expr();
            }
        }
        throw new SyntaxException("token null in expr");
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
        Expr ex = additive_expr();
        IToken.Kind k = IToken.Kind.ERROR;
        if(token != null){
            k = token.getKind();
        }

        IToken t = token;
        if(k == IToken.Kind.EXP){
            consume();
            Expr right = additive_expr();
            return new UnaryExpr(t,k,right);
        }else{
            Expr p = primary_expr();

            throw new SyntaxException("power expr");
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
            default -> {
                throw new SyntaxException("Primary expr is illegal");
            }
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
