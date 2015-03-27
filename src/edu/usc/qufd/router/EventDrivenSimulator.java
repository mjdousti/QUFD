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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.ChannelEdge;
import edu.usc.qufd.layout.Interaction;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.layout.Qubit;
import edu.usc.qufd.layout.Well;
import edu.usc.qufd.qasm.Vertex;

/**
 * The Class EventDrivenSimulator.
 *
 * @author Mohammad Javad Dousti and Hadi Goudarzi
 */
public class EventDrivenSimulator {
	
	/** The added edges. */
	private ArrayList<DefaultWeightedEdge> addedEdges= new ArrayList<DefaultWeightedEdge>();
	
	/** The resource estimate. */
	private HashMap<String, Integer> resourceEstimate=new HashMap<String, Integer>();
	
	/** The sim time. */
	private long simTime=0;
	
	/** The output file. */
	private PrintWriter outputFile;
	
	/**
	 * The Enum Scheduling.
	 */
	public enum Scheduling{
		
		/** The asap. */
		ASAP, 
 /** The alap. */
 ALAP
	}

	/** The layout. */
	Layout layout;
	
	/** The wait queue. */
	WaitingQueue waitQueue=new WaitingQueue();
	
	/** The busy insts. */
	List<Vertex> busyInsts=new ArrayList<Vertex>();
	
	/** The issue queue. */
	PriorityQueue<Path> issueQueue=new PriorityQueue<Path>();
	
	/** The iss queue. */
	ArrayList<Path> issQueue=new ArrayList<Path>();
	
	/** The ready queue. */
	ReadyQueue readyQueue;
	
	/** The commands. */
	List<Vertex> commands;
	
	
	/**
	 * Reset added edges.
	 */
	public void resetAddedEdges(){
		addedEdges.clear();
	}
	
	/**
	 * Removes the extra edges.
	 *
	 * @param DFG the dfg
	 */
	public void removeExtraEdges(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG){
		for (int i = 0; i < addedEdges.size(); i++) {
			DFG.removeEdge(addedEdges.get(i));
		}
		addedEdges.clear();
	}
	
	/**
	 * Instantiates a new event driven simulator.
	 *
	 * @param layout the layout
	 * @param cmds the cmds
	 * @param file the file
	 */
	public EventDrivenSimulator(Layout layout, List<Vertex> cmds, PrintWriter file) {
		this.layout=layout;
		commands=cmds;
		outputFile=file;
		
		resourceEstimate.put("Qubit", new Integer(layout.getQubitCount()));
		resourceEstimate.put("Move", new Integer(0));		
	}
	
	/**
	 * Output resource estimate.
	 *
	 * @param resourceEstimateAddr the resource estimate addr
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void outputResourceEstimate(String resourceEstimateAddr) throws IOException{
		PrintWriter resourceEstimateFile;
		if (RuntimeConfig.OUTPUT_TO_FILE){
			resourceEstimateFile=new PrintWriter(new BufferedWriter(new FileWriter(resourceEstimateAddr, false)), true);
		}else{
			resourceEstimateFile=new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);
		}
		
		for (Iterator<String> iterator = resourceEstimate.keySet().iterator(); iterator.hasNext();) {
			String resourceType=iterator.next();
			
			resourceEstimateFile.println(resourceType + "\t"+resourceEstimate.get(resourceType));
		}
		
		resourceEstimateFile.close();
	}
	
	/**
	 * Schedules the instructions in the commands list. It clears the <i>waitQueue</i>, <i>readyQueue</i>, and <i>issueQueue</i>. 
	 * Then it reschedules instructions in the <i>waitQueue</i> and <i>readyQeue</i>. 
	 */
	public void schedule(){
		waitQueue.clear();
		issueQueue.clear();
		
		for (Vertex v : commands) {
			if (v.isSentinel())
				continue;
			for (int i = 0; i < v.getOperandsNumber(); i++) {
				v.setReadyStatus(i, false);
				if (!waitQueue.containsKey(v.getOperand(i))){
					waitQueue.put(v.getOperand(i), new LinkedList<Vertex>());
				}
				waitQueue.get(v.getOperand(i)).add(v);
			}
		}
	
		issueQueue.clear();
		readyQueue=new ReadyQueue(waitQueue);

		if (RuntimeConfig.DEBUG)
		{
			System.out.println(waitQueue.toString());
			System.out.println(readyQueue.toString());
		}
		if (RuntimeConfig.VERBOSE)
			System.out.println("Scheduling completed successfully!");

	}
	
	
	
	/**
	 * Prints the issued queue in a human readible format.
	 */
	public void printIssuedQueue(){
		for (Iterator<Path> iterator = issueQueue.iterator(); iterator.hasNext();) {
			System.out.println(iterator.next().getFullInstruction());
		}
	}

