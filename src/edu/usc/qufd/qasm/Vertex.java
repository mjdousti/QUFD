/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.qasm;

import edu.usc.qufd.layout.Interaction;

// TODO: Auto-generated Javadoc
/**
 * The Class Vertex.
 */
public class Vertex extends Command implements Comparable<Vertex>{
	
	/** The command no. */
	private int commandNo;
	
	/** The priority. */
	private int priority=-1;
	
	/** The ready inpts. */
	private int readyInpts=0;
	
	/** The level. */
	private int level;
	
	/** The Tcongestion. */
	private long Tcongestion;
	
	/** The ASA p_level. */
	private int ASAP_level=-1;
	
	/** The ALA p_level. */
	private int ALAP_level=-1;
	
	/** The min level. */
	private int minLevel=0;
	
	/** The rank. */
	private double rank=-1;
	
	/** The ready inter pr. */
	private int readyInterPr=0;
	
	/** The x. */
	private double x=0;
	
	/** The y. */
	private double y=0;
	
	/** The Fx. */
	private double Fx=0;
	
	/** The Fy. */
	private double Fy=0;
	
	/** The moveable. */
	private boolean moveable=true;
	
	/** The interaction. */
	private Interaction interaction=null;
	
	/** The slack. */
	private int slack=-1;
	
	/** The anchor. */
	private boolean anchor=false;
	
	/** The anchor pos. */
	private double [] anchorPos = new double [2];
	
	/** The executed finish time. */
	private int executedFinishTime = Integer.MAX_VALUE;
//	private boolean ready;
//	private List<String> operands;
	
	/**
 * Instantiates a new vertex.
 *
 * @param c the c
 * @param no the no
 * @param ops the ops
 */
Vertex(String c, int no, String ...ops){
		super(c, ops);
		commandNo=no;
		//sentinels are already ready
//		ready= (ops==null) ? true : false;
	}
	
	
	/**
	 * Sets the execution finished time.
	 *
	 * @param ft the new execution finished time
	 */
	public void setExecutionFinishedTime (int ft){
		executedFinishTime = ft;
	}
	
	/**
	 * Gets the execution finish time.
	 *
	 * @return the execution finish time
	 */
	public int getExecutionFinishTime(){
		return executedFinishTime;
	}
	
	/**
	 * Sets the min level.
	 *
	 * @param ml the new min level
	 */
	public void setMinLevel(int ml){
		minLevel=ml;
	}
	
	/**
	 * Gets the min level.
	 *
	 * @return the min level
	 */
	public int getMinLevel(){
		return minLevel;
	}
	
	/**
	 * Anch pos.
	 *
	 * @return the double[]
	 */
	public double [] anchPos (){
		return anchorPos;
	}
	
	/**
	 * Checks for anchor.
	 *
	 * @return true, if successful
	 */
	public boolean hasAnchor (){
		return anchor;
	}
	
	/**
	 * Sets the anchor.
	 *
	 * @param anPos the new anchor
	 */
	public void setAnchor (double [] anPos){
		anchor=true;
		anchorPos[0]=anPos[0];
		anchorPos[1]=anPos[1];
	}
	
	/**
	 * Reset anchor.
	 */
	public void resetAnchor (){
		anchor=false;
	}
	
	/**
	 * Adds the to queue.
	 *
	 * @param simTime the sim time
	 */
	public void addToQueue(long simTime){
		Tcongestion=simTime;
	}
	
	/**
	 * Removes the from queue.
	 *
	 * @param simTime the sim time
	 * @return the long
	 */
	public long removeFromQueue(long simTime){
		return Tcongestion=simTime-Tcongestion;
	}
	
	/**
	 * Gets the tcongestion.
	 *
	 * @return the tcongestion
	 */
	public long getTcongestion(){
		return Tcongestion;
	}
	
	//***********Getters***********
	/**
	 * Gets the slack.
	 *
	 * @return the slack
	 */
	public int getSlack(){
		return slack;
	}
	
	/**
	 * Gets the interaction.
	 *
	 * @return the interaction
	 */
	public Interaction getInteraction(){
		return interaction;
	}
	
	/**
	 * Gets the moveablity.
	 *
	 * @return the moveablity
	 */
	public boolean getMoveablity(){
		return moveable;
	}
	
	/**
	 * Gets the fx.
	 *
	 * @return the fx
	 */
	public double getFx(){
		return Fx;
	}
	
	/**
	 * Gets the fy.
	 *
	 * @return the fy
	 */
	public double getFy(){
		return Fy;
	}
	
	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	public double getx(){
		return x;
	}
	
	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	public double gety(){
		return y;
	}
	
	/**
	 * Gets the aSAP level.
	 *
	 * @return the aSAP level
	 */
	public int getASAPLevel(){
		return ASAP_level;
	}
	
	/**
	 * Gets the aLAP level.
	 *
	 * @return the aLAP level
	 */
	public int getALAPLevel(){
		return ALAP_level;
	}
	
	/**
	 * Gets the rank.
	 *
	 * @return the rank
	 */
	public double getrank(){
		return rank;
	}
	
	/**
	 * Gets the level.
	 *
	 * @return the level
	 */
	public int getLevel(){
		return level;
	}
	
	/**
	 * Gets the ready inpts.
	 *
	 * @return the ready inpts
	 */
	public int getReadyInpts(){
		return readyInpts;
	}
	
