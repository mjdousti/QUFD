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
import java.io.PrintWriter;
import java.util.HashMap;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.Layout.Types;


// TODO: Auto-generated Javadoc
/**
 * The Class Qubit.
 */
public class Qubit {
	
	/** The sleep. */
	private boolean sleep=false;
	
	/** The name. */
	private String name;
	
	/** The init pos. */
	private Dimension initPos;
	
	/** The final pos. */
	private Dimension finalPos;
	
	/** The pos. */
	private Dimension pos;
	
	/** The layout. */
	private Layout layout;
	
	/** The dir. */
	private Direction dir=Direction.Stall;
	
	/** The ready to go. */
	private boolean readyToGo=false;

	/**
	 * Sets the inits the position.
	 *
	 * @param d the new inits the position
	 */
	public void setInitPosition(Dimension d){
		initPos = d;
	}
	
	/**
	 * Sets the final position.
	 *
	 * @param d the new final position
	 */
	public void setFinalPosition(Dimension d){
		finalPos = d;
	}
	
	/**
	 * Gets the inits the position.
	 *
	 * @return the inits the position
	 */
	public Dimension getInitPosition(){
		return initPos;
	}
	
	/**
	 * Gets the final position.
	 *
	 * @return the final position
	 */
	public Dimension getFinalPosition(){
		return finalPos;
	}
	
	/**
	 * Sets the sleep.
	 */
	public void setSleep(){
		sleep=true;
	}
	
	/**
	 * Reset sleep.
	 */
	public void resetSleep(){
		sleep=false;
	}
	
	/**
	 * Checks if is sleep.
	 *
	 * @return true, if is sleep
	 */
	public boolean isSleep(){
		return sleep;
	}
	
	/**
	 * Gets the ready.
	 *
	 * @return the ready
	 */
	public boolean getReady(){
		return readyToGo;
	}
	
	/**
	 * Sets the ready.
	 *
	 * @param rtg the new ready
	 */
	public void setReady(boolean rtg){
		readyToGo=rtg;
	}
	
	
	/**
	 * The Enum Direction.
	 */
	public static enum Direction{
		
		/** The Right. */
		Right, 
 /** The Left. */
 Left, 
 /** The Up. */
 Up, 
 /** The Down. */
 Down, 
 /** The Stall. */
 Stall
	}
	
	/**
	 * Sets the direction.
	 *
	 * @param d the new direction
	 */
	public void setDirection(Direction d){
		dir=d;		
	}
	
	/**
	 * Gets the direction.
	 *
	 * @return the direction
	 */
	public Direction getDirection(){
		return dir;
	}
	
	
	/**
	 * Instantiates a new qubit.
	 *
	 * @param s the s
	 * @param d the d
	 * @param l the l
	 * @param ini the ini
	 * @param fin the fin
	 */
	public Qubit(String s, Dimension d, Layout l,Dimension ini,Dimension fin){
		name=new String(s);
		pos=new Dimension(d);
		initPos=new Dimension(ini); // TO DO
		finalPos=new Dimension(fin); // TO DO
		layout=l;
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
	 * Gets the position.
	 *
	 * @return the position
	 */
	public Dimension getPosition(){
		return pos;
	}
	
	/**
	 * Move.
	 *
	 * @param outputFile the output file
	 * @param resourceEstimate the resource estimate
	 */
	public void move(PrintWriter outputFile, HashMap<String, Integer> resourceEstimate){
		Dimension prevPosition=new Dimension(pos);

		switch (dir){
		case Right:
			pos.width+=1;
			break;
		case Left:
			pos.width-=1;
			break;
		case Up:
			pos.height-=1;
			break;
		case Down:
			pos.height+=1;
			break;
		}
		assert pos.width>=0;
		assert pos.width<layout.getLayoutSize().width;
		assert pos.height>=0;
		assert pos.height<layout.getLayoutSize().height;

		if (layout.getWell(pos).getQubitsNo()==RuntimeConfig.CHANNEL_CAP)
			return;
//		System.out.print("Move "+getName()+" ("+prevPosition.height+","+prevPosition.width+")->(");
		if (layout.isIntraction(pos) && (layout.getWell(pos)).getQubitsNo()==0) ((Interaction)layout.getWell(pos)).setInProgress(false);
		if (layout.getWell(pos).isInDst(prevPosition) || (layout.isIntraction(pos) && ((Interaction)layout.getWell(pos)).isInProgress())){
			pos.setSize(prevPosition);
			return;
		}
		layout.getWell(prevPosition).removeFromDst(pos);
		
		
		if (dir!=Direction.Stall){
			layout.free(prevPosition, this);
			layout.occupy(pos, this);
			if(RuntimeConfig.VERBOSE){
				outputFile.print("Move "+getName()+" "+layout.XYtoQuadruple(prevPosition)+"->");
				outputFile.print(layout.XYtoQuadruple(getPosition())+"\t");
				if (!RuntimeConfig.OUTPUT_TO_FILE){
					outputFile.flush();
				}
			}
			resourceEstimate.put("Move", new Integer(resourceEstimate.get("Move")+1));
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return obj.equals(name);
	}
	
	//	Might be needed in the future
	/**
	 * Sets the position.
	 *
	 * @param d the new position
	 */
	public void setPosition(Dimension d){
		pos.height=d.height;
		pos.width=d.width;
	}
}