	/**
	 * Base simulate.
	 *
	 * @return the long
	 */
	public long baseSimulate(){
		PriorityQueue<BasePath> issueQueue=new PriorityQueue<BasePath>();;
		Set<Vertex> initList=readyQueue.getNext();
		BasePath temp0;
		simTime=0;

		
		for (Iterator<Vertex> iterator = initList.iterator(); iterator.hasNext();) {
			Vertex v=iterator.next();
			issueQueue.add(new BasePath(v, simTime, layout));			
		}

		while (!readyQueue.isEmpty() || !issueQueue.isEmpty()){
			simTime=issueQueue.peek().getDelay();
			if(RuntimeConfig.TIME)
				System.out.print(System.getProperty("line.separator")+"SimTime:"+simTime+"\t");
			else if (RuntimeConfig.VERBOSE)
				System.out.print(System.getProperty("line.separator"));
			
			do{
				temp0=issueQueue.remove();
				if(RuntimeConfig.VERBOSE)
					System.out.print(temp0.getVertex()+"\t");
				LinkedList<Vertex> vTemp=readyQueue.getNext(temp0.getVertex());

				for (int i = 0; i < vTemp.size(); i++) {
					issueQueue.add(new BasePath(vTemp.get(i), simTime, layout));
				}
			}while(!issueQueue.isEmpty() && simTime==issueQueue.peek().getDelay());
		}
		return simTime;
	}

	
	/**
	 * Give a the best route between the operands of instruction v.
	 *
	 * @param DFG the dfg
	 * @param v the given instruction
	 * @param simTime the simulation time
	 * @return list of channelEdges participating in the computed path. Returns null if no route could be found.
	 */
	private List<Path> fixedPlacementrouter(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG, Vertex v, long simTime){
		boolean newTrapReserved=false;
		LinkedList<Path> pathList=new LinkedList<Path>();		
		LinkedList<Dimension> src=new LinkedList<Dimension>();
		LinkedList<Dimension> dst=new LinkedList<Dimension>();
		ArrayList<Well> trapEmptyList=new ArrayList<Well>();
		Dimension temp1, temp2;
		Qubit q = null;
		Interaction destInteraction=null, curInter=null;
		boolean included=false, foundDestination=false, destinationIsFree=true;
		Vertex cur0, cur1=null;
		int firstLevel=0;
		ArrayList<Qubit> Q=new ArrayList<Qubit>();
		ArrayList<Vertex> V=new ArrayList<Vertex>();
		
		issQueue=new ArrayList<Path>();
		for (int i = v.getOperandsNumber()-1; i>=0 ; i--) {
			q = layout.getQubit(v.getOperand(i));
			for (Iterator<Path> iterator = issueQueue.iterator(); iterator.hasNext();) {
				Path p = iterator.next();
				if (p.getQubit().equals(q)){
					issQueue.add(p);	
					q.setReady(false);
				}
			}
		}
		

		
		destInteraction=v.getInteraction();
		if (RuntimeConfig.HDEBUG){
			System.out.println(destInteraction.getPosition());
			System.out.println(v+" "+v.getInteraction().getPosition()+" "+v.getInteraction().firstLevel()+" "+v.getLevel());
		}
		//Which Qubits are placed in this location before?
		if (!v.getMoveablity()){
			for (Iterator<Qubit> iterator = destInteraction.getQubitSet().iterator(); iterator.hasNext();) {
				q = iterator.next();
				if (RuntimeConfig.HDEBUG){
					System.out.println(q.getName()+" - "+q.getReady());
				}
				included=false;
				//check if it is included in the next operation
				for (int i = 0; i < v.getOperandsNumber(); i++) {
					if (v.getOperand(i).equals(q.toString())) 
						included=true;
				}
				if (!included && !q.getReady()){
					src.add(q.getPosition());
					Q.add(q);
					if (RuntimeConfig.HDEBUG){
						System.out.println("SRC000 "+q+" - "+v);
					}
					//Now find destination for this qubit
					curInter=null;
					foundDestination=false;
					firstLevel=Integer.MAX_VALUE;
					//find the first next level instruction
					for (Iterator<Vertex> iterator2 = DFG.vertexSet().iterator(); iterator2.hasNext();) {
						cur0 = iterator2.next();
						if (!cur0.isSentinel() && cur0.getLevel()>=v.getLevel() && cur0.haveOperand(q.getName()) && cur0.getLevel()<firstLevel){
							firstLevel=cur0.getLevel();
							foundDestination=true;
							cur1=cur0;
						}
					}
					//if new destination is found
					if (foundDestination){
						//there is a destination to assign (check if it is going to be used before the time needed for this qubit)
						destinationIsFree=true;
						curInter=cur1.getInteraction();
						if (RuntimeConfig.HDEBUG){					
							System.out.println("NNNNNNNNNN   "+curInter.getQubitsNo()+" - "+curInter.getPosition()+" - "+curInter.getExpectedQubits());
						}
						//if destination does not have enough space or it is reserved for other qubits
						if (curInter.getQubitsNo()==RuntimeConfig.CHANNEL_CAP || (curInter.getExpectedQubits()+curInter.getQubitsNo())>=RuntimeConfig.CHANNEL_CAP)
							destinationIsFree=false;
						for (int i = cur1.getLevel()-1; i >= v.getLevel(); i--) {
							if (curInter.containsLevel(i) || curInter.getQubitsNo()==RuntimeConfig.CHANNEL_CAP || (curInter.getExpectedQubits()+curInter.getQubitsNo())>=RuntimeConfig.CHANNEL_CAP)
								destinationIsFree=false;
						}
//						if (destinationIsFree){
//							System.out.println("NNNNNNNNNN   "+curInter.getQubitsNo()+" - "+curInter.getPosition()+" - "+curInter.getExpectedQubits());
//							V.add(cur1);
//							curInter.incExp();
//							dst.add(curInter.getPosition());
//						}
//						else{
							if (RuntimeConfig.HDEBUG){
								System.out.println("MMMMMMMMMMMMMM   "+curInter.getQubitsNo()+" - "+curInter.getPosition()+" - "+curInter.getExpectedQubits());
							}
							V.add(null);
							if (RuntimeConfig.HDEBUG){
								System.out.println(curInter.getPosition());
							}
							Well dstWell=layout.getNearestFreeWell(curInter.getPosition());
							dstWell.incExp();
							dst.add(dstWell.getPosition());
//						}
					}
					else{
						V.add(null);
						//assign the qubit to a location outside the box
						//should we check that this location is not used in between?
						if (RuntimeConfig.HDEBUG){
							System.out.println(destInteraction.getPosition());
						}
						Well dstWell;
						if (q.getFinalPosition().width>0){
							dstWell=layout.getWell(q.getFinalPosition());
							dstWell.incExp();
						}else{
							dstWell=layout.getNearestFreeWell(destInteraction.getPosition());
							dstWell.incExp();
						}
						dst.add(dstWell.getPosition());
					}
				}
			}
		}

		if (!v.getMoveablity()){
			for (int i = v.getOperandsNumber()-1; i>=0 ; i--) {
				q = layout.getQubit(v.getOperand(i));
				if (RuntimeConfig.HDEBUG){
					System.out.println("EEEEEEEEEEEEEEEEEEE "+q.getName()+" - "+q.getReady());
				}
				included=false;
				if (q.getPosition().equals(destInteraction.getPosition())) 
					included=true;
				if (!included && !q.getReady()){
					if (RuntimeConfig.HDEBUG){
						System.out.println("SRC111 "+q+" - "+v);
					}
					Q.add(q);
					V.add(v);
					src.add(layout.getQubit(v.getOperand(i)).getPosition());
					v.getInteraction().incExp();
					dst.add(destInteraction.getPosition());
				}
				if (included && !q.getReady()){
					if (RuntimeConfig.HDEBUG){
						System.out.println("SRC222 "+q+" - "+v+destInteraction.getPosition()+" - "+layout.getQubit(v.getOperand(i)).getPosition());
					}
					Q.add(q);
					V.add(v);
					v.getInteraction().incExp();
					src.add(destInteraction.getPosition());
					dst.add(destInteraction.getPosition());
				}
			}
			if (RuntimeConfig.HDEBUG){
				System.out.println(V);
			}
//			Path path
			GraphPath<Well, ChannelEdge> path=null;
			for (int i = 0; i < dst.size(); i++) {
				if (RuntimeConfig.HDEBUG){
					System.out.println("BBBBBBBBBBOOOOOOOOOOOOOOOGGGGGGGGGGGGGGHHHHHHHHHHHHHHHHH"+i+"   "+src.get(i)+"  "+dst.get(i)+" "+Q.get(i).getName());
				}
				if (!src.get(i).equals(dst.get(i))){
					if (RuntimeConfig.HDEBUG){
						System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAA "+Q.get(i).getName()+" - "+Q.get(i).getPosition()+" - "+dst.get(i)+" - "+V.get(i));
					}

//					path = Router.findPath(layout.getNearestNode(src.get(i)).getPosition(), layout.getNearestNode(dst.get(i)).getPosition(), layout);
					path = Router.findPath(src.get(i), dst.get(i), layout);

					if (RuntimeConfig.HDEBUG){
						System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAA "+Q.get(i).getName()+" - "+Q.get(i).getPosition()+" - "+dst.get(i)+" - "+V.get(i));
					}
					pathList.add(new Path(path, dst.get(i), Q.get(i), V.get(i), simTime, layout));

					if (RuntimeConfig.HDEBUG){
						//priniting the routed path
						if (pathList.getLast().getPath()!=null){
							System.out.println(pathList.getLast());
						}
					}
					
//					pathList.addAll(Router.router(Q.get(i), dst.get(i), simTime, layout));
					if (RuntimeConfig.DEBUG)
					{
						System.out.println(pathList.getLast());
					}
				}else{
					if (RuntimeConfig.HDEBUG){
						System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAA "+Q.get(i).getName()+" - "+Q.get(i).getPosition()+" - "+dst.get(i)+" - "+V.get(i));
					}

					if (RuntimeConfig.HDEBUG){
						System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAA "+Q.get(i).getName()+" - "+Q.get(i).getPosition()+" - "+dst.get(i)+" - "+V.get(i));
					}
					pathList.add(new Path(null, dst.get(i), Q.get(i), V.get(i), simTime, layout));
				}
			}
			if (pathList.isEmpty()){
				pathList.add(new Path(path, dst.get(0), Q.get(0), V.get(0), simTime, layout));
				System.out.println(pathList.getFirst().getDelay());
			}
		} else {
			Well dstWell=layout.getNearestFreeWell(v.getInteraction().getPosition());
			dstWell.incExp();
			for (int i = v.getOperandsNumber()-1; i>=0 ; i--) {
				q = layout.getQubit(v.getOperand(i));
				included=false;
				if (!included && !q.getReady()){
					Q.add(q);
					V.add(null);
					src.add(layout.getQubit(v.getOperand(i)).getPosition());
					dst.add(dstWell.getPosition());
				}
			}
			GraphPath<Well, ChannelEdge> path=null;
			for (int i = 0; i < dst.size(); i++) {
				if (!src.get(i).equals(dst.get(i))){
					path = Router.findPath(src.get(i), dst.get(i), layout);
					pathList.add(new Path(path, dst.get(i), Q.get(i), null, simTime, layout));
				}
			}
		}
		
		if (pathList.isEmpty())
			return null;
		return pathList;
	}
	
	
	
	
	/**
	 * Determine Qubit movement after performing an instruction.
	 *
	 * @param DFG the dfg
	 * @param v the vertex
	 * @param simTime the sim time
	 * @return the moving path
	 */
	private List<Path> QubitMovementAfterInstruction(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG, Vertex v, long simTime){
		boolean newTrapReserved=false;
		LinkedList<Path> pathList=new LinkedList<Path>();		
		LinkedList<Dimension> src=new LinkedList<Dimension>();
		LinkedList<Dimension> dst=new LinkedList<Dimension>();
		ArrayList<Well> trapEmptyList=new ArrayList<Well>();
		Dimension temp1, temp2;
		Qubit q = null;
		Interaction destInteraction=null, curInter=null;
		boolean included=false, foundDestination=false, destinationIsFree=true;
		Vertex cur0, cur1=null;
		int firstLevel=0;
		ArrayList<Qubit> Q=new ArrayList<Qubit>();	

		
		destInteraction=v.getInteraction();
		//Which Qubits are placed in this location before?
		for (Iterator<Qubit> iterator = destInteraction.getQubitSet().iterator(); iterator.hasNext();) {
			q = iterator.next();
			included=false;
			for (Iterator<Path> iterator1 = issueQueue.iterator(); iterator1.hasNext();) {
				Path p = iterator1.next();
				if (p.getQubit().equals(q)){	
					included=true;
				}
			}
			if (!included && !q.getReady()){
				//Now find destination for this qubit
				curInter=null;
				foundDestination=false;
				firstLevel=Integer.MAX_VALUE;
				//find the first next level instruction
				for (Iterator<Vertex> iterator2 = DFG.vertexSet().iterator(); iterator2.hasNext();) {
					cur0 = iterator2.next();
					if (!cur0.isSentinel() && cur0.getLevel()>v.getLevel() && cur0.haveOperand(q.getName()) && cur0.getLevel()<firstLevel){
						firstLevel=cur0.getLevel();
						foundDestination=true;
						cur1=cur0;
					}
				}
				//if new destination is found
				if (foundDestination){
					src.add(q.getPosition());
					Q.add(q);
					//there is a destination to assign (check if it is going to be used before the time needed for this qubit)
					destinationIsFree=true;
					curInter=cur1.getInteraction();
					Well dstWell=layout.getNearestFreeWell(curInter.getPosition());
					dstWell.incExp();
					dst.add(dstWell.getPosition());
				}
				else{
					//assign the qubit to a location outside the box
					//should we check that this location is not used in between?
					Well dstWell;
					if (q.getFinalPosition().width>=0){
						src.add(q.getPosition());
						Q.add(q);
						dstWell=layout.getWell(q.getFinalPosition());
						dstWell.incExp();
						dst.add(dstWell.getPosition());
					}
				}
			}
		}
		GraphPath<Well, ChannelEdge> path=null;
		for (int i = 0; i < dst.size(); i++) {
			if (!src.get(i).equals(dst.get(i))){
				path = Router.findPath(src.get(i), dst.get(i), layout);
				pathList.add(new Path(path, dst.get(i), Q.get(i), null, simTime, layout));
			}
		}
		
		if (pathList.isEmpty())
			return null;
		return pathList;
	}
	
	
	
	
	/**
	 * Event driven simulator. Having fixed instruction and qubit placement solution.
	 *
	 * @param DFG the dfg
	 * @return the length of simulation in &microsec
	 */
	public long simluateFixedPlacement(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG){
		Path temp0;
		simTime=0;
		List<Path> paths;
		ArrayList<Vertex> RQ = new ArrayList<Vertex>();
		Vertex cur0=null, cur1=null;
		int incomplete=0;

		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			cur0.setUnMoveable();
			cur0.setReadyInpts(0);
			cur0.setrank(cur0.getLevel());
		}
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.isSentinel() && cur0.getName().compareToIgnoreCase("start")==0){
				for (Iterator<DefaultWeightedEdge> iterator2 = DFG.outgoingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
					cur1 = DFG.getEdgeTarget(iterator2.next());
					cur1.setReadyInpts(1);
				}
			}
		}
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (!cur0.isSentinel() && cur0.getReadyInpts()==DFG.incomingEdgesOf(cur0).size() &&
					cur0.getLevel()==cur0.getInteraction().firstLevel() && cur0.getrank()>=0){
				RQ.add(cur0);
				cur0.setUnMoveable();
				cur0.setrank(-1);
			}
			else if (!cur0.isSentinel() && cur0.getReadyInpts()==DFG.incomingEdgesOf(cur0).size() &&
					cur0.getLevel()>cur0.getInteraction().firstLevel() && cur0.getrank()>=0){
				RQ.add(cur0);
				cur0.setMoveable();
			}
		}
		
		for (Iterator<Vertex> iterator = RQ.iterator(); iterator.hasNext();) {
			Vertex v=iterator.next();
			paths=fixedPlacementrouter(DFG, v, simTime);
			for (int i = 0; i < paths.size(); i++) {
				if (!paths.get(i).getDestination().equals(paths.get(i).getQubit().getPosition()))
					paths.get(i).getQubit().setReady(true);
			}
			issueQueue.addAll(paths);
			if (RuntimeConfig.HDEBUG){
				System.out.println(issueQueue.size()+" - "+issueQueue.peek().getDelay());
			}
		}
		

		while (!issueQueue.isEmpty()){
//			System.out.println("PPPPPPPPPPP"+issueQueue.isEmpty()+issueQueue.size()+" "+simTime);
			simTime=issueQueue.peek().getDelay();
			if(RuntimeConfig.TIME){
				outputFile.print(System.getProperty("line.separator")+"SimTime: "+simTime+"\t");
				outputFile.flush();
			}
//			else if (RuntimeConfig.VERBOSE)
//				System.out.print(System.getProperty("line.separator"));

			do{
				temp0=issueQueue.remove();
				temp0.nextMove(outputFile, resourceEstimate);
				incomplete=100;
				if (!temp0.isFinished()){
					issueQueue.add(temp0);
				}else if (temp0.isExecutionFinished()){
					try{
						temp0.getVertex().getInteraction().RemoveLevel(temp0.getVertex().getLevel());
						temp0.getVertex().getInteraction().setInProgress(false);
					}catch(Exception e){
						e.printStackTrace();
					}
					temp0.getVertex().setrank(-1);
					temp0.getVertex().getInteraction().resetExp();

					for (int i = 0; i < temp0.getVertex().getOperandsNumber() ; i++) {
						layout.getQubit(temp0.getVertex().getOperand(i)).setReady(false);						
					}
//					System.out.println(temp0.getQubit().getName());
					LinkedList<Vertex> vTemp=new LinkedList<Vertex>();
					for (Iterator<DefaultWeightedEdge> iterator = DFG.outgoingEdgesOf(temp0.getVertex()).iterator(); iterator.hasNext();) {
						cur0 = DFG.getEdgeTarget(iterator.next());
						cur0.setReadyInpts(cur0.getReadyInpts()+1);
					}
					incomplete=0;
					for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
						cur0 =  iterator.next();
						if (!cur0.isSentinel() && cur0.getReadyInpts()==DFG.incomingEdgesOf(cur0).size() &&
								cur0.getLevel()==cur0.getInteraction().firstLevel() && cur0.getrank()>=0){
							cur0.setUnMoveable();
							vTemp.add(cur0);
						}
						if (!cur0.isSentinel() && cur0.getReadyInpts()==DFG.incomingEdgesOf(cur0).size() &&
								cur0.getLevel()>cur0.getInteraction().firstLevel() && cur0.getrank()>=0 && !cur0.getMoveablity()){
							cur0.setMoveable();
							vTemp.add(cur0);
						}
						if (!cur0.isSentinel() && cur0.getrank()>0){
							if (RuntimeConfig.HDEBUG){
								System.out.print("  VERTEX = {"+cur0+"} "+"  "+cur0.getInteraction().firstLevel()+" "+cur0.getLevel()+" "+DFG.incomingEdgesOf(cur0).size()+" "+cur0.getReadyInpts()+"/// ");
								if (cur0.getLevel()<10){
									System.out.print("{");
									for (Iterator<DefaultWeightedEdge> iterator2 = DFG.incomingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
										Vertex v = DFG.getEdgeSource(iterator2.next());
										if (v.getrank()>0)
											System.out.print(v+", ");									
									}
									System.out.print("}  ");
								}
							}
							if (DFG.incomingEdgesOf(cur0).size()<cur0.getReadyInpts()){
								System.exit(-1);
							}
							if (cur0.getInteraction().firstLevel()>cur0.getLevel()){
								System.exit(-1);
							}
							incomplete++;
						}
					}
					if (RuntimeConfig.HDEBUG){	
						System.out.println("HEHE"+incomplete);
					}
					
					if (RuntimeConfig.HDEBUG){
						System.out.println(vTemp);
					}
					for (int i = 0; i < vTemp.size(); i++) {
						if (!vTemp.get(i).getMoveablity())
							vTemp.get(i).setrank(-1);
						paths=fixedPlacementrouter(DFG, vTemp.get(i), simTime);
						for (Iterator<Path> iterator = issQueue.iterator(); iterator.hasNext();) {
							Path p = iterator.next();
							issueQueue.remove(p);
						}
						if (paths!=null){
							for (int ij = 0; ij < paths.size(); ij++) {
								if (!paths.get(ij).getDestination().equals(paths.get(ij).getQubit().getPosition()))
									paths.get(ij).getQubit().setReady(true);
							}
							issueQueue.addAll(paths);
						}
					}
					paths=QubitMovementAfterInstruction(DFG, temp0.getVertex(), simTime);
					if (paths!=null){
						for (int ij = 0; ij < paths.size(); ij++) {
							if (!paths.get(ij).getDestination().equals(paths.get(ij).getQubit().getPosition()))
								paths.get(ij).getQubit().setReady(true);
						}
						issueQueue.addAll(paths);
					}
				}else if (temp0.isFinished()){
					if (temp0.getVertex()!=null){
						temp0.getVertex().getInteraction().decExp();
						if (temp0.getVertex().getInteraction().getExpectedQubits()<0)
							temp0.getVertex().getInteraction().resetExp();
					}
					temp0.getQubit().getName();
					temp0.getQubit().setReady(false);
				}
			}while(!issueQueue.isEmpty() && simTime==issueQueue.peek().getDelay());
		}
		outputFile.println();
		
		incomplete=0;
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0 =  iterator.next();
			if (!cur0.isSentinel() && cur0.getrank()>0){
				incomplete++;
			}
		}
		
		if (incomplete==0)
		return simTime;
		else return Integer.MAX_VALUE;
	}
	
	
	
	
	
	/**
	 * Event driven simulator.
	 *
	 * @return the length of simulation in &microsec
	 */
	public long simluate(){
		ChannelEdge freedChannel;
		Path temp0;
		simTime=0;
		List<Path> paths;
		
		PriorityQueue<Vertex> initList=new PriorityQueue<Vertex>(readyQueue.getNext());
		
		for (Iterator<Vertex> iterator = initList.iterator(); iterator.hasNext();) {
			Vertex v=iterator.next();
			if (RuntimeConfig.DEBUG)
				System.out.println("Routing "+v);
			paths=Router.router(v, simTime, layout);

			if (paths!=null)
			{
				issueQueue.addAll(paths);
			}else{
				busyInsts.add(v);
				v.addToQueue(simTime);
			}
		}

		while (!readyQueue.isEmpty() || !issueQueue.isEmpty()){
			//For debugging
			if (issueQueue.isEmpty() ){
				System.out.println(System.getProperty("line.separator")+"Fatal Error: No more instruction to issue!");
				System.exit(-1);
			}
			
			simTime=issueQueue.peek().getDelay();
			if(RuntimeConfig.TIME){
				System.out.print(System.getProperty("line.separator")+"SimTime:"+simTime+"\t");
			}else if (RuntimeConfig.VERBOSE){
				System.out.print(System.getProperty("line.separator"));
			}
			do{
				temp0=issueQueue.remove();
				freedChannel = temp0.nextMove(outputFile, resourceEstimate);
				if (!temp0.isFinished()){
					issueQueue.add(temp0);
				}else if (temp0.isExecutionFinished()){

					LinkedList<Vertex> vTemp=readyQueue.getNext(temp0.getVertex());
					
					for (int i = 0; i < vTemp.size(); i++) {
						if (RuntimeConfig.DEBUG)
							System.out.println("Routing "+vTemp.get(i));
//						System.out.println("Issued: "+vTemp.get(i));
						paths=Router.router(vTemp.get(i), simTime, layout);
						if (paths!=null){
							issueQueue.addAll(paths);
						}else{
							busyInsts.add(vTemp.get(i));
							vTemp.get(i).addToQueue(simTime);
							System.out.println(":(((((((((((((((((((((((((((((((((((((((((");
							System.exit(-1);
							
						}
					}
				}else{
//					}
				}
			}while(!issueQueue.isEmpty() && simTime==issueQueue.peek().getDelay());
		}
		return simTime;
	}

	/**
	 * Checks if an instruction is scheduled.
	 *
	 * @param temp the temp
	 * @param v the v
	 * @return true, if is scheduled
	 */
	private boolean isScheduled(ArrayList<ArrayList<Vertex>> temp, Vertex v){
		for (int i = 0; i < temp.size(); i++) {
			if (temp.get(i).contains(v))
				return true;
		}
		return false;
	}

	/**
	 * Basic scheduling.
	 *
	 * @param type the type
	 * @param DFG the dfg
	 * @param threshold the threshold
	 * @return true, if successful
	 */
	public boolean BasicScheduling(Scheduling type, SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG, int threshold){
		ArrayList<ArrayList<Vertex>> result=new ArrayList<ArrayList <Vertex>>();
		ArrayList<Vertex> temp=new ArrayList<Vertex>();
		
		DefaultWeightedEdge curedge; 
		int maxLevel=0;
		Vertex cur0 = null, cur1, cur2;
		
		// To Calculate the maximum Level
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			cur0.setLevel(-1);
			cur0.setReadyInpts(0);
			if (cur0.isSentinel()){
				if (cur0.getName().compareToIgnoreCase("start")==0){
					temp.add(cur0);
					cur0.setLevel(-1);
				}
			}
		}
		
		
		
		result.add(new ArrayList<Vertex>());

		while(!temp.isEmpty()){
			cur0=temp.remove(0);
			for (Iterator<DefaultWeightedEdge> iterator0 = DFG.outgoingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
				cur1=DFG.getEdgeTarget(iterator0.next());
				if (cur1.isSentinel())
					continue;
					cur1.incReadyInpts();
				if (cur1.getLevel()==-1 && cur1.getReadyInpts() == DFG.incomingEdgesOf(cur1).size()){
					int tempLevel=-1;
					for (Iterator<DefaultWeightedEdge> iterator01 = DFG.incomingEdgesOf(cur1).iterator(); iterator01.hasNext();) {
						tempLevel=Math.max(tempLevel, DFG.getEdgeSource(iterator01.next()).getLevel());
					}
					cur1.setLevel(Math.max(cur1.getMinLevel(), tempLevel+1));
					temp.add(cur1);
					while (result.size()<=cur1.getLevel())
						result.add(new ArrayList<Vertex>());
					result.get(cur1.getLevel()).add(cur1);
				}				
			}
		}
		int maximumLevel = result.size()-1;
		result.clear();
		temp.clear();
		
		
		//Initialize graph for traversing
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			cur0.setLevel(-1);
			cur0.setReadyInpts(0);
			if (cur0.isSentinel()){
				if ((type == Scheduling.ASAP && cur0.getName().compareToIgnoreCase("start")==0)||
						(type == Scheduling.ALAP && cur0.getName().compareToIgnoreCase("end")==0)){
					temp.add(cur0);
					cur0.setLevel(-1);
					if (type == Scheduling.ALAP) cur0.setLevel(maximumLevel+1);
				}
			}
		}
		
		
		
		result.add(new ArrayList<Vertex>());

		while(!temp.isEmpty()){
			cur0=temp.remove(0);
			if (type==Scheduling.ASAP){
				for (Iterator<DefaultWeightedEdge> iterator0 = DFG.outgoingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
					cur1=DFG.getEdgeTarget(iterator0.next());
//					System.out.println("ASAP "+cur1+" "+cur1.getReadyInpts()+" "+DFG.incomingEdgesOf(cur1).size()+" "+cur0+" ");
					if (cur1.isSentinel())
						continue;
						cur1.incReadyInpts();
					if (cur1.getLevel()==-1 && cur1.getReadyInpts() == DFG.incomingEdgesOf(cur1).size()){
						int tempLevel=-1;
						for (Iterator<DefaultWeightedEdge> iterator01 = DFG.incomingEdgesOf(cur1).iterator(); iterator01.hasNext();) {
							tempLevel=Math.max(tempLevel, DFG.getEdgeSource(iterator01.next()).getLevel());
						}
						cur1.setLevel(Math.max(cur1.getMinLevel(), tempLevel+1));
						temp.add(cur1);
						while (result.size()<=cur1.getLevel())
							result.add(new ArrayList<Vertex>());
						result.get(cur1.getLevel()).add(cur1);
					}				
				}
			}

			else{
				for (Iterator<DefaultWeightedEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
					cur1=DFG.getEdgeSource(iterator0.next());
//					System.out.println("ALAP "+cur1+" "+cur1.getReadyInpts()+" "+DFG.incomingEdgesOf(cur1).size()+" "+cur0+" ");
					if (cur1.isSentinel())
						continue;
					cur1.incReadyInpts();
					if (cur1.getLevel()==-1 && cur1.getReadyInpts() == DFG.outgoingEdgesOf(cur1).size()){
						int tempLevel=maximumLevel+1;
						for (Iterator<DefaultWeightedEdge> iterator01 = DFG.outgoingEdgesOf(cur1).iterator(); iterator01.hasNext();) {
							tempLevel=Math.min(tempLevel, DFG.getEdgeTarget(iterator01.next()).getLevel());
						}
						cur1.setLevel(Math.max(cur1.getMinLevel(), tempLevel-1));
//						if (cur1.getMinLevel()>0 || cur0.getMinLevel()>0)
//							System.out.println("ALAP "+cur1+" "+cur1.getLevel()+" "+cur1.getMinLevel()+" "+cur0+" "+cur0.getLevel()+" "+cur0.getMinLevel());
						temp.add(cur1);
						while (result.size()<=cur1.getLevel())
							result.add(0,new ArrayList<Vertex>());
						result.get(cur1.getLevel()).add(cur1);
					}				
				}
			}
		}
		if (type==Scheduling.ASAP){
			for (int i = 0; i < result.size(); i++) {
				for (int j = 0; j < result.get(i).size(); j++) {
					result.get(i).get(j).setASAPLevel(result.get(i).get(j).getLevel());
				}
			}
		}	
		
		if (type==Scheduling.ALAP){
			maxLevel=result.size()-1;
			for (int i = 0; i < result.size(); i++) {
				for (int j = 0; j < result.get(i).size(); j++) {
//					result.get(i).get(j).setLevel(maximumLevel - result.get(i).get(j).getLevel());
					result.get(i).get(j).setALAPLevel(result.get(i).get(j).getLevel());
				}
			}
		}
		
		if(type==Scheduling.ASAP){
		double temp0;
		//update the RANKS
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (cur0.getASAPLevel() == cur0.getALAPLevel()) {temp0 = 1000;}
			else { temp0 = (double)1/(double)(cur0.getALAPLevel()-cur0.getASAPLevel()); }
			cur0.setrank(temp0);
			cur0.setPriority((int) (-1000*temp0));
		}
		}
		
		
		
		//add priority to interdependent operations
		List<Integer> levelRank = new ArrayList<Integer>();
		boolean AddedEdge=false;
		if(type==Scheduling.ASAP){
		boolean AddEdge=true;
		int numEqLevel=0, iMax=0;
		double tempinterpr=0;
		//find out which level is the most congested one.
		for (int i = 0; i<result.size() ; i++) {
			numEqLevel=0;
			for (int j = 0; j < result.get(i).size(); j++) {
				for (int j2 = 0; j2 < result.get(i).size(); j2++) {
					if (j==j2)
						continue;
					AddEdge=false;
					if (result.get(i).get(j).haveSameOperand(result.get(i).get(j2)))
						numEqLevel++;
				}
			}
			levelRank.add(numEqLevel);
		}
		if (RuntimeConfig.HDEBUG)
		System.out.println("{     "+levelRank.size()+"      }");
		numEqLevel=0;
		for (int i = 0; i<result.size() ; i++) {
			if(numEqLevel<levelRank.get(i)){numEqLevel=levelRank.get(i);iMax=i;}
			if (RuntimeConfig.HDEBUG)
				System.out.println("{     "+levelRank.get(i)+"      }");
		}
		for (int i = 0; i<result.size() ; i++) {
			numEqLevel=0;
			if (!AddedEdge){
				for (int j = 0; j < result.get(i).size(); j++) {
					for (int j2 = 0; j2 < result.get(i).size(); j2++) {
						if (j==j2)
							continue;
						if (result.get(i).get(j).haveSameOperand(result.get(i).get(j2)) && result.get(i).get(j).getrank()<result.get(i).get(j2).getrank()){
							AddEdge=true;
							if (DFG.containsEdge(result.get(i).get(j), result.get(i).get(j2)) || DFG.containsEdge(result.get(i).get(j2), result.get(i).get(j)))
								AddEdge=false;
							if (AddEdge){ 
								AddedEdge=true;
//								System.out.println("OOO0 "+result.get(i).get(j)+" "+DFG.incomingEdgesOf(result.get(i).get(j)).size()+" + "+result.get(i).get(j2)+" "+DFG.incomingEdgesOf(result.get(i).get(j2)).size());
								curedge = DFG.addEdge(result.get(i).get(j2), result.get(i).get(j));
								addedEdges.add(curedge);
								DFG.setEdgeWeight(curedge, -5);
								if (RuntimeConfig.HDEBUG)
									System.out.println("OOO1 "+result.get(i).get(j)+" "+DFG.incomingEdgesOf(result.get(i).get(j)).size()+" + "+result.get(i).get(j2)+" "+DFG.incomingEdgesOf(result.get(i).get(j2)).size());
							}
						}
						else if (result.get(i).get(j).haveSameOperand(result.get(i).get(j2)) && result.get(i).get(j).getrank()==result.get(i).get(j2).getrank()){
							numEqLevel++;
							tempinterpr=result.get(i).get(j).getrank();
						}
					}
				}
				if (numEqLevel>1 && !AddedEdge && tempinterpr>0){
					//add an edge for cases with equal rank randomly
					for (int j = 0; j < result.get(i).size(); j++) {
						if (!AddedEdge){
							for (int j2 = 0; j2 < result.get(i).size(); j2++) {
								if (j==j2)
									continue;
								if (result.get(i).get(j).haveSameOperand(result.get(i).get(j2)) && result.get(i).get(j).getrank()==result.get(i).get(j2).getrank()){
									AddEdge=true;
									if (DFG.containsEdge(result.get(i).get(j), result.get(i).get(j2)) || DFG.containsEdge(result.get(i).get(j2), result.get(i).get(j)))
										AddEdge=false;
									if (AddEdge){
										AddedEdge=true;
										curedge = DFG.addEdge(result.get(i).get(j2), result.get(i).get(j));
										addedEdges.add(curedge);
										DFG.setEdgeWeight(curedge, -5);
										if (RuntimeConfig.HDEBUG)
											System.out.println("BBB "+result.get(i).get(j)+" + "+result.get(i).get(j2)+"  "+result.get(i).get(j).haveSameOperand(result.get(i).get(j2))+result.get(i).get(j).getrank()+result.get(i).get(j2).getrank());
									}
								}
							}
						}
					}
				}
				//check if the number of instructions in each level is greater than limit or not
				if (!AddedEdge && result.get(i).size()>threshold){
					PriorityQueue<Vertex> ranks=new PriorityQueue<Vertex>();
					for (int j = 0; j < result.get(i).size(); j++) {
						ranks.add(result.get(i).get(j));
					}
					for (int j = threshold; j <= result.get(i).size(); j++) {
						cur0=ranks.remove();
						cur0.setMinLevel(i+1);
					}
					AddedEdge=true;
				}
			}
		}
		}
		
		if (RuntimeConfig.HDEBUG){
		for (int i = 0; i < result.size(); i++) {
			for (int j = 0; j < result.get(i).size(); j++) {
				System.out.print("{"+result.get(i).get(j)+" - "+result.get(i).get(j).getASAPLevel()+" - "+result.get(i).get(j).getALAPLevel()+""+"}");
			}
			System.out.println();
		}
		}
		return !AddedEdge;
	}
	
	/**
	 * Compute earliest time that an instruction can be scheduled.
	 *
	 * @param DFG the dfg
	 */
	public void computeEarliestTimeSlack(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG){
		int minSlack=-1, maxSlack=-1;
		Vertex cur0 = null, cur1 = null, cur2 = null;
		DefaultWeightedEdge cure=null, cure2=null;
		boolean ready=false, inloop=false;
		boolean readWrite, writeWrite, readRead, writeRead;
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (cur0.isSentinel() && cur0.getName().compareToIgnoreCase("start")==0)
			cur0.setASAPLevel(0);
		}
		
		
		while (minSlack<0){
			minSlack=100;
			maxSlack=-1;
			for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
				ready=true;
				inloop=false;
				cur0=iterator.next();
				System.out.println(cur0+" - "+cur0.getASAPLevel());
				ArrayList<Integer> slackArray=new ArrayList<Integer>();
				int slackWriteWrite=-1;
				int slackWriteRead=-1;
				int slackReadRead=-1;
				if (cur0.getASAPLevel()<0){
					for (Iterator<DefaultWeightedEdge> iterator2 = DFG.incomingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
						readWrite=false;writeWrite=false;readRead=false;writeRead=false;
						inloop=true;
						cure= iterator2.next();
						cur1=DFG.getEdgeSource(cure);
						if (cur0.getOperandsNumber()>1 && cur1.getOperandsNumber()>1){
							if (cur0.getOperand(1).equals(cur1.getOperand(0))) writeRead=true;
							else if (cur0.getOperand(0).equals(cur1.getOperand(1))) readWrite=true;
							else if (cur0.getOperand(1).equals(cur1.getOperand(1))) readRead=true;
							else if (cur0.getOperand(0).equals(cur1.getOperand(0))) writeWrite=true;
						}
						else if (cur0.getOperandsNumber()==1 && cur1.getOperandsNumber()>1){
							if (cur0.getOperand(0).equals(cur1.getOperand(1))) readWrite=true;
							else if (cur0.getOperand(0).equals(cur1.getOperand(0))) writeWrite=true;
						}
						else if (cur0.getOperandsNumber()>1 && cur1.getOperandsNumber()==1){
							if (cur0.getOperand(1).equals(cur1.getOperand(0))) writeRead=true;
							else if (cur0.getOperand(0).equals(cur1.getOperand(0))) writeWrite=true;
						}
						else if (cur0.getOperandsNumber()==1 && cur1.getOperandsNumber()==1){
							writeWrite=true;
						}
						else if (cur0.getOperandsNumber()==0 || cur1.getOperandsNumber()==0){
							readWrite=true;
						}
						System.out.println(cur0+" - "+cur1+" - "+cur0.getASAPLevel()+" - "+cur1.getASAPLevel());
						if (cur1.getASAPLevel()<0){ 
							ready=false;
							break;
						}
						if (readWrite){
							slackArray.add(-1);
							System.out.println(slackArray.size());
							if (slackArray.size()>1){
								int i=0;
								for (i = 0; i < slackArray.size(); i++) {
									if (slackArray.get(i)>cur1.getASAPLevel()) break;
								}
								if (i==slackArray.size()) i--;
								for (int i2 = slackArray.size()-1; i2 >i ; i2--) {
									slackArray.set(i2, slackArray.get(i2-1));
								}
								slackArray.set(i, cur1.getASAPLevel());
							}
							else{
								slackArray.set(0, cur1.getASAPLevel());
							}
						}
						else if (writeRead){
							slackWriteRead = cur1.getASAPLevel();
						}
						else if (writeWrite){
							slackWriteWrite = cur1.getASAPLevel();
						}
						else if (readRead){
							slackReadRead = cur1.getASAPLevel();
						}
					}
					if (ready && inloop && !cur0.isSentinel()){
						for (int i1 = 0; i1 < slackArray.size(); i1++) {
							if(i1==0) slackArray.set(0, slackArray.get(0)+1);
							else slackArray.set(i1, Math.max(slackArray.get(i1-1)+1, slackArray.get(i1)+1));
						}
						int maxSlackChild=-1;
						for (int i1 = 0; i1 < slackArray.size(); i1++) {
							maxSlackChild=Math.max(maxSlackChild, slackArray.get(i1));
						}
						maxSlackChild=Math.max(maxSlackChild, slackWriteRead+1);
						maxSlackChild=Math.max(maxSlackChild, slackWriteWrite+1);
						maxSlackChild=Math.max(maxSlackChild, slackReadRead+1);
						cur0.setASAPLevel(maxSlackChild);
						System.out.println(cur0+" - "+cur0.getASAPLevel());
					}
					else if (ready && inloop && cur0.isSentinel()){
						int maxSlackChild=-1;
						slackArray.removeAll(slackArray);
						System.out.println("PPPP"+slackArray.size());
						for (Iterator<DefaultWeightedEdge> iterator2 = DFG.incomingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
							maxSlackChild=-1;
							cure= iterator2.next();
							cur1=DFG.getEdgeSource(cure);
							ArrayList<Integer> slackArray1=new ArrayList<Integer>();
							slackArray1.add(cur1.getASAPLevel());
							for (Iterator<DefaultWeightedEdge> iterator3 = DFG.incomingEdgesOf(cur0).iterator(); iterator3.hasNext();) {
								cure2= iterator3.next();
								cur2=DFG.getEdgeSource(cure2);
								System.out.println(cur1+" - "+cur2);
								if (cur1.equals(cur2)) continue;
								if (cur1.haveSameOperand(cur2)){
									slackArray1.add(-1);
									int i=0;
									for (i = 0; i < slackArray1.size(); i++) {
										if (slackArray1.get(i)>cur2.getASAPLevel()) break;
									}
									if (i==slackArray1.size()) i--;
									for (int i2 = slackArray1.size()-1; i2 >i ; i2--) {
										slackArray1.set(i2, slackArray1.get(i2-1));
									}
									slackArray1.set(i, cur2.getASAPLevel());
								}
							}
							for (int i1 = 0; i1 < slackArray1.size(); i1++) {
								if(i1==0) slackArray1.set(0, slackArray1.get(0));
								else slackArray1.set(i1, Math.max(slackArray1.get(i1-1)+1, slackArray1.get(i1)));
							}
							for (int i1 = 0; i1 < slackArray1.size(); i1++) {
								maxSlackChild=Math.max(maxSlackChild, slackArray1.get(i1));
							}
							System.out.println(slackArray1.size()+" - "+maxSlackChild+" - "+slackArray1);
							slackArray.add(maxSlackChild);
						}
						maxSlackChild=-1;						
						for (int i1 = 0; i1 < slackArray.size(); i1++) {
							maxSlackChild=Math.max(maxSlackChild, slackArray.get(i1));
						}
						cur0.setASAPLevel(maxSlackChild);
						System.out.println(cur0+" - "+cur0.getASAPLevel());
					}
				}
				if (cur0.getASAPLevel()<=minSlack) minSlack=cur0.getASAPLevel();
				if (cur0.getASAPLevel()>maxSlack) maxSlack=cur0.getASAPLevel();
			}
		}
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			System.out.println(cur0+" - "+cur0.getASAPLevel());
		}
	}
	
	/**
	 * Compute the time window that an instruction can be scheduled to guarantee minimum latency operation.
	 *
	 * @param DFG the dfg
	 */
	public void computeSlack(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG){
		int minSlack=-1, maxSlack=-1;;
		Vertex cur0 = null, cur1 = null;
		DefaultWeightedEdge cure=null;
		boolean ready=false, inloop=false;
		boolean readWrite, writeWrite, readRead, writeRead;
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (cur0.isSentinel() && cur0.getName().compareToIgnoreCase("end")==0)
			cur0.setSlack(cur0.getASAPLevel()+1);
		}
		
		
		while (minSlack<0){
			minSlack=100;
			maxSlack=-1;
			for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
				ready=true;
				inloop=false;
				cur0=iterator.next();
				System.out.println(cur0+" - "+cur0.getSlack());
				ArrayList<Integer> slackArray=new ArrayList<Integer>();
				int slackWriteWrite=100;
				int slackReadWrite=100;
				int slackReadRead=100;
				if (cur0.getSlack()<0){
					for (Iterator<DefaultWeightedEdge> iterator2 = DFG.outgoingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
						readWrite=false;writeWrite=false;readRead=false;writeRead=false;
						inloop=true;
						cure= iterator2.next();
						cur1=DFG.getEdgeTarget(cure);
						if (cur0.getOperandsNumber()>1 && cur1.getOperandsNumber()>1){
							if (cur0.getOperand(0).equals(cur1.getOperand(1))) writeRead=true;
							else if (cur0.getOperand(1).equals(cur1.getOperand(0))) readWrite=true;
							else if (cur0.getOperand(1).equals(cur1.getOperand(1))) readRead=true;
							else if (cur0.getOperand(0).equals(cur1.getOperand(0))) writeWrite=true;
						}
						else if (cur0.getOperandsNumber()==1 && cur1.getOperandsNumber()>1){
							if (cur0.getOperand(0).equals(cur1.getOperand(1))) writeRead=true;
							else if (cur0.getOperand(0).equals(cur1.getOperand(0))) writeWrite=true;
						}
						else if (cur0.getOperandsNumber()>1 && cur1.getOperandsNumber()==1){
							if (cur0.getOperand(1).equals(cur1.getOperand(0))) readWrite=true;
							else if (cur0.getOperand(0).equals(cur1.getOperand(0))) writeWrite=true;
						}
						else if (cur0.getOperandsNumber()==1 && cur1.getOperandsNumber()==1){
							writeWrite=true;
						}
						else if (cur0.getOperandsNumber()==0 || cur1.getOperandsNumber()==0){
							writeRead=true;
						}
						System.out.println(cur0+" - "+cur1+" - "+cur0.getSlack()+" - "+cur1.getSlack());
						if (cur1.getSlack()<0){ 
							ready=false;
							break;
						}
						if (writeRead){
							slackArray.add(-1);
							System.out.println(slackArray.size());
							if (slackArray.size()>1){
								int i=0;
								for (i = 0; i < slackArray.size(); i++) {
									if (slackArray.get(i)<cur1.getSlack()) break;
								}
								if (i==slackArray.size()) i--;
								for (int i2 = slackArray.size()-1; i2 >i ; i2--) {
									slackArray.set(i2, slackArray.get(i2-1));
								}
								slackArray.set(i, cur1.getSlack());
							}
							else{
								slackArray.set(0, cur1.getSlack());
							}
						}
						else if (readWrite){
							slackReadWrite = cur1.getSlack();
						}
						else if (writeWrite){
							slackWriteWrite = cur1.getSlack();
						}
						else if (readRead){
							slackReadRead = cur1.getSlack();
						}
					}
					if (ready && inloop && !cur0.isSentinel()){
						for (int i1 = 0; i1 < slackArray.size(); i1++) {
							if(i1==0) slackArray.set(0, slackArray.get(0)-1);
							else slackArray.set(i1, Math.min(slackArray.get(i1-1)-1, slackArray.get(i1)-1));
						}
						int maxSlackChild=100;
						for (int i1 = 0; i1 < slackArray.size(); i1++) {
							maxSlackChild=Math.min(maxSlackChild, slackArray.get(i1));
						}
						maxSlackChild=Math.min(maxSlackChild, slackReadWrite-1);
						maxSlackChild=Math.min(maxSlackChild, slackWriteWrite-1);
						maxSlackChild=Math.min(maxSlackChild, slackReadRead-1);
						cur0.setSlack(Math.max(maxSlackChild, cur0.getASAPLevel()));
						System.out.println(cur0+" - "+cur0.getSlack());
					}
					else if (ready && inloop && cur0.isSentinel()){
						int maxSlackChild=100;
						for (int i1 = 0; i1 < slackArray.size(); i1++) {
							maxSlackChild=Math.min(maxSlackChild, slackArray.get(i1));
						}
						cur0.setSlack(Math.max(maxSlackChild-1, cur0.getASAPLevel()));
						System.out.println(cur0+" - "+cur0.getSlack());
					}
				}
				if (cur0.getSlack()<=minSlack) minSlack=cur0.getSlack();
				if (cur0.getSlack()>maxSlack) maxSlack=cur0.getSlack();
			}
		}
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			System.out.println(cur0+" - "+cur0.getASAPLevel()+" - "+cur0.getSlack());
		}
	}
	
	/**
	 * Compute list scheduling ranking parameter.
	 *
	 * @param DFG the dfg
	 */
	public void computeListSchRank(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG){
		double temp;
		Vertex cur0 = null;
//		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
//			cur0=iterator.next();
//			cur0.setSlack(cur0.getLevel());
//		}
//		BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, DFG);
//		BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, DFG);
//		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
//			cur0=iterator.next();
//			cur0.setLevel(cur0.getSlack());
//			cur0.setASAPLevel(cur0.getLevel());
//		}
		//Initialize graph for traversing
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			cur0.setLevel(cur0.getASAPLevel());
			if (cur0.getASAPLevel() == cur0.getALAPLevel()) {temp = 1000;}
			else { temp = (double)1/(double)(cur0.getALAPLevel()-cur0.getASAPLevel()); }
			cur0.setrank(temp);
//			if (RuntimeConfig.HDEBUG) System.out.println(cur0+" "+cur0.getLevel()+" "+cur0.getrank());
		}
	}
	
	
	/**
	 * Make spring network to be used in placement solution.
	 *
	 * @param DFG the dfg
	 * @param R,K,mD force-directed placement parameters
	 * @return a simple weighted graph
	 */
	public SimpleWeightedGraph<Vertex, DefaultWeightedEdge> makeSpringNetwork(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG, double R, double K, double mD){
		SimpleWeightedGraph<Vertex, DefaultWeightedEdge> springNet = new SimpleWeightedGraph<Vertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		DefaultWeightedEdge temp0, temp1;
		Vertex cur0, cur1, cur2, cur3;
		DefaultWeightedEdge cure = null;
		int maxLevel=0;
		boolean hasOp=false;
		double repulsion = R;
		double temp;
		double minimumDeadline = mD;
		double moveDelay = 1;
		double gateDelay = K;
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (!cur0.isSentinel()){
				springNet.addVertex(cur0);
				cur0.setMoveable();
//				if (cur0.getLevel()==0) cur0.setUnMoveable();
			}
		}
		
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (!cur0.isSentinel()){				
				for (int j = 0; j < cur0.getOperandsNumber(); j++) {
					String curo = cur0.getOperand(j);
					maxLevel=0;
					for (Iterator<DefaultWeightedEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
						cure=iterator0.next();
						cur1=DFG.getEdgeSource(cure);
						hasOp=false;
						for (int i = 0; i < cur1.getOperandsNumber(); i++) {
							if (cur1.getOperand(i).equals(curo))
								hasOp=true;
						}
						if (hasOp)
							maxLevel=Math.max(maxLevel, cur1.getLevel());
					}
					for (Iterator<Vertex> iterator1 = DFG.vertexSet().iterator(); iterator1.hasNext();) {
						cur1=iterator1.next();
						hasOp=false;
						for (int i1 = 0; i1 < cur1.getOperandsNumber(); i1++) {
							if (cur1.getOperand(i1).equals(curo))
								hasOp=true;
						}
						if (hasOp && cur1.getLevel()>maxLevel && cur1.getLevel()<cur0.getLevel()){
//							System.out.println("PPPPPPPPPPPPPPP"+cur0+" "+cur1+" ");
							cure=DFG.addEdge(cur1, cur0);
							DFG.setEdgeWeight(cure, -10);
							addedEdges.add(cure);
						}
					}
				}
			}
		}		
		
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (!cur0.isSentinel()){				
				for (int j = 0; j < cur0.getOperandsNumber(); j++) {
					String curo = cur0.getOperand(j);
					maxLevel=0;
					for (Iterator<DefaultWeightedEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
						cure=iterator0.next();
						cur1=DFG.getEdgeSource(cure);
						hasOp=false;
						for (int i = 0; i < cur1.getOperandsNumber(); i++) {
							if (cur1.getOperand(i).equals(curo))
								hasOp=true;
						}
						if (hasOp)
							maxLevel=Math.max(maxLevel, cur1.getLevel());
					}
					for (Iterator<DefaultWeightedEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
						cure=iterator0.next();
						cur1=DFG.getEdgeSource(cure);
						hasOp=false;
						for (int i = 0; i < cur1.getOperandsNumber(); i++) {
							if (cur1.getOperand(i).equals(curo))
								hasOp=true;
						}
						if (hasOp && maxLevel> cur1.getLevel()){
							DFG.setEdgeWeight(cure, -5);
						}
						else if (hasOp && maxLevel== cur1.getLevel()){
							DFG.setEdgeWeight(cure, 1);
						}
					}
				}
			}
		}		
		for (Iterator<DefaultWeightedEdge> iterator = DFG.edgeSet().iterator(); iterator.hasNext();) {
			temp0=iterator.next();
			if (DFG.getEdgeWeight(temp0)!=-5 && !DFG.getEdgeSource(temp0).isSentinel() && !DFG.getEdgeTarget(temp0).isSentinel() ){
				cur0=DFG.getEdgeTarget(temp0);
				temp = ((double)1/((double)1/cur0.getrank()+-1+Math.abs(cur0.getLevel()-DFG.getEdgeSource(temp0).getLevel()))) /(double)gateDelay;
				if (cur0.getrank()==1000) temp = (double)1/(double)minimumDeadline;
//				System.out.println(DFG.getEdgeSource(temp0)+" - "+ DFG.getEdgeTarget(temp0)+" - "+ DFG.getEdgeWeight(temp0)+" "+DFG.getEdgeSource(temp0).getLevel());
				if (!springNet.containsEdge(DFG.getEdgeSource(temp0),DFG.getEdgeTarget(temp0))){
					temp1=springNet.addEdge(DFG.getEdgeSource(temp0), DFG.getEdgeTarget(temp0));
					springNet.setEdgeWeight(temp1, temp);
				}
			}
		}
		
		
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (!cur0.isSentinel()){				
				for (Iterator<Vertex> iterator1 = DFG.vertexSet().iterator(); iterator1.hasNext();) {
					cur1 = iterator1.next();
					if (cur1.equals(cur0) || cur1.isSentinel()) continue;
					if (cur0.getLevel()==cur1.getLevel()){
						boolean AddEdge=true;
						for (Iterator<DefaultWeightedEdge> iterator0 = springNet.edgesOf(cur1).iterator(); iterator0.hasNext();) {
							temp0=iterator0.next();
							cur2=DFG.getEdgeTarget(temp0);
							cur3=DFG.getEdgeSource(temp0);
							if (cur2.equals(cur0) || cur3.equals(cur0)){
								AddEdge=false;
							}
						}
						if (AddEdge){
							temp1=springNet.addEdge(cur0, cur1);
							springNet.setEdgeWeight(temp1, repulsion);
						}
					}
				}
			}
		}
		
		for (Iterator<DefaultWeightedEdge> iterator = springNet.edgeSet().iterator(); iterator.hasNext();) {
			temp0=iterator.next();
			cur0=springNet.getEdgeTarget(temp0);
//			System.out.println(springNet.getEdgeSource(temp0)+" - "+ springNet.getEdgeTarget(temp0)+" - "+ springNet.getEdgeWeight(temp0)+" "+springNet.getEdgeSource(temp0).getLevel());
		}
