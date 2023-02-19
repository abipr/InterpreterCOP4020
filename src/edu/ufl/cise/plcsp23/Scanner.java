package edu.ufl.cise.plcsp23;

import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;

public class Scanner implements IScanner {

    int line, column, pos;
    char ch;
    final String input;

    StringBuilder tokenString;
    //look up string builder documentation
    final char[] inputChars;
    enum State{START,IN_IDENT,IN_NUM_LIT,HAVE_EQ,IN_STRING_LIT,
        IN_LT,IN_GT,IN_AND,IN_TIMES,IN_OR,IN_ESCAPE,IN_COMMENT}
    State state;
    Scanner(String s) {
    //track line and column
        line = 1;
        column = 1;
        pos = 0;
        input = s;
        inputChars = Arrays.copyOf(input.toCharArray(), input.length()+1);
        ch = inputChars[pos];
    }
    private static HashMap<String, IToken.Kind > reservedWords;
    static {
        reservedWords = new HashMap<String, IToken.Kind>();
        reservedWords.put("if", IToken.Kind.RES_if);
        reservedWords.put("int",IToken.Kind.RES_int);
        reservedWords.put("image",IToken.Kind.RES_image);
        reservedWords.put("pixel",IToken.Kind.RES_pixel);
        reservedWords.put("string",IToken.Kind.RES_string);
        reservedWords.put("void",IToken.Kind.RES_void);
        reservedWords.put("nil",IToken.Kind.RES_nil);
        reservedWords.put("load",IToken.Kind.RES_load);
        reservedWords.put("display",IToken.Kind.RES_display);
        reservedWords.put("write",IToken.Kind.RES_write);
        reservedWords.put("rand",IToken.Kind.RES_rand);
        reservedWords.put("sin",IToken.Kind.RES_sin);
        reservedWords.put("cos",IToken.Kind.RES_cos);
        reservedWords.put("atan",IToken.Kind.RES_atan);
        reservedWords.put("while",IToken.Kind.RES_while);
        reservedWords.put("x_cart",IToken.Kind.RES_x_cart);
        reservedWords.put("y_cart",IToken.Kind.RES_y_cart);
        reservedWords.put("x",IToken.Kind.RES_x);
        reservedWords.put("y",IToken.Kind.RES_y);
        reservedWords.put("a",IToken.Kind.RES_a);
        reservedWords.put("r",IToken.Kind.RES_r);
        reservedWords.put("X",IToken.Kind.RES_X);
        reservedWords.put("Y",IToken.Kind.RES_Y);
        reservedWords.put("Z",IToken.Kind.RES_Z);
        reservedWords.put("a_polar",IToken.Kind.RES_a_polar);
        reservedWords.put("r_polar",IToken.Kind.RES_r_polar);
    }
    public void nextChar(){
        ch = inputChars[++pos];
        ++column;
    }
    private void error(String message) throws LexicalException{
        throw new LexicalException("Error at line "+line+" column "+column+": "+message);
    }
    private boolean isDigit(int ch){
        return '0' <= ch && ch <= '9';
    }
    private boolean isLetter(int ch){
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
    }
    private boolean isIdentStart(int ch){
        return isLetter(ch) || (ch == '_');
    }
    private boolean isQuote(int ch){
        return (ch == '"');
    }
    private boolean isInputChar(int ch) {//visible and control ascii but not LF or CR
        if(ch == 10 || ch == 13){
            return false;
        }else{
            return true;
        }
    }

