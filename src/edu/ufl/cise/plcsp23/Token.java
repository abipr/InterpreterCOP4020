package edu.ufl.cise.plcsp23;

public class Token implements IToken{
    Token(){
        SourceLocation location = new SourceLocation(1,1);
    }
    //record classes in java
    public SourceLocation getSourceLocation() {

        return null;
    }

    public Kind getKind() {
        return null;
    }

    public String getTokenString() {
        return null;
    }
}
