package edu.ufl.cise.plcsp23;
//hashmap of linked lists for symbol table //store stack where each entry is the id of the specific scope

import edu.ufl.cise.plcsp23.ast.Declaration;
import edu.ufl.cise.plcsp23.ast.NameDef;

import java.util.HashMap;
import java.util.Stack;

public class SymbolTable {
    public class LinkedList{
        Declaration declaration;
    }
    HashMap<String, NameDef> table ;//implement with linked list later
    Stack scope;
    //EnterNewScope(){}

    //set up symbol table
    //implement the type checking for the language ignoring scoping
}
/*top to bottom
* look at slides
* similar recursive pattern to parser*/