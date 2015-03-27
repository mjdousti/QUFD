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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.jgrapht.GraphPath;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.ChannelEdge;
import edu.usc.qufd.layout.Interaction;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.layout.Qubit;
import edu.usc.qufd.layout.Well;
import edu.usc.qufd.layout.Layout.Types;
import edu.usc.qufd.layout.Qubit.Direction;
import edu.usc.qufd.qasm.Vertex;

// TODO: Auto-generated Javadoc
/**
 * The Class Path.
 */
public class Path implements Comparable<Path>{
	
	/** The path. */
	private GraphPath<Well, ChannelEdge> path;
	
	/** The dst. */
	private Dimension src, dst;
	
	/** The qubit. */
	private Qubit qubit;
	
	/** The inst. */
	private Vertex inst;
	
	/** The delay. */
	private long delay;
	
	/** The layout. */
	private Layout layout;
	
	/** The finished. */
	private boolean finished;
	
	/** The execution finished. */
	private boolean executionFinished;
	
	/** The path start vertex. */
	private Dimension pathStartVertex;
	
	/** The wait. */
	private boolean wait=false;


	/**
	 * Instantiates a new path.
	 *
	 * @param p the p
	 * @param dst the dst
	 * @param q the q
	 * @param instruction the instruction
	 * @param simTime the sim time
	 * @param l the l
	 */
	public Path(GraphPath<Well, ChannelEdge> p, Dimension dst, Qubit q, Vertex instruction, long simTime, Layout l) {
		layout=l;
		qubit=q;
		this.dst=dst;
		this.src=qubit.getPosition();
		pathStartVertex=new Dimension(-1,-1);


		path=p;
		inst=instruction;
		finished=false;
		executionFinished=false;
		if (path==null && dst.equals(src)){
			delay=simTime;
			return;
		}
		if(path!=null && path.getEdgeList().size()>0){
			//reached to a junction
			if (path.getEdgeList().get(0).getV1().getPosition().equals(qubit.getPosition())){
				pathStartVertex=new Dimension(path.getEdgeList().get(0).getV2().getPosition());
				path.getEdgeList().remove(0);
			}else if(path.getEdgeList().get(0).getV2().getPosition().equals(qubit.getPosition())){
				pathStartVertex=new Dimension(path.getEdgeList().get(0).getV1().getPosition());
				path.getEdgeList().remove(0);
			}
		}
		determineDirection();
		delay=simTime+updateDelay();		
	}

