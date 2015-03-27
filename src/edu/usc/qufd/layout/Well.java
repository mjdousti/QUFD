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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.Layout.Types;
import edu.usc.qufd.layout.Qubit.Direction;

// TODO: Auto-generated Javadoc
/**
 * The Class Well.
 */
public class Well {
	
	/** The location. */
	protected Dimension location;
	
	/** The type. */
	private Types type;
	
	/** The qubit list. */
	protected List<Qubit> qubitList=new ArrayList<Qubit>(RuntimeConfig.CHANNEL_CAP);
	
	/** The qubit no. */
	protected int qubitNo;
	
	/** The used. */
	private boolean used=false;

	
	/** The expect qubit. */
	private int expectQubit=0;
	//store the destination in order to avoid passing of ions over one another
	/** The dst. */
	protected List<Dimension> dst=new ArrayList<Dimension>(4);
	
	/**
	 * Reset exp.
	 */
	public void resetExp(){
		expectQubit=0;
	}
	
	/**
	 * Inc exp.
	 */
	public void incExp(){
		expectQubit++;
	}
	
	/**
	 * Dec exp.
	 */
	public void decExp(){
		expectQubit--;
	}
	
	/**
	 * Gets the expected qubits.
	 *
	 * @return the expected qubits
	 */
	public int getExpectedQubits(){
		return expectQubit;
	}
	
	/**
	 * Gets the stalled qubits.
	 *
	 * @return total number of current stalled and futur stalled qubits
	 */
	public int getStalledQubits(){
		int result=expectQubit;
		for (int i = 0; i < qubitList.size(); i++) {
			if (qubitList.get(i).getDirection()==Direction.Stall)
				result++;
		}
		return result;
	}
	
	/**
	 * Adds the to dst.
	 *
	 * @param x the x
	 */
	public void addToDst(Dimension x){
		dst.add(x);		
	}

	/**
	 * Removes the from dst.
	 *
	 * @param x the x
	 */
	public void removeFromDst(Dimension x){
		dst.remove(x);		
	}

	
	/**
	 * Checks if is in dst.
	 *
	 * @param x the x
	 * @return true, if is in dst
	 */
	public boolean isInDst(Dimension x){
		return dst.contains(x);
	}
	
	/**
	 * Instantiates a new well.
	 *
	 * @param m the m
	 * @param t the t
	 */
	public Well(Dimension m, Types t) {
		location=new Dimension(m);
		type=t;
		qubitNo=0;
	}


	/**
	 * Sets the used.
	 *
	 * @param status the new used
	 */
	public void setUsed(boolean status){
		used=status;
		if (status==false){
			qubitNo=0;
			qubitList.clear();
		}
	}
	
	/**
	 * Checks if is used.
	 *
	 * @return true, if is used
	 */
	public boolean isUsed(){
		return used;
	}
		
	
	/**
	 * Gets the position.
	 *
	 * @return the position
	 */
	public Dimension getPosition(){
		return location;
	}


	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Types getType(){
		return type;
	}
	
	/**
	 * Checks if is occupied.
	 *
	 * @return true, if is occupied
	 */
	public boolean isOccupied(){
		return qubitNo>0;
		//return qubitList[0]!=null;
	}
	
	
	/**
	 * Adds the qubits.
	 *
	 * @param q the q
	 */
	public void addQubits(Qubit q){
		setUsed(true);
		if (qubitNo==RuntimeConfig.CHANNEL_CAP){
			System.err.println("Not enough space at the well! "+qubitNo);
			/*try{
				throw new Exception ("Not enough space!");
			}catch(Exception e){
				e.printStackTrace();
			}*/
			System.exit(-1);
		}
		qubitList.add(q);
		qubitNo++;		
	}

	/**
	 * Removes the qubit.
	 *
	 * @param q the q
	 * @return true, if successful
	 */
	public boolean removeQubit(Qubit q){
		if (qubitList.remove(q)){
			qubitNo--;
			return true;
		}else
			return false;
	}
	
	/**
	 * Gets the qubits no.
	 *
	 * @return the qubits no
	 */
	public int getQubitsNo(){
		return qubitNo;
	}
	
	/**
	 * Gets the qubit set.
	 *
	 * @return the qubit set
	 */
	public List<Qubit> getQubitSet(){
		return qubitList;
	}
	
	/**
	 * Prints the qubit set.
	 */
	public void printQubitSet(){
		System.out.println("Qubit List @"+getPosition().height+"x"+getPosition().width);
		for (Qubit q : qubitList) {
			System.out.println(q.getName());
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String output=new String();
		switch (type) {
		case Basic:
			output+="Basic";
			break;
		case Interaction:
			output+="Interaction";
			break;
		case Creation:
			output+="Creation";
			break;
		}
		output+="("+location.height+"x"+location.width+")";
		return output;
	}
}
