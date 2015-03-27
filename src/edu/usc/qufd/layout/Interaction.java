/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.layout;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.Layout.Types;
import edu.usc.qufd.qasm.Vertex;

// TODO: Auto-generated Javadoc
/**
 * The Class Interaction.
 */
public class Interaction extends Well{
//	private PriorityQueue<Interval> TTR;
	/** The interaction in progress. */
private boolean interactionInProgress=false;
	
	/** The array level. */
	private ArrayList<Integer> arrayLevel=new ArrayList<Integer>();
	
	/** The in progress list. */
	private ArrayList<String> inProgressList=new ArrayList<String>();
	
	
	/**
	 * Adds the to progress list.
	 *
	 * @param q the q
	 */
	public void addToProgressList(String q){
		inProgressList.add(q);
	}

	/**
	 * Clear progress list.
	 */
	public void clearProgressList(){
		inProgressList.clear();
	}

	
	/**
	 * Removes the from progress list.
	 *
	 * @param q the q
	 * @return true, if successful
	 */
	public boolean removeFromProgressList(String q){
		boolean result;
//		if (inProgressList.contains(q)){
//			System.out.print("Avizoon Qubits: ");
//			for (int i = 0; i < inProgressList.size(); i++) {
//				System.out.print(inProgressList.get(i)+" ");
//			}
//			System.out.println();			
//		}
		result=inProgressList.remove(q);
		if (result==false){
			return false;
		}
		if (inProgressList.isEmpty()){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	/**
	 * Removes the level.
	 *
	 * @param level the level
	 */
	public void RemoveLevel(int level){
		if (arrayLevel.contains(level)){
			arrayLevel.remove(arrayLevel.indexOf(level));
		}
		else{
			System.out.println("error in removeLevel for interactions");
			System.exit(-1);
		}
	}
	
	/**
	 * First level.
	 *
	 * @return the int
	 */
	public int firstLevel(){
		if (arrayLevel.size()>0){
			return arrayLevel.get(0);
		}
		return -1;
	}
	
	
	/**
	 * Adds the level.
	 *
	 * @param level the level
	 */
	public void AddLevel(int level){
		if (!arrayLevel.contains(level))
			arrayLevel.add(level);
		else{
			System.out.println("error in AddLevel for interactions");
			System.exit(-1);
		}
	}
	
	/**
	 * Max level.
	 *
	 * @return the int
	 */
	public int maxLevel(){
		int maxLevel=0;
		for (int i = 0; i < arrayLevel.size(); i++) {
			maxLevel=Math.max(arrayLevel.get(i), maxLevel);
		}
		return maxLevel;
	}
	
	/**
	 * Contains level.
	 *
	 * @param level the level
	 * @return true, if successful
	 */
	public boolean containsLevel(int level){
		return arrayLevel.contains(level);
	}
	
	/**
	 * Instantiates a new interaction.
	 *
	 * @param m the m
	 * @param t the t
	 */
	public Interaction(Dimension m, Types t) {
		super(m, t);
	}

	/**
	 * Checks if is in progress.
	 *
	 * @return true, if is in progress
	 */
	public boolean isInProgress(){
		return interactionInProgress;
	}

	/**
	 * Sets the in progress.
	 *
	 * @param b the new in progress
	 */
	public void setInProgress(boolean b){
		interactionInProgress=b;
	}	
	
 	/**
	  * Gets the other qubit.
	  *
	  * @param q the q
	  * @return the other qubit
	  */
	 public Qubit getOtherQubit(String q){
		for (Qubit qubit : qubitList) {
			if (!qubit.getName().equals(q))
				return qubit;
		}
		return null;
	}
	
 	
 	/**
	  * All qubits arrived.
	  *
	  * @param v the v
	  * @return true, if successful
	  */
	 public boolean allQubitsArrived(Vertex v){
 		boolean found;
 		
 		for (int i = 0; i < v.getOperandsNumber(); i++) {
 			found=false;
 	 		for (int j = 0; j < qubitNo; j++) {
 	 			if (qubitList.get(j)==null){
 	 				System.out.println("Qubit#: "+qubitNo);
 	 				System.exit(-1);
 	 			}else if (qubitList.get(j).getName().equals(v.getOperand(i))){
 	 				found=true;
 	 				break;
 	 			}
 			}
 	 		if (!found)
 	 			return false;
		}
 		return true;
 	}
 	
	/**
	 * Wait time.
	 *
	 * @param arrive the arrive
	 * @return the int
	 */
	public int waitTime(int arrive){
		return 0;
	}
	
	
}