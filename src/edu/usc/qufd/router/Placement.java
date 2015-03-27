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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveDouble;
import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.Interaction;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.layout.Well;
import edu.usc.qufd.qasm.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBQuadExpr;

// TODO: Auto-generated Javadoc
/**
 * The Class placement is used to place instructions on 2D trapped-ion grid.
 * The optimization is done in an iterative manner to capture and fix the 
 * scheduling constraints.
 * Different function exists in this class to create objective and constraint
 * functions and perform optimization, placement legalization and track rough
 * scheduling constraints. 
 * 
 * @author Hadi Goudarzi and Mohammad Javad Dousti
 */
public class Placement {
	
	/** The vertexes. */
	private Map<Integer, Vertex> vertexes=new HashMap<Integer, Vertex>();
	
	/** The edges. */
	private Map<DefaultWeightedEdge, Integer> edges=new HashMap<DefaultWeightedEdge, Integer>();
	
	/** Quadratic part of the objective function in x dimension. */
	Double[][] Qx;
	
	/** Linear part of the objective function in x dimension. */
	Double[]   cx,dx;
	
	/** Quadratic part of the objective function in y dimension. */
	Double[][] Qy;
	
	/** Linear part of the objective function in y dimension. */
	Double[]   cy,dy;
	
	/** The density of placing instructions in different part of the fabric. */
	int[][][] density;
	
	/** The number of committed locations in different part of the fabric. */
	int[][][] commited;
	
	/** The minimum deadline. */
	double minimumDeadline;
	
	/** The data flow graph. */
	SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFG;
	
	/** The layout. */
	Layout layout;
	
	/** The spring network to construct objective function. */
	public SimpleWeightedGraph<Vertex, DefaultWeightedEdge> springNet;
	
	/** The spring network parameters. */
	double R, K, mD;
	
	/** The min distance. */
	Double minDistance;
	
	/** The granularity. */
	int granularity=2;
	
	/** The max level. */
	int maxLevel=0;
	
	/** The ry. */
	int lx, ly, rx, ry;
	
	/** The it. */
	int kt, jt, it;
	
	/** The iteration. */
	int iteration=0;
	
	/** The gamma. */
	double gamma=1;
	
	/** The max number. */
	int maxNumber=0;
	
	/** The max weight. */
	double maxWeight;
	
	/** The octave. */
	private OctaveEngine octave;
	
	/**
	 * Flmxn.
	 *
	 * @param a the a
	 * @return the int
	 */
	public int flmxn (double a){
		return (int) Math.min(Math.max(0, Math.floor((a))),maxNumber);
	}

	/**
	 * Instantiates a new placement.
	 *
	 * @param DFGin the input data flow graph
	 * @param layoutin the input layout
	 * @param Ri, Ki, mDi the spring network parameters
	 * @param minDistancei the parameter to determine the biggest force in the network
	 */
	public Placement (SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> DFGin,Layout layoutin,double Ri, double Ki, double mDi, Double minDistancei){
		if (!RuntimeConfig.use_GUROBI){
			octave = new OctaveEngineFactory().getScriptEngine();
			octave.setErrorWriter(null);	//avoiding the no X11 found & other errors
			//disabling the core dump
			octave.eval("crash_dumps_octave_core(0);");
			octave.eval("sigterm_dumps_octave_core(0);");
			octave.eval("sighup_dumps_octave_core(0);");
			//adding qpOASES to the path
			octave.eval("addpath('"+RuntimeConfig.qpOASES_path+"/interfaces/octave');");
			
			//MPC seems to be faster
			octave.eval("options=qpOASES_options('MPC', 'terminationTolerance', 1e-1, 'boundTolerance', 1e-1, 'printLevel',0 );");
		}
		layout = layoutin;
		DFG=DFGin;
		R=Ri;
		K=Ki;
		mD=mDi;
		minDistance=minDistancei;
		maxWeight=1/mD;
		springNet=makeSpringNetwork();
		Qx = new Double[springNet.vertexSet().size()][springNet.vertexSet().size()];
		cx = new Double[springNet.vertexSet().size()];
		Qy = new Double[springNet.vertexSet().size()][springNet.vertexSet().size()];
		cy = new Double[springNet.vertexSet().size()];
		dx = new Double[springNet.vertexSet().size()];
		dy = new Double[springNet.vertexSet().size()];
		maxNumber=(int) Math.ceil(layout.getLayoutSize().height/10/granularity)-1;
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			maxLevel=Math.max(iterator.next().getLevel(),maxLevel);
		}
		density = new int[maxLevel+1][maxNumber+1][maxNumber+1];
		commited = new int[maxLevel+1][maxNumber+1][maxNumber+1];

