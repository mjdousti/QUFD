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
import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * The Class NearestWell.
 */
public class NearestWell implements Comparator<Well>{
	
	/** The a. */
	private Dimension a;
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Well o1, Well o2) {
		int d1=Math.abs(a.height-o1.getPosition().height)+Math.abs(a.width-o1.getPosition().width);
		int d2=Math.abs(a.height-o2.getPosition().height)+Math.abs(a.width-o2.getPosition().width);
		
		if (d1<d2)
			return -1;
		else if (d1>d2)
			return 1;
		 //just to get unique answer
		else if(o1.getPosition().height<o2.getPosition().height)
			return -1;
		else if(o1.getPosition().height>o2.getPosition().height)
			return 1;
		else if(o1.getPosition().width<o2.getPosition().width)
			return -1;
		else if(o1.getPosition().width>o2.getPosition().width)
			return 1;
		else
			return 0;
	}
	
	/**
	 * Instantiates a new nearest well.
	 *
	 * @param x the x
	 */
	public NearestWell(Dimension x) {
		a=x;
	}


}
