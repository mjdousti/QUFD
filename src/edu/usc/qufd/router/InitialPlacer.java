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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.Interaction;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.layout.Well;
import edu.usc.qufd.qasm.QASM;
import edu.usc.qufd.qasm.Vertex;


// TODO: Auto-generated Javadoc
/**
 * The Class InitialPlacer.
 */
public class InitialPlacer {
	
	/** The qasm. */
	private static QASM qasm;
	
	/** The layout. */
	private static Layout layout;
	
	/** The random generator. */
	private static Random randomGenerator;
	
	/** The output file. */
	private static PrintWriter outputFile;
	
	/**
	 * The Enum Heuristic.
	 */
	public static enum Heuristic{
		
		/** The Center. */
		Center, 
 /** The Random. */
 Random, 
 /** The Shuffled center. */
 ShuffledCenter
	}
	
	/**
	 * Place.
	 *
	 * @param q the q
	 * @param l the l
	 * @param heuristic the heuristic
	 */
	public static void place(QASM q, Layout l, Heuristic heuristic) {
		randomGenerator=new Random();
		layout=l;
		qasm=q;
		assignQubits(heuristic);
		if (RuntimeConfig.PRINT_QUBIT){
			System.out.println("Initial Placement");
			for (int i = 0; i < q.getQubitList().length; i++) {
				System.out.println(q.getQubitList()[i]+" @ "+layout.getQubit(q.getQubitList()[i]).getPosition().height+","+layout.getQubit(q.getQubitList()[i]).getPosition().width);
			}
		}
		if (RuntimeConfig.VERBOSE)
			System.out.println("Qubit placement completed successfully!");
	}
	
	/**
	 * Initial placer.
	 *
	 * @param q the q
	 * @param l the l
	 * @param DFG the dfg
	 */
	public static void InitialPlacer(QASM q, Layout l, SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG) {
		randomGenerator=new Random();
		layout=l;
		qasm=q;
		assignQubitsFixedPlacement(DFG);
	}
	
	/**
	 * Assign qubits.
	 *
	 * @param heuristic the heuristic
	 */
	private static void assignQubits (Heuristic heuristic){
		Dimension place=new Dimension();
		Dimension size=layout.getLayoutSize();
		Dimension temp = null;
		
		String[] qubits=qasm.getQubitList();

		switch (heuristic){
		case Center:
			place.height = size.height/2;
			place.width  = size.width/2;
			layout.sortCreationWells(place, false, qasm.getQubitList().length);
			for (int i = 0; i < qubits.length; i++) {
				if (layout.getQubit(qubits[i]).getInitPosition().getHeight()<0){
					temp=layout.getNearestFreeCreation(true).getPosition();
					layout.assignNewQubit(qubits[i], temp, temp, new Dimension(-10, -10));
				}
			}		
			break;
		case ShuffledCenter:
			place.height = size.height/2;
			place.width  = size.width/2;
			layout.sortCreationWells(place, true, qasm.getQubitList().length);
			for (int i = 0; i < qubits.length; i++) {
				layout.assignNewQubit(qubits[i], layout.getNearestFreeCreation(true).getPosition(),new Dimension(),new Dimension());
			}		
			break;
		case Random:
			for (int i = 0; i < qubits.length; i++) {
				place.width=randomGenerator.nextInt(size.width);
				place.height=randomGenerator.nextInt(size.height);
				layout.sortCreationWells(place, false,  qasm.getQubitList().length);
				layout.assignNewQubit(qubits[i], layout.getNearestFreeCreation(true).getPosition(),new Dimension(),new Dimension());
			}		
			break;
		}
	}
	
	/**
	 * Assign qubits fixed placement.
	 *
	 * @param DFG the dfg
	 */
	private static void assignQubitsFixedPlacement (SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG){
		Dimension place=new Dimension();
		Dimension size=layout.getLayoutSize();
		Vertex cur0=null;
		
		String[] qubits=qasm.getQubitList();

		for (int i = 0; i < qubits.length; i++) {
			if (layout.getQubit(qubits[i]).getInitPosition().height<0){
				int minLevel=100;
				Vertex cur1=null;
				for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
					cur0 = iterator.next();
					if (cur0.getLevel()<minLevel && cur0.haveOperand(qubits[i])){
						minLevel=cur0.getLevel(); 
						cur1=cur0;
					}
				}
				if (RuntimeConfig.HDEBUG){
					System.out.println(qubits[i]+" - "+cur1.getInteraction().getPosition());
				}
				Interaction curI=cur1.getInteraction();
				layout.sortCreationWells(curI.getPosition(), false,  1);
				ArrayList<Well> TEL=layout.getCreationEmptyList();
				boolean assigned= false;
				int index=0;
				while (!assigned){
					Well curc=TEL.remove(index++);
					if (RuntimeConfig.HDEBUG){
						System.out.println(qubits[i]+" - "+curc.getPosition());
					}
					if (curc.getQubitsNo()==0){
						layout.assignNewQubit(qubits[i], curc.getPosition(),new Dimension(),new Dimension());
						assigned = true;
					}
				}
			}
		}
		for (int i = 0; i < qubits.length; i++) {
			if (RuntimeConfig.HDEBUG)
				System.out.println(qubits[i]+" - "+layout.getQubit(qubits[i]).getPosition());
		}
	}
	
	
	/**
	 * Initial placer.
	 *
	 * @param q the q
	 * @param l the l
	 * @param DFG the dfg
	 * @param bvb the bvb
	 * @param file the file
	 */
	public static void InitialPlacer(QASM q, Layout l, SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG, boolean bvb, PrintWriter file) {
		randomGenerator=new Random();
		layout=l;
		qasm=q;
		outputFile=file;
		assignQubitssimPL(DFG);
	}
	
	
	/**
	 * Assign qubitssim pl.
	 *
	 * @param DFG the dfg
	 */
	private static void assignQubitssimPL (SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG){
		//Place the qubits that are not I/O qubits.
		Dimension place=new Dimension();
		Dimension size=layout.getLayoutSize();
		Vertex cur0=null;
		
		String[] qubits=qasm.getQubitList();

		for (int i = 0; i < qubits.length; i++) {
			if (layout.getQubit(qubits[i]).getInitPosition().height<0){
				int minLevel=100;
				Vertex cur1=null;
				for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
					cur0 = iterator.next();
					if (cur0.getLevel()<minLevel && cur0.haveOperand(qubits[i])){
						minLevel=cur0.getLevel(); 
						cur1=cur0;
					}
				}
				if (RuntimeConfig.HDEBUG){
					System.out.println(qubits[i]+" - "+cur1.getInteraction().getPosition());
				}
				Interaction curI=cur1.getInteraction();
				layout.sortCreationWells(curI.getPosition(), false,  1);
				ArrayList<Well> TEL=layout.getCreationEmptyList();
				boolean assigned= false;
				int index=0;
				while (!assigned){
					Well curc=TEL.remove(index++);
					if (RuntimeConfig.HDEBUG){
						System.out.println(qubits[i]+" - "+curc.getPosition());
					}
					if (curc.getQubitsNo()==0){
						layout.assignNewQubit(qubits[i], curc.getPosition(),new Dimension(),new Dimension());
						assigned = true;
					}
				}
			}
		}
		
		outputFile.println("Qubit count: "+ qubits.length);
		for (int i = 0; i < qubits.length; i++) {
			outputFile.println("Qubit "+qubits[i]+" is placed @"+
						layout.XYtoQuadruple(layout.getQubit(qubits[i]).getPosition())+".");
		}
	}
	
	
}
