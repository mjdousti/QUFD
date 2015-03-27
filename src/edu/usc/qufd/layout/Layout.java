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
import java.util.*;
import java.util.Map.Entry;

import org.jgrapht.graph.*;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.Operation;


// TODO: Auto-generated Javadoc
/**
 * The Class Layout.
 */
public class Layout {	
	
	/** The dim. */
	private Dimension dim=new Dimension();
	
	/** The fabric. */
	private Well [][]fabric;
	
	/** The supported ops. */
	private Map<String, Operation>supportedOps=new HashMap<String, Operation>();
	
	/** The time unit. */
	private String timeUnit;
	
	/** The well insts. */
	private Map<String, Vector<Operation>> wellInsts = new HashMap<String, Vector<Operation>>();
	
	/** The cell size. */
	private Dimension cellSize=new Dimension();


	//May it's better to use a MiniMap
	/** The qubits. */
	private Map<String, Qubit> qubits=new HashMap<String, Qubit>();
	
	/** The interaction well empty list. */
	private ArrayList<Well> interactionWellEmptyList=new ArrayList<Well>();
	
	/** The creation well empty list. */
	private ArrayList<Well> creationWellEmptyList=new ArrayList<Well>();

	/** The layout graph. */
	private SimpleWeightedGraph<Well, ChannelEdge> layoutGraph;
	//	private int availableLasers=RuntimeConfig.LASERS;


	/**
	 * The Enum Types.
	 */
	public enum Types{
		
		/** The Basic. */
		Basic, 
 /** The Creation. */
 Creation, 
 /** The Interaction. */
 Interaction, 
 /** The Empty. */
 Empty, 
 /** The Unknown. */
 Unknown
	}

	/**
	 * Instantiates a new layout.
	 */
	public Layout(){
		wellInsts.put(new String ("basic"), new Vector<Operation>());
		wellInsts.put(new String ("creation"), new Vector<Operation>());
		wellInsts.put(new String ("interaction"), new Vector<Operation>());
	}
	
	/**
	 * Adds the new well.
	 *
	 * @param wellName the well name
	 */
	public void addNewWell(String wellName){
		wellInsts.put(new String (wellName.toLowerCase()), new Vector<Operation>());
	}
	
	/**
	 * X yto quadruple.
	 *
	 * @param d the d
	 * @return the string
	 */
	public String XYtoQuadruple(Dimension d){
		String temp;
		
		temp="("+(int)(d.getHeight()/cellSize.getHeight())+","+(int)(d.getWidth()/cellSize.getWidth())+","+
				(int)(d.getHeight()%cellSize.getHeight())+","+(int)(d.getWidth()%cellSize.getWidth())+")";		
		return temp;
	}

	/**
	 * Sets the time unit.
	 *
	 * @param s the new time unit
	 */
	public void setTimeUnit(String s){
		timeUnit=s;
	}
	
	/**
	 * Gets the time unit.
	 *
	 * @return the time unit
	 */
	public String getTimeUnit(){
		return timeUnit;
	}	
	
	/**
	 * Inits the fabric.
	 *
	 * @param fabricSize the fabric size
	 * @param tileSize the tile size
	 * @param tile the tile
	 */
	public void initFabric(Dimension fabricSize, Dimension tileSize, Types[][] tile){
		Dimension temp=new Dimension();
		dim=new Dimension(fabricSize);
		int i,j,k,l;
		
		cellSize.setSize(tileSize);
		dim.height=(fabricSize.height)*(tileSize.height-1)+1;
		dim.width=(fabricSize.width)*(tileSize.width-1)+1;

		fabric=new Well[dim.height][dim.width];

		for (i = 0; i < fabricSize.height; i++) {
			for (j = 0; j < fabricSize.width; j++) {
				for (k = 0; k < tileSize.height-1; k++) {
					for (l = 0; l < tileSize.width-1; l++) {
						temp.height=i*(tileSize.height-1) + k;
						temp.width=j*(tileSize.width-1) + l;
						if (tile[k][l]==Types.Interaction)
							fabric[temp.height][temp.width]=new Interaction(temp, tile[k][l]);
						else
							fabric[temp.height][temp.width]=new Well(temp, tile[k][l]);
					}
				}
			}
		}

		//tiling the horizontal border
		i=fabricSize.height-1;
		for (j = 0; j < fabricSize.width; j++) {
			k = tileSize.height-1;
			for (l = 0; l < tileSize.width-1; l++) {
				temp.height=i*(tileSize.height-1) + k;
				temp.width=j*(tileSize.width-1) + l;
				if (tile[k][l]==Types.Interaction)
					fabric[temp.height][temp.width]=new Interaction(temp, tile[k][l]);
				else
					fabric[temp.height][temp.width]=new Well(temp, tile[k][l]);
			}
		}


		j=fabricSize.width-1;
		for (i = 0; i < fabricSize.height; i++) {
			l = tileSize.width-1;
			for (k = 0; k < tileSize.height-1; k++) {
				temp.height=i*(tileSize.height-1) + k;
				temp.width=j*(tileSize.width-1) + l;
				if (tile[k][l]==Types.Interaction)
					fabric[temp.height][temp.width]=new Interaction(temp, tile[k][l]);
				else
					fabric[temp.height][temp.width]=new Well(temp, tile[k][l]);
			}
		}

		//placing the last well @ bottom right
		temp.height=dim.height-1;
		temp.width=dim.width-1;
		if (tile[tileSize.height-1][tileSize.width-1]==Types.Interaction)
			fabric[dim.height-1][dim.width-1]=new Interaction(temp, tile[tileSize.height-1][tileSize.width-1]);
		else
			fabric[dim.height-1][dim.width-1]=new Well(temp, tile[tileSize.height-1][tileSize.width-1]);


		makeGraph();
//		printLayoutGraph();
//		printFabric();
	}
	
