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
 * The Class Interval.
 */
public class Interval{
	
	/** The weight. */
	private int TTR, TTL, weight; 
	
	/** The qubit. */
	private Qubit qubit;
	
	/**
	 * Instantiates a new interval.
	 *
	 * @param ttr the ttr
	 * @param ttl the ttl
	 * @param w the w
	 * @param q the q
	 */
	public Interval(int ttr, int ttl, int w, Qubit q) {
		TTL=ttl;
		TTR=ttr;
		weight=w;
		qubit=q;
	}
	
	/**
	 * Checks if is inside.
	 *
	 * @param time the time
	 * @return true, if is inside
	 */
	public boolean isInside(int time){
		if (time>=TTR && time <=TTL){
			return true;
		}else{
			return false;
		}
			
	}
	
	/**
	 * Gets the qubit.
	 *
	 * @return the qubit
	 */
	public Qubit getQubit(){
		return qubit;
	}
	
	/**
	 * Gets the ttl.
	 *
	 * @return the ttl
	 */
	public int getTTL(){
		return TTL;
	}

	/**
	 * Gets the ttr.
	 *
	 * @return the ttr
	 */
	public int getTTR(){
		return TTR;
	}
	
	/**
	 * Gets the weight.
	 *
	 * @return the weight
	 */
	public int getWeight(){
		return weight;
	}
	
	/**
	 * Compare to ttr.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareToTTR(Interval o) {
		if (TTR<o.getTTR())
			return -1;
		else if (TTR==o.getTTR())
			return 0;
		else
			return 1;
	}
}
