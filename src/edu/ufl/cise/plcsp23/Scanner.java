package edu.ufl.cise.plcsp23;

public class Scanner implements IScanner {

    int line, column;
    Scanner() {
    //track line and column

    }
    public IToken next() throws LexicalException{
        //read in characters until final state
        //review the powerpoint to get started
        Token t = new Token(line, column);
        t.state = Token.State.START;

        return t;
    }

}