	/**
	 * Inits the fabric2.
	 *
	 * @param fabricSize the fabric size
	 * @param tileSize the tile size
	 * @param tile the tile
	 */
	public void initFabric2(Dimension fabricSize, Dimension tileSize, Types[][] tile){
		Dimension temp=new Dimension();
		dim=new Dimension(fabricSize);
		int i,j,k,l;
		
		cellSize.setSize(tileSize);
		dim.height=(fabricSize.height)*(tileSize.height);
		dim.width=(fabricSize.width)*(tileSize.width);

		fabric=new Well[dim.height][dim.width];

		for (i = 0; i < fabricSize.height; i++) {
			for (j = 0; j < fabricSize.width; j++) {
				for (k = 0; k < tileSize.height; k++) {
					for (l = 0; l < tileSize.width; l++) {
						temp.height=i*(tileSize.height) + k;
						temp.width=j*(tileSize.width) + l;
						if (tile[k][l]==Types.Interaction)
							fabric[temp.height][temp.width]=new Interaction(temp, tile[k][l]);
						else
							fabric[temp.height][temp.width]=new Well(temp, tile[k][l]);
					}
				}
			}
		}

		makeGraph();
	}
	
	/**
	 * Adds the inst to well.
	 *
	 * @param wellName the well name
	 * @param inst the inst
	 */
	public void addInstToWell(String wellName, String inst){
		wellInsts.get(wellName.toLowerCase()).add(supportedOps.get(inst.toLowerCase()));
	}
	
	/**
	 * Adds the new operation.
	 *
	 * @param op the op
	 */
	public void addNewOperation(Operation op){
		supportedOps.put(op.getName().toLowerCase(), op);
	}
	
	/**
	 * Prints the fabric.
	 */
	public void printFabric(){
		int i, j;
		System.out.println("Fabric "+dim.height+","+dim.width+":");
		for (j = 0; j < dim.width; j++) {
			for (i = 0; i < dim.height; i++) {
				if (fabric[i][j].getType()==Types.Empty)
					System.out.print(" ");
				else if (fabric[i][j].getType()==Types.Basic)
					System.out.print("B");
				else if (fabric[i][j].getType()==Types.Creation)
					System.out.print("C");
				else if (fabric[i][j].getType()==Types.Interaction)
					System.out.print("I");
				else
					System.out.print("X");
			}
			System.out.println();
		}

	}

	//	
	/**
	 * Prints the qubit places.
	 */
	public void printQubitPlaces(){
		System.out.println("Qubit places:");
		Set<Entry<String, Qubit>> qSet =qubits.entrySet();
		for (Iterator<Entry<String, Qubit>> iterator = qSet.iterator(); iterator.hasNext();) {
			Entry<String, Qubit> entry = iterator.next();
			System.out.println(entry.getKey()+" "+entry.getValue().getPosition().height+"x"+entry.getValue().getPosition().width);

		}
	}