		initilize(false);
	}

	/**
	 * Create spring network to be used in objective and constraint function 
	 * generation for the quadratic programming.
	 *
	 * @return the simple weighted graph
	 */
	public SimpleWeightedGraph<Vertex, DefaultWeightedEdge> makeSpringNetwork(){
		SimpleWeightedGraph<Vertex, DefaultWeightedEdge> springNet = new SimpleWeightedGraph<Vertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		DefaultWeightedEdge temp0, temp1;
		Vertex cur0, cur1, cur2, cur3;
		DefaultWeightedEdge cure = null;
		int maxLevel=0;
		boolean hasOp=false;
		double repulsion = R;
		double temp;
		minimumDeadline = mD; //it should be 2 or 3 times the K
		double moveDelay = 1;
		double gateDelay = K; // ONe Operation Delay

		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			cur0.setReadyInpts(cur0.getLevel());
			if (!cur0.isSentinel()){
				springNet.addVertex(cur0);
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
							cure=DFG.addEdge(cur1, cur0);
							DFG.setEdgeWeight(cure, -10);
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
				if (cur0.getrank()==1000) temp = (double)1/(double)(minimumDeadline);
				else if (cur0.getLevel()==DFG.getEdgeSource(temp0).getLevel())  temp = (double)1/(double)minimumDeadline;
				else temp = ((double)1/(-1+Math.abs(cur0.getLevel()-DFG.getEdgeSource(temp0).getLevel()))) /(double)gateDelay;
				if (temp>20) temp =10;
				if (!springNet.containsEdge(DFG.getEdgeSource(temp0),DFG.getEdgeTarget(temp0))){
					temp1=springNet.addEdge(DFG.getEdgeSource(temp0), DFG.getEdgeTarget(temp0));
					springNet.setEdgeWeight(temp1, temp);
				}
			}
		}

		return springNet;
	}

	/**
	 * Initilizes objective and constraint functions.
	 *
	 * @param anchorBased the anchor based
	 */
	public void initilize(boolean anchorBased){
		Vertex cur0, cur1;
		DefaultWeightedEdge edg0,edg1,cure;
		int index=0, maxLevel;
		boolean hasOp = false;

		for (int i = 0; i < cy.length; i++) {
			cy[i]=(double) 0;
			cx[i]=(double) 0;
			dy[i]=(double) 0;
			dx[i]=(double) 0;
			for (int j = 0; j < cy.length; j++) {
				Qx[i][j]=(double) 0;
				Qy[i][j]=(double) 0;
			}
		}
		vertexes.clear();
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			cur0.setrank(-1);
			if (!cur0.getMoveablity()){
				cur0.setx(cur0.getInteraction().getPosition().width);
				cur0.sety(cur0.getInteraction().getPosition().height);
			}
		}

		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (!cur0.isSentinel()){
				if (cur0.getrank()<0){
					cur0.setrank(index++);
					cur0.setMoveable();
					vertexes.put(index-1,cur0);
				}
				for (Iterator<DefaultWeightedEdge> iterator2 = springNet.edgesOf(cur0).iterator(); iterator2.hasNext();){
					edg0=iterator2.next();
					cur1=springNet.getEdgeTarget(edg0);
					if (cur1.equals(cur0))	cur1=springNet.getEdgeSource(edg0);
					if (!cur1.isSentinel()){
						if (cur1.getrank()<0){
							cur1.setrank(index++);
							vertexes.put(index-1,cur1);
						}
						Qx[(int) cur0.getrank()][(int) cur1.getrank()]=
								(double) springNet.getEdgeWeight(edg0)*1/(Math.abs(cur0.getx()-cur1.getx()));
						if (Math.abs(cur0.getx()-cur1.getx())<5) Qx[(int) cur0.getrank()][(int) cur1.getrank()]=(double) springNet.getEdgeWeight(edg0)*1/minDistance;
						Qy[(int) cur0.getrank()][(int) cur1.getrank()]=
								(double) springNet.getEdgeWeight(edg0)*1/(Math.abs(cur0.gety()-cur1.gety()));
						if (Math.abs(cur0.gety()-cur1.gety())<5) Qy[(int) cur0.getrank()][(int) cur1.getrank()]=(double) springNet.getEdgeWeight(edg0)*1/minDistance;
						//						Qx[(int) cur0.getrank()][(int) cur1.getrank()]=(double) springNet.getEdgeWeight(edg0);
						//						Qy[(int) cur0.getrank()][(int) cur1.getrank()]=(double) springNet.getEdgeWeight(edg0);
					}
				}
			}
		}

		if (anchorBased){
			for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
				cur0 = iterator.next();
				if (cur0.hasAnchor()){
					double [] tempPos = cur0.anchPos();
					double [] tempD= new double[2];
					if (tempPos[0]==cur0.getx()) tempD[0]=1;
					else tempD[0]=1/Math.abs(tempPos[0]-cur0.getx());
					if (tempPos[1]==cur0.gety()) tempD[1]=1;
					else tempD[1]=1/Math.abs(tempPos[1]-cur0.gety());
					cx[(int) cur0.getrank()] = cx[(int) cur0.getrank()]+(double) -2*(1+iteration)*0.01*tempD[0]*tempPos[0];
					dx[(int) cur0.getrank()] = dx[(int) cur0.getrank()] + (double) (1+iteration)*0.01*tempD[0];
					cy[(int) cur0.getrank()] = cy[(int) cur0.getrank()]+(double) -2*(1+iteration)*0.01*tempD[1]*tempPos[1];
					dy[(int) cur0.getrank()] = dy[(int) cur0.getrank()] + (double) (1+iteration)*0.01*tempD[1];
				}
			}
		}




		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (!cur0.isSentinel()){				
				for (int j = 0; j < cur0.getOperandsNumber(); j++) {
					String curo = cur0.getOperand(j);
					maxLevel=0;
					hasOp=false;
					for (Iterator<DefaultWeightedEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
						cure=iterator0.next();
						cur1=DFG.getEdgeSource(cure);
						for (int i = 0; i < cur1.getOperandsNumber(); i++) {
							if (cur1.getOperand(i).equals(curo))
								hasOp=true;
						}
					}
					if (!hasOp && layout.getQubit(curo).getInitPosition().getWidth()>=0){
						if ((Math.abs(cur0.getx()-layout.getQubit(curo).getInitPosition().getWidth()))<5){
							cx[(int) cur0.getrank()] = cx[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getInitPosition().getWidth()/minDistance;
							dx[(int) cur0.getrank()] = dx[(int) cur0.getrank()] + (double) 1/minimumDeadline*1/minDistance;
						}else{
							cx[(int) cur0.getrank()] = cx[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getInitPosition().getWidth()
									/(Math.abs(cur0.getx()-layout.getQubit(curo).getInitPosition().getWidth()));
							dx[(int) cur0.getrank()] = dx[(int) cur0.getrank()] + (double) 1/minimumDeadline
									*1/(Math.abs(cur0.getx()-layout.getQubit(curo).getInitPosition().getWidth()));
						}if ((Math.abs(cur0.gety()-layout.getQubit(curo).getInitPosition().getHeight()))<5){
							cy[(int) cur0.getrank()] = cy[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getInitPosition().getHeight()/minDistance;
							dy[(int) cur0.getrank()] = dy[(int) cur0.getrank()] + (double) 1/minimumDeadline/minDistance;
						}else{
							cy[(int) cur0.getrank()] = cy[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getInitPosition().getHeight()
									/(Math.abs(cur0.gety()-layout.getQubit(curo).getInitPosition().getHeight()));
							dy[(int) cur0.getrank()] = dy[(int) cur0.getrank()] + (double) 1/minimumDeadline
									*1/(Math.abs(cur0.gety()-layout.getQubit(curo).getInitPosition().getHeight()));
						}
						//						dx[(int) cur0.getrank()] = dx[(int) cur0.getrank()] + (double) 1/minimumDeadline;
						//						dy[(int) cur0.getrank()] = dy[(int) cur0.getrank()] + (double) 1/minimumDeadline;
						//						cx[(int) cur0.getrank()] = cx[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getInitPosition().getWidth();
						//						cy[(int) cur0.getrank()] = cy[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getInitPosition().getHeight();
					}
				}
				for (int j = 0; j < cur0.getOperandsNumber(); j++) {
					String curo = cur0.getOperand(j);
					maxLevel=0;
					hasOp=false;
					for (Iterator<DefaultWeightedEdge> iterator0 = DFG.outgoingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
						cure=iterator0.next();
						cur1=DFG.getEdgeTarget(cure);
						for (int i = 0; i < cur1.getOperandsNumber(); i++) {
							if (cur1.getOperand(i).equals(curo))
								hasOp=true;
						}
					}
					if (!hasOp & layout.getQubit(curo).getFinalPosition().getWidth()>=0){
						if ((Math.abs(cur0.getx()-layout.getQubit(curo).getFinalPosition().getWidth()))<5){
							cx[(int) cur0.getrank()] = cx[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getFinalPosition().getWidth()/minDistance;
							dx[(int) cur0.getrank()] = dx[(int) cur0.getrank()] + (double) 1/minimumDeadline*1/minDistance;
						}else{
							cx[(int) cur0.getrank()] = cx[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getFinalPosition().getWidth()
									/(Math.abs(cur0.getx()-layout.getQubit(curo).getFinalPosition().getWidth()));
							dx[(int) cur0.getrank()] = dx[(int) cur0.getrank()] + (double) 1/minimumDeadline
									*1/(Math.abs(cur0.getx()-layout.getQubit(curo).getFinalPosition().getWidth()));
						}if ((Math.abs(cur0.gety()-layout.getQubit(curo).getFinalPosition().getHeight()))<5){
							cy[(int) cur0.getrank()] = cy[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getFinalPosition().getHeight()/minDistance;
							dy[(int) cur0.getrank()] = dy[(int) cur0.getrank()] + (double) 1/minimumDeadline/minDistance;
						}else{
							cy[(int) cur0.getrank()] = cy[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getFinalPosition().getHeight()
									/(Math.abs(cur0.gety()-layout.getQubit(curo).getFinalPosition().getHeight()));
							dy[(int) cur0.getrank()] = dy[(int) cur0.getrank()] + (double) 1/minimumDeadline
									*1/(Math.abs(cur0.gety()-layout.getQubit(curo).getFinalPosition().getHeight()));
						}
						//						dx[(int) cur0.getrank()] = dx[(int) cur0.getrank()] + (double) 1/minimumDeadline;
						//						dy[(int) cur0.getrank()] = dy[(int) cur0.getrank()] + (double) 1/minimumDeadline;
						//						cx[(int) cur0.getrank()] = cx[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getFinalPosition().getWidth();
						//						cy[(int) cur0.getrank()] = cy[(int) cur0.getrank()]+(double) -2*1/minimumDeadline*layout.getQubit(curo).getFinalPosition().getHeight();
					}
				}
			}
		}

		for (int i = 0; i < cy.length; i++) {
			Double temp=(double) 0;
			for (int j = 0; j < cy.length; j++) {
				if (RuntimeConfig.PHDEBUG)
					System.out.print(Qx[i][j]+",");
				temp=temp+Qx[i][j];
				Qx[i][j]=-Qx[i][j];
			}
			if (RuntimeConfig.PHDEBUG)
				System.out.println();
			Qx[i][i]=temp+dx[i];
			Double temp2=(double) 0;
			for (int j = 0; j < cy.length; j++) {
				if (RuntimeConfig.PHDEBUG)
					System.out.print(Qy[i][j]+",");
				temp2=temp2+Qy[i][j];
				Qy[i][j]=-Qy[i][j];
			}
			if (RuntimeConfig.PHDEBUG)
				System.out.println();
			Qy[i][i]=temp2+dy[i];
			if (RuntimeConfig.PHDEBUG)
				System.out.println(vertexes.get(i)+"   "+cy[i]+" "+cx[i]+" "+temp+" "+temp2);
		}

	}

	/**
	 * Calls optimize() function and calculate the objective function at the end.
	 */
	public void optimization(){
		// first apply one iteration
		optimize();
		double currentSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
		double newSum=currentSum-5;
		if (RuntimeConfig.PHDEBUG)     System.out.println("total Objective   "+currentSum);
		int index=0;
		while ((currentSum-newSum)>1 || index<10){
			index++;
			currentSum=newSum;
			if (RuntimeConfig.PHDEBUG)     System.out.println("total Objective1   "+currentSum);
			initilize(false);
			optimize();
			newSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
			densityCalculator();
			if (RuntimeConfig.PHDEBUG)     System.out.println("total Objective2   "+newSum);
		}
		//		updateSpringForces(0);
		iteration=1;
		boolean legalized=false;
		int i=0,j=0;
		for (j = 0; j <= maxLevel; j++){
			legalized=false;i=0;
			while (!legalized && i<5) {
				i++;
				iteration++;
				legalized=roughLegalization(j);
				initilize(true);
				optimize();
				newSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
				densityCalculator();
				System.out.println("------------------------------------------------------"+iteration+" "+i+" "+j);
				//				updateSpringForces(j);
				if (RuntimeConfig.PHDEBUG)     System.out.println(j+" "+i+" "+"total Objective1   "+newSum);
			}
			legalPlacement(j);
			if (j<maxLevel){
				for(int j1 = 0; j1 <= 2; j1++){
					currentSum=newSum;
					if (RuntimeConfig.PHDEBUG)     System.out.println("total Objective1   "+currentSum);
					initilize(false);
					optimize();
					newSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
					densityCalculator();
					System.out.println("------------------------------------------------------"+iteration+" "+i+" "+j);
					//					updateSpringForces(j+1);
					if (RuntimeConfig.PHDEBUG)     System.out.println(j+" "+i+" "+"total Objective2   "+newSum);
				}
			}
		}
		//		for (int j1 = 0; j1 <= maxLevel; j1++) {
		//			legalPlacement(j1);
		//		}
		//		optimize();
		newSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
		densityCalculator();
		if (RuntimeConfig.PHDEBUG)     System.out.println("total Objective   "+"         "+newSum);
		commitedCalculator();
		System.out.println("total Objective   "+"         "+newSum);
		//		printLocations();
		//		updateSpringForces(j);
		slackCalculation();

	}

	/**
	 * Prints the location of instructions.
	 */
	private void printLocations() {
		System.out.println();
		System.out.println();
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			Vertex cur0 = iterator.next();
			System.out.println(cur0+"  @["+cur0.getx()+" , "+cur0.gety()+"]");
		}
		System.out.println();
		System.out.println();
	}

	/**
	 * Calculate objective function value.
	 *
	 * @param coefFinal the coef of routing for final qubit placement
	 * @param coefInitial the coef of routing for initial qubit placement
	 * @return the objective function value
	 */
	public double objectiveSum (double coefFinal, double coefInitial){
		double sum=(double) 0;
		Vertex cur0, cur1;
		DefaultWeightedEdge edg0, cure;
		int maxLevel=0;
		Boolean hasOp=false;

		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			for (Iterator<DefaultWeightedEdge> iterator2 = springNet.edgesOf(cur0).iterator(); iterator2.hasNext();) {
				edg0 = iterator2.next();
				cur1=springNet.getEdgeSource(edg0);
				if (cur1.equals(cur0))
					cur1=springNet.getEdgeTarget(edg0);
				sum=sum+springNet.getEdgeWeight(edg0)*(Math.abs(cur1.getx()-cur0.getx())+Math.abs(cur1.gety()-cur0.gety()));
			}
		}
		sum=sum/2;
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			if (!cur0.isSentinel()){				
				for (int j = 0; j < cur0.getOperandsNumber(); j++) {
					String curo = cur0.getOperand(j);
					maxLevel=0;
					hasOp=false;
					for (Iterator<DefaultWeightedEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
						cure=iterator0.next();
						cur1=DFG.getEdgeSource(cure);
						for (int i = 0; i < cur1.getOperandsNumber(); i++) {
							if (cur1.getOperand(i).equals(curo))
								hasOp=true;
						}
					}
					if (!hasOp & layout.getQubit(curo).getInitPosition().getWidth()>=0){
						sum=sum+coefInitial*(Math.abs(cur0.getx()-layout.getQubit(curo).getInitPosition().getWidth())+
								Math.abs(cur0.gety()-layout.getQubit(curo).getInitPosition().getHeight()));
					}
				}
				for (int j = 0; j < cur0.getOperandsNumber(); j++) {
					String curo = cur0.getOperand(j);
					maxLevel=0;
					hasOp=false;
					for (Iterator<DefaultWeightedEdge> iterator0 = DFG.outgoingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
						cure=iterator0.next();
						cur1=DFG.getEdgeTarget(cure);
						for (int i = 0; i < cur1.getOperandsNumber(); i++) {
							if (cur1.getOperand(i).equals(curo))
								hasOp=true;
						}
					}
					if (!hasOp & layout.getQubit(curo).getFinalPosition().getWidth()>=0){
						sum=sum+coefFinal*(Math.abs(cur0.getx()-layout.getQubit(curo).getFinalPosition().getWidth())+
								Math.abs(cur0.gety()-layout.getQubit(curo).getFinalPosition().getHeight()));
					}
				}
			}
		}
		return sum;
	}

	/**
	 * Calls the quadratic optimization tool. 
	 */
	public void optimize() {
		if (RuntimeConfig.use_GUROBI==false){
			int count=0;
			int counter=0;

			//x
			double[] Hx=new double[cy.length*cy.length];
			for (int i = 0; i < cy.length; i++) {
				if (!vertexes.get(i).getMoveablity()){
					count++;
				}
				for (int j = 0; j < cy.length; j++){
					Hx[i*cy.length+j]=Qx[i][j];
				}			
			}

			double[] qx=new double[cy.length];
			for (int i = 0; i < cy.length; i++) {
				qx[i]=cx[i];
			}

			//lower bound
			String lbx="zeros("+cy.length+",1);";

			//lower bound
			String ubx="repmat("+(layout.getLayoutSize().getWidth()-1)+","+cy.length+",1);";

			double[] Ax=new double[count*cy.length];
			for (int i = 0; i < cy.length; i++) {
				if (!vertexes.get(i).getMoveablity()){
					for (int j = 0; j < cy.length; j++) {
						if (i==j)
							Ax[counter*cy.length+j]=1.0;
						else
							Ax[counter*cy.length+j]=0.0;
					}
					counter++;
				}
			}


			double[] bx=new double[count];
			counter=0;
			for (int i = 0; i < cy.length; i++) {
				if (!vertexes.get(i).getMoveablity()){
					bx[counter]=vertexes.get(i).getInteraction().getPosition().getWidth();
					counter++;
				}
			}

			//y
			double[] Hy=new double[cy.length*cy.length];
			for (int i = 0; i < cy.length; i++) {
				for (int j = 0; j < cy.length; j++){
					Hy[i*cy.length+j]=Qy[i][j];
				}			
			}

			double[] qy=new double[cy.length];
			for (int i = 0; i < cy.length; i++) {
				qy[i]=cy[i];
			}

			//lower bound
			String lby="zeros("+cy.length+",1);";

			//lower bound
			String uby="repmat("+(layout.getLayoutSize().getHeight()-1)+","+cy.length+",1);";

			double[] Ay=new double[count*cy.length];
			counter=0;
			for (int i = 0; i < cy.length; i++) {
				if (!vertexes.get(i).getMoveablity()){
					for (int j = 0; j < cy.length; j++) {
						if (i==j)
							Ay[counter*cy.length+j]=1.0;
						else
							Ay[counter*cy.length+j]=0.0;
					}
					counter++;
				}
			}
			
			double[] by=new double[count];
			counter=0;
			for (int i = 0; i < cy.length; i++) {
				if (!vertexes.get(i).getMoveablity()){
					by[counter]=vertexes.get(i).getInteraction().getPosition().getHeight();
					counter++;
				}
			}

			//Calling Octave
			//entring x variables
			octave.put("Hx",new OctaveDouble(Hx,cy.length,cy.length));
			octave.put("qx",new OctaveDouble(qx,cy.length,1));
			if (count==0){
				octave.eval("Ax=[];");
				octave.eval("Bx=[];");
			}else{
				//Dimension of the matrix is "count" rows and "cy.length" columns
				//but due to the strange way OctaveDouble works, it is reversely specified
				//In the optimization, Ax' is used to compensate
				octave.put("Ax",new OctaveDouble(Ax,cy.length,count));
				octave.put("Bx",new OctaveDouble(bx,count,1));
			}
			octave.eval("lbx="+lbx);
			octave.eval("ubx="+ubx);
			octave.eval("x0=zeros("+cy.length+",1);");

			

			//entring y variables
			octave.put("Hy",new OctaveDouble(Hy,cy.length,cy.length));
			octave.put("qy",new OctaveDouble(qy,cy.length,1));
			if (count==0){
				octave.eval("Ay=[];");
				octave.eval("By=[];");
			}else{
				//Dimension of the matrix is "count" rows and "cy.length" columns
				//but due to the strange way OctaveDouble works, it is reversely specified
				//In the optimization, Ay' is used to compensate
				octave.put("Ay",new OctaveDouble(Ay,cy.length,count));
				octave.put("By",new OctaveDouble(by,count,1));
			}
			octave.eval("lby="+lby);
			octave.eval("uby="+uby);
			octave.eval("y0=zeros("+cy.length+",1);");

			octave.eval("[result_x,fval_x,exitFlag_x]=qpOASES(Hx,qx,Ax',lbx,ubx,Bx,Bx,x0,options);");
			octave.eval("[result_y,fval_y,exitFlag_y]=qpOASES(Hy,qy,Ay',lby,uby,By,By,y0,options);");

			OctaveDouble exitFlag_x=octave.get(OctaveDouble.class, "exitFlag_x");
			OctaveDouble exitFlag_y=octave.get(OctaveDouble.class, "exitFlag_y");
			
			if (exitFlag_x.getData()[0]!=0 || exitFlag_y.getData()[0]!=0){
				System.out.println("QP cannot be solved.");
				System.exit(0);
			}
			
			OctaveDouble result_x = octave.get(OctaveDouble.class, "result_x");
			OctaveDouble result_y = octave.get(OctaveDouble.class, "result_y");

			//octave.eval("0.5*result_x'*Hy*result_x+qy'*result_x");
			//octave.eval("0.5*result_y'*Hy*result_y+qy'*result_y");

			for (int i = 0; i < cy.length; i++) {

				vertexes.get(i).setx(Math.max(0, Math.min(result_x.getData()[i], layout.getLayoutSize().height)));
				vertexes.get(i).sety(Math.max(0, Math.min(result_y.getData()[i], layout.getLayoutSize().width)));

//				System.out.print(vertexes.get(i)+"  "+i+" ["+Math.round(result_x.getData()[i])+","+Math.round(result_y.getData()[i])+"]   ");
			}
//			System.out.println();
		}
		else{
			try {
				GRBEnv    env   = new GRBEnv();
				env.set(GRB.IntParam.OutputFlag,0);

				GRBModel  modelx = new GRBModel(env);
				GRBModel  modely = new GRBModel(env);
				// Create variables

				for (int i = 0; i < cy.length; i++) {
					modelx.addVar(0.0, layout.getLayoutSize().getWidth()-1, 0.0, GRB.CONTINUOUS, null);
					modely.addVar(0.0, layout.getLayoutSize().getHeight()-1, 0.0, GRB.CONTINUOUS, null);
				}

				// Integrate new variables

				modelx.update();
				modely.update();

				// Set objective
				GRBQuadExpr expX = new GRBQuadExpr(), expY= new GRBQuadExpr();

				for (int i = 0; i < cy.length; i++) {
					for (int j = 0; j < cy.length; j++){
						expX.addTerm(Qx[i][j], modelx.getVar(i), modelx.getVar(j));
						expY.addTerm(Qy[i][j], modely.getVar(i), modely.getVar(j));
					}
					expX.addTerm(cx[i], modelx.getVar(i));
					expY.addTerm(cy[i], modely.getVar(i));
				}	

				modelx.setObjective(expX, GRB.MINIMIZE);
				modely.setObjective(expY, GRB.MINIMIZE);

				// Add constraint

				for (int i = 0; i < cy.length; i++) {
					if (!vertexes.get(i).getMoveablity()){
						GRBLinExpr constraintX = new GRBLinExpr(), constraintY = new GRBLinExpr();
						constraintX.addTerm(1, modelx.getVar(i));
						constraintY.addTerm(1, modely.getVar(i));
						modelx.addConstr(constraintX, GRB.EQUAL,  vertexes.get(i).getInteraction().getPosition().getWidth(), null);
						modely.addConstr(constraintY, GRB.EQUAL,  vertexes.get(i).getInteraction().getPosition().getHeight() , null);
					}
				}

				//		      if (false){
				//		    	  expr = new GRBLinExpr();
				//			      expr.addTerm(1.0, x); expr.addTerm(2.0, y); expr.addTerm(3.0, z);
				//			      model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0"); 
				//
				//			      // Add constraint: x + y >= 1
				//
				//			      expr = new GRBLinExpr();
				//			      expr.addTerm(1.0, x); expr.addTerm(1.0, y);
				//			      model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "c1");
				//		      }

				//		      GRBLinExpr constraintX = new GRBLinExpr(), constraintY = new GRBLinExpr();;
				//		      for (int i = 0; i < cy.length; i++) {
				//		    	  constraintX.addTerm(1, modelx.getVar(i));
				//		    	  constraintY.addTerm(1, modely.getVar(i));
				//		      }
				//		      modelx.addConstr(constraintX, GRB.EQUAL, cy.length*layout.getLayoutSize().getWidth()/2 , null);
				//		      modely.addConstr(constraintY, GRB.EQUAL, cy.length*layout.getLayoutSize().getHeight()/2 , null);
				//		      
				// Optimize model
				modelx.optimize();
				modely.optimize();

				for (int i = 0; i < cy.length; i++) {

					vertexes.get(i).setx(Math.max(0, Math.min(modelx.getVar(i).get(GRB.DoubleAttr.X), layout.getLayoutSize().height)));
					vertexes.get(i).sety(Math.max(0, Math.min(modely.getVar(i).get(GRB.DoubleAttr.X), layout.getLayoutSize().width)));

					//				System.out.println("["+modelx.getVar(i).get(GRB.DoubleAttr.X)+", "+modely.getVar(i).get(GRB.DoubleAttr.X)+"]");
					if (RuntimeConfig.PHDEBUG)     
						System.out.print(vertexes.get(i)+"  "+i+" ["+Math.round(modelx.getVar(i).get(GRB.DoubleAttr.X))+","+Math.round(modely.getVar(i).get(GRB.DoubleAttr.X))+"]   ");
				}	
				if (RuntimeConfig.PHDEBUG)     System.out.println();
				if (RuntimeConfig.PHDEBUG)     System.out.println("Obj: " + modelx.get(GRB.DoubleAttr.ObjVal));
				if (RuntimeConfig.PHDEBUG)     System.out.println("Obj: " + modely.get(GRB.DoubleAttr.ObjVal));

				// Dispose of model and environment

				modelx.dispose();
				modely.dispose();
				env.dispose();			
			} 
			catch (GRBException e) {
				if (RuntimeConfig.PHDEBUG)     System.out.println("Error code: " + e.getErrorCode() + ". " +
						e.getMessage());
			}
		}

	}

	/**
	 * Calculate Density for small part of fabric as the result of the placement solution.
	 */
	public void densityCalculator (){
		Vertex cur0, cur1;

		for(int k=0; k<=maxLevel; k++){
			for (int i = 0; i < flmxn(layout.getLayoutSize().height/10/granularity)+1; i++) {
				for (int j = 0; j < flmxn(layout.getLayoutSize().width/10/granularity)+1; j++) {
					density[k][i][j]=0;
				}
			}
		}

		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.getx()>=layout.getLayoutSize().width) cur0.setx(layout.getLayoutSize().width-1.01);
			if (cur0.gety()>=layout.getLayoutSize().height) cur0.sety(layout.getLayoutSize().height-1.01);
			density[cur0.getLevel()][flmxn(cur0.gety()/10/granularity)][flmxn((cur0.getx()/10/granularity))]++;
		}
		if (RuntimeConfig.PHDEBUG){
			for(int k=0; k<=maxLevel; k++){
				System.out.print(k+"     ");
				for (int i = 0; i < flmxn(layout.getLayoutSize().width/10/granularity)+1; i++) {
					for (int j = 0; j < flmxn(layout.getLayoutSize().width/10/granularity)+1; j++) {
						System.out.print(density[k][i][j]+" ");
					}
					System.out.print("///");
				}
				System.out.println();
			}
		}
	}

	/**
	 * Do the rough legalization after finding a placement solution to resolve overlap.
	 *
	 * @param KT the target scheduling level to do the rough legalization.
	 * @return true, if successful
	 */
	public boolean roughLegalization(int KT){
		int [] maxIndex= new int[4];
		maxIndex[0]=0;
		int [] neighbors = new int [8];
		boolean join=false;
		int sumCongested=0;
		int []  limitLow = new int [2] , limitHigh= new int [2];

		for(int k=KT; k<KT+1; k++){
			for (int i = 0; i < flmxn(layout.getLayoutSize().height/10/granularity); i++) {
				for (int j = 0; j < flmxn(layout.getLayoutSize().width/10/granularity); j++) {
					if (density[k][i][j]>maxIndex[0]){
						maxIndex[0]=density[k][i][j];maxIndex[1]=k;maxIndex[2]=j;maxIndex[3]=i;}
				}
			}
		}
		if (maxIndex[0]/granularity/granularity<gamma)
			return true;
		kt = maxIndex[1]; jt = maxIndex[2]; it=maxIndex[3];
		//find over-filled neighbors
		int index=0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				if (i!=0 || j!=0){
					if (it+i<0 || j+jt<0 || i+it>=density[kt][it].length || j+jt>=density[kt][it].length) neighbors[index]=0;
					else if (density[kt][it+i][jt+j]/granularity/granularity>=gamma){ neighbors[index]=1;}
					else neighbors[index]=0;
					index++;
					join=true;
				}
			}
		}
		limitLow[0]=0; limitLow[1]=0;
		limitHigh[0]=0; limitHigh[1]=0;
		if (join){
			if (neighbors[0]==1 || neighbors[3]==1 || neighbors[5]==1) limitLow[0]=-1;
			if (neighbors[0]==1 || neighbors[1]==1 || neighbors[2]==1) limitLow[1]=-1;
			if (neighbors[5]==1 || neighbors[6]==1 || neighbors[7]==1) limitHigh[1]=1;
			if (neighbors[2]==1 || neighbors[4]==1 || neighbors[7]==1) limitHigh[0]=1;
		}
		for (int i = limitLow[1]; i < limitHigh[1]+1; i++) {
			for (int j = limitLow[0]; j < limitHigh[0]+1; j++) 
				sumCongested=sumCongested+density[kt][i+it][j+jt];
		}
		if (RuntimeConfig.PHDEBUG)     System.out.println(sumCongested/granularity/granularity/gamma+" "+kt+" "+it+" "+jt+" "+sumCongested+" "+limitLow[0]+" "+limitLow[1]+" "+limitHigh[0]+" "+limitHigh[1]);

		int numGrids=(int) Math.ceil(sumCongested/granularity/granularity/gamma);

		lx=limitLow[0]; ly=limitLow[1]; rx=limitHigh[0]; ry=limitHigh[1];
		int lxa=0, lya=0, rxa=0, rya=0;
		while ((rx-lx+1)*(ry-ly+1)<numGrids){
			int minex=Math.min(Math.min(Math.abs(rx+rxa), Math.abs(lx+lxa)), Math.min(Math.abs(ry+rya), Math.abs(ly+lya)));
			if (jt+rx+1>density[kt][it].length) rxa=1000;
			if (it+ry+1>density[kt][it].length) rya=1000;
			if (it+ly-1<0) lya=1000;
			if (jt+lx-1<0) lxa=1000;
			if (Math.abs(rx)==minex && jt+rx+1<=density[kt][it].length)	rx=rx+1;
			else if (Math.abs(ly)==minex && it+ly-1>=0)	ly=ly-1;
			else if (Math.abs(lx)==minex && jt+lx-1>=0)	lx=lx-1;
			else if (Math.abs(ry)==minex && it+ry+1<=density[kt][it].length)	ry=ry+1;
			if (RuntimeConfig.PHDEBUG)     System.out.println(minex+" "+numGrids+" "+sumCongested+" "+rx+" "+ry+" "+lx+" "+ly+" "+rxa+" "+rya+" "+lxa+" "+lya);
			sumCongested=0;
			for (int i = ly; i < ry+1; i++) {
				for (int j = lx; j < rx+1; j++) 
					if (i+it>=0 && i+it<density[kt][it].length && j+jt>=0 && j+jt<density[kt][it].length)
						sumCongested=sumCongested+density[kt][i+it][j+jt];
			}
			numGrids=(int) Math.ceil(sumCongested/granularity/granularity/gamma);
		}

		if (RuntimeConfig.PHDEBUG)     System.out.println(numGrids+" "+sumCongested+" "+rx+" "+ry+" "+lx+" "+ly);

		nonLinearScaling();
		return false;


	}

	/**
	 * Do the non-linear scaling after finding a placement solution to resolve overlap. (see simPL description for more details)
	 */
	public void nonLinearScaling(){
		double sumX=0, sumY=0, number = 0;
		//vertical cut and horizontal scaling
		PriorityQueue<Vertex> xPos=new PriorityQueue<Vertex>();
		xPos.clear();
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			Vertex cur0 = iterator.next();
			if (kt==cur0.getLevel() && cur0.gety()/10/granularity>=it+ly && cur0.gety()/10/granularity<=it+ry
					&& cur0.getx()/10/granularity>=jt+lx && cur0.getx()/10/granularity<=jt+rx){
				cur0.setPriority((int) cur0.getx());
				xPos.add(cur0);
				sumX+=cur0.getx();
				number++;
			}
		}
		double avgX=sumX/number, median = 0, max = 0, min;
		// find median
		int index=0;
		for (Iterator<Vertex> iterator = xPos.iterator(); iterator.hasNext();) {
			Vertex cur0 = iterator.next();
			index++;
			if (index==xPos.size()/2)  median=cur0.getx();	
			else if (index==0) max = cur0.getx();
			else if (index==xPos.size()-1) min = cur0.getx();
		}
		//First half
		index=0;
		double step=(10*granularity*(rx-lx+1))/number;
		double currentX=10*granularity*(rx+1)+jt*10*granularity;
		while (index++<number/2){
			Vertex cur0 = xPos.remove();
			cur0.setx(currentX);
			currentX-=step;
		}
		currentX=5*granularity*(rx+1-lx)+jt*10*granularity;
		while (!xPos.isEmpty()){
			Vertex cur0 = xPos.remove();
			cur0.setx(currentX);
			currentX-=step;
		}

		//Horizontal Cut line and vertical scaling // first half
		number=0;
		PriorityQueue<Vertex> yPos=new PriorityQueue<Vertex>();
		yPos.clear();
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			Vertex cur0 = iterator.next();
			if (kt==cur0.getLevel() && flmxn(cur0.gety()/10/granularity)>=it+ly && flmxn(cur0.gety()/10/granularity)<=it+ry
					&& flmxn(cur0.getx()/10/granularity)>=jt+lx && flmxn(cur0.getx()/10/granularity)<=(double)(rx-lx+1)/2+jt+lx){
				cur0.setPriority((int) cur0.gety());
				yPos.add(cur0);
				number++;
			}
		}
		//First half
		index=0;
		step=(10*granularity*(ry-ly+1))/number;
		double currentY=10*granularity*(ry+2)+it*10*granularity;
		while (index++<number/2){
			Vertex cur0 = yPos.remove();
			cur0.sety(currentY);
			currentY-=step;
		}
		currentY=5*granularity*(ry+1-ly)+(it-1)*10*granularity;
		while (!yPos.isEmpty()){
			Vertex cur0 = yPos.remove();
			cur0.sety(currentY);
			currentY-=step;
		}
		// second half
		number=0;
		yPos.clear();
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			Vertex cur0 = iterator.next();
			if (kt==cur0.getLevel() && flmxn(cur0.gety()/10/granularity)>=it+ly && flmxn(cur0.gety()/10/granularity)<=it+ry
					&& flmxn(cur0.getx()/10/granularity)>(double)(rx-lx+1)/2+jt+lx && flmxn(cur0.getx()/10/granularity)<=jt+rx){
				cur0.setPriority((int) cur0.gety());
				yPos.add(cur0);
				number++;
			}
		}
		//First half
		index=0;
		step=(10*granularity*(ry-ly+1))/number;
		currentY=10*granularity*(ry+2)+it*10*granularity;
		while (index++<number/2){
			Vertex cur0 = yPos.remove();
			cur0.sety(currentY);
			currentY-=step;
		}
		currentY=5*granularity*(ry+1-ly)+(it-1)*10*granularity;
		while (!yPos.isEmpty()){
			Vertex cur0 = yPos.remove();
			cur0.sety(currentY);
			currentY-=step;
		}

		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			Vertex cur0 = iterator.next();
			if (kt==cur0.getLevel() && flmxn(cur0.gety()/10/granularity)>=it+ly && flmxn(cur0.gety()/10/granularity)<=it+ry
					&& flmxn(cur0.getx()/10/granularity)>=jt+lx && flmxn(cur0.getx()/10/granularity)<=jt+rx){
				double [] temp = {cur0.getx(),cur0.gety()};
				cur0.setAnchor(temp);
			}
		}

		densityCalculator();

	}

	/**
	 * Legalize placement at the end of placement solution for each scheduling level.
	 *
	 * @param cL the target scheduling level
	 */
	public void legalPlacement(int cL){
		Vertex cur0;
		PriorityQueue<Vertex> xPos=new PriorityQueue<Vertex>();
		PriorityQueue<Vertex> yPos=new PriorityQueue<Vertex>();
		ArrayList<Well> interactionWells=new ArrayList<Well>();
		Dimension place=new Dimension();
		Interaction AssignedTrap;
		int tol=(int) Math.floor(granularity*granularity*gamma), tolMax=granularity*granularity, ind=0;
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.getLevel()==cL)
				cur0.resetAnchor(); //non-commit
		}
		layout.clean();
		for (int i = 0; i < density[cL][0].length; i++) {
			for (int j = 0; j < density[cL][0].length; j++) {
				ind=0;
				for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
					cur0 = iterator.next();
					if (cur0.getMoveablity() && cur0.getLevel()==cL && flmxn(cur0.gety()/10/granularity)==i && flmxn(cur0.getx()/10/granularity)==j && ind<tol){
						ind++;
						place.height = (int)cur0.gety();
						place.width  = (int)cur0.getx();
						layout.sortInteractionWells(place, false,  2);
						interactionWells.clear();
						AssignedTrap=(Interaction)layout.getNearestFreeInteraction(place, true);
						while (flmxn(AssignedTrap.getPosition().height/10/granularity)!=i || flmxn(AssignedTrap.getPosition().width/10/granularity)!=j){
							interactionWells.add((Well) AssignedTrap);
							AssignedTrap=(Interaction)layout.getNearestFreeInteraction(place, true);
						}
						for (int k = 0; k < interactionWells.size(); k++) {
							layout.freeInteraction(interactionWells.get(k).getPosition());
						}
						AssignedTrap.AddLevel(cL);
						cur0.setTrap(AssignedTrap);
						if (RuntimeConfig.PHDEBUG)
							System.out.println("["+cL+","+i+","+j+"] "+cur0+" ["+cur0.getx()+","+cur0.gety()+"] ["+AssignedTrap.getPosition().width+","+AssignedTrap.getPosition().height+"] ");
						cur0.setx(AssignedTrap.getPosition().width);
						cur0.sety(AssignedTrap.getPosition().height);
						cur0.setUnMoveable();
					}
				}
			}
		}
		if (RuntimeConfig.PHDEBUG)
			System.out.println("--------------------------------------------------------------");
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.getx()>=layout.getLayoutSize().width-1) cur0.setx(layout.getLayoutSize().width-1.01);
			if (cur0.gety()>=layout.getLayoutSize().height-1) cur0.sety(layout.getLayoutSize().height-1.01);
			if (!cur0.getMoveablity())
				commited[cur0.getLevel()][flmxn(cur0.gety()/10/granularity)][flmxn(cur0.getx()/10/granularity)]++;
		}
		for (int i = 0; i < density[cL][0].length; i++) {
			for (int j = 0; j < density[cL][0].length; j++) {
				ind=0;
				for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
					cur0 = iterator.next();
					if (cur0.getMoveablity() && cur0.getLevel()==cL && flmxn(cur0.gety()/10/granularity)==i && flmxn(cur0.getx()/10/granularity)==j && ind<tol){
						//it does not have a location
						place.height = (int)cur0.gety();
						place.width  = (int)cur0.getx();
						layout.sortInteractionWells(place, false,  2);
						interactionWells.clear();
						AssignedTrap=(Interaction)layout.getNearestFreeInteraction(place, true);
						while (commited[cL][flmxn(AssignedTrap.getPosition().height/10/granularity)][flmxn(AssignedTrap.getPosition().width/10/granularity)]>=tol){
							if (RuntimeConfig.PHDEBUG){
								System.out.println("["+cL+","+i+","+j+"] "+cur0+" ["+cur0.getx()+","+cur0.gety()+"] ["+AssignedTrap.getPosition().width+","+AssignedTrap.getPosition().height+"] ");
								System.out.println(tol+" "+commited[cL][flmxn(AssignedTrap.getPosition().height/10/granularity)][flmxn(AssignedTrap.getPosition().width/10/granularity)]);
							}
							interactionWells.add((Well) AssignedTrap);
							AssignedTrap=(Interaction)layout.getNearestFreeInteraction(place, true);
						}
						for (int k = 0; k < interactionWells.size(); k++) {
							layout.freeInteraction(interactionWells.get(k).getPosition());
						}
						commited[cL][flmxn(AssignedTrap.getPosition().height/10/granularity)][flmxn(AssignedTrap.getPosition().width/10/granularity)]++;
						AssignedTrap.AddLevel(cL);
						cur0.setTrap(AssignedTrap);
						if (RuntimeConfig.PHDEBUG)
							System.out.println("["+cL+","+i+","+j+"]"+cur0+" ["+cur0.getx()+","+cur0.gety()+"] ["+AssignedTrap.getPosition().width+","+AssignedTrap.getPosition().height+"] ");
						cur0.setx(AssignedTrap.getPosition().width);
						cur0.sety(AssignedTrap.getPosition().height);
						cur0.setUnMoveable();
					}
				}
			}
		}
	}

	/**
	 * Calculate the number of committed instructions to small part of the fabric after rough legalization.
	 */
	public void commitedCalculator (){
		Vertex cur0, cur1;

		for(int k=0; k<=maxLevel; k++){
			for (int i = 0; i < flmxn(layout.getLayoutSize().height/10/granularity)+1; i++) {
				for (int j = 0; j < flmxn(layout.getLayoutSize().width/10/granularity)+1; j++) {
					commited[k][i][j]=0;
				}
			}
		}

		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.getx()>=layout.getLayoutSize().width) cur0.setx(layout.getLayoutSize().width-1.01);
			if (cur0.gety()>=layout.getLayoutSize().height) cur0.sety(layout.getLayoutSize().height-1.01);
			if (!cur0.getMoveablity())
				commited[cur0.getLevel()][flmxn(cur0.gety()/10/granularity)][flmxn(cur0.getx()/10/granularity)]++;
		}
		if (RuntimeConfig.PHDEBUG){
			for(int k=0; k<=maxLevel; k++){
				System.out.print(k+"     ");
				for (int i = 0; i < 5; i++) {
					for (int j = 0; j < flmxn(layout.getLayoutSize().width/10/granularity)+1; j++) {
						System.out.print(commited[k][i][j]+" ");
					}
					System.out.print("///");
				}
				System.out.println();
			}
		}


	}

	/**
	 * Calculate instruction slack based on placement of parent instructions and rough routing latency.
	 */
	public void slackCalculation(){
		Vertex cur0 = null,cur1 = null;
		int index=0, maxSlack = 0;

		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.getInteraction()!=null){
				cur0.setx(cur0.getInteraction().getPosition().width);
				cur0.sety(cur0.getInteraction().getPosition().height);
				cur0.setUnMoveable();
				cur0.setPriority((int)Math.floor(((int)layout.getOpDelay(cur0.getInstruction()))*cur0.getLevel()));
				cur0.setSlack((int)Math.floor(((int)layout.getOpDelay(cur0.getInstruction()))*(cur0.getALAPLevel()-cur0.getASAPLevel())));
			}
		}	
		for (int currentLevel = 0; currentLevel <= maxLevel; currentLevel++) {
			for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
				maxSlack=0;
				cur0 = iterator.next();
				if (cur0.getLevel()==currentLevel && cur0.getInteraction()!=null){
					for (Iterator<DefaultWeightedEdge> iterator2 = springNet.edgesOf(cur0).iterator(); iterator2.hasNext();){
						DefaultWeightedEdge edg0 = iterator2.next();
						cur1 = springNet.getEdgeTarget(edg0);
						if (cur1.equals(cur0))	cur1=springNet.getEdgeSource(edg0);
						if (!cur1.isSentinel() && cur0.getLevel()>=cur1.getLevel()){
							int temp2=		(int)(layout.getOpDelay("move")*(Math.abs(cur0.getx()-cur1.getx())+Math.abs(cur0.gety()-cur1.gety())));
							int temp = layout.getOpDelay(cur1.getInstruction())+cur1.getPriority()+
									(int)(layout.getOpDelay("move")*(Math.abs(cur0.getx()-cur1.getx())+Math.abs(cur0.gety()-cur1.gety())));
							if (Math.abs(cur0.getx()-cur1.getx())>0 && Math.abs(cur0.gety()-cur1.gety())==0){ temp +=10; temp2+=10;}
							maxSlack=Math.max(maxSlack, temp);
							if (RuntimeConfig.PHDEBUG) System.out.println("relation    "+cur0+"  "+cur1+" "+cur0.getLevel()+"  "+cur1.getLevel()+" "+temp+" "+temp2+" "+
									Math.floor(((int)layout.getOpDelay(cur0.getInstruction()))*(cur0.getALAPLevel()-cur0.getASAPLevel()+cur0.getLevel()-cur1.getLevel()-1)));

						}
					}
					if (!cur0.isSentinel()){				
						boolean hasOp;
						DefaultWeightedEdge cure;
						for (int j = 0; j < cur0.getOperandsNumber(); j++) {
							String curo = cur0.getOperand(j);
							hasOp=false;
							for (Iterator<DefaultWeightedEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
								cure=iterator0.next();
								cur1=DFG.getEdgeSource(cure);
								for (int i1 = 0; i1 < cur1.getOperandsNumber(); i1++) {
									if (cur1.getOperand(i1).equals(curo))
										hasOp=true;
								}
							}
							if (!hasOp && layout.getQubit(curo).getInitPosition().getWidth()>=0){
								int temp = (int)(layout.getOpDelay("move")*(Math.abs(cur0.getx()-layout.getQubit(curo).getInitPosition().getWidth())+
										Math.abs(cur0.gety()-layout.getQubit(curo).getInitPosition().getHeight())));
								if (Math.abs(cur0.getx()-layout.getQubit(curo).getInitPosition().getWidth())>0 &&
										Math.abs(cur0.gety()-layout.getQubit(curo).getInitPosition().getHeight())==0) temp +=10;
								maxSlack=Math.max(maxSlack, temp);
								if (RuntimeConfig.PHDEBUG) System.out.println("initial   "+cur0+"  "+" "+temp+layout.getQubit(curo).getInitPosition().getWidth()+" "+
										layout.getQubit(curo).getInitPosition().getHeight());
							}
							else if (!hasOp && layout.getQubit(curo).getInitPosition().getWidth()<0){
								int temp = 10;
								maxSlack=Math.max(maxSlack, temp);
								if (RuntimeConfig.PHDEBUG) System.out.println("initial-assumed   "+cur0+"  "+" "+temp);
							}
						}
					}
					if (RuntimeConfig.PHDEBUG) System.out.println(cur0+"  @["+cur0.getInteraction().getPosition().width+" , "+cur0.getInteraction().getPosition().height+"]"+" "+cur0.getSlack()+" "+cur0.getPriority());
					cur0.setSlack(cur0.getSlack()-Math.max(0, (maxSlack-cur0.getPriority())));
					cur0.setPriority(maxSlack);
					if (RuntimeConfig.PHDEBUG) System.out.println(cur0+"  @["+cur0.getInteraction().getPosition().width+" , "+cur0.getInteraction().getPosition().height+"]"+" "+cur0.getSlack()+" "+cur0.getPriority());
					if (RuntimeConfig.PHDEBUG) System.out.println();
				}
			}
		}
		for (int i = 0; i < maxLevel+1; i++) {
			//			System.out.print("Level "+i+"  "+"{");
			for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
				cur0 = iterator.next();
				if (cur0.getLevel()==i && cur0.getInteraction()!=null)
					if (RuntimeConfig.PHDEBUG) 
						System.out.print("{ "+cur0+"  @["+cur0.getInteraction().getPosition().width+" , "+cur0.getInteraction().getPosition().height+"]"+" "+cur0.getLevel()+" "+cur0.getASAPLevel()+" "+cur0.getALAPLevel()+"}, ");
			}
			//			System.out.println("}");
		}

	}

	/**
	 * Update spring forces to be used in next optimization procedures.
	 *
	 * @param currentLevel the current scheduling level
	 */
	public void updateSpringForces (int currentLevel){
		Vertex cur0, cur1;
		double temp=0;
		edges.clear();


		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			edges.clear();
			if (cur0.getLevel()>=0){
				for (Iterator<DefaultWeightedEdge> iterator2 = springNet.edgesOf(cur0).iterator(); iterator2.hasNext();){
					DefaultWeightedEdge edg0 = iterator2.next();
					cur1 = springNet.getEdgeTarget(edg0);
					if (cur1.equals(cur0))	cur1=springNet.getEdgeSource(edg0);
					if (!cur1.isSentinel() && cur0.getLevel()>=cur1.getLevel()){
						int temp2=		(int)(layout.getOpDelay("move")*(Math.abs(cur0.getx()-cur1.getx())+Math.abs(cur0.gety()-cur1.gety())));
						int temp1 = layout.getOpDelay(cur1.getInstruction())+cur1.getPriority()+
								(int)Math.floor(layout.getOpDelay("move")*(Math.abs(cur0.getx()-cur1.getx())+Math.abs(cur0.gety()-cur1.gety())));
						edges.put(edg0, temp2);
					}
				}
				if (!cur0.isSentinel()){				
					boolean hasOp;
					for (int j = 0; j < cur0.getOperandsNumber(); j++) {
						String curo = cur0.getOperand(j);
						hasOp=false;
						for (Iterator<DefaultWeightedEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
							DefaultWeightedEdge edg0=iterator0.next();
							cur1=DFG.getEdgeSource(edg0);
							for (int i1 = 0; i1 < cur1.getOperandsNumber(); i1++) {
								if (cur1.getOperand(i1).equals(curo))
									hasOp=true;
							}
						}
						if (!hasOp && layout.getQubit(curo).getInitPosition().getWidth()>=0){
							int temp1 = (int)(layout.getOpDelay("move")*(Math.abs(cur0.getx()-layout.getQubit(curo).getInitPosition().getWidth())+
									Math.abs(cur0.gety()-layout.getQubit(curo).getInitPosition().getHeight())));
							edges.put(null, temp1);
						}
					}
				}
				for (Iterator<DefaultWeightedEdge> iterator1 = springNet.edgesOf(cur0).iterator(); iterator1.hasNext();) {
					DefaultWeightedEdge edg0 = iterator1.next();
					cur1 = springNet.getEdgeTarget(edg0);
					if (cur1.equals(cur0))	cur1=springNet.getEdgeSource(edg0);
					if (!cur1.isSentinel() && cur0.getLevel()>cur1.getLevel() && ((edges.get(edg0)-1)/(layout.getOpDelay("c-x")*Math.max(1, (cur0.getALAPLevel()-cur0.getASAPLevel()-1+cur0.getLevel()-cur1.getLevel()))))>1.0){
						double temp0 = ((double)1/(cur0.getALAPLevel()-cur0.getASAPLevel()+-1+Math.abs(cur0.getLevel()-cur1.getLevel()))) /(double)layout.getOpDelay("c-x");
						if (cur0.getALAPLevel()==cur0.getASAPLevel() || cur0.getLevel()==cur1.getLevel()) temp0 = (double)1/(double)(minimumDeadline);
						temp = Math.max(Math.min(temp0* Math.pow(((double)(edges.get(edg0))/((double)1/temp0)), 0.01*(1+iteration)), maxWeight), springNet.getEdgeWeight(edg0));
						System.out.println(cur0+" "+cur1+" "+springNet.getEdgeWeight(edg0)+" "+edges.get(edg0)+" "+layout.getOpDelay("c-x")*(cur0.getALAPLevel()-cur0.getASAPLevel()-1+cur0.getLevel()-cur1.getLevel()));
						springNet.setEdgeWeight(edg0, temp);
						System.out.println(cur0+" "+cur1+" "+springNet.getEdgeWeight(edg0)+"      "+cur0.getLevel()+"         "+cur1.getLevel());
					}
				}
			}
		}
	}

	/**
	 * Optimization with redefinition of levels.
	 * Main placement optimization function. Calls the other functions iteratively to finalize
	 * the placement solution. 
	 * Make changes to the scheduling level of instructions based on the placement solution
	 *  and start the placement solution from that point.
	 *
	 * @param eds the event driven simualtor handle to perform scheduling level change
	 * @param threshold the tolerable threshold for slack 
	 */
	public void optimizationwithRedefinitionofLevels(EventDrivenSimulator eds, int threshold){
		// first apply one iteration
		optimize();
		double currentSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
		double newSum=currentSum-5;
		if (RuntimeConfig.PHDEBUG)     
			System.out.println("total Objective   "+currentSum);
		int index=0;
		while ((currentSum-newSum)>1 || index<10){
			index++;
			currentSum=newSum;
			if (RuntimeConfig.PHDEBUG)     
				System.out.println("total Objective1   "+currentSum);
			initilize(false);
			optimize();
			newSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
			densityCalculator();
			if (RuntimeConfig.PHDEBUG)     
				System.out.println("total Objective2   "+newSum);
		}
		//		updateSpringForces(0);
		iteration=1;
		boolean legalized=false;
		int i=0,j=0;
		for (j = 0; j <= maxLevel; j++){
			legalized=false;i=0;
			while (!legalized && i<5) {
				i++;
				iteration++;
				legalized=roughLegalization(j);
				initilize(true);
				optimize();
				newSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
				densityCalculator();
				//				System.out.println("------------------------------------------------------"+iteration+" "+i+" "+j);
				//				updateSpringForces(j);
				if (RuntimeConfig.PHDEBUG)     
					System.out.println(j+" "+i+" "+"total Objective1   "+newSum);
			}
			legalPlacement(j);
			slackCalculation();
			changeLevels(eds,j,threshold);
			if (j<maxLevel){
				for(int j1 = 0; j1 <=2; j1++){
					currentSum=newSum;
					if (RuntimeConfig.PHDEBUG)     
						System.out.println("total Objective1   "+currentSum);
					initilize(false);
					optimize();
					newSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
					densityCalculator();
					//					System.out.println("------------------------------------------------------"+iteration+" "+i+" "+j);
					//					updateSpringForces(j+1);
					if (RuntimeConfig.PHDEBUG)     
						System.out.println(j+" "+i+" "+"total Objective2   "+newSum);
				}
			}
		}
		//		for (int j1 = 0; j1 <= maxLevel; j1++) {
		//			legalPlacement(j1);
		//		}
		//		optimize();
		newSum=objectiveSum(1/minimumDeadline, 1/minimumDeadline);
		densityCalculator();
		if (RuntimeConfig.PHDEBUG)
			System.out.println("total Objective   "+"         "+newSum);
		commitedCalculator();
		if (RuntimeConfig.PHDEBUG)
			System.out.println("total Objective   "+"         "+newSum);
		//		printLocations();
		//		updateSpringForces(j);
		slackCalculation();

		//		System.out.println("--------------------------------------DONE WITH LEVELIZATION----------------------------------------------------");
		//		//Now Levels are determined.
		//		//remove all the assigned interactions
		//		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
		//			Vertex cur0 = iterator.next();
		//			cur0.setMoveable();
		//			if (cur0.getInteraction()!=null){
		//				cur0.getInteraction().RemoveLevel(cur0.getLevel());
		//			}
		//			cur0.setTrap(null);
		//		}
		//		optimization();
		if (!RuntimeConfig.use_GUROBI){
			octave.close();
		}
	}


	/**
	 * Change scheduling level of instructions with big slack.
	 *
	 * @param eds the event driven simualtor handle to perform scheduling level change
	 * @param currentLevel the current level
	 * @param threshold the tolerable threshold for slack 
	 */
	public void changeLevels (EventDrivenSimulator eds, int currentLevel, int threshold){
		PriorityQueue<Vertex> slackPos=new PriorityQueue<Vertex>();
		PriorityQueue<Vertex> slackNeg=new PriorityQueue<Vertex>();
		Vertex cur0, cur1;
		double temp=0;
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.getLevel()==currentLevel){// This means that this Vertex is legalized in this step
				if (cur0.getPriority()>(currentLevel+0.6)*layout.getOpDelay(cur0.getInstruction())){
					slackNeg.add(cur0);
					//This Vertex does not fit in this step, reassign it to the next step and remove the interaction reservation
					cur0.setMinLevel(currentLevel+1);
					cur0.getInteraction().RemoveLevel(currentLevel);
					cur0.setTrap(null);
					cur0.setMoveable();
				}
			}
		}
		//		AddVertex(currentLevel);

		eds.removeExtraEdges(DFG);
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, DFG,threshold);
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, DFG,threshold);
		eds.computeListSchRank(DFG);
		boolean complete=false;
		while(!complete){
			eds.BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, DFG,threshold);
			complete=eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, DFG,threshold);
			eds.computeListSchRank(DFG);
		}
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, DFG,threshold);
		complete=eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, DFG,threshold);
		eds.computeListSchRank(DFG);
		//    	eds.addDummyEdgesForceDirectedScheduling(DFG);
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, DFG,threshold);
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, DFG,threshold);
		springNet = new SimpleWeightedGraph<Vertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		//    	System.out.println("OPTIMIZATION   "+springNet.edgeSet().size()+"   OPTIMIZATION   "+DFG.edgeSet().size());
		springNet=makeSpringNetwork();
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			maxLevel=Math.max(maxLevel,cur0.getLevel());
		}

		System.out.println("OPTIMIZATION   "+springNet.edgeSet().size()+"   OPTIMIZATION   "+DFG.edgeSet().size()+"  "+maxLevel);
		density = new int[maxLevel+1][maxNumber+1][maxNumber+1];
		commited = new int[maxLevel+1][maxNumber+1][maxNumber+1];
	}


	/**
	 * Adds the vertex to the new spring network which is used for obj and const. function creation.
	 *
	 * @param currentLevel the current level
	 */
	public void AddVertex (int currentLevel){
		PriorityQueue<Vertex> slackPos=new PriorityQueue<Vertex>();
		PriorityQueue<Vertex> slackNeg=new PriorityQueue<Vertex>();
		ArrayList<Vertex> MV= new ArrayList<Vertex>();
		Vertex cur0, cur1;
		double temp=0;
		for (Iterator<Vertex> iterator = springNet.vertexSet().iterator(); iterator.hasNext();) {
			cur0 = iterator.next();
			if (cur0.getLevel()==currentLevel){// This means that this Vertex is legalized in this step
				if (cur0.getPriority()<=(currentLevel+0.6)*layout.getOpDelay(cur0.getInstruction())){
					slackPos.add(cur0);
					cur0.getInteraction().RemoveLevel(currentLevel);
					cur0.setTrap(null);
					cur0.setMoveable();
				}
				else if (cur0.getPriority()>(currentLevel+0.6)*layout.getOpDelay(cur0.getInstruction())){
					//This Vertex does not fit in this step, reassign it to the next step and remove the interaction reservation
					if ((int)Math.round((double)cur0.getPriority()/layout.getOpDelay(cur0.getInstruction()))>maxLevel)
						MV.add(cur0);
					cur0.setLevel(Math.min(maxLevel, (int)Math.round((double)cur0.getPriority()/layout.getOpDelay(cur0.getInstruction()))));
					cur0.setPriority(-cur0.getPriority());
					slackNeg.add(cur0);
				}
			}
		}

		boolean legalized=false;int i=0;

		//Add neg Slack vertexes one by one to see a bad event
		boolean badEvent = false;
		while(slackNeg.size()>0 &&  !badEvent){
			cur0=slackNeg.remove();
			cur0.setLevel(currentLevel);
			legalized=false; i=0;
			while (!legalized && i<5) {
				i++;
				legalized=roughLegalization(currentLevel);
				initilize(true);
				optimize();
				densityCalculator();
			}
			legalPlacement(currentLevel);
			slackCalculation();
			if (cur0.getPriority()>(currentLevel+0.6)*layout.getOpDelay(cur0.getInstruction()))
				badEvent=true;
			for (Iterator<Vertex> iterator = slackPos.iterator(); iterator.hasNext();) {
				cur1 = iterator.next();
				if (cur1.getInteraction()!=null){
					cur1.getInteraction().RemoveLevel(currentLevel);
				}
				cur1.setTrap(null);
				cur1.setMoveable();
				if (cur1.getPriority()>(currentLevel+0.6)*layout.getOpDelay(cur0.getInstruction()))
					badEvent=true;
			}
			if (badEvent){
				cur0.setLevel(Math.min(maxLevel, currentLevel+1));
				if (currentLevel+1>maxLevel)
					MV.add(cur0);
				if (cur0.getInteraction()!=null)
					cur0.getInteraction().RemoveLevel(currentLevel);
				cur0.setTrap(null);
				cur0.setMoveable();
				slackNeg.add(cur0);
			}
			else{
				cur0.setMinLevel(currentLevel);
				cur0.getInteraction().RemoveLevel(currentLevel);
				cur0.setTrap(null);
				cur0.setMoveable();
				slackPos.add(cur0);
			}
		}

		legalized=false;i=0;
		while (!legalized && i<5) {
			i++;
			legalized=roughLegalization(currentLevel);
			initilize(true);
			optimize();
			densityCalculator();
		}
		legalPlacement(currentLevel);
		slackCalculation();
		for (Iterator<Vertex> iterator = slackPos.iterator(); iterator.hasNext();) {
			cur1 = iterator.next();
			if (cur1.getInteraction()==null){
				System.out.println("WTF");
				System.exit(-1);
			}
		}
		for (int j = 0; j < MV.size(); j++) {
			cur0 = MV.get(j);
			if (cur0.getInteraction()!=null)
				cur0.getInteraction().RemoveLevel(currentLevel);
			cur0.setTrap(null);
			cur0.setMoveable();
		}
	}

}