	/**
	 * Gets the ready inter pr.
	 *
	 * @return the ready inter pr
	 */
	public int getReadyInterPr(){
		return readyInterPr;
	}
	
	/**
	 * Gets the command no.
	 *
	 * @return the command no
	 */
	public int getCommandNo(){
		return commandNo;
	}
	
	/**
	 * Gets the priority.
	 *
	 * @return the priority
	 */
	public int getPriority(){
		return priority;
	}
	
	/**
	 * Gets the instruction.
	 *
	 * @return the instruction
	 */
	public String getInstruction(){
		return getName();
	}
//	public boolean isReady(){
//		return ready;
//	}	
	/**
 * Checks if is sentinel.
 *
 * @return true, if is sentinel
 */
public boolean isSentinel(){
		return getName().equals("start")||getName().equals("end") ? true : false;
	}
//	public String getInstType(){
//		return instType;		
//	}


	//TODO: Should be added again if needed
//	public Qubit[] getOperands(){
//		return operands;
//	}
	
	//***********Setters***********	
	/**
 * Sets the slack.
 *
 * @param s the new slack
 */
public void setSlack(int s){
		slack = s;
	}	
	
	/**
	 * Sets the trap.
	 *
	 * @param t the new trap
	 */
	public void setTrap(Interaction t){
		interaction = t;
	}
	
	/**
	 * Sets the moveable.
	 */
	public void setMoveable(){
		moveable=true;
	}
	
	/**
	 * Sets the un moveable.
	 */
	public void setUnMoveable(){
		moveable=false;
	}
	
	/**
	 * Sets the fx.
	 *
	 * @param k the new fx
	 */
	public void setFx(double k){
		Fx=k;
	}
	
	/**
	 * Sets the fy.
	 *
	 * @param k the new fy
	 */
	public void setFy(double k){
		Fy=k;
	}
	
	/**
	 * Sets the x.
	 *
	 * @param k the new x
	 */
	public void setx(double k){
		x=k;
	}
	
	/**
	 * Sets the y.
	 *
	 * @param k the new y
	 */
	public void sety(double k){
		y=k;
	}
	
	/**
	 * Sets the ready inpts.
	 *
	 * @param k the new ready inpts
	 */
	public void setReadyInpts(int k){
		readyInpts = k;
	}
	
	/**
	 * Sets the ready inter pr.
	 *
	 * @param k the new ready inter pr
	 */
	public void setReadyInterPr(int k){
		readyInterPr = k;
	}
	
	/**
	 * Sets the aSAP level.
	 *
	 * @param k the new aSAP level
	 */
	public void setASAPLevel(int k){
		ASAP_level=k;
	}
	
	/**
	 * Sets the aLAP level.
	 *
	 * @param k the new aLAP level
	 */
	public void setALAPLevel(int k){
		ALAP_level=k;
	}
	
	/**
	 * Sets the rank.
	 *
	 * @param k the new rank
	 */
	public void setrank(double k){
		rank=k;
	}
	
	/**
	 * Sets the level.
	 *
	 * @param k the new level
	 */
	public void setLevel(int k){
		level=k;				
	}

	/**
	 * Inc ready inpts.
	 */
	public void incReadyInpts(){
		readyInpts++;
	}
	
	/**
	 * Inc ready inter pr.
	 */
	public void incReadyInterPr(){
		readyInterPr++;
	}
	
	/**
	 * Sets the priority.
	 *
	 * @param p the new priority
	 */
	public void setPriority (int p){
		priority=p;
	}
	
	/**
	 * Adds the priority.
	 *
	 * @param p the p
	 */
	public void addPriority (int p){
		priority+=p;
	}

//	public void setReady(boolean d){
//		ready=d;
//	}	
	/**
 * Sets the command no.
 *
 * @param no the no
 * @return the vertex
 */
public Vertex setCommandNo(int no){
		commandNo=no;
		return this;
	}
	
	/**
	 * Have operand.
	 *
	 * @param op the op
	 * @return true, if successful
	 */
	public boolean haveOperand (String op){
		for (int i = 0; i < getOperandsNumber(); i++) {
			if (getOperand(i).equals(op))
					return true;
		}
		return false;
	}
	
	
	/**
	 * Have same operand.
	 *
	 * @param v1 the v1
	 * @return true, if successful
	 */
	public boolean haveSameOperand (Vertex v1){
		for (int i = 0; i < getOperandsNumber(); i++) {
			for (int j = 0; j < v1.getOperandsNumber(); j++) {
				if (getOperand(i).equals(v1.getOperand(j)))
					return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String out=String.valueOf(commandNo)+": "+getName()+" ";
		for (int i = 0; i < getOperandsNumber(); i++) {
			if (i!=getOperandsNumber()-1)
				out+=getOperand(i)+", ";
			else
				out+=getOperand(i);
		}
		return out;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Vertex o) {
		if (this==null || o==null) return 0;
		if (priority>o.getPriority())
			return -1;
		else if (priority<o.getPriority())
			return 1;
		else{
			if (commandNo<o.getCommandNo())
				return -1;
			else if (commandNo>o.getCommandNo())
				return 1;
			else
				return 1;	
		}
			
	}
	
	/**
	 * Checks for operand.
	 *
	 * @param qubit the qubit
	 * @return true, if successful
	 */
	public boolean hasOperand(String qubit){
		for (int i = 0; i < getOperandsNumber(); i++) {
			if (getOperand(i).equals(qubit))
				return true;
		}
		return false;
	}
}