	/**
	 * Free interaction.
	 *
	 * @param x the x
	 */
	public void freeInteraction (Dimension x){
		if (getWellType(x)==Types.Interaction){
			interactionWellEmptyList.add(getWell(x));		
		}
	}
	
	
	/**
	 * Free.
	 *
	 * @param x the x
	 * @param qubit the qubit
	 */
	public void free(Dimension x, Qubit qubit) {
		getWell(x).removeQubit(qubit);
		//		if (!getWell(x).isOccupied() && getWellType(x)==Types.Interaction){
		//			interactionWellEmptyList.add(getWell(x));
		//			if (RuntimeConfig.DEBUG)
		//				System.out.println("\n"+x.height+","+x.width+" is freed!");
		//		}else if (!getWell(x).isOccupied() && getWellType(x)==Types.Creation){
		//			creationWellEmptyList.add(getWell(x));
		//		}
		if (getWellType(x)==Types.Interaction && ((Interaction)getWell(x)).removeFromProgressList(qubit.getName())){
//			System.out.println("BEFORE:----");
//			printFreeIneractionWells();
			interactionWellEmptyList.add(getWell(x));
//			System.out.println("AFTER:----");
//			printFreeIneractionWells();

			if (RuntimeConfig.DEBUG)
				System.out.println("\n"+qubit.getName()+" freed "+x.height+","+x.width+"!");			
		}
	}
	//
	//
	/**
	 * Occupy.
	 *
	 * @param x the x
	 * @param q the q
	 */
	public void occupy(Dimension x, Qubit q){
		getWell(x).addQubits(q);
		//		if (getWellType(x)==Types.Interaction){
		//			interactionWellEmptyList.remove(getWell(x));
		//		}else if (getWellType(x)==Types.Creation){
		//			creationWellEmptyList.remove(getWell(x));
		//		}
	}

	/**
	 * Find nearest node (in layout graph) next to the passed dimension on the fabric.
	 *
	 * @param a the given dimension
	 * @return nearest node
	 */

	public Well getNearestNode(Dimension a){
		if (layoutGraph.containsVertex(getWell(a)))
			return getWell(a);

		//the passed basic well is in a horizontal chain
		if (a.width+1<dim.width && fabric[a.height][a.width+1].getType()!=Types.Empty){
			for (int i=1;;i++){
				if (a.width+i<dim.width && layoutGraph.containsVertex(fabric[a.height][a.width+i])){
					return fabric[a.height][a.width+i];
				}else if (0<=a.width-i && layoutGraph.containsVertex(fabric[a.height][a.width-i])){
					return fabric[a.height][a.width-i];
				}
			}
		}//the passed basic well is in a vertical chain
		else{
			for (int i=1;;i++){
				if (a.height+i<dim.height && layoutGraph.containsVertex(fabric[a.height+i][a.width])){
					return fabric[a.height+i][a.width];
				}else if (0<=a.height-i && layoutGraph.containsVertex(fabric[a.height-i][a.width])){
					return fabric[a.height-i][a.width];
				}
			}
		}
	}

	
	/**
	 * Gets the nearest nodes.
	 *
	 * @param a the a
	 * @return the nearest nodes
	 */
	public ArrayList<Well> getNearestNodes(Dimension a){
		ArrayList<Well> nodes=new ArrayList<Well>();
		if (layoutGraph.containsVertex(getWell(a))){
			nodes.add(getWell(a));
			return nodes;
		}

		//the passed basic well is in a horizontal chain
		if (a.width+1<dim.width && fabric[a.height][a.width+1].getType()!=Types.Empty){
			for (int i=1;;i++){
				if (a.width+i<dim.width && layoutGraph.containsVertex(fabric[a.height][a.width+i])){
					nodes.add(fabric[a.height][a.width+i]);
					if (nodes.size()==2)
						return nodes;
				}else if (0<=a.width-i && layoutGraph.containsVertex(fabric[a.height][a.width-i])){
					nodes.add(fabric[a.height][a.width-i]);
					if (nodes.size()==2)
						return nodes;
				}
			}
		}//the passed basic well is in a vertical chain
		else{
			for (int i=1;;i++){
				if (a.height+i<dim.height && layoutGraph.containsVertex(fabric[a.height+i][a.width])){
					nodes.add(fabric[a.height+i][a.width]);
					if (nodes.size()==2)
						return nodes;		
				}else if (0<=a.height-i && layoutGraph.containsVertex(fabric[a.height-i][a.width])){
					nodes.add(fabric[a.height-i][a.width]);
					if (nodes.size()==2)
						return nodes;		
				}
			}
		}
	}

	
	
	/**
	 * Checks for vert way.
	 *
	 * @param a the a
	 * @return true, if successful
	 */
	public boolean hasVertWay(Dimension a){
		if (a.height-1>=0 && fabric[a.height-1][a.width].getType()!=Types.Empty)
			return true;
		else if (a.height+1<dim.height && fabric[a.height+1][a.width].getType()!=Types.Empty)
			return true;
		else
			return false;
	}

	/**
	 * Checks for horz way.
	 *
	 * @param a the a
	 * @return true, if successful
	 */
	public boolean hasHorzWay(Dimension a){
		if (a.width-1>=0 && fabric[a.height][a.width-1].getType()!=Types.Empty)
			return true;
		else if (a.width+1<dim.width && fabric[a.height][a.width+1].getType()!=Types.Empty)
			return true;
		else
			return false;
	}



