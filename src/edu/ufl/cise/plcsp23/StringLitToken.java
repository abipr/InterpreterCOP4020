package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken{
    StringLitToken(Kind kind, String tokenstring, int line, int column, char[] source) {
        super(kind, tokenstring, line, column, source);
    }
    public String getValue() {
        StringBuilder temp = new StringBuilder();
        char ch;
        for(int i = 1; i<tokenstring.length()-1; i++){
            ch = tokenstring.charAt(i);
            if(ch == '\\' && i+1 <tokenstring.length()-1){
                i++;
                ch = tokenstring.charAt(i);
                switch(ch){
                    case 'b' -> {
                        temp.append('\b');
                    }
                    case 't' -> {
                        temp.append('\t');
                    }
                    case 'r' -> {
                        temp.append('\r');
                    }
                    case '"' -> {
                        temp.append('\"');
                    }
                    case '\\' -> {
                        temp.append('\\');
                    }
                    case 'n' -> {
                        temp.append('\n');
                    }
                }
            }else{
                temp.append(ch);
            }
        }
        return temp.toString();
    }
//create string builder, in forloop i=1 i<string.length()-1. if backslash, look at next characher, replace '\''b' with '\b'
}