//		System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
		
		return springNet;
	}
	

	/**
	 * An old version of force-directed placement solution.
	 *
	 * @param springNet the spring net
	 * @param layout the layout
	 * @param tol the tol
	 * @return the int
	 */
	public  int springMove(SimpleWeightedGraph<Vertex, DefaultWeightedEdge> springNet, Layout layout, double tol){
		Dimension size=layout.getLayoutSize(), center = new Dimension();
		DefaultWeightedEdge temp0, temp1;
		Vertex cur0, cur1;
		
//		System.out.println(size.height+"   "+size.width);
//		System.exit(-1);
		//assume (0,0) as the center of fabric and center of mass for movable items
		double xMin = 0, xMax = size.width, yMin = 0, yMax = size.height;
		double Fx=0, Fy=0, Fpx=0, Fpy=0, dx=0, dy=0, ds=0, k=0, sumMove=1000;
		double sumFx=(tol*springNet.vertexSet().size()), sumFy=(tol*springNet.vertexSet().size()),FCMX, FCMY, fixedConstant=2;
		
		//Initialize the location of gates
		//Also we can assume there are some fixed creation module placed in the metric as non-movable items.
		int index=-1;
		while((sumFx+sumFy)>(tol*springNet.vertexSet().size())){
//			System.out.println("******************************************");
//			System.out.println("one");
//			System.out.println("******************************************");
			//Calculate Fx and Fy
			int fixeditem=0;
			FCMX=0;
			FCMY=0;
			sumFx=0;
			sumFy=0;
			sumMove=0;
			for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
				cur0 = iterator.next();
				if (!cur0.getMoveablity()){
					fixeditem++;
					Fx=0; Fy=0; Fpx=0; Fpy=0;
					for (Iterator<DefaultWeightedEdge> iterator2 = springNet.edgesOf(cur0).iterator(); iterator2.hasNext();) {
						temp0 = iterator2.next();
						cur1 = springNet.getEdgeTarget(temp0);
						if (cur1.getMoveablity()){
							dx = (cur1.getx()-cur0.getx());
							dy = (cur1.gety()-cur0.gety());
							ds = (Math.abs(dx)+Math.abs(dy));
							k = springNet.getEdgeWeight(temp0);
							if (k >0){
								FCMX += - k * dx;
								FCMY += - k * dy;
							}
						}
					}
				}
			}
			FCMX=FCMX/(springNet.vertexSet().size()-fixeditem);
			FCMY=FCMY/(springNet.vertexSet().size()-fixeditem);			
			for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
				cur0 = iterator.next();
				if (cur0.getMoveablity()){
					Fx=-FCMX; Fy=-FCMX; Fpx=0; Fpy=0;
					for (Iterator<DefaultWeightedEdge> iterator2 = springNet.edgesOf(cur0).iterator(); iterator2.hasNext();) {
						temp0 = iterator2.next();
						cur1 = springNet.getEdgeTarget(temp0);
						if (cur1.equals(cur0)) cur1 = springNet.getEdgeSource(temp0);
//						System.out.println("{"+cur0+" - "+cur1+"}"+springNet.getEdgeWeight(temp0));
//						System.out.println("{"+cur0+" -"+cur0.getLevel()+" "+"("+cur0.getx()+","+cur0.gety()+")"+"}");
//						System.out.println("{"+cur1+" -"+cur1.getLevel()+" "+"("+cur1.getx()+","+cur1.gety()+")"+"}");
						dx = (cur1.getx()-cur0.getx());
						dy = (cur1.gety()-cur0.gety());
						ds = (Math.abs(dx)+Math.abs(dy));
						k = springNet.getEdgeWeight(temp0);
						if (cur1.getMoveablity()){
							if (k < 0){
								if (ds<0.5){
									Fx += 5;	Fy += 5;Fpx += 0.01;Fpy += 0.01;
								}
								else if (ds<2){
									Fx += -2*k * dx/ds;
									Fy += -2*k * dy/ds;
									Fpx += -2*k* Math.abs(dx)/Math.pow(ds, 2);
									Fpy += -2*k* Math.abs(dy)/Math.pow(ds, 2);
								}						
								else if (ds<4){
									Fx += -k * dx/ds;
									Fy += -k * dy/ds;
									Fpx += -k* Math.abs(dx)/Math.pow(ds, 2);
									Fpy += -k* Math.abs(dy)/Math.pow(ds, 2);
								}
								else if (ds<7){
									Fx += -0.7*k * dx/ds;
									Fy += -0.7*k * dy/ds;
									Fpx += -0.7*k* Math.abs(dx)/Math.pow(ds, 2);
									Fpy += -0.7*k* Math.abs(dy)/Math.pow(ds, 2);
								}
								else if (ds<11){
									Fx += -0.5*k * dx/ds;
									Fy += -0.5*k * dy/ds;
									Fpx += -0.5*k* Math.abs(dx)/Math.pow(ds, 2);
									Fpy += -0.5*k* Math.abs(dy)/Math.pow(ds, 2);
								}
								else {
									Fx += -0.1*k * dx/ds;
									Fy += -0.1*k * dy/ds;
									Fpx += -0.1*k* Math.abs(dx)/Math.pow(ds, 2);
									Fpy += -0.1*k* Math.abs(dy)/Math.pow(ds, 2);
								}
							}
							else{
								Fx += - k * dx;
								Fy += - k * dy;
								Fpx += - k;
								Fpy += - k;
							}
						}
						else{
							if (ds<0.2){
								Fpx += fixedConstant*k/(springNet.vertexSet().size()-fixeditem);
								Fpy += fixedConstant*k/(springNet.vertexSet().size()-fixeditem);}
							else if (ds<1){
								Fpx += fixedConstant*0.2*k/(springNet.vertexSet().size()-fixeditem);
								Fpy += fixedConstant*0.2*k/(springNet.vertexSet().size()-fixeditem);
							}
							else if (ds<2){
								Fpx += fixedConstant*0.5*k/(springNet.vertexSet().size()-fixeditem);
								Fpy += fixedConstant*0.5*k/(springNet.vertexSet().size()-fixeditem);
							}
							else if (ds<4){
								Fpx += fixedConstant*0.8*k/(springNet.vertexSet().size()-fixeditem);
								Fpy += fixedConstant*0.8*k/(springNet.vertexSet().size()-fixeditem);
							}
							else{
								Fpx += fixedConstant*k/(springNet.vertexSet().size()-fixeditem);
								Fpy += fixedConstant*k/(springNet.vertexSet().size()-fixeditem);
							}
						}
//						System.out.println("ppp "+cur0+" ppp "+cur1+" ppp "+Fx+" ppp "+Fy+" ppp "+Fpx+" ppp "+Fpy);
					}
					sumFx+=Math.abs(Fx);
					sumFy+=Math.abs(Fy);
//					System.out.println(Fx+" "+Fy+" "+Fpx+" "+Fpy+" "+cur0.getx()+" "+cur0.gety());
					cur0.setFx(cur0.getx()+0.01*(double)(Fx)/(double)Fpx);
					cur0.setFy(cur0.gety()+0.01*(double)(Fy)/(double)Fpy);
					if (cur0.getFx()>(1+cur0.getx())) cur0.setFx((1+cur0.getx()));
					if (cur0.getFx()<(-1+cur0.getx())) cur0.setFx((-1+cur0.getx()));
					if (cur0.getFy()>(1+cur0.gety())) cur0.setFy((1+cur0.gety()));
					if (cur0.getFy()<(-1+cur0.gety())) cur0.setFy((-1+cur0.gety()));
//					if (Math.abs(cur0.getFx()-cur0.getx())<0.05) cur0.setFx((cur0.getx()));
//					if (Math.abs(cur0.getFy()-cur0.gety())<0.05) cur0.setFy((cur0.gety()));
//					System.out.println("{"+cur0+" - "+"("+cur0.getx()+","+cur0.gety()+")"+"}");
				}
			}
			center.setSize(0, 0);
