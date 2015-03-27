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
import java.util.Iterator;
import java.util.Random;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.Interaction;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.qasm.QASM;
import edu.usc.qufd.qasm.Vertex;
import edu.usc.qufd.router.InitialPlacer.Heuristic;

import org.jgrapht.*;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

// TODO: Auto-generated Javadoc
/**
 * The Class InitialGatePlacer.
 */
public class InitialGatePlacer {
	
	/** The qasm. */
	private static QASM qasm;
	
	/** The spring net. */
	private static SimpleWeightedGraph<Vertex, DefaultWeightedEdge> springNet;
	
	/** The layout. */
	private static Layout layout;
	
	/** The random generator. */
	private static Random randomGenerator;
	
	
	/**
	 * The Enum Heur.
	 */
	public static enum Heur{
		
		/** The Random. */
		Random
	}
	
	/**
	 * Initial gate placer.
	 *
	 * @param q the q
	 * @param sNet the s net
	 * @param l the l
	 * @param heuristic the heuristic
	 */
	public static void InitialGatePlacer(QASM q,SimpleWeightedGraph<Vertex, DefaultWeightedEdge> sNet, Layout l, Heur heuristic) {
		randomGenerator=new Random();
		layout=l;
		springNet=sNet;
		qasm=q;
		assignGates(heuristic);
	}
	
	/**
	 * Assign gates.
	 *
	 * @param heuristic the heuristic
	 */
	private static void assignGates (Heur heuristic){
		Dimension place=new Dimension();
		Dimension size=layout.getLayoutSize(), position;
		Vertex cur0;
		int level = 0;
		boolean ExistVertxInLevel=true;
		
		String[] qubits=qasm.getQubitList();

		switch (heuristic){
		case Random:
			layout.clean();
			if (RuntimeConfig.HDEBUG)
				System.out.println("INITIAL GATE PLACEMENT");
			place.height = size.height/2;
			place.width  = size.width/2;
			layout.sortInteractionWells(place, false,  2*(springNet.vertexSet().size()));
			for (int i = 0; i < qubits.length; i++) {
				while (ExistVertxInLevel){
					ExistVertxInLevel=false;
					int ij = 0;
					for (Iterator <Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
						cur0 = iterator.next();
						if (cur0.getLevel()==level){
							ExistVertxInLevel=true;
							Interaction trap =(Interaction) layout.getInteractionEmptyList().get(ij++);
							cur0.setx(trap.getPosition().width);
							cur0.sety(trap.getPosition().height);
							if (RuntimeConfig.HDEBUG)
								System.out.println("{"+cur0+" - "+cur0.getLevel()+"("+cur0.getx()+","+cur0.gety()+")"+"}");
						}
					}
					level++;
				}
			}
			if (RuntimeConfig.HDEBUG)
				System.out.println("INITIAL GATE PLACEMENT");
			
			break;
		}
	}
}
