/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the spring semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */


package edu.ufl.cise.plcsp23;

public interface IStringLitToken extends IToken {
	
	String getValue();
//create string builder, in forloop i=1 i<string.length()-1. if backslash, look at next characher, replace '\''b' with '\b'
}
