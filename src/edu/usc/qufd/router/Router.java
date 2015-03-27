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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.GraphPathImpl;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.ChannelEdge;
import edu.usc.qufd.layout.Interaction;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.layout.Qubit;
import edu.usc.qufd.layout.Well;
import edu.usc.qufd.layout.Layout.Types;
import edu.usc.qufd.qasm.Vertex;

// TODO: Auto-generated Javadoc
/**
 * The Class Router.
 */
public class Router {
	
	/**
	 * Give a the best route between the operands of instruction v.
	 *
	 * @param v the given instruction
	 * @param simTime the simulation time
	 * @param layout the layout
	 * @return list of channelEdges participating in the computed path. Returns null if no route could be found.
	 */
	public static List<Path> router(Vertex v, long simTime, Layout layout){
		LinkedList<Path> pathList=new LinkedList<Path>();		
		LinkedList<Dimension> src=new LinkedList<Dimension>();
		LinkedList<Dimension> dst=new LinkedList<Dimension>();
		Dimension temp1, temp2;
		//if there's one qubit to move, store it in q

		if (RuntimeConfig.DEBUG)
			System.out.println("Routing for instruction: "+v);
		Qubit q = null;
		src.add(layout.getQubit(v.getOperand(0)).getPosition());

		//TODO: generalize for n-qubit operations
		if (v.getOperandsNumber()==2){
			dst.add(layout.getQubit(v.getOperand(1)).getPosition());
			temp1=layout.getNearestFreeInteraction(new Dimension((src.getFirst().width+dst.getFirst().width)/2, (src.getFirst().height+dst.getFirst().height)/2), false).getPosition();

			if (((layout.getWell(dst.getFirst())).getQubitsNo()<2 
					&& distance(src.getFirst(), dst.getFirst())<distance(src.getFirst(), temp1)+distance(temp1, dst.getFirst()))
					&& layout.getWellType(dst.getFirst())==Types.Interaction){
				q=layout.getQubit(v.getOperand(0));
//			}
//			else if ((layout.getWell(src.getFirst())).getQubitsNo()<2 
//					&& distance(src.getFirst(), dst.getFirst())<distance(src.getFirst(), temp1)+distance(temp1, dst.getFirst())
//					&& layout.getWellType(src.getFirst())==Types.Interaction){
//				temp2=dst.remove();
//				dst.add(src.remove());
//				src.add(temp2);
//				q=layout.getQubit(v.getOperand(1));
			}else{
				temp2=dst.remove();
//				if (layout.assignLastInteractionWell(temp1)==false){
//					System.out.println("BADBAKHT shodi!");
//					System.exit(-1);
//				}
				dst.add(temp1);
				dst.add(dst.getFirst());
				src.add(temp2);
				q=null;
			}
			layout.assignLastInteractionWell(dst.get(0));
			
		}else{
			q=layout.getQubit(v.getOperand(0));
			if  (((layout.getWell(src.getFirst()))).getQubitsNo()==1 && layout.isIntraction(layout.getWell(src.getFirst()).getPosition())){
				dst.add(layout.getNearestFreeInteraction(src.getFirst(), true).getPosition());
//				dst=src;
			}else{
				dst.add(layout.getNearestFreeInteraction(src.getFirst(), true).getPosition());
			}
		}
		GraphPath<Well, ChannelEdge> path=null;
		if (RuntimeConfig.DEBUG)
			System.out.println("Routing Destination: "+dst.get(0).height+"," +dst.get(0).width);
		
		for (int i = 0; i < dst.size(); i++) {
			path = findPath(src.get(i), dst.get(i), layout);
			((Interaction)layout.getWell(dst.get(i))).addToProgressList(v.getOperand(i));
			if (path!=null){
				if (q==null){
					pathList.add(new Path(path, dst.get(i), layout.getQubit(v.getOperand(i)), v, simTime, layout));
					if (RuntimeConfig.DEBUG)
						System.out.println("Routing of Qubit "+v.getOperand(i));
				}else{
					pathList.add(new Path(path, dst.get(i), q, v, simTime, layout));
					if (RuntimeConfig.DEBUG)
						System.out.println("Routing of Qubit "+q);
				}
				//priniting the routed path
				if (RuntimeConfig.DEBUG)
				{
					System.out.println(pathList.getLast());
				}
			}
		}

		return pathList;
	}

		
	/**
	 * Give a the best route between the the current qubit position and given destination.
	 *
	 * @param qubit the given qubit
	 * @param dst the dst
	 * @param simTime the simulation time
	 * @param layout the layout
	 * @return list of channelEdges participating in the computed path. Returns null if no route could be found.
	 */
	public static List<Path> router(Qubit qubit, Dimension dst, long simTime, Layout layout){
		LinkedList<Path> pathList=new LinkedList<Path>();		

		if (RuntimeConfig.DEBUG)
			System.out.println("Routing for qubit: "+qubit);

		if (layout.isIntraction(dst))
			layout.assignLastInteractionWell(dst);

		GraphPath<Well, ChannelEdge> path=null;

		path = findPath(layout.getNearestNode(qubit.getPosition()).getPosition(), layout.getNearestNode(dst).getPosition(), layout);
		if (path!=null){
			pathList.add(new Path(path, dst, qubit, null, simTime, layout));

			//priniting the routed path
			if (RuntimeConfig.DEBUG){
				System.out.println(pathList.getLast());
			}
		}
		return pathList;
	}
	
	/**
	 * Computes the Manhattan distance between two points.
	 *
	 * @param a first given point
	 * @param b second given point
	 * @return the computed distance
	 */
	public static int distance(Dimension a, Dimension b){
		return Math.abs(a.height-b.height)+Math.abs(a.width-b.width);
	}

