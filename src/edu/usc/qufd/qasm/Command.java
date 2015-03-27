/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.qasm;

// TODO: Auto-generated Javadoc
/**
 * The Class Command.
 */
public class Command {
	
	/** The name. */
	private String name;
	
	/** The operands. */
	private String[] operands=null;
	
	/** The ready. */
	private boolean[] ready=null;
	
	/**
	 * Instantiates a new command.
	 *
	 * @param s the s
	 * @param ops the ops
	 */
	public Command(String s, String ...ops) {
		name=s;
		operands=ops;
		
		if (ops!=null){
			ready=new boolean[ops.length];
			for (int i = 0; i < ops.length; i++) {
				ready[i]=false;
			}
		}
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
	 * Gets the operands number.
	 *
	 * @return the operands number
	 */
	public int getOperandsNumber(){
		return operands==null? 0 : operands.length;
	}

	/**
	 * Gets the operand.
	 *
	 * @param index the index
	 * @return the operand
	 */
	public String getOperand(int index){
		return operands[index];
	}

	/**
	 * Gets the ready status.
	 *
	 * @param i the i
	 * @return the ready status
	 */
	public boolean getReadyStatus(int i){
		return ready[i];
	}
	
	/**
	 * Checks if is ready.
	 *
	 * @return true, if is ready
	 */
	public boolean isReady(){
		for (int i = 0; i < ready.length; i++) {
			if (ready[i]==false)
				return false;
		}
		return true;
	}

	
	/**
	 * Sets the ready status.
	 *
	 * @param i the i
	 * @param status the status
	 * @return true, if successful
	 */
	public boolean setReadyStatus(int i, boolean status){
		if (i>=0 && i<operands.length){
			ready[i]=status;
			return true;
		}else
			return false;
	}
	
	/**
	 * Sets the ready status.
	 *
	 * @param q the q
	 * @return true, if successful
	 */
	public boolean setReadyStatus(String q){
		for (int i = 0; i < operands.length; i++) {
			if (operands[i].equals(q)){
				ready[i]=true;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if is target.
	 *
	 * @param q the q
	 * @return true, if is target
	 */
	public boolean isTarget(String q){
		if (operands[0].equals(q))
			return true;
		else
			return false;
	}
}