//			System.out.println("Final up to this point");
			for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
				cur0 = iterator.next();
				center.setSize(center.width+cur0.getx(), center.height+cur0.gety());
//				System.out.println("{"+cur0+" - "+cur0.getLevel()+" - "+" "+"("+Math.round(cur0.getx())+","+Math.round(cur0.gety())+")"+"}");
				sumMove+=Math.abs(cur0.getFx()-cur0.getx())+Math.abs(cur0.getFy()-cur0.gety());
				if (cur0.getMoveablity()){
					cur0.setx(Math.min(Math.max(cur0.getFx(), 0), xMax));
					cur0.sety(Math.min(Math.max(cur0.getFy(), 0), yMax));
				}
			}
//			JFrame frame = new JFrame("Title!"+index);
//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			frame.setSize(20*layout.getLayoutSize().width, 20*layout.getLayoutSize().height);
//			frame.setVisible(true);
//			GatePlacementPlotter panel = new GatePlacementPlotter(springNet,layout);
//			frame.add(panel);
			center.setSize((double)center.width/(double)springNet.vertexSet().size(), (double)center.height/(double)springNet.vertexSet().size());
//			System.out.println("Final up to this point"+"----------------------------------"+sumMove+" "+sumFx+" "+sumFy+" "+5*springNet.vertexSet().size());
		}