	/**
	 * Checks if is in channel.
	 *
	 * @param ce the ce
	 * @param x the x
	 * @return true, if is in channel
	 */
	private static boolean isInChannel(ChannelEdge ce, Dimension x){
		Dimension a=ce.getV1().getPosition();
		Dimension b=ce.getV2().getPosition();
		
		if (a.height==b.height && ((a.width<x.width && x.width<b.width)||(b.width<x.width && x.width<a.width)))
			return true;
		else if (a.width==b.width && ((a.height<x.height && x.height<b.height)||(b.height<x.height && x.height<a.height)))
			return true;
		else
			return false;
	}
	
	
	
	/**
	 * Find path.
	 *
	 * @param src the src
	 * @param dst the dst
	 * @param layout the layout
	 * @return the graph path
	 */
	public static GraphPath<Well, ChannelEdge> findPath(Dimension src, Dimension dst, Layout layout){
		GraphPath<Well, ChannelEdge> designated=null;
//		KShortestPaths<Well, ChannelEdge> ksp;
		DijkstraShortestPath<Well, ChannelEdge> sp;

		if (src.equals(dst) || (layout.getNearestNode(src).equals(layout.getNearestNode(dst)))){
			return new GraphPathImpl<Well, ChannelEdge>(layout.getGraph(), layout.getNearestNode(src), layout.getNearestNode(dst), new ArrayList<ChannelEdge>(), 0);
//			return null;
		}else{
//			ksp=new  KShortestPaths<Well, ChannelEdge>(layout.getGraph(), layout.getNearestNode(src), RuntimeConfig.kPaths, layout.getGraph().edgeSet().size());
			sp=new DijkstraShortestPath<Well, ChannelEdge>(layout.getGraph(), layout.getNearestNode(src), layout.getNearestNode(dst));
			List<GraphPath<Well, ChannelEdge>> list=null;
			try{
//				list=ksp.getPaths(layout.getNearestNode(dst));
				designated=sp.getPath();
				
				//TODO: Should be improved! to select the best path out of k paths
//				designated=list.get(0);
				if (designated.getEdgeList().size()>0 && isInChannel(designated.getEdgeList().get(0), src)){
					designated.getEdgeList().remove(0);
				}
				if (designated.getEdgeList().size()>0 && isInChannel(designated.getEdgeList().get(designated.getEdgeList().size()-1), src)){
					designated.getEdgeList().remove(designated.getEdgeList().size()-1);
				}

			}catch(Exception e){
				e.printStackTrace();
				System.out.println("DST: "+dst);
				System.exit(-1);
			}			
			return designated;

		}
		
	}
	
	
	
	/**
	 * Find path2.
	 *
	 * @param src the src
	 * @param dst the dst
	 * @param layout the layout
	 * @return the graph path
	 */
	public static GraphPath<Well, ChannelEdge> findPath2(Dimension src, Dimension dst, Layout layout){
		GraphPath<Well, ChannelEdge> designated=null;
//		KShortestPaths<Well, ChannelEdge> ksp;
		DijkstraShortestPath<Well, ChannelEdge> sp;
		ArrayList<Well> ndest=new ArrayList<Well>();
		ArrayList<Well> nsrc=new ArrayList<Well>();
		Well nearestDest, nearestSrc;
		
		
		ndest=layout.getNearestNodes(dst);
		nsrc=layout.getNearestNodes(src);
		nearestSrc=nsrc.get(0);
		nearestDest=ndest.get(0);
		if (ndest.size()==2){
			if (distance(ndest.get(0).getPosition(), nsrc.get(0).getPosition()) < distance(ndest.get(1).getPosition(), nsrc.get(0).getPosition()))
				nearestDest=ndest.get(0);
			else
				nearestDest=ndest.get(1);
		}
		else if (nsrc.size()==2){
			if (distance(ndest.get(0).getPosition(), nsrc.get(0).getPosition()) < distance(ndest.get(0).getPosition(), nsrc.get(1).getPosition()))
				nearestSrc=nsrc.get(0);
			else
				nearestSrc=nsrc.get(1);
		}
			
		
		
		if (src.equals(dst) || (nearestSrc.equals(nearestDest))){
			return new GraphPathImpl<Well, ChannelEdge>(layout.getGraph(), nearestSrc, nearestDest, new ArrayList<ChannelEdge>(), 0);
//			return null;
		}else{
//			ksp=new  KShortestPaths<Well, ChannelEdge>(layout.getGraph(), layout.getNearestNode(src), RuntimeConfig.kPaths, layout.getGraph().edgeSet().size());
			sp=new DijkstraShortestPath<Well, ChannelEdge>(layout.getGraph(),nearestSrc, nearestDest);
			List<GraphPath<Well, ChannelEdge>> list=null;
			try{
//				list=ksp.getPaths(layout.getNearestNode(dst));
				designated=sp.getPath();
				
				//TODO: Should be improved! to select the best path out of k paths
//				designated=list.get(0);
				if (designated.getEdgeList().size()>0 && isInChannel(designated.getEdgeList().get(0), src)){
					designated.getEdgeList().remove(0);
				}
				if (designated.getEdgeList().size()>0 && isInChannel(designated.getEdgeList().get(designated.getEdgeList().size()-1), src)){
					designated.getEdgeList().remove(designated.getEdgeList().size()-1);
				}

			}catch(Exception e){
				e.printStackTrace();
				System.out.println("DST: "+dst);
				System.exit(-1);
			}			
			return designated;

		}
		
	}
}
