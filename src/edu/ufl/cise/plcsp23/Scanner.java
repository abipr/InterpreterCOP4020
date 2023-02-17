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
    enum State{START,IN_IDENT,IN_NUM_LIT,HAVE_EQ,IN_STRING_LIT}
    State state;
    Scanner(String s) {
    //track line and column
        line = 1;
        column = 1;
        pos = 0;
        input = s;
        inputChars = Arrays.copyOf(input.toCharArray(), input.length()+1);
        ch = inputChars[pos];
        //tokenString = new StringBuilder();
    }
    private static HashMap<String, IToken.Kind > reservedWords;
    static {
        reservedWords = new HashMap<String, IToken.Kind>();
        reservedWords.put("if", IToken.Kind.RES_if);
        reservedWords.put("int",IToken.Kind.RES_int);
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
    /*
    *
        IDENT,
		NUM_LIT,
		STRING_LIT,
		RES_image,
		RES_pixel,
		RES_int,
		RES_string,
		RES_void,
		RES_nil,
		RES_load,
		RES_display,
		RES_write,
		RES_x,
		RES_y,
		RES_a,
		RES_r,
		RES_X,
		RES_Y,
		RES_Z,
		RES_x_cart,
		RES_y_cart,
		RES_a_polar,
		RES_r_polar,
		RES_rand,
		RES_sin,
		RES_cos,
		RES_atan,
		RES_if,
		RES_while,
		DOT, //  .
		COMMA, // ,
		QUESTION, // ?
		COLON, // :
		LPAREN, // (
		RPAREN, // )
		LT, // <
		GT, // >
		LSQUARE, // [
		RSQUARE, // ]
		LCURLY, // {
		RCURLY, // }
		ASSIGN, // =
		EQ, // ==
		EXCHANGE, // <->
		LE, // <=
		GE, // >=
		BANG, // !
		BITAND, // &
		AND, // &&
		BITOR, // |
		OR, // ||
		PLUS, // +
		MINUS, // -
		TIMES, // *
		EXP, // **
		DIV, // /
		MOD, // %
		EOF,
		ERROR
		* */
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
                        case '\b','\n','\r','\t','\"','\\' -> nextChar();

                        //operators
                        case '<' -> {
                            //increment counter, update ch
                            //set state to less than
                            //create state has less than
                        }
                        case '=' -> {
                            //set state to has equal
                            state = State.HAVE_EQ;
                            tokenString.append(ch);
                            nextChar();
                        }
                        case '+' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.PLUS, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        case '*' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.TIMES, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        case '0' -> {
                            tokenString.append(ch);
                            nextChar();
                            return new Token(IToken.Kind.NUM_LIT, tokenString.toString(), line, tokenStart, inputChars);
                        }
                        case '1','2','3','4','5','6','7','8','9' -> {//ch is a nonzero digit
                            state = State.IN_NUM_LIT;
                            tokenString.append(ch);
                            nextChar();
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

                                throw new UnsupportedOperationException(ch + " not implemented yet");
                            }

                        }
                    }
                }
                case HAVE_EQ -> {
                    if (ch != '='){
                        return new Token(IToken.Kind.ASSIGN,tokenString.toString(), line, tokenStart, inputChars);
                    }
                    else {
                        //try state = state.START;
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
                        int value = Integer.parseInt(tokenString.toString());
                        return new NumLitToken(IToken.Kind.NUM_LIT, tokenString.toString(), line, tokenStart, inputChars, value);
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
                        return new Token(IToken.Kind.STRING_LIT, tokenString.toString(), line, tokenStart, inputChars);
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
