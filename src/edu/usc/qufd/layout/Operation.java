/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.layout;

// TODO: Auto-generated Javadoc
/**
 * The Class Operation.
 */
public class Operation {
	
	/** The name. */
	private String name;
	
	/** The error. */
	private double error;
	
	/** The delay. */
	private int delay;
	
	/**
	 * Instantiates a new operation.
	 *
	 * @param s the s
	 * @param e the e
	 * @param d the d
	 */
	public Operation(String s, double e, int d){
		name=new String(s);
		error=e;
		delay=d;
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
	 * Gets the delay.
	 *
	 * @return the delay
	 */
	public int getDelay(){
		return delay;
	}
	
	/**
	 * Gets the error.
	 *
	 * @return the error
	 */
	public double getError(){
		return error;
	}	
}