	//	
	//	public Channel getNearestChannel(Dimension a){
	//		return getNearestChannel(a.height, a.width);	
	//	}	
	//	
	//	public Well getNearestJunction(Dimension x){
	//		return getNearestJunction(x.height, x.width);
	//	}	
	//	
	//	public Well getNearestJunction(int height, int width){
	//		Dimension temp;
	//		switch (fabric[height][width][0].getSquareType()){
	//		case Junction:
	//			return fabric[height][width][1];
	//		case Trap:	//Finds the nearest channel and use the same method for the nearest neighbour for channel. 
	//			if (fabric[height-1][width][0]!=null && fabric[height-1][width][0].getSquareType()==Types.Channel)
	//				height--;
	//			else if (fabric[height+1][width][0]!=null && fabric[height+1][width][0].getSquareType()==Types.Channel)
	//				height++;
	//			else if (fabric[height][width-1][0]!=null && fabric[height][width-1][0].getSquareType()==Types.Channel)
	//				width--;
	//			else if (fabric[height][width+1][0]!=null && fabric[height][width+1][0].getSquareType()==Types.Channel)
	//				width++;
	//		case Channel:
	//			if (distance(((Channel)fabric[height][width][0]).getLeftBorder(), new Dimension(width, height)) <= distance(((Channel)fabric[height][width][0]).getRightBorder(), new Dimension(width, height))){
	//				temp=new Dimension(((Channel)fabric[height][width][0]).getLeftBorder());
	//				if (((Channel)fabric[height][width][0]).isHorizontal())
	//					temp.width--;
	//				else
	//					temp.height--;
	////				return getSquare(temp);
	//				if (((Channel)fabric[height][width][0]).isHorizontal())
	//					return fabric[temp.height][temp.width][1];
	//				else
	//					return fabric[temp.height][temp.width][2];
	//			}else{
	//				temp=new Dimension(((Channel)fabric[height][width][0]).getRightBorder());
	//				if (((Channel)fabric[height][width][0]).isHorizontal())
	//					temp.width++;
	//				else
	//					temp.height++;
	////				return getSquare(temp);
	//				if (((Channel)fabric[height][width][0]).isHorizontal())
	//					return fabric[temp.height][temp.width][1];
	//				else
	//					return fabric[temp.height][temp.width][2];
	//			}
	//		default:
	//			return null;
	//
	//		}
	//	}
	//	
	//	public boolean repeated(ArrayList<Trap> shuffleList){
	//		for(int i=0;i<explored.size();i++){
	//			for(int j=0;j<shuffleList.size();j++){
	//				if (!shuffleList.get(j).getPosition().equals(explored.get(i).get(j).getPosition())){
	//					continue;
	//				}
	//				else if (j==shuffleList.size()-1){
	//					return true;
	//				}
	//			}
	//		}
	//		return false;
	//	}
	//	
	//	public void printExploredPaths(){
	//		for (int i = 0; i < explored.size(); i++) {
	//			for (int j = 0; j < explored.get(i).size(); j++) {
	//				System.out.print(explored.get(i).get(j).getPosition().height+"x"+explored.get(i).get(j).getPosition().width+" ");
	//			}
	//			System.out.println();
	//
	//		}
	//	}
	//	
	/**
	 * Sort creation wells.
	 *
	 * @param a the a
	 * @param shuffle the shuffle
	 * @param qubitCount the qubit count
	 */
	public void sortCreationWells( Dimension a, boolean shuffle, int qubitCount){
		Collections.sort(creationWellEmptyList, new NearestWell(a));
		if (shuffle){
			ArrayList<Well> shuffleList=new ArrayList<Well>();
			for (int i=0;i<qubitCount;i++){
				shuffleList.add(creationWellEmptyList.remove(0));
			}
			//do{
			Collections.shuffle(shuffleList, new Random(System.nanoTime()));
			//}while(repeated(shuffleList));
			//explored.add(shuffleList);
			for (int i=0;i<qubitCount;i++){
				creationWellEmptyList.add(i, shuffleList.get(i));
			}
		}
	}

	/**
	 * Sort interaction wells.
	 *
	 * @param a the a
	 * @param shuffle the shuffle
	 * @param qubitCount the qubit count
	 */
	public void sortInteractionWells( Dimension a, boolean shuffle, int qubitCount){
		Collections.sort(interactionWellEmptyList, new NearestWell(a));
		if (shuffle){
			ArrayList<Well> shuffleList=new ArrayList<Well>();
			for (int i=0;i<qubitCount;i++){
				shuffleList.add(interactionWellEmptyList.remove(0));
			}
			//do{
			Collections.shuffle(shuffleList, new Random(System.nanoTime()));
			//}while(repeated(shuffleList));
			//explored.add(shuffleList);
			for (int i=0;i<qubitCount;i++){
				interactionWellEmptyList.add(i, shuffleList.get(i));
			}
		}
	}