    private boolean isEscape(int ch) {//follow up
        //go back in slack, dr. sanders made a helpful post
        //'\b','\r','\t','\"','\\','\n'
        return false;
    }
    //" \t \r\n \f \n"
    private boolean isWhiteSpace(int ch) {//follow up, look at ASCII values
        //9 is TAB, 10 is LF
        if(ch == 10 || ch == 9 || ch == 13 || ch == 12 || (char)ch == ' '){
            return true;
        }
        else{
            return false;
        }
    }
    private boolean isStringChar(char ch) {
        //'\b','\r','\t','\"','\\','\n'
        return ch == '\\' || isInputChar(ch);
    }
    //improve by using ERROR state
    private IToken scanToken() throws LexicalException{
        tokenString = new StringBuilder();
        state = State.START;
        int tokenStart = -1;
        while(true){
            switch(state){
                case START -> {
                    tokenStart = column;
                    switch(ch){
                        case 0 -> {
                            return new Token(IToken.Kind.EOF, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        case '\r','\t',' ', '\f' -> nextChar();
                        case '\n' -> {
                            line += 1;
                            column = 0;
                            nextChar();
                        }
                        case '~' -> {
                            state = State.IN_COMMENT;
                        }
                        //operators
                        case '=' -> {
                            //set state to has equal
                            state = State.HAVE_EQ;
                            tokenString.append(ch);
                            nextChar();
                        }
                        case '0' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new NumLitToken(IToken.Kind.NUM_LIT, tokenString.toString(), line, tokenStart, inputChars, 0);
                        }
                        case '1','2','3','4','5','6','7','8','9' -> {//ch is a nonzero digit
                            state = State.IN_NUM_LIT;
                            tokenString.append(ch);
                            nextChar();
                        }
                        //      DOT, //  .
                        case '.' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.DOT, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		COMMA, // ,
                        case ',' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.COMMA, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		QUESTION, // ?
                        case '?' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.QUESTION, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		COLON, // :
                        case ':' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.COLON, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		LPAREN, // (
                        case '(' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.LPAREN, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		RPAREN, // )
                        case ')' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.RPAREN, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        case '<' -> {
                            state = State.IN_LT;
                            tokenString.append(ch);
                            nextChar();
                        }
                        case '>' -> {
                            state = State.IN_GT;
                            tokenString.append(ch);
                            nextChar();
                        }
                        //		LSQUARE, // [
                        case '[' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.LSQUARE, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		RSQUARE, // ]
                        case ']' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.RSQUARE, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		LCURLY, // {
                        case '{' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.LCURLY, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		RCURLY, // }
                        case '}' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.RCURLY, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		BANG, // !
                        case '!' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.BANG, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        case '&' -> {
                            state = State.IN_AND;
                            tokenString.append(ch);
                            nextChar();
                        }
                        case '|' -> {
                            state = State.IN_OR;
                            tokenString.append(ch);
                            nextChar();
                        }
                        //		PLUS, // +
                        case '+' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.PLUS, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		MINUS, // -
                        case '-' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.MINUS, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        case '*' -> {
                            state = State.IN_TIMES;
                            tokenString.append(ch);
                            nextChar();
                        }
                        //		DIV, // /
                        case '/' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.DIV, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        //		MOD, // %
                        case '%' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.MOD, tokenString.toString(), line, tokenStart, inputChars);
                        }

                        default -> {
                            //for long cases check here
                            //ascii, numbers etc
                            //check if ident start
                            if(isIdentStart(ch)){
                                state = State.IN_IDENT;
                                tokenString.append(ch);
                                nextChar();
                            } else if (isQuote(ch)) {
                                state = State.IN_STRING_LIT;
                                tokenString.append(ch);
                                nextChar();
                            }
                            else{
                                error(ch + " not implemented yet");
                            }
                        }
                    }
                }
                case HAVE_EQ -> {
                    //		ASSIGN, // =
                    if (ch != '='){
                        return new Token(IToken.Kind.ASSIGN,tokenString.toString(), line, tokenStart, inputChars);
                    }
                    //		EQ, // ==
                    else {
                        tokenString.append(ch);
                        nextChar();
                        return new Token(IToken.Kind.EQ,tokenString.toString(), line, tokenStart, inputChars);
                    }
                }
                case IN_NUM_LIT -> {
                    //check for more digits
                    //add the digit to the string
                    //when not a digit return
                    if(isDigit(ch)){
                        tokenString.append(ch);
                        nextChar();
                    }
                    else{
                        int value;
                        try{
                            value = Integer.parseInt(tokenString.toString());
                            return new NumLitToken(IToken.Kind.NUM_LIT, tokenString.toString(), line, tokenStart, inputChars, value);
                        }
                        catch(Exception e){
                            throw new LexicalException("IntLit too big") ;
                        }
                    }

                }
                case IN_COMMENT -> {
                    if(ch != 10){
                        nextChar();
                    }
                    else{
                        state = State.START;
                    }
                }
                case IN_IDENT -> {
                    if(isDigit(ch) || isIdentStart(ch)){
                        tokenString.append(ch);
                        nextChar();
                    }
                    else{
                        IToken.Kind kind = reservedWords.get(tokenString.toString());
                        if(kind == null) {
                            kind = IToken.Kind.IDENT;
                        }
                        return new Token(kind,tokenString.toString(), line, tokenStart, inputChars);
                    }
                }
                case IN_STRING_LIT -> {
                    if (isQuote(ch)) {
                        tokenString.append(ch);
                        nextChar();//the absence of this was the bug for stringLiterals1
                        return new StringLitToken(IToken.Kind.STRING_LIT,tokenString.toString(), line, tokenStart, inputChars);
                    }
                    else if ((ch == '\\')){
                        state = State.IN_ESCAPE;
                        nextChar();
                    }
                    else if (isStringChar(ch)) {
                        tokenString.append(ch);
                        nextChar();
                    }
                    else{
                        //use if statement to figure out which case was violated
                        throw new LexicalException("unexpected ch'" + ch + "' in string:"+tokenString.toString());
                    }
                }
                case IN_ESCAPE -> {
                    if (ch =='b' || ch == 't' || ch == 'r' || ch == '"' || ch == '\\' || ch == 'n'){//expand for other esc sequences
                        tokenString.append('\\');
                        tokenString.append(ch);
                        nextChar();
                        state = State.IN_STRING_LIT;
                    }

                    else{
                        error("invalid escape");
                    }
                }
                case IN_LT -> {
                    //		EXCHANGE, // <->
                    if (ch == '-') {
                        tokenString.append(ch);
                        nextChar();
                        if (ch == '>') {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.EXCHANGE, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        else{
                            error("incomplete exchange");
                        }
                    }
                    //		LE, // <=
                    else if(ch == '='){
                        tokenString.append(ch);
                        nextChar();
                        return new Token(IToken.Kind.LE,tokenString.toString(), line, tokenStart, inputChars);
                    }
                    //		LT, // <
                    else{
                        return new Token(IToken.Kind.LT,tokenString.toString(), line, tokenStart, inputChars);
                    }
                }
                case IN_GT -> {
                    //		GE, // >=
                    if(ch == '='){
                        tokenString.append(ch);
                        nextChar();
                        return new Token(IToken.Kind.GE,tokenString.toString(), line, tokenStart, inputChars);
                    }
                    //		GT, // >
                    else{
                        return new Token(IToken.Kind.GT,tokenString.toString(), line, tokenStart, inputChars);
                    }
                }
                case IN_AND -> {
                    //		AND, // &&
                    if(ch == '&'){
                        tokenString.append(ch);
                        nextChar();
                        return new Token(IToken.Kind.AND,tokenString.toString(), line, tokenStart, inputChars);
                    }
                    //		BITAND, // &
                    else{
                        return new Token(IToken.Kind.BITAND,tokenString.toString(), line, tokenStart, inputChars);
                    }
                }
                case IN_TIMES -> {
                    //		EXP, // **
                    if(ch == '*'){
                        tokenString.append(ch);
                        nextChar();
                        return new Token(IToken.Kind.EXP,tokenString.toString(), line, tokenStart, inputChars);
                    }
                    //		TIMES, // *
                    else{
                        return new Token(IToken.Kind.TIMES,tokenString.toString(), line, tokenStart, inputChars);
                    }
                }
                case IN_OR -> {
                    //		OR, // ||
                    if(ch == '|'){
                        tokenString.append(ch);
                        nextChar();
                        return new Token(IToken.Kind.OR,tokenString.toString(), line, tokenStart, inputChars);
                    }
                    //		BITOR, // |
                    else{
                        return new Token(IToken.Kind.BITOR,tokenString.toString(), line, tokenStart, inputChars);
                    }
                }
                default -> {
                    throw new UnsupportedOperationException("Bug in Scanner");
                }
            }
        }
    }




    public IToken next() throws LexicalException{
        //read in characters until final state
        //review the powerpoint to get started
        return scanToken();
    }
}
//write getValue in StringLitToken

//isEscape, isWhiteSpace
//white space strategy
//comment strategy
//input chars
//write out IN_LT etc. while waiting