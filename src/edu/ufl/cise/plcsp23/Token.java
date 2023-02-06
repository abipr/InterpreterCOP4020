package edu.ufl.cise.plcsp23;

public class Token implements IToken{
    SourceLocation location;
    Kind kind;

    enum State{START,IN_IDENT,IN_NUM_LIT,HAVE_EQ}
    State state;
    Token(int line, int column){
        location = new SourceLocation(line, column);
    }
    //record classes in java
    public SourceLocation getSourceLocation() {

        return location;
    }

    public Kind getKind() {
        return kind;
    }

    public String getTokenString() {
        return null;
    }
}