	//	
	/**
	 * Gets the nearest free interaction.
	 *
	 * @param a the a
	 * @param assign the assign
	 * @return the nearest free interaction
	 */
	public Well getNearestFreeInteraction(Dimension a, boolean assign){
		int count=0;
		Well temp;
		if (interactionWellEmptyList.size()==0){
			System.err.println("No new interaction well!");
			System.exit(-1);
		}
		Collections.sort(interactionWellEmptyList, new NearestWell(a));
		if (assign){
			if (RuntimeConfig.DEBUG)
				System.out.println("Interaction "+a.height+","+a.width+" is reserved.");
//			System.out.println("BEFORE:----");
//			printFreeIneractionWells();
			temp= interactionWellEmptyList.remove(0);
//			System.out.println("AFTER:----");
//			printFreeIneractionWells();
			return temp;
		}else
			return interactionWellEmptyList.get(0);
	}
	
	/**
	 * Prints the free ineraction wells.
	 */
	public void printFreeIneractionWells(){
		for (int i = 0; i < interactionWellEmptyList.size(); i++) {
			System.out.print("["+interactionWellEmptyList.get(i).getPosition().height+","+interactionWellEmptyList.get(i).getPosition().width+"]////");
		}
		System.out.println();
	}

	/**
	 * Gets the nearest free creation.
	 *
	 * @param assign the assign
	 * @return the nearest free creation
	 */
	public Well getNearestFreeCreation(boolean assign){
		if (creationWellEmptyList.size()==0){
			System.err.println("No new creation well!");
			System.exit(-1);
		}
		//		Collections.sort(creationWellEmptyList, new NearestWell(a));
		if (assign)
			return creationWellEmptyList.remove(0);
		else
			return creationWellEmptyList.get(0);
	}

	//	public static int distance (Dimension p, Dimension q){
	//		if (p.height==q.height)
	//			return Math.abs(p.width-q.width);
	//		else
	//			return Math.abs(p.height-q.height);
	//	}
	//
	//	
	//	public int getMaxAllowedQubits(){
	//		return qubitsNo;
	//	}
	//	
	/**
	 * Assign new qubit.
	 *
	 * @param name the name
	 * @param initPos the init pos
	 * @param inital the inital
	 * @param fin the fin
	 * @return true, if successful
	 */
	public boolean assignNewQubit(String name, Dimension initPos, Dimension inital, Dimension fin){
		Qubit temp;
		if (RuntimeConfig.DEBUG)
			System.out.println("Qubit "+name+" is assigned @ "+initPos.height+"x"+initPos.width+".");
		
		if (!qubits.containsKey(name)){
			temp= new Qubit(name, initPos, this, inital, fin );
			qubits.put(name, temp);
		}
		else{
			temp = qubits.get(name);
			temp.setPosition(initPos);
		}

		//Just to be extra cautious
		if (initPos.height>=0 && initPos.width>=0 && isCreation(initPos.height, initPos.width)){
			occupy(initPos, temp);
		}

		return true;
	}

