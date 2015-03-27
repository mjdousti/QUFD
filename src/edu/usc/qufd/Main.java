/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.parser.LayoutParser;
import edu.usc.qufd.qasm.QASM;
import edu.usc.qufd.qasmParser.QASMParser;
import edu.usc.qufd.router.EventDrivenSimulator;
import edu.usc.qufd.router.InitialPlacer;
import edu.usc.qufd.router.Placement;



// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */
public class Main {	
	
	/** The layout. */
	static Layout layout;
	
	/** The qasm. */
	static QASM qasm;
	
	/** The eds. */
	static EventDrivenSimulator eds;
	
	/** The sim pl. */
	static Placement simPL;
	
	/** The resource estimate file addr. */
	static String qasmFileAddr, pmdFileAddr, outputFileAddr, resourceEstimateFileAddr;


	/**
	 * Print_usage.
	 *
	 * @param options the options
	 */
	private static void print_usage(Options options){
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(90);
		formatter.printHelp( "qufd", "QUFD maps a given QASM " +
				"to a supplied PMD fabric. The resultant MCL file of " +
				"the mapped circuit will be generated.", options,"", true);
	}
	
	/**
	 * Parses the inputs.
	 *
	 * @param args the args
	 * @return true, if successful
	 */
	private static boolean parseInputs(String [] args){
		Options options=new Options();

		options.addOption("p", "pmd", true, "PMD file");
		options.getOption("p").setArgName("file");

		
		options.addOption("i", "input", true, "QASM input file");
		options.getOption("i").setArgName("file");

		options.addOption("m", "mcl", true, "MCL output file");
		options.getOption("m").setArgName("file");

		options.addOption("r", "re", true, "Resource estimation file");
		options.getOption("r").setArgName("file");
		
		options.addOption("q", "qpOASES", true, "Uses qpOASES solver");
		options.getOption("q").setArgName("path");

		options.addOption("h", "help", false, "Shows this help menu");

		CommandLineParser parser=new GnuParser();
		CommandLine cmd=null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}


		if (!cmd.hasOption("pmd")||!cmd.hasOption("mcl")||!cmd.hasOption("input")||!cmd.hasOption("re")||cmd.hasOption("help")){
			print_usage(options);
			System.exit(-1);
		}
		
		if (cmd.hasOption("qpOASES")){
			File qpOASES_path=new File(cmd.getOptionValue("qpOASES")+"/interfaces/octave");
			if (!qpOASES_path.isDirectory()){
				System.err.println("The directory for the Octave interface of qpOASES is not found.");
				System.exit(-1);
			}
			RuntimeConfig.use_GUROBI=false;
			RuntimeConfig.qpOASES_path=cmd.getOptionValue("qpOASES");
		}else{
			RuntimeConfig.use_GUROBI=true;
		}

		

		qasmFileAddr=cmd.getOptionValue("input");
		if (!new File(qasmFileAddr).exists()){
			System.err.println("QASM file "+qasmFileAddr+" does not exist.");
			return false; 
		}
		pmdFileAddr=cmd.getOptionValue("pmd");
		if (!new File(pmdFileAddr).exists()){
			System.err.println("PMD file "+pmdFileAddr+" does not exist.");
			return false;
		}

		resourceEstimateFileAddr=cmd.getOptionValue("re");
		outputFileAddr=cmd.getOptionValue("mcl");

		return true;
	}

	/**
	 * Qufd.
	 *
	 * @param outputFile the output file
	 * @return the long
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static long qufd(PrintWriter outputFile) throws IOException{
		eds=new EventDrivenSimulator(layout, qasm.getCommandsList(), outputFile);
		//    	eds.computeEarliestTimeSlack(qasm.getDFG());
		//    	eds.computeSlack(qasm.getDFG());
		//    	System.exit(-1);
		double density=0.7;
		int interactionNumber=(int) ((layout.getLayoutSize().height-1)*(layout.getLayoutSize().width-1)/10/10);
		int threshold=(int)(interactionNumber*density);
		eds.resetAddedEdges();
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, qasm.getDFG(),threshold);
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, qasm.getDFG(),threshold);
		eds.computeListSchRank(qasm.getDFG());
		boolean complete=false;
		while(!complete){
			eds.BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, qasm.getDFG(),threshold);
			complete=eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, qasm.getDFG(),threshold);
			eds.computeListSchRank(qasm.getDFG());
		}
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, qasm.getDFG(),threshold);
		complete=eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, qasm.getDFG(),threshold);
		eds.computeListSchRank(qasm.getDFG());
		//    	eds.addDummyEdgesForceDirectedScheduling(qasm.getDFG());
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ALAP, qasm.getDFG(),threshold);
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, qasm.getDFG(),threshold);
		//    	System.exit(-1);
		//    	eds.forceDirectedScheduling(qasm.getDFG());
		eds.computeListSchRank(qasm.getDFG());
		//    	System.exit(-1);
		double K=0.5, R=0, mD=1, minDistance=1;
		double tol1=.2, tol2=0.2;
		simPL=new Placement(qasm.getDFG(),layout, R, K, mD, minDistance);
		//    	InitialGatePlacer.InitialGatePlacer(qasm, simPL.springNet, layout, Heur.Random);
		//    	simPL.optimization();
		simPL.optimizationwithRedefinitionofLevels(eds,threshold);
		if (RuntimeConfig.HDEBUG)
			System.out.println("hadi");
		InitialPlacer.InitialPlacer(qasm, layout, qasm.getDFG(),  true, outputFile);	

		long latency= eds.simluateFixedPlacement(qasm.getDFG());

		//storing the resource estimation
		eds.outputResourceEstimate(resourceEstimateFileAddr);

		return latency;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String [] args) throws IOException{
		if (parseInputs(args)==false){
			System.exit(-1);	//The input files do not exist
		}

		/*
		 * Parsing inputs: fabric & qasm file
		 */
		PrintWriter outputFile;
		RandomAccessFile raf=null;
		String latencyPlaceHolder;
		if (RuntimeConfig.OUTPUT_TO_FILE){
			latencyPlaceHolder="Total Latency: "+Long.MAX_VALUE+" us"+System.lineSeparator();
			raf=new RandomAccessFile(outputFileAddr, "rws");
			//removing the old values in the file
			raf.setLength(0);
			//writing a place holder for the total latency
			raf.writeBytes(latencyPlaceHolder);
			raf.close();

			outputFile=new PrintWriter(new BufferedWriter(new FileWriter(outputFileAddr, true)), true);
		}else{ //writing to stdout
			outputFile=new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);
		}
		/* parsing the input*/
		layout=LayoutParser.parse(pmdFileAddr);
		qasm= QASMParser.QASMParser(qasmFileAddr,layout);

		long totalLatency=qufd(outputFile);

		if (RuntimeConfig.OUTPUT_TO_FILE){
			outputFile.close();
			//Over writing the place holder with the actual latency
			String latencyActual="Total Latency: "+totalLatency+" "+layout.getTimeUnit();
			latencyActual=StringUtils.rightPad(latencyActual, latencyPlaceHolder.length()-System.lineSeparator().length());
			raf=new RandomAccessFile(outputFileAddr, "rws");
			//Writing to the top of a file
			raf.seek(0);
			//writing the actual total latency in the at the top of the output file
			raf.writeBytes(latencyActual+System.lineSeparator());
			raf.close();
		}else{
			outputFile.flush();
			System.out.println("Total Latency: "+totalLatency+" "+layout.getTimeUnit());
		}

		if (RuntimeConfig.VERBOSE){
			System.out.println("Done.");
		}    	
		outputFile.close();
	}
}