//		boolean hadi=true;
//		while(hadi){
			
//		}
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
//			cur0.setx(cur0.getx()-(center.width-size.width/(double)2));
//			cur0.sety(cur0.gety()-(center.height-size.height/(double)2));
//			System.out.println("{"+cur0+" -"+cur0.getLevel()+" "+"("+(cur0.getx())+","+(cur0.gety())+")"+"}");
		}
		return 0;
	}
	
	
	/**
	 * Place instructions related to an old placement solution.
	 *
	 * @param springNet the spring net
	 * @param layout the layout
	 * @param level the level
	 * @return the int
	 */
	public  int placeGates(SimpleWeightedGraph<Vertex, DefaultWeightedEdge> springNet, Layout layout, int level){
		Dimension size=layout.getLayoutSize(), center = new Dimension(), place=new Dimension();
		DefaultWeightedEdge temp0, temp1;
		Vertex cur0, cur1;
		Interaction AssignedTrap;
		
		layout.clean();
//		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//		System.out.println("			Lelel"+level);
//		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.getLevel()==level){
				cur0.setUnMoveable();
				place.height = (int)cur0.gety();
				place.width  = (int)cur0.getx();
				layout.sortInteractionWells(place, false,  2);
				AssignedTrap=(Interaction)layout.getNearestFreeInteraction(place, true);
//				System.out.println(level+"  "+AssignedTrap);
				AssignedTrap.AddLevel(level);
				cur0.setTrap(AssignedTrap);
				cur0.setx(AssignedTrap.getPosition().width);
				cur0.sety(AssignedTrap.getPosition().height);
//				System.out.println("{"+cur0+" - "+cur0.getLevel()+" - "+" "+"("+Math.round(cur0.getx())+","+Math.round(cur0.gety())+")"+"}");
			}
		}
		return 0;
	}
	
	/**
	 * Place instructions related to an old placement solution.
	 *
	 * @param springNet the spring net
	 * @param layout the layout
	 * @param tol1 the tol1
	 * @param tol2 the tol2
	 * @return the int
	 */
	public  int placeGatesFull(SimpleWeightedGraph<Vertex, DefaultWeightedEdge> springNet, Layout layout, double tol1 , double tol2){
		int maxLevel=0;
		Vertex cur0;
		
		
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			maxLevel=Math.max(maxLevel, cur0.getLevel());
		}