	/*
	 * Added by Hadi!
	 */
	/**
	 * Removes the qubit.
	 *
	 * @param name the name
	 * @param initPos the init pos
	 * @return true, if successful
	 */
	public boolean removeQubit(String name, Dimension initPos){
		if (qubits.containsKey(name)){
			Qubit temp=new Qubit(name, qubits.get(name).getPosition(), this, qubits.get(name).getPosition(), qubits.get(name).getPosition());
			getWell(initPos).removeQubit(temp);
			qubits.remove(name);
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Gets the qubit.
	 *
	 * @param s the s
	 * @return the qubit
	 */
	public Qubit getQubit(String s){
		return qubits.get(s);		
	}
	
	/**
	 * Gets the qubit count.
	 *
	 * @return the qubit count
	 */
	public int getQubitCount(){
		return qubits.size();		
	}

	/**
	 * Gets the op delay.
	 *
	 * @param s the s
	 * @return the op delay
	 */
	public int getOpDelay(String s){
		return supportedOps.get(s.toLowerCase()).getDelay();
	}

	/**
	 * Return operation.
	 *
	 * @param s the s
	 * @return the operation
	 */
	public Operation returnOperation(String s){
		return supportedOps.get(s.toLowerCase());
	}

	/**
	 * Checks if is operation supported.
	 *
	 * @param s the s
	 * @return true, if is operation supported
	 */
	public boolean isOperationSupported(String s){
		return supportedOps.containsKey(s.toLowerCase());
	}
	//
	//
	//	public void addJunction(int height, int width){
	//		fabric[height][width][0]=new Junction(new Dimension(width, height), new Dimension(width, height), 1, Types.Junction, Direction.Old);
	//	}
	//
	/**
	 * Gets the well type.
	 *
	 * @param height the height
	 * @param width the width
	 * @return the well type
	 */
	public Types getWellType(int height, int width){
		return fabric[height][width].getType();
	}
	//	
	/**
	 * Gets the well type.
	 *
	 * @param a the a
	 * @return the well type
	 */
	public Types getWellType(Dimension a){
		return getWellType(a.height, a.width);
	}

	/**
	 * Gets the well.
	 *
	 * @param d the d
	 * @return the well
	 */
	public Well getWell(Dimension d){
		return fabric[d.height][d.width];
	}

	/**
	 * Gets the well.
	 *
	 * @param height the height
	 * @param width the width
	 * @return the well
	 */
	public Well getWell(int height, int width){
		return fabric[height][width];
	}


	/**
	 * Gets the layout size.
	 *
	 * @return the layout size
	 */
	public Dimension getLayoutSize(){
		//returns a new instance of Dimension to keep data of layout safe
		return new Dimension(dim);
	}



	/**
	 * Checks if is intraction.
	 *
	 * @param height the height
	 * @param width the width
	 * @return true, if is intraction
	 */
	public boolean isIntraction(int height, int width){
		if (fabric[height][width].getType()==Types.Interaction)
			return true;
		else
			return false;
	}

	/**
	 * Checks if is node.
	 *
	 * @param x the x
	 * @return true, if is node
	 */
	public boolean isNode(Dimension x){
		if (layoutGraph.containsVertex(getWell(x)))
			return true;
		else
			return false;
	}

	/**
	 * Checks if is intraction.
	 *
	 * @param x the x
	 * @return true, if is intraction
	 */
	public boolean isIntraction(Dimension x){
		return isIntraction(x.height, x.width);
	}

	/**
	 * Checks if is creation.
	 *
	 * @param height the height
	 * @param width the width
	 * @return true, if is creation
	 */
	public boolean isCreation(int height, int width){
		if (fabric[height][width].getType()==Types.Creation)
			return true;
		else
			return false;
	}

	/**
	 * Checks if is creation.
	 *
	 * @param x the x
	 * @return true, if is creation
	 */
	public boolean isCreation(Dimension x){
		return isCreation(x.height, x.width);
	}



	/**
	 * Checks if is basic.
	 *
	 * @param height the height
	 * @param width the width
	 * @return true, if is basic
	 */
	public boolean isBasic(int height, int width){
		if (fabric[height][width].getType()==Types.Basic)
			return true;
		else
			return false;
	}

	/**
	 * Checks if is basic.
	 *
	 * @param x the x
	 * @return true, if is basic
	 */
	public boolean isBasic(Dimension x){
		return isBasic(x.height, x.width);
	}


	/**
	 * Gets the graph.
	 *
	 * @return the graph
	 */
	public SimpleWeightedGraph<Well, ChannelEdge> getGraph(){
		return layoutGraph;
	}


	/**
	 * Prints the layout graph.
	 */
	public void printLayoutGraph(){
		ChannelEdge ce;
		for (Iterator<ChannelEdge> iterator = layoutGraph.edgeSet().iterator(); iterator.hasNext(); ) {
			ce=iterator.next();
			System.out.println(ce + "   cost: "+ ce.getCost(0) + ", weight: "+layoutGraph.getEdgeWeight(ce));
		}
	}


	/**
	 * Make graph.
	 */
	public void makeGraph(){
		layoutGraph = new SimpleWeightedGraph<Well, ChannelEdge>(new ClassBasedEdgeFactory<Well, ChannelEdge>(ChannelEdge.class)); 
		Well prev;
		ChannelEdge channelEdge;
		int cost=0;

		for (int i = 0; i < dim.height; i++) {
			prev=null;
			for (int j = 0; j < dim.width; j++) {
				switch (fabric[i][j].getType()){
				case Creation:
					creationWellEmptyList.add(fabric[i][j]);
				case Interaction:
					if (fabric[i][j].getType()==Types.Interaction)
						interactionWellEmptyList.add(fabric[i][j]);
					layoutGraph.addVertex(fabric[i][j]);
					//Looking for any edge to the left node
					if (prev!=null && j>0 && fabric[i][j-1].getType()!=Types.Empty){
						channelEdge=new ChannelEdge(prev, fabric[i][j], cost);
						layoutGraph.addEdge(prev, fabric[i][j], channelEdge);	
						layoutGraph.setEdgeWeight(channelEdge, cost);
					}
					//Looking for any edge to the upper node
					if (i>0){
						cost=0;
						for (int k = i-1; k >= 0; k--) {
							if (fabric[k][j].getType()==Types.Empty)
								break;
							else if (layoutGraph.containsVertex(fabric[k][j])){
								channelEdge=new ChannelEdge(fabric[k][j], fabric[i][j], cost);
								layoutGraph.addEdge(fabric[k][j], fabric[i][j], channelEdge);
								layoutGraph.setEdgeWeight(channelEdge, cost);
								break;
							}
							cost++;
						}
					}
					prev=fabric[i][j];
					cost=0;
					break;
				case Basic:
					if 	//NW Junction
					((i<dim.height-1 && j<dim.width-1 && fabric[i+1][j].getType()==Types.Basic && fabric[i][j+1].getType()==Types.Basic)||
							//SW Junction
							(i>0			&& j<dim.width-1 && fabric[i-1][j].getType()==Types.Basic && fabric[i][j+1].getType()==Types.Basic)||
							//NE Junction
							(i<dim.height-1 && j>0           && fabric[i+1][j].getType()==Types.Basic && fabric[i][j-1].getType()==Types.Basic)||
							//SE Junction
							(i>0			&& j>0           && fabric[i-1][j].getType()==Types.Basic && fabric[i][j-1].getType()==Types.Basic)){
						layoutGraph.addVertex(fabric[i][j]);
						//Looking for any edge to the left node
						if (prev!=null && j>0 && fabric[i][j-1].getType()!=Types.Empty){
							channelEdge=new ChannelEdge(prev, fabric[i][j], cost);
							layoutGraph.addEdge(prev, fabric[i][j], channelEdge);
							layoutGraph.setEdgeWeight(channelEdge, cost);
						}
						//Looking for any edge to the upper node
						if (i>0){
							cost=0;
							for (int k = i-1; k >= 0; k--) {
								if (fabric[k][j].getType()==Types.Empty)
									break;
								else if (layoutGraph.containsVertex(fabric[k][j])){
									channelEdge=new ChannelEdge(fabric[k][j], fabric[i][j], cost);
									layoutGraph.addEdge(fabric[k][j], fabric[i][j], channelEdge);
									layoutGraph.setEdgeWeight(channelEdge, cost);
									break;
								}
								cost++;
							}
						}
						prev=fabric[i][j];
						cost=0;
					}else{
						cost++;
					}
					break;
				}
			}
		}
	}




	/**
	 * Assign last interaction well.
	 *
	 * @param x the x
	 * @return true, if successful
	 */
	public boolean assignLastInteractionWell(Dimension x) {
		boolean temp;
		temp=interactionWellEmptyList.remove(getWell(x));
		if (temp){
			if (RuntimeConfig.DEBUG)
				System.out.println("Interaction "+x.height+","+x.width+" is reserved.");
			return true;
		}else
			return false;

	}
	//
	//	public void usageStatistics(){
	//		double channelUtilization=0;
	//		int channelCount=0;
	//		
	//		double jucntionUtilization=0;
	//		int junctionCount=0;
	//
	//		double trapUtilization=0;
	//		int trapCount=0;
	//
	//		ChannelEdge c;
	//		for (Iterator<ChannelEdge> iterator = layoutGraph.edgeSet().iterator(); iterator.hasNext();) {
	//			c=iterator.next();
	//			if (c.getChannel()!=null){
	//				channelCount++;
	//				if (c.isUsed())
	//					channelUtilization++;
	//			}
	//		}
	//
	//
	//		for (int i = 0; i < dim.height; i++) {
	//			for (int j = 0; j < dim.width; j++) {
	//				if (fabric[i][j][0]==null)
	//					continue;
	//				if (isJunction(i, j)){
	//					junctionCount++;
	//					if (getSquare(i, j).isUsed())
	//						jucntionUtilization++;
	//				}else if (isTrap(i, j)){
	//					trapCount++;
	//					if (getSquare(i, j).isUsed())
	//						trapUtilization++;
	//				}
	//			}
	//		}
	//
	//		System.out.printf("Trap Utilization: %.2f%%\n",trapUtilization/trapCount*100);
	//		System.out.printf("Channel Utilization: %.2f%%\n",channelUtilization/channelCount*100);
	//		System.out.printf("Junction Utilization: %.2f%%\n",jucntionUtilization/junctionCount*100);
	//
	//	}
	//	
	//	
	//retains all the traps to the empty trap list
	/**
	 * Clean.
	 */
	public void clean(){
		//Clearing the usage statistics for channels
		ChannelEdge c;
		for (Iterator<ChannelEdge> iterator = layoutGraph.edgeSet().iterator(); iterator.hasNext();) {
			c=iterator.next();
			c.setUsed(false);
		}

		//Clearing the usage statistics for junctions and traps
		for (int i = 0; i < dim.height; i++) {
			for (int j = 0; j < dim.width; j++) {
				switch(fabric[i][j].getType()){
				case Empty:
					break;
				case Creation:
					fabric[i][j].setUsed(false);
					if (!creationWellEmptyList.contains(fabric[i][j]))
						creationWellEmptyList.add(fabric[i][j]);
					break;
				case Interaction:
					fabric[i][j].setUsed(false);
					((Interaction)fabric[i][j]).clearProgressList();
					if (!interactionWellEmptyList.contains(fabric[i][j]))
						interactionWellEmptyList.add(fabric[i][j]);
					break;
				case Basic:
					fabric[i][j].setUsed(false);
				}
			}
		}
//		qubits.clear();
	}


	//retains all the traps to the empty trap list but the ones which are occupied
	/**
	 * Clear.
	 */
	public void clear(){
		//Clearing the usage statistics for channels
		ChannelEdge c;
		for (Iterator<ChannelEdge> iterator = layoutGraph.edgeSet().iterator(); iterator.hasNext();) {
			c=iterator.next();
			c.setUsed(false);
		}

		//Clearing the usage statistics for junctions and traps
		for (int i = 0; i < dim.height; i++) {
			for (int j = 0; j < dim.width; j++) {
				switch(fabric[i][j].getType()){
				case Empty:
					break;
				case Creation:
					fabric[i][j].setUsed(false);
					if (!creationWellEmptyList.contains(fabric[i][j]))
						creationWellEmptyList.add(fabric[i][j]);
					break;
				case Interaction:
					fabric[i][j].setUsed(false);
					((Interaction)fabric[i][j]).clearProgressList();
					if (!interactionWellEmptyList.contains(fabric[i][j]))
						interactionWellEmptyList.add(fabric[i][j]);
					break;
				case Basic:
					fabric[i][j].setUsed(false);
				}
			}
		}

		//		for
		Qubit q;
		Dimension current;
		for (Iterator<Entry<String, Qubit>> iterator = qubits.entrySet().iterator(); iterator.hasNext();) {
			q=iterator.next().getValue();
			current=q.getPosition();
			sortCreationWells(current, false, 0);
			q.setPosition(getNearestFreeCreation(false).getPosition());
			occupy(getNearestFreeCreation(false).getPosition(), q);
			if (RuntimeConfig.PRINT_QUBIT){
				System.out.println(q.getName()+" @"+q.getPosition().height+","+q.getPosition().width);
			}
		}
		//		interactionWellEmptyList
		HashSet<Well> test=new HashSet<Well>(interactionWellEmptyList);
		if (test.size()!=interactionWellEmptyList.size()){
			System.out.println(":((((((((((");
			System.exit(-1);
		}

	}

	/**
	 * Gets the interaction empty list.
	 *
	 * @return the interaction empty list
	 */
	public ArrayList<Well> getInteractionEmptyList(){
		return interactionWellEmptyList;
	}
	
	/**
	 * Gets the creation empty list.
	 *
	 * @return the creation empty list
	 */
	public ArrayList<Well> getCreationEmptyList(){
		return creationWellEmptyList;
	}
	
	/**
	 * Gets the nearest free well.
	 *
	 * @param position the position
	 * @return the nearest free well
	 */
	public Well getNearestFreeWell(Dimension position) {
		//the passed basic well is in a horizontal chain
		if (position.width+1<dim.width && fabric[position.height][position.width+1].getType()!=Types.Empty){
			for (int i=1;;i++){
				if (position.width+i<dim.width && fabric[position.height][position.width+i].getQubitsNo()<RuntimeConfig.CHANNEL_CAP-2){
					if (fabric[position.height][position.width+i].getType()==Types.Basic){
						return fabric[position.height][position.width+i];
					}
				}else if (0<=position.width-i && fabric[position.height][position.width-i].getQubitsNo()<RuntimeConfig.CHANNEL_CAP-2){
					if (fabric[position.height][position.width-i].getType()==Types.Basic){
						return fabric[position.height][position.width-i];
					}
					
				}
			}
		}//the passed basic well is in a vertical chain
		else{
			for (int i=1;;i++){
				if (position.height+i<dim.height && fabric[position.height+i][position.width].getQubitsNo()<RuntimeConfig.CHANNEL_CAP-2){
					if (fabric[position.height+i][position.width].getType()==Types.Basic){
						return fabric[position.height+i][position.width];
					}
				}else if (0<=position.height-i && fabric[position.height-i][position.width].getQubitsNo()<RuntimeConfig.CHANNEL_CAP-2){
					if (fabric[position.height-i][position.width].getType()==Types.Basic){
						return fabric[position.height-i][position.width];
					}
					
				}
			}
		}
	}



}
