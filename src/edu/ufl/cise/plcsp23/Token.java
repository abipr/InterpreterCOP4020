package edu.ufl.cise.plcsp23;

public class Token implements IToken{
    SourceLocation location;
    Kind kind;
    String tokenstring;
    char[] source;
    Token(Kind kind, String tokenstring, int line, int column, char[] source){
        location = new SourceLocation(line, column);
        this.tokenstring = tokenstring;
        this.kind = kind;
        this.source = source;
    }
    //record classes in java
    public SourceLocation getSourceLocation() {
        return location;
    }

    public Kind getKind() {
        return kind;
    }

    public String getTokenString() {
        return tokenstring;
    }


    //prints token; used for development only
    public void printToken(){
        System.out.println(tokenstring);
    }


}
