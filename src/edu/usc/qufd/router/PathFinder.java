/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.router;


import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import edu.usc.qufd.layout.ChannelEdge;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.layout.Well;
import edu.usc.qufd.qasm.Vertex;

// TODO: Auto-generated Javadoc
/**
 * The Class PathFinder.
 */
public class PathFinder {
//	Map<String, LinkedList<Vertex>> waitList=new HashMap<String, LinkedList<Vertex>>();

	
	/**
 * Instantiates a new path finder.
 *
 * @param layout the layout
 * @param dfg the dfg
 */
public PathFinder(Layout layout, DirectedGraph<Vertex, DefaultEdge> dfg) {
		SimpleWeightedGraph<Well, ChannelEdge> graph=layout.getGraph();
//		for (Command cmd : readyQueue) {
//			//TODO: Generalize for more than 2-operand commands
//			if (cmd.getOperandsNumber()==2){
//				Square 
//				int x0=cmd.getOperand(0).getPosition().height;
//				int y0=cmd.getOperand(0).getPosition().width;
//				int x1=cmd.getOperand(1).getPosition().height;
//				int y1=cmd.getOperand(1).getPosition().width;
//				DijkstraShortestPath.findPathBetween(graph, layout.getNearestJunction(x0, y0), layout.getNearestJunction(x1, y1));
//			}
//				
//		}
	
	}
}
