/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.router;

import java.awt.Dimension;
import java.util.ArrayList;

import edu.usc.qufd.layout.ChannelEdge;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.layout.Qubit;
import edu.usc.qufd.qasm.Vertex;

// TODO: Auto-generated Javadoc
/**
 * The Class BasePath.
 */
public class BasePath  implements Comparable<BasePath>{
	
	/** The cmd. */
	private Vertex cmd;
	
	/** The delay. */
	private long delay;
	
	/** The layout. */
	private Layout layout;

	/**
	 * Instantiates a new base path.
	 *
	 * @param command the command
	 * @param simTime the sim time
	 * @param layout the layout
	 */
	public BasePath(Vertex command, long simTime, Layout layout) {
		cmd=command;
//		delay=simTime+layout.getMoveDelay()+layout.getOpDelay(command.getName());
		delay=simTime+layout.getOpDelay(command.getName());
		this.layout=layout;
	}
	
	/**
	 * Gets the vertex.
	 *
	 * @return the vertex
	 */
	public Vertex getVertex(){
		return cmd;
	}
	
	/**
	 * Gets the delay.
	 *
	 * @return the delay
	 */
	public long getDelay(){
		return delay;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BasePath o) {
		if (getDelay()<o.getDelay())
			return -1;
		else if (getDelay()>o.getDelay())
			return 1;
		else{
			return cmd.compareTo(o.getVertex());
		}
	}
}