//		System.out.println("******************************************");
//		System.out.println("zero");
//		System.out.println("******************************************");
		springMove(springNet, layout, tol1);
		placeGates(springNet, layout, 0);
//		System.out.println("HEEEEEEEEEEEEEEEELLLLLLLLLLLLLLLLLLLLOOOOOOOOOOOO			"+maxLevel);
		for (int i = 1; i <= maxLevel; i++) {
//			System.out.println("******************************************");
//			System.out.println(i);
//			System.out.println("******************************************");
			springMove(springNet, layout, tol2);
			placeGates(springNet, layout, i);
//			System.out.println("HEEEEEEEEEEEEEEEELLLLLLLLLLLLLLLLLLLLOOOOOOOOO			"+i);
		}
		
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (RuntimeConfig.HDEBUG)
				System.out.println("{"+cur0+" -"+cur0.getLevel()+" "+"("+(cur0.getx())+","+(cur0.gety())+")"+"}");
		}
		
		return 0;
	}


	/**
	 * Adds the dummy edges for force directed scheduling.
	 *
	 * @param DFG the dfg
	 */
	public void addDummyEdgesForceDirectedScheduling(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG) {
		double temp;
		Vertex cur0 = null, cur1 = null;
		DefaultWeightedEdge cure=null;
		//Initialize graph for traversing
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			for (Iterator<Vertex> iterator2 = DFG.vertexSet().iterator(); iterator2.hasNext();) {
				cur1=iterator2.next();
				if (cur0.equals(cur1)) continue;
				if (cur0.haveSameOperand(cur1) && cur0.getASAPLevel()<cur1.getASAPLevel()){
					cure=DFG.addEdge(cur0, cur1);
					if (cure!= null){
						DFG.setEdgeWeight(cure, -100);
						addedEdges.add(cure);
					}
				}	
			}
		}
	}
	
	/**
	 * Update springr's constant in force-directed scheduling.
	 *
	 * @param DFG the dfg
	 * @param maxLevel the max level
	 * @return the array list
	 */
	public ArrayList<Double> updateK(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG, int maxLevel){
		ArrayList<Double> K = new ArrayList<Double>();
		for (int i = 0; i <= maxLevel; i++) {
			K.add((double) 0);
		}
		Vertex cur0 = null;
		//Initialize graph for traversing
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (!cur0.isSentinel()){
				if (cur0.getLevel()>=0){
					K.set(cur0.getLevel(),K.get(cur0.getLevel())+1);
				}
				else{
					for (int i = cur0.getASAPLevel(); i <= cur0.getALAPLevel(); i++) {
						if (K.size()>i+1 && cur0.getALAPLevel()>cur0.getASAPLevel()){
							K.set(i, K.get(i)+(double)1/(1+cur0.getALAPLevel()-cur0.getASAPLevel()));
						}
						else if (K.size()>i+1 && cur0.getALAPLevel()==cur0.getASAPLevel()){
							K.set(i, K.get(i)+1);
						}
						else{
							for (int j = K.size(); j < i+1; j++) {
								K.add((double) 0);
							}
							if (cur0.getALAPLevel()>cur0.getASAPLevel()){
								K.set(i, K.get(i)+(double)1/(1+cur0.getALAPLevel()-cur0.getASAPLevel()));
							}
							else if (cur0.getALAPLevel()==cur0.getASAPLevel()){
								K.set(i, K.get(i)+1);
							}
						}
					}
				}
			}
		}
		return K;
	}
	
	/**
	 * Force directed scheduling solution.
	 *
	 * @param DFG the dfg
	 */
	public void forceDirectedScheduling(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG) {
		ArrayList<Double> K = new ArrayList<Double>();
		int temp=0, maxLevel=0;
		Vertex cur0 = null, cur1 = null;
		DefaultWeightedEdge cure=null;
		//Initialize graph for traversing
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			maxLevel=Math.max(maxLevel, cur0.getALAPLevel());
			if (!cur0.isSentinel()){
				if (cur0.getALAPLevel()==cur0.getASAPLevel()){
					cur0.setLevel(cur0.getASAPLevel());
				} 
				else{
					cur0.setLevel(-1);
					cur0.setx(0);
				}
			}
		}
		updateASAPALAPLevels(DFG,maxLevel);
		K = updateK(DFG, maxLevel);
