/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.qasm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

// TODO: Auto-generated Javadoc
/**
 * The Class QASM.
 */
public class QASM {
	
	/** The dependency list. */
	private Map<String, ArrayList<Integer>> dependencyList=new Hashtable<String, ArrayList<Integer>>();
	
	/** The command no. */
	private int commandNo=0;
	
	/** The commands. */
	List<Vertex> commands=new ArrayList<Vertex>();
	
	/** The graph. */
	private SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> graph;

	/**
	 * Instantiates a new qasm.
	 */
	public QASM(){
		graph = new SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		//adding "start" node in graph
		commands.add(new Vertex("start", commandNo, (String[])null));
		graph.addVertex(commands.get(commandNo));

		//adding "end" node in graph
		commands.add(new Vertex("end", commandNo+1, (String[])null));
		graph.addVertex(commands.get(commandNo+1));

		//adding a direct edge from "start" node to "end" node
		graph.addEdge(commands.get(0), commands.get(1));

	}

	/**
	 * Reverse commands order.
	 */
	public void reverseCommandsOrder(){
		Collections.reverse(commands);
		for (int i = 0; i < commands.size(); i++) {
			commands.get(i).setCommandNo(i);
		}
	}
	
	/**
	 * Gets the dfg.
	 *
	 * @return the dfg
	 */
	public SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> getDFG(){
		return graph;
	}
	
	/**
	 * Gets the commands list.
	 *
	 * @return the commands list
	 */
	public List<Vertex> getCommandsList(){
		return commands;		
	}
	
	/**
	 * Gets the qubit list.
	 *
	 * @return the qubit list
	 */
	public String[] getQubitList(){
		String[] qubits=new String[dependencyList.size()];
		Iterator<Entry<String, ArrayList<Integer>>> it=dependencyList.entrySet().iterator();
		int i=0;
		while (it.hasNext()){
			qubits[i]=it.next().getKey();
			i++;
		}
		return qubits;
	}
	
	/**
	 * Prints the dfg.
	 */
	public void printDFG(){
		for (DefaultEdge e : graph.edgeSet()) {
			System.out.println(e.toString());                    
		}
	}
	
	/**
	 * Prints the commands.
	 */
	public void printCommands(){
		for (int i = 0; i < commands.size(); i++) {
			System.out.println(commands.get(i));
		}
	}
	
//	public void printDependancyList(){
//		for (Map.Entry<String, Integer> entry : dependencyList.entrySet())
//		{
//			System.out.println(entry.getKey());
//		}
//	}

	/**
 * *************************************************************************.
 *
 * @param <T> the generic type
 * @param elements the elements
 * @return the array list
 */
	// For helping parser
	@SafeVarargs
	public static <T> ArrayList<T> createArrayList(T ... elements) { 
		ArrayList<T> list = new ArrayList<T>();  
		for (T element : elements) { 
			list.add(element); 
		} 
		return list; 
	} 

	/**
	 * Parses the error.
	 *
	 * @param token the token
	 */
	private void parseError(String token){
		System.err.println("Qubit `"+token+"` is not defined.");
		//TODO: convert to an exception with correct message
		System.exit(-1);
	}

	/**
	 * Inc command no.
	 */
	public void incCommandNo(){
		commandNo++;
	}

	/**
	 * Define qubit.
	 *
	 * @param q the q
	 */
	public void defineQubit(String q){
		//		System.out.println(q);
		//Qubit definition
		//Adding the qubits in dependencyList
		if (dependencyList.containsKey(q)==true){
			System.err.println("Qubit "+q+" is already defined.");
			System.exit(-1);
		}
		dependencyList.put(q, new ArrayList<Integer>());

		
		//To access the value of qubit
		//if (qubitValue!=null)
		//	System.out.println(qubitValue.image);
	}

	//Shifting "end" one place ahead in commands list
	/**
	 * Shift end.
	 */
	public void shiftEnd(){
		commands.add(commands.get(commandNo).setCommandNo(commandNo+1));
	}

	/**
	 * Adds the one op inst.
	 *
	 * @param cmd the cmd
	 * @param op the op
	 */
	public void addOneOpInst(String cmd, String op){
		//Reports error if the used qubit is not defined before
		if (dependencyList.containsKey(op)==false){
			parseError(op);
		}
		String[] temp={op};
		//Vertex(String c, int no, int p, Qubit ...ops)
		commands.set(commandNo, new Vertex (cmd, commandNo, temp));
		//Adding new command in the graph
		graph.addVertex(commands.get(commandNo));
		graph.addEdge(commands.get(0), commands.get(commandNo));

		//Adding an edge to the node which depends on 
		if (dependencyList.get(op).size()>0){
			for (Iterator<Integer> iterator = dependencyList.get(op).iterator(); iterator.hasNext();) {
				graph.addEdge(commands.get(iterator.next()), commands.get(commandNo));				
			}
			
			//Remove edge to the "start" node
			if (graph.containsEdge(commands.get(0), commands.get(commandNo)))
				graph.removeEdge(commands.get(0), commands.get(commandNo));
		}
		//Changing the dependency of its operand to point to itself
		dependencyList.get(op).clear();
		dependencyList.get(op).add(new Integer (commandNo));
	}