	/**
	 * Gets the destination.
	 *
	 * @return the destination
	 */
	public Dimension getDestination(){
		return dst;
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public GraphPath<Well, ChannelEdge> getPath(){
		return path;
	}


	/**
	 * Move reservation.
	 */
	private void moveReservation(){
		Dimension nextPlace=new Dimension(qubit.getPosition());
		switch (qubit.getDirection()){
		case Right:
			nextPlace.width+=1;
			break;
		case Left:
			nextPlace.width-=1;
			break;
		case Up:
			nextPlace.height-=1;
			break;
		case Down:
			nextPlace.height+=1;
			break;
		}
//		try{
			if (!layout.getWell(nextPlace).isInDst(qubit.getPosition())){
				layout.getWell(qubit.getPosition()).removeFromDst(nextPlace);
			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
	}
	
	/**
	 * Update delay.
	 *
	 * @return the long
	 */
	private long updateDelay() {
		Dimension x=qubit.getPosition();
		switch (layout.getWellType(x)){
		case Basic:
		case Creation:
			//reached @ the destination which is not an interaction well
			if (qubit.getPosition().equals(dst)){
				qubit.setSleep();
				finished=true;
				return delay;
			}
			delay+=layout.getOpDelay("Move");
			moveReservation();
			break;
		case Interaction:
			if (inst!=null && qubit.getPosition().equals(dst)){
				if ( ((Interaction)layout.getWell(x)).allQubitsArrived(inst) && (layout.getWell(qubit.getPosition())).getQubitsNo() == inst.getOperandsNumber() ){
					//reserve well for interaction
					if (RuntimeConfig.HDEBUG){
						System.out.println("THE WELL IS RESERVED"+x+"  "+qubit.getName());
					}
					((Interaction)layout.getWell(x)).setInProgress(true);
					if ((layout.getWell(qubit.getPosition())).getQubitsNo() == inst.getOperandsNumber()){
						delay+=layout.getOpDelay(inst.getName());
					}else{
						if (wait){
							System.out.println("Problem @ "+x.height+"x"+x.width+" for qubit "+qubit.getName());
							System.out.println(layout.getWell(x).getQubitSet());
//							System.out.println("AGAIIIIIN!");
							System.exit(-1);							
						}
						if (RuntimeConfig.DEBUG){
							System.out.println("Qubit "+qubit.getName()+" waiting for excessive qubit to pass over "+x.height+","+x.width);
						}
						delay+=layout.getOpDelay("Move")+layout.getOpDelay(inst.getName());
						wait=true;
					}
				}else if ( ((Interaction)layout.getWell(x)).allQubitsArrived(inst) && (layout.getWell(qubit.getPosition())).getQubitsNo() > inst.getOperandsNumber() ){
					delay++;
				}else if ((layout.getWell(qubit.getPosition())).getQubitsNo() < inst.getOperandsNumber()){
					if (RuntimeConfig.DEBUG)
						System.out.println("Jumped!");
					finished=true;
					delay=0;
				}else if ((layout.getWell(qubit.getPosition())).getQubitsNo() > inst.getOperandsNumber()){
					if (RuntimeConfig.DEBUG)
						System.out.println("Jumped!");
					delay=0;
				}
			}
			else if (inst!=null || !qubit.getPosition().equals(dst)){
				delay+=layout.getOpDelay("Move");
				moveReservation();
			}
			else{
				finished=true;
				delay=0;
			}
			break;
		}
		return delay;
	}

	/**
	 * Gets the delay.
	 *
	 * @return the delay
	 */
	public long getDelay(){
		return delay;
	}

	/**
	 * Next move.
	 *
	 * @param outputFile the output file
	 * @param resourceEstimate the resource estimate
	 * @return the channel edge
	 */
	public ChannelEdge nextMove(PrintWriter outputFile, HashMap<String, Integer> resourceEstimate){
		ChannelEdge removedPath=null;
		Dimension current=qubit.getPosition();
		finished=false;
		
//		if (path==null && current.equals(dst)){
//			qubit.setDirection(Direction.Stall);
//			layout.getWell(current).decExp();
//			finished=true;
//			return removedPath;		}
		
		switch (layout.getWellType(current)){
		case Basic:
		case Creation:
			//reached @ the destination which is not an interaction well
			if (qubit.getPosition().equals(dst)){
				qubit.setDirection(Direction.Stall);
				layout.getWell(current).decExp();
				finished=true;
				return removedPath;
			}
			if (qubit.getDirection()==Direction.Stall){
//				executionFinished=true;
				finished=true;
			}else{
				updateDelay();
				qubit.move(outputFile, resourceEstimate);
				if (path!=null && path.getEdgeList().size()>0){
					//reached to a junction
					if (layout.isNode(current)){
						if (path.getEdgeList().get(0).getV1().getPosition().equals(current)){
							pathStartVertex=new Dimension(path.getEdgeList().get(0).getV2().getPosition());
						}else if(path.getEdgeList().get(0).getV2().getPosition().equals(current)){
							pathStartVertex=new Dimension(path.getEdgeList().get(0).getV1().getPosition());
						}
						removedPath=path.getEdgeList().remove(0);
						determineDirection();
					}
				}else if (layout.isNode(qubit.getPosition())){
					determineDirection();
				}
			}
			break;
		case Interaction:
			//Reached at the destination
			if (current.equals(dst)){
				if (((Interaction)layout.getWell(current)).allQubitsArrived(inst)){
					//The second condition is put there in order not to have 2 interactions by each of qubits
					if (inst.getExecutionFinishTime()<=delay && (layout.getWell(qubit.getPosition())).getQubitsNo() == inst.getOperandsNumber() && ((Interaction)layout.getWell(current)).isInProgress()){
						//TODO: Should be moved to the layout. Layout is responsible of doing quantum operations 
						if(RuntimeConfig.VERBOSE){
							outputFile.print("'"+getFullInstruction()+"'"+" @("+qubit.getPosition().height+","+qubit.getPosition().width+") ");
							//Resource estimation
							if (resourceEstimate.containsKey(inst.getInstruction())){
								resourceEstimate.put(inst.getInstruction(),
										new Integer(resourceEstimate.get(inst.getInstruction())+1));
							}else{
								resourceEstimate.put(inst.getInstruction(),
										new Integer(1));
							}
						}
						executionFinished=true;
						((Interaction)layout.getWell(current)).setInProgress(false);
					}
					else if (inst.getExecutionFinishTime()>delay && (layout.getWell(qubit.getPosition())).getQubitsNo() == inst.getOperandsNumber() && !((Interaction)layout.getWell(current)).isInProgress()){
						updateDelay();
						if (RuntimeConfig.VERBOSE){
//							System.out.print(qubit.getName()+" H "+inst.getrank()+" '"+getFullInstruction()+"'"+" @("+qubit.getPosition().height+","+qubit.getPosition().width+") ");
						}
						inst.setExecutionFinishedTime((int)delay);
						break;
					}
					else if ((layout.getWell(qubit.getPosition())).getQubitsNo() != inst.getOperandsNumber()){
						((Interaction)layout.getWell(current)).setInProgress(false);
						if (RuntimeConfig.DEBUG){
							System.out.print("Qubit "+qubit.getName()+" waiting for excessive qubit to pass over "+current.height+","+current.width);
						}
						updateDelay();
						break;
					}
				}
				finished=true;
				break;
			}//Just started the journey
			else{
				updateDelay();
				qubit.move(outputFile, resourceEstimate);
				if (path!=null && path.getEdgeList().size()>0){
					//reached to a junction
					if (layout.isNode(current)){
						if (path.getEdgeList().get(0).getV1().getPosition().equals(current)){
							pathStartVertex=new Dimension(path.getEdgeList().get(0).getV2().getPosition());
						}else if(path.getEdgeList().get(0).getV2().getPosition().equals(current)){
							pathStartVertex=new Dimension(path.getEdgeList().get(0).getV1().getPosition());
						}
						removedPath=path.getEdgeList().remove(0);
						determineDirection();						
					}
				}
			}			
		}
		return removedPath;
	}

	/**
	 * Checks if is finished.
	 *
	 * @return true, if is finished
	 */
	public boolean isFinished(){
		return finished;
	}

	/**
	 * Checks if is execution finished.
	 *
	 * @return true, if is execution finished
	 */
	public boolean isExecutionFinished(){
		return executionFinished;
	}

	/**
	 * Gets the instruction.
	 *
	 * @return the instruction
	 */
	public String getInstruction(){
		if (inst==null)
			return qubit.getName();
		else
			return inst.getInstruction();
	}

	/**
	 * Gets the full instruction.
	 *
	 * @return the full instruction
	 */
	public String getFullInstruction(){
		if (inst==null){
			return qubit.getName();
		}else{
			String s=inst.getInstruction()+" "+inst.getOperand(0);
			for (int i = 1; i < inst.getOperandsNumber(); i++) {
				s+=", "+inst.getOperand(i);
			}
			return s;
		}
	}


	/**
	 * Gets the qubit position.
	 *
	 * @return the qubit position
	 */
	public Dimension getQubitPosition(){
		return qubit.getPosition();
	}

	/**
	 * Gets the qubit.
	 *
	 * @return the qubit
	 */
	public Qubit getQubit(){
		return qubit;
	}

	/**
	 * Gets the operands number.
	 *
	 * @return the operands number
	 */
	public int getOperandsNumber(){
		return inst.getOperandsNumber();
	}

	/**
	 * Gets the operand.
	 *
	 * @param index the index
	 * @return the operand
	 */
	public String getOperand(int index){
		return inst.getOperand(index);
	}


	/**
	 * Determine direction.
	 */
	private void determineDirection(){
		Dimension current=qubit.getPosition();
		Dimension dest=null;
		if (current.equals(dst))
			return;
		else{
			if (!layout.isNode(current)){
				if (path!=null && path.getEdgeList().size()!=0){
					if (Router.distance(path.getEdgeList().get(0).getV1().getPosition(), current) >Router.distance(path.getEdgeList().get(0).getV2().getPosition(), current)){
						dest=path.getEdgeList().get(0).getV2().getPosition();
						pathStartVertex.setSize(dest);
//						System.out.println("pathStartVertex: "+pathStartVertex);
					}else{
						dest=path.getEdgeList().get(0).getV1().getPosition();
						pathStartVertex.setSize(dest);
//						System.out.println("pathStartVertex: "+pathStartVertex);
					}
				}else{
					dest=new Dimension(dst);
					if (layout.hasHorzWay(current)){
						dest.height=current.height;
					}else{
						dest.width=current.width;
					}
				}
			}else if (path==null || path.getEdgeList().isEmpty())
				if (path!=null && pathStartVertex.height!=-1 && pathStartVertex.width!=-1 && !pathStartVertex.equals(current))
					dest=pathStartVertex;
				else
					dest=dst;
			else{
//				dest = path.getEdgeList().get(0).getOtherVertex(layout.getWell(current)).getPosition();
				dest=pathStartVertex;
			}
		}

	
		if (current.getHeight()==dest.getHeight()){
			if (current.getWidth()>dest.getWidth()){
				qubit.setDirection(Qubit.Direction.Left);
			}else{
				qubit.setDirection(Qubit.Direction.Right);
			}			
		}else if (current.getWidth()==dest.getWidth()){
			if (current.getHeight()>dest.getHeight()){
				qubit.setDirection(Qubit.Direction.Up);
			}else{
				qubit.setDirection(Qubit.Direction.Down);
			}			
		}else if (dest.equals(dst)) {
			qubit.setDirection(Qubit.Direction.Stall);
		}else{
			System.out.println("Unknown direction for "+qubit.getName()+"!");
			System.exit(-1);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Path o) {
		if (getDelay()<o.getDelay())
			return -1;
		else if (getDelay()>o.getDelay())
			return 1;
		else if (inst!=null){
			return inst.compareTo(o.getVertex());
		}else{
			return 0;
		}
	}

	/**
	 * Gets the vertex.
	 *
	 * @return the vertex
	 */
	public Vertex getVertex(){
		return inst;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String output=new String();
		int i=0;
		for (Iterator<ChannelEdge> iterator = path.getEdgeList().iterator(); iterator.hasNext();) {
			ChannelEdge ce= iterator.next();			
			output+=i+": "+ ce.toString()
					+System.getProperty("line.separator");
			i++;
		}
		return output;
	}
}
