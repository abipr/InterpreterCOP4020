package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken{
    int value;
    NumLitToken(Kind kind, String tokenstring, int line, int column, char[] source, int value){
        super(kind, tokenstring, line, column, source);
        this.value = value;
    }
    public int getValue() {
        //integer value of token
        return value;
    }
}