	/**
	 * Adds the two op inst.
	 *
	 * @param cmd the cmd
	 * @param op0 the op0
	 * @param op1 the op1
	 */
	public void addTwoOpInst(String cmd, String op0, String op1){
		if (op0.equals(op1)){
			System.err.println("Error: operands of a 2-qubit operator, i.e. `"+op0+"`, cannot be the same");
			//TODO: convert to an exception with correct message
			System.exit(-1);
		}
		//Reports error if the used qubits are not defined before
		if (dependencyList.containsKey(op0)==false){
			parseError(op0);
		} else if (dependencyList.containsKey(op1)==false){
			parseError(op1);
		}

		//Adding the command in commands list
		//		vertex=cmd.image+" "+q0.image+","+q1.image;
		//Vertex(String c, int no, int p, Qubit ...ops)
		String[] temp={op0, op1};
		commands.set(commandNo, new Vertex (cmd,commandNo, temp));

		//Adding new command in the graph
		graph.addVertex(commands.get(commandNo));
		graph.addEdge(commands.get(0), commands.get(commandNo));

		boolean flag=false;
		//Adding an edge to the node which depends on 
		if (commandNo==5){
//			System.out.println(commands.get(commandNo));
//			System.out.println(dependencyList.get(op0).size());
//			System.out.println(dependencyList.get(op1).size());
//			System.exit(-1);
		}
		if(dependencyList.get(op0).size()==1){
			graph.addEdge(commands.get(dependencyList.get(op0).get(0)), commands.get(commandNo));

			//Remove edge to the "start" node
			if (graph.containsEdge(commands.get(0), commands.get(commandNo)))
				graph.removeEdge(commands.get(0), commands.get(commandNo));
			
		}
		else if(dependencyList.get(op0).size()>1){
			for (int i = 0; i < dependencyList.get(op0).size(); i++) {
				if (!commands.get(dependencyList.get(op0).get(i)).getOperand(0).equals(op0))
				{
					graph.addEdge(commands.get(dependencyList.get(op0).get(i)), commands.get(commandNo));
					flag=true;
				}
			}

			//Remove edge to the "start" node
			if (flag==true && graph.containsEdge(commands.get(0), commands.get(commandNo)))
				graph.removeEdge(commands.get(0), commands.get(commandNo));
		}
			
		
		flag=false;
		//for control qubit, just trust the first element in the list
		if (dependencyList.get(op1).size()>0){
			for (int i = 0; i < dependencyList.get(op1).size(); i++) {
				if (commands.get(dependencyList.get(op1).get(i)).getOperand(0).equals(op1))
				{
					graph.addEdge(commands.get(dependencyList.get(op1).get(i)), commands.get(commandNo));
					flag=true;
				}
			}
			
			//Remove edge to the "start" node
			if (flag && graph.containsEdge(commands.get(0), commands.get(commandNo)))
				graph.removeEdge(commands.get(0), commands.get(commandNo));
		}

		
		//Dependency for writer; clean the list and add it as the only element in the list
		dependencyList.get(op0).clear();
		dependencyList.get(op0).add(new Integer (commandNo));


		//Dependency for reader; just add it to the end of list
		dependencyList.get(op1).add(new Integer (commandNo));
	}	


	//Common operation after adding 1-2 -qubit operators
	/**
	 * Operation.
	 */
	public void operation(){
		//add an edge to the "end" sentinel
		graph.addEdge(commands.get(commandNo), commands.get(commandNo+1));

		//removing any common edges between the "end" and the parent of newly added node
		List<DefaultWeightedEdge> l=new LinkedList<DefaultWeightedEdge>();
		for (DefaultWeightedEdge v : graph.incomingEdgesOf(commands.get(commandNo+1))) {
			Vertex vv=graph.getEdgeSource(v);
			if (vv!=commands.get(commandNo) && graph.containsEdge (vv,commands.get(commandNo)))
				//g1.removeEdge(v);
				l.add(v);
		}
		//This for is added to avoid strange problems while removing edges dynamically!
		for (DefaultWeightedEdge defaultEdge : l) {
			graph.removeEdge(defaultEdge);
		}

	}

}