//		System.out.println(cur0+" - "+" - "+cur0.getLevel()+" - "+cur0.getASAPLevel()+" - "+cur0.getALAPLevel()+" - ");
		while (cur0!=null){		
			cur0 = calculateMovementForce(DFG, K);
			if (cur0==null) continue;
//			System.out.println(K);
//			System.out.println(cur0+" - "+" - level = "+cur0.getLevel()+" - x = "+cur0.getx()+" - y = "+cur0.gety()+" -ASAP = "+cur0.getASAPLevel()+" - ALAP= "+cur0.getALAPLevel()+" - ");
			if (cur0.getLevel()>=0) cur0=null;
			cur0.setLevel((int) cur0.gety());
			for (Iterator<DefaultWeightedEdge> iterator2 = DFG.outgoingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
				cure=iterator2.next();
				cur1=DFG.getEdgeTarget(cure);
				if (!cur1.isSentinel() && cur1.getASAPLevel()<=cur0.getLevel() && !cur1.equals(cur0)){
					if (cur1.getLevel()<0){
						cur1.setASAPLevel(cur0.getLevel()+1);
					}
					if (cur1.getALAPLevel()<cur1.getASAPLevel()){
						System.out.println("OUT "+cur0+" - "+cur1+" - "+cur1.getLevel()+" - "+cur1.getASAPLevel()+" - "+cur1.getALAPLevel()+" - ");
						cur1.setASAPLevel(Math.max(cur1.getALAPLevel(), cur1.getASAPLevel()));
						cur1.setALAPLevel(Math.max(cur1.getALAPLevel(), cur1.getASAPLevel()));
//						System.exit(-1);
					}
					if (cur1.getALAPLevel()==cur1.getASAPLevel()){
						cur1.setLevel(cur1.getALAPLevel());
					}
					updateASAPALAPLevels(DFG,maxLevel);
				}
			}
			for (Iterator<DefaultWeightedEdge> iterator2 = DFG.incomingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
				cure=iterator2.next();
				cur1=DFG.getEdgeSource(cure);
				if (!cur1.isSentinel() && cur1.getALAPLevel()>=cur0.getLevel() && !cur1.equals(cur0)){
					if (cur1.getLevel()<0){
						cur1.setALAPLevel(cur0.getLevel()-1);
					}
					if (cur1.getALAPLevel()<cur1.getASAPLevel()){
						System.out.println("IN "+cur0+" - "+cur1+" - "+cur1.getLevel()+" - "+cur1.getASAPLevel()+" - "+cur1.getALAPLevel()+" - ");
						cur1.setASAPLevel(Math.max(cur1.getALAPLevel(), cur1.getASAPLevel()));
						cur1.setALAPLevel(Math.max(cur1.getALAPLevel(), cur1.getASAPLevel()));
//						System.exit(-1);
					}
					if (cur1.getALAPLevel()==cur1.getASAPLevel()){
						cur1.setLevel(cur1.getALAPLevel());
					}
					updateASAPALAPLevels(DFG,maxLevel);
				}
			}
			K = updateK(DFG, maxLevel);
		}
		if (RuntimeConfig.HDEBUG){
		for (int i = 0; i < K.size(); i++) {
			for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
				cur0=iterator.next();
				if (cur0.getLevel()==i)
				System.out.print("{"+cur0+" - "+cur0.getASAPLevel()+" "+cur0.getALAPLevel()+" "+ cur0.getLevel()+"}");
			}
			System.out.println();
		}}
	}


	/**
	 * Update asap and alap levels.
	 *
	 * @param DFG the dfg
	 * @param maxLevel the maximum number of levels
	 */
	private void updateASAPALAPLevels(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG, int maxLevel) {
		for (int i = 0; i < maxLevel; i++) {
			for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
				Vertex cur0 = iterator.next();
				DefaultWeightedEdge cure;
				Vertex cur1;
				if (cur0.getASAPLevel()==i){
					for (Iterator<DefaultWeightedEdge> iterator2 = DFG.outgoingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
						cure=iterator2.next();
						cur1=DFG.getEdgeTarget(cure);
						if (!cur1.isSentinel()){
							if (cur0.getLevel()>=0) cur1.setASAPLevel(Math.max(cur0.getLevel()+1, cur1.getASAPLevel()));
							else cur1.setASAPLevel(Math.max(cur0.getASAPLevel()+1, cur1.getASAPLevel()));
							cur1.setALAPLevel(Math.max(cur1.getASAPLevel(), cur1.getALAPLevel()));
							if (cur1.getALAPLevel()==cur1.getASAPLevel()){
								cur1.setLevel(cur1.getALAPLevel());
							}
						}
					}
					for (Iterator<DefaultWeightedEdge> iterator2 = DFG.incomingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
						cure=iterator2.next();
						cur1=DFG.getEdgeSource(cure);
						if (!cur1.isSentinel()){
							if (cur0.getLevel()>=0) cur1.setALAPLevel(Math.min(cur0.getLevel()-1, cur1.getALAPLevel()));
							else cur1.setALAPLevel(Math.min(cur0.getALAPLevel()-1, cur1.getALAPLevel()));
							cur1.setASAPLevel(Math.min(cur1.getASAPLevel(), cur1.getALAPLevel()));
							if (cur1.getALAPLevel()==cur1.getASAPLevel()){
								cur1.setLevel(cur1.getALAPLevel());
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Calculate movement force. part of force-directed scheduling solution.
	 *
	 * @param DFG the dfg
	 * @param K the spring constant
	 * @return the vertex
	 */
	private Vertex calculateMovementForce(SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG, ArrayList<Double> K) {
		ArrayList<Double> change=new ArrayList<Double>();
		Vertex cur0 = null, cur1 = null;
		DefaultWeightedEdge cure=null;
		for (int i = 0; i < K.size(); i++) {
			change.add((double) 0);
		}
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (!cur0.isSentinel() && cur0.getLevel()<0){
				cur0.setx(100);
				for (int i = cur0.getASAPLevel(); i <= cur0.getALAPLevel(); i++) {
					for (int j = 0; j < change.size(); j++){
						if(j<cur0.getASAPLevel() || j>cur0.getALAPLevel())
							change.set(j, (double) 0);
						else if (j!=i)
							change.set(j, -(double)1/(1+cur0.getALAPLevel()-cur0.getASAPLevel()));
						else
							change.set(i, 1-(double)1/(1+cur0.getALAPLevel()-cur0.getASAPLevel()));
					}
					for (Iterator<DefaultWeightedEdge> iterator2 = DFG.outgoingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
						cure=iterator2.next();
						cur1=DFG.getEdgeSource(cure);
						if (!cur1.isSentinel() && DFG.getEdgeWeight(cure)>-6 && !cur1.equals(cur0) && cur1.getLevel()<0 && cur1.getASAPLevel()>cur0.getASAPLevel()){
							change.set(cur1.getASAPLevel(), change.get(cur1.getASAPLevel())-(double)1/(1+cur1.getALAPLevel()-cur1.getASAPLevel()));
							for (int j = cur1.getASAPLevel()+1; j < cur1.getALAPLevel(); j++){
								change.set(j, change.get(j)-(double)1/(cur1.getALAPLevel()-cur1.getASAPLevel()+1)+
										(double)1/(cur1.getALAPLevel()-cur1.getASAPLevel()));
							}
						}
					}
					for (Iterator<DefaultWeightedEdge> iterator2 = DFG.incomingEdgesOf(cur0).iterator(); iterator2.hasNext();) {
						cure=iterator2.next();
						cur1=DFG.getEdgeTarget(cure);
						if (!cur1.isSentinel() && DFG.getEdgeWeight(cure)>-6 && !cur1.equals(cur0) && cur1.getLevel()<0 && cur1.getASAPLevel()<cur0.getASAPLevel()){
							change.set(cur1.getALAPLevel()+1, (double)1/(cur1.getALAPLevel()-cur1.getASAPLevel()+2));
							for (int j = cur1.getASAPLevel()+1; j < cur1.getALAPLevel(); j++){
								change.set(j, change.get(j)-(double)1/(1+cur1.getALAPLevel()-cur1.getASAPLevel())+
										(double)1/(cur1.getALAPLevel()-cur1.getASAPLevel()+2));
							}
						}
					}
					double totalChange=0;
					for (int j = 0; j < change.size(); j++){
						totalChange+=change.get(j)*K.get(j);
					}
					if (cur0.getx()>totalChange){
						cur0.setx(totalChange);
						cur0.sety(i);
					}
				}
			}
		}
		
		double minChange=100;
		cur1=null;
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (!cur0.isSentinel() && cur0.getLevel()<0 && cur0.getx()<minChange){
				minChange=cur0.getx();	
				cur1 = cur0;
			}
		}
		
		return cur1;
	}


}
