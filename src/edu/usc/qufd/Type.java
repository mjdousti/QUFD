/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd;

// TODO: Auto-generated Javadoc
/**
 * The Class Type.
 */
public class Type {
	
	/**
	 * The Enum type.
	 */
	public enum type{
		
		/** The id. */
		id,
/** The relop. */
relop,
/** The addop. */
addop,
/** The mulop. */
mulop,
/** The assignop. */
assignop,
/** The num. */
num,
/** The punc. */
punc,
/** The reserved. */
reserved,
/** The nt. */
nt, 
 /** The dollar. */
 dollar, 
 /** The semantic. */
 semantic
	};
	
	/** The mytype. */
	private type mytype;
	
	/** The name. */
	private String name;
	
	/**
	 * Instantiates a new type.
	 *
	 * @param t the t
	 * @param s the s
	 */
	public Type (type t, String s){
		mytype=t;
		name=s;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public type getType(){
		return mytype;
	}
	
	/**
	 * Gets the formatted type.
	 *
	 * @return the formatted type
	 */
	public String getFormattedType(){
		switch (mytype){
			case id:
				return "identifier";
			case relop:
				return "relational operator";
			case addop:
				return "summation operator";
			case mulop:
				return "multiplication operator";
			case assignop:
				return "assignment operator";
			case num:
				return "number";
			case punc:
				return "punctuation";
			case reserved:
				return "reserved word";
			default:
				return "";
		}		
	}	
}
