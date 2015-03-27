/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.layout;

import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * The Class TTLComparator.
 */
public class TTLComparator implements Comparator<Interval> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
    public int compare(Interval x, Interval y)
    {
		if (x.getTTL()<y.getTTL())
			return -1;
		else if (x.getTTL()==y.getTTL())
			return 0;
		else
			return 1;
    }
}
