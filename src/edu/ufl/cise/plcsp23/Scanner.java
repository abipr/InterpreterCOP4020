package edu.ufl.cise.plcsp23;

public class Scanner implements IScanner {
    private enum State{START,IN_IDENT,IN_NUM_LIT,HAVE_EQ}
    Scanner() {
//track line and column
    }
    public IToken next() throws LexicalException{
        //read in characters until final state
        //review the powerpoint to get started
        Token t = new Token();

        return t;
    }

}
