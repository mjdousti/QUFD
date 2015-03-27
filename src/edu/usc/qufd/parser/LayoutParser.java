/*
 * 
 * Copyright (C) 2014 Hadi Goudarzi, Mohammad Javad Dousti, Alireza Shafaei,
 * and Massoud Pedram, University of Southern California. All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qufd.parser;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.usc.qufd.RuntimeConfig;
import edu.usc.qufd.layout.Layout;
import edu.usc.qufd.layout.Operation;
import edu.usc.qufd.layout.Layout.Types;


// TODO: Auto-generated Javadoc
/**
 * The Class LayoutParser.
 */
public class LayoutParser {

	/**
	 * Parses the.
	 *
	 * @param pmdFileAddr the pmd file addr
	 * @return the layout
	 */
	public static Layout parse(String pmdFileAddr){
		Layout layout=new Layout();
		//parsing the pmd file
		if (!defsParser(layout, pmdFileAddr))
			return null;
		if (RuntimeConfig.VERBOSE){
			System.out.println("Layout parsing completed successfully!");
		}
		return layout;
	}

	/**
	 * Defs parser.
	 *
	 * @param layout the layout
	 * @param pmdFileAddr the pmd file addr
	 * @return true, if successful
	 */
	public static boolean defsParser(final Layout layout, String pmdFileAddr){
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {
				//Highest level tag
				boolean pmd=false;
				
				//Level 2 tags
				boolean pmd_name=false;
				boolean fheight=false;
				boolean fwidth=false;
				boolean create_borders=false;
				boolean create_cell_border=false;
				boolean time_unit=false;
				boolean instructions=false;
				boolean types=false;
				boolean cell=false;
				

				//Level 3 tags
				//instructions
				boolean instruction=false;

				//types
				boolean well_types = false;
				boolean well_type = false;
				boolean instruction_set=false;
				boolean shape=false;
				boolean color=false;
				boolean label=false;
				boolean description=false;
				
				//connection type
				boolean connection_types = false;
				boolean connection_type = false;
				boolean style = false;

				//connection type
				boolean connections = false;
				boolean connection = false;
				boolean source = false;
				boolean destination = false;
				boolean well_name = false;
				boolean cell_number = false;
				
				//Level 4 tags
				//instruction
				boolean delay = false;
				boolean error = false;
				boolean qubitNo =false;

				boolean cheight = false;
				boolean cwidth = false;
				boolean wells = false;
				
				boolean well=false;
				boolean name=false;
				boolean type=false;
				boolean location=false;
				
				boolean x=false;
				boolean y=false;

				Dimension fabricSize=new Dimension(-1, -1);
				
				Dimension tileSize=new Dimension();
				Layout.Types [][]tile;
				
				int i=-1, j=-1;
				Layout.Types tempType=Types.Unknown;

				String instName, wellName;
				double errorRate; 
				int delayValue;


				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					if (qName.equalsIgnoreCase("pmd")){
						pmd=true;
					}else if (qName.equalsIgnoreCase("pmd_name") && pmd){
						pmd_name = true;
					}else if (qName.equalsIgnoreCase("fheight") && pmd){
						fheight = true;
					}else if (qName.equalsIgnoreCase("fwidth") && pmd){
						fwidth = true;
					}else if (qName.equalsIgnoreCase("create_borders") && pmd){
						create_borders = true;
					}else if (qName.equalsIgnoreCase("create_cell_border") && pmd){
						create_cell_border = true;
					}else if (qName.equalsIgnoreCase("time_unit") && pmd){
						time_unit = true;
					}else if (qName.equalsIgnoreCase("instructions") && pmd){
						instructions = true;
					}else if (qName.equalsIgnoreCase("cell") && pmd){
						cell = true;
					}else if (qName.equalsIgnoreCase("types") && pmd){
						types = true;
					}					
					//ISA
					else if (qName.equalsIgnoreCase("instruction") && pmd && instructions){
						instruction = true;
					}else if (qName.equalsIgnoreCase("name") && pmd && instructions && instruction){
						name = true;
					}else if (qName.equalsIgnoreCase("delay") && pmd && instructions && instruction) {
						delay = true;
					}else if (qName.equalsIgnoreCase("error") && pmd && instructions && instruction) {
						error = true;
					}else if (qName.equalsIgnoreCase("num_qubits") && pmd && instructions && instruction) {
						qubitNo = true;
					}

					//Wells
					else if (qName.equalsIgnoreCase("well_types") && pmd && types){
						well_types = true;
					}else if (qName.equalsIgnoreCase("well_type") && well_types && pmd && types){
						well_type = true;
					}else if (qName.equalsIgnoreCase("name") && well_type && well_types && pmd && types) {
						name = true;
					}else if (qName.equalsIgnoreCase("shape") && well_type && well_types && pmd && types) {
						name = true;
					}else if (qName.equalsIgnoreCase("color") && well_type && well_types && pmd && types) {
						name = true;
					}else if (qName.equalsIgnoreCase("label") && well_type && well_types && pmd && types) {
						name = true;
					}else if (qName.equalsIgnoreCase("description") && well_type && well_types && pmd && types) {
						name = true;
					}else if (qName.equalsIgnoreCase("instruction_set") && well_type && well_types && pmd && types) {
						instruction_set = true;
					}else if (qName.equalsIgnoreCase("instruction") && instruction_set && well_type && well_types && pmd && types) {
						instruction = true;
					}
					//connection_types
					else if (qName.equalsIgnoreCase("connection_types") && pmd && types){
						connection_types = true;
					}else if (qName.equalsIgnoreCase("connection_type") && connection_types && pmd && types){
						connection_type = true;
					}else if (qName.equalsIgnoreCase("name") && connection_type && connection_types && pmd && types){
						name = true;
					}else if (qName.equalsIgnoreCase("color") && connection_type && connection_types && pmd && types){
						color = true;
					}else if (qName.equalsIgnoreCase("style") && connection_type && connection_types && pmd && types){
						style = true;
					}					
					//Cells
					else if (qName.equalsIgnoreCase("cheight") && cell){
						cheight = true;
					}else if (qName.equalsIgnoreCase("cwidth") && cell){
						cwidth = true;
					}else if (qName.equalsIgnoreCase("wells") && cell){
						wells = true;
					}else if (qName.equalsIgnoreCase("well") && wells && cell){
						well = true;
					}else if (qName.equalsIgnoreCase("name") && well && wells && cell){
						name = true;
					}else if (qName.equalsIgnoreCase("type") && well && wells && cell){
						type = true;
					}else if (qName.equalsIgnoreCase("location") && well && wells && cell){
						location = true;
					}else if (qName.equalsIgnoreCase("x") && location && well && wells && cell){
						x = true;
					}else if (qName.equalsIgnoreCase("y") && location && well && wells && cell){
						y = true;
					}
					//connections
					else if (qName.equalsIgnoreCase("connections") && cell){
						connections = true;
					}else if (qName.equalsIgnoreCase("connection") && connections && cell){
						connection = true;
					}else if (qName.equalsIgnoreCase("name") && connection){
						name = true;
					}else if (qName.equalsIgnoreCase("type") && connection){
						type = true;
					}else if (qName.equalsIgnoreCase("source") && connection){
						source = true;
					}else if (qName.equalsIgnoreCase("well_name") && source && connection){
						well_name = true;
					}else if (qName.equalsIgnoreCase("cell_number") && source && connection){
						cell_number = true;
					}else if (qName.equalsIgnoreCase("destination") && connection){
						destination = true;
					}else if (qName.equalsIgnoreCase("well_name") && destination && connection){
						well_name = true;
					}else if (qName.equalsIgnoreCase("cell_number") && destination && connection){
						cell_number = true;
					}
					else{
						throw new SAXException("Incorrect XML Format (start): "+qName);
					}
				}

				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (qName.equalsIgnoreCase("pmd")){
						pmd=false;
						layout.initFabric(fabricSize, tileSize, tile);
					}else if (qName.equalsIgnoreCase("pmd_name")){
						pmd_name = false;
					}else if (qName.equalsIgnoreCase("fheight")){
						fheight = false;
					}else if (qName.equalsIgnoreCase("fwidth")){
						fwidth = false;
					}else if (qName.equalsIgnoreCase("create_borders")){
						create_borders = false;
					}else if (qName.equalsIgnoreCase("create_cell_border")){
						create_cell_border = false;
					}else if (qName.equalsIgnoreCase("time_unit")){
						time_unit = false;
					}else if (qName.equalsIgnoreCase("instructions")){
						instructions = false;
					}else if (qName.equalsIgnoreCase("types")){
						types = false;
					}else if (qName.equalsIgnoreCase("cell")){
						cell = false;
					}
					//ISA
					else if (qName.equalsIgnoreCase("instruction") && pmd && instructions){
						instruction = false;
					}else if (qName.equalsIgnoreCase("name") && pmd && instructions && instruction){
						name = false;
					}else if (qName.equalsIgnoreCase("delay") && pmd && instructions && instruction) {
						delay = false;
					}else if (qName.equalsIgnoreCase("error") && pmd && instructions && instruction) {
						error = false;
					}else if (qName.equalsIgnoreCase("num_qubits") && pmd && instructions && instruction) {
						qubitNo = false;
					}

					//Types
					else if (qName.equalsIgnoreCase("well_types") && pmd && types){
						well_types = false;
					}else if (qName.equalsIgnoreCase("well_type") && well_types && pmd && types){
						well_type = false;
					}else if (qName.equalsIgnoreCase("name") && well_type && well_types && pmd && types) {
						name = false;
					}else if (qName.equalsIgnoreCase("shape") && well_type && well_types && pmd && types) {
						name = false;
					}else if (qName.equalsIgnoreCase("color") && well_type && well_types && pmd && types) {
						name = false;
					}else if (qName.equalsIgnoreCase("label") && well_type && well_types && pmd && types) {
						name = false;
					}else if (qName.equalsIgnoreCase("description") && well_type && well_types && pmd && types) {
						name = false;
					}else if (qName.equalsIgnoreCase("instruction_set") && well_type && well_types && pmd && types) {
						instruction_set = false;
					}else if (qName.equalsIgnoreCase("instruction") && instruction_set && well_type && well_types && pmd && types) {
						instruction = false;
					}
					//connection types
					else if (qName.equalsIgnoreCase("connection_types") && pmd && types){
						connection_types = false;
					}else if (qName.equalsIgnoreCase("connection_type") && connection_types && pmd && types){
						connection_type = false;
					}else if (qName.equalsIgnoreCase("name") && connection_type && connection_types && pmd && types){
						name = false;
					}else if (qName.equalsIgnoreCase("color") && connection_type && connection_types && pmd && types){
						color = false;
					}else if (qName.equalsIgnoreCase("style") && connection_type && connection_types && pmd && types){
						style = false;
					}
					
					//Cells
					else if (qName.equalsIgnoreCase("cheight") && cell){
						cheight = false;
					}else if (qName.equalsIgnoreCase("cwidth") && cell){
						cwidth = false;
					}else if (qName.equalsIgnoreCase("wells") && cell){
						wells = false;
						//pattern is generated
					}else if (qName.equalsIgnoreCase("well") && wells && cell){
						well = false;
					}else if (qName.equalsIgnoreCase("name") && well && wells && cell){
						name = false;
					}else if (qName.equalsIgnoreCase("type") && well && wells && cell){
						type = false;
					}else if (qName.equalsIgnoreCase("location") && well && wells && cell){
						location = false;
					}else if (qName.equalsIgnoreCase("x") && location && well && wells && cell){
						x = false;
					}else if (qName.equalsIgnoreCase("y") && location && well && wells && cell){
						y = false;
					}
					//connections
					else if (qName.equalsIgnoreCase("connections") && cell){
						connections = false;
					}else if (qName.equalsIgnoreCase("connection") && connections){
						connection = false;
					}else if (qName.equalsIgnoreCase("name") && connection){
						name = false;
					}else if (qName.equalsIgnoreCase("type") && connection){
						type = false;
					}else if (qName.equalsIgnoreCase("source") && connection){
						source = false;
					}else if (qName.equalsIgnoreCase("well_name") && source && connection){
						well_name = false;
					}else if (qName.equalsIgnoreCase("cell_number") && source && connection){
						cell_number = false;
					}else if (qName.equalsIgnoreCase("destination") && connection){
						destination = false;
					}else if (qName.equalsIgnoreCase("well_name") && destination && connection){
						well_name = false;
					}else if (qName.equalsIgnoreCase("cell_number") && destination && connection){
						cell_number = false;
					}
					else{
						throw new SAXException("Incorrect XML Format (end): "+qName);
					}
				}

				public void characters(char ch[], int start, int length) throws SAXException {
					if (pmd_name){
						if (!new String(ch, start, length).equalsIgnoreCase("Trapped Ion")){
							throw new SAXException("Incorrect XML: The supplied XML should be of type Trapped Ion.");
						}
					}else if (fheight){
						fabricSize.height=Integer.parseInt(new String(ch, start, length));
					}else if (fwidth){
						fabricSize.width=Integer.parseInt(new String(ch, start, length));
					}else if (create_borders||create_cell_border){
						//DO nothing
					}else if (time_unit){
						layout.setTimeUnit(new String(ch, start, length));
					}else if (name && instructions){	//ISA
						//System.out.println("ISA::NAME: " + new String(ch, start, length));
						instName=new String(ch, start, length);					
					}else if (delay && instructions) {
						//System.out.println("Delay: " + new String(ch, start, length));
						delayValue=Integer.parseInt(new String(ch, start, length));
					}else if (error){
						//System.out.println("Error: " + new String(ch, start, length));
						errorRate=Double.parseDouble(new String(ch, start, length));
						layout.addNewOperation(new Operation(instName, errorRate, delayValue));
					}else if (qubitNo){
						//adding the no. of qubits
					}

					//wells
					else if (name && well_type) {
						wellName=new String(ch, start, length);
						layout.addNewWell(wellName);
					}else if (instruction && well_type) {
						instName= new String(ch, start, length);
						layout.addInstToWell(wellName, instName);
					}else if ((shape || color || label || description) && well_type){
						//DO nothing
					}else if ((name || color || style) && connection_type){
						//DO nothing
					}
					//Cells
					else if (cheight && cell){
						tileSize.height=Integer.parseInt(new String(ch, start, length));
					}else if (cwidth && cell){
						tileSize.width=Integer.parseInt(new String(ch, start, length));
						//initializing fabric tile
						tile=new Layout.Types[tileSize.height][tileSize.width];
						for (int i = 0; i < tileSize.height; i++) {
							for (int j = 0; j < tileSize.width; j++) {
								tile[i][j]=Types.Empty;
							}
						}
					}else if (name && well){
						//No use!
					}else if (type && well){
						String temp=new String(ch, start, length);
						if (temp.equalsIgnoreCase("basic")){
							tempType=Layout.Types.Basic;
						}else if (temp.equalsIgnoreCase("creation")){
							tempType=Layout.Types.Creation;
						}else if (temp.equalsIgnoreCase("interaction")){
							tempType=Layout.Types.Interaction;
						}else{
							throw new SAXException("Incorrect XML Format: undefined well type: "+temp);
						}
					}else if (x && location){
						i=Integer.parseInt(new String(ch, start, length));
					}else if (y && location){
						j=Integer.parseInt(new String(ch, start, length));
					}else if (connections){
						//DO NOTHING
					}else if (new String(ch, start, length).trim().length()!=0){
						throw new SAXException("Incorrect XML Format (chars): "+ new String(ch, start, length));
					}
					
					
					if (i!=-1 && j!=-1 && tempType!=Layout.Types.Unknown){
						tile[i][j]=tempType;
						
						i=-1;
						j=-1;
						tempType=Layout.Types.Unknown;
					}

				}
			};

			File file = new File(pmdFileAddr);
			InputStream inputStream= new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream,"UTF-8");

			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");

			saxParser.parse(is, handler);
		}catch(FileNotFoundException e){
			System.err.println("Layout definition file not found!");
			return false;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Old_parse.
	 *
	 * @param defAddr the def addr
	 * @param layoutAddr the layout addr
	 * @return the layout
	 */
	public static Layout old_parse(String defAddr, String layoutAddr){
		Layout layout=new Layout();
		if (!old_defsParser(layout, defAddr))
			return null;
		if (!old_layoutParser(layout, layoutAddr))
			return null;
    	System.out.println("Layout parsing completed successfully!");
		return layout;
	}
	
	/**
	 * Old_defs parser.
	 *
	 * @param layout the layout
	 * @param addr the addr
	 * @return true, if successful
	 */
	public static boolean old_defsParser(final Layout layout, String addr){
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {
				boolean definitions=false;
				boolean ISA=false;
				boolean wells=false;
				boolean inst=false;

				//ISA
				boolean name = false;
				boolean delay = false;
				boolean error = false;

				//wells			
				boolean well = false;
				boolean instructions = false;
				boolean i=false;

				String instName, wellName;
				double errorRate; 
				int delayValue;


				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					if (qName.equalsIgnoreCase("definitions")){
						definitions=true;
					}else if (qName.equalsIgnoreCase("ISA")){
						ISA = true;
					}else if (qName.equalsIgnoreCase("wells")){
						wells = true;
					}
					//ISA
					else if (qName.equalsIgnoreCase("inst") && definitions && ISA){
						inst = true;
					}else if (qName.equalsIgnoreCase("name") && definitions && ISA && inst){
						name = true;
					}else if (qName.equalsIgnoreCase("delay") && definitions && ISA && inst) {
						delay = true;
					}else if (qName.equalsIgnoreCase("error") && definitions && ISA && inst) {
						error = true;
					}

					//Wells
					else if (qName.equalsIgnoreCase("well") && definitions && wells){
						well = true;
					}else if (qName.equalsIgnoreCase("name") && definitions && wells && well) {
						name = true;
					}else if (qName.equalsIgnoreCase("instructions") && definitions && wells && well) {
						instructions = true;
					}else if (qName.equalsIgnoreCase("i") && definitions && wells && well && instructions) {
						i = true;
					}else{
						throw new SAXException("Incorrect XML Format: "+qName);
					}
				}

				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (qName.equalsIgnoreCase("definitions")){
						definitions=false;
					}else if (qName.equalsIgnoreCase("ISA")){
						ISA = false;
					}else if (qName.equalsIgnoreCase("wells")){
						wells = false;
					}
					//ISA
					else if (qName.equalsIgnoreCase("inst") && definitions && ISA){
						inst = false;
					}else if (qName.equalsIgnoreCase("name") && definitions && ISA && inst){
						name = false;
					}else if (qName.equalsIgnoreCase("delay") && definitions && ISA && inst) {
						delay = false;
					}else if (qName.equalsIgnoreCase("error") && definitions && ISA && inst) {
						error = false;
					}

					//Wells
					else if (qName.equalsIgnoreCase("well") && definitions && wells){
						well = false;
					}else if (qName.equalsIgnoreCase("name") && definitions && wells && well) {
						name = false;
					}else if (qName.equalsIgnoreCase("instructions") && definitions && wells && well) {
						instructions = false;
					}else if (qName.equalsIgnoreCase("i") && definitions && wells && well && instructions) {
						i = false;
					}else{
						throw new SAXException("Incorrect XML Format: "+qName);
					}
				}

				public void characters(char ch[], int start, int length) throws SAXException {
					//ISA
					if (name && ISA) {
						//System.out.println("ISA::NAME: " + new String(ch, start, length));
						instName=new String(ch, start, length);					
					}else if (delay) {
						//System.out.println("Delay: " + new String(ch, start, length));
						delayValue=Integer.parseInt(new String(ch, start, length));
					}else if (error){
						//System.out.println("Error: " + new String(ch, start, length));
						errorRate=Double.parseDouble(new String(ch, start, length));

						layout.addNewOperation(new Operation(instName, errorRate, delayValue));
					}

					//wells
					else if (name && wells) {
						wellName=new String(ch, start, length);
					}else if (i) {
						instName= new String(ch, start, length);
						layout.addInstToWell(wellName, instName);
					}//skipping over white spaces
					else if (new String(ch, start, length).trim().length()!=0){
						throw new SAXException("Incorrect XML Format: "+ new String(ch, start, length) + length);
					}
				}

			};

			File file = new File(addr);
			InputStream inputStream= new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream,"UTF-8");

			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");

			saxParser.parse(is, handler);
		}catch(FileNotFoundException e){
			System.err.println("Layout definition file not found!");
			return false;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Old_layout parser.
	 *
	 * @param layout the layout
	 * @param addr the addr
	 * @return true, if successful
	 */
	public static boolean old_layoutParser(final Layout layout, String addr){
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {
				boolean gridConfig=false;
				boolean numXPatterns=false;
				boolean numYPatterns=false;
				boolean pattern=false;
				boolean rows = false;
				boolean cols = false;
				boolean row = false;
				boolean col = false;

				Dimension fabricSize=new Dimension();
				Dimension tileSize=new Dimension();
				Layout.Types [][]tile;

				int i,j;

				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					if (qName.equalsIgnoreCase("gridConfig")){
						gridConfig=true;
					}else if (qName.equalsIgnoreCase("numXPatterns") && gridConfig){
						numXPatterns = true;
					}else if (qName.equalsIgnoreCase("numYPatterns") && gridConfig){
						numYPatterns = true;
					}else if (qName.equalsIgnoreCase("pattern") && gridConfig){
						pattern = true;
					}else if (qName.equalsIgnoreCase("rows") && pattern && gridConfig){
						rows = true;
					}else if (qName.equalsIgnoreCase("cols") && pattern && gridConfig){
						cols = true;
					}else if (qName.equalsIgnoreCase("row") && pattern && gridConfig){
						j=0;
						i++;
						row = true;
					}else if (qName.equalsIgnoreCase("col") && row && pattern && gridConfig){
						col = true;
					}else{
						throw new SAXException("Incorrect XML Format: "+qName);
					}
				}

				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (qName.equalsIgnoreCase("gridConfig")){
						gridConfig=false;
					}else if (qName.equalsIgnoreCase("numXPatterns") && gridConfig){
						numXPatterns = false;
					}else if (qName.equalsIgnoreCase("numYPatterns") && gridConfig){
						numYPatterns = false;
					}else if (qName.equalsIgnoreCase("pattern") && gridConfig){
						pattern = false;
						//pattern is generated
						layout.initFabric(fabricSize, tileSize, tile);
					}else if (qName.equalsIgnoreCase("rows") && pattern && gridConfig){
						rows = false;
					}else if (qName.equalsIgnoreCase("cols") && pattern && gridConfig){
						cols = false;
					}else if (qName.equalsIgnoreCase("row") && pattern && gridConfig){
						row = false;
					}else if (qName.equalsIgnoreCase("col") && row && pattern && gridConfig){
						col = false;
					}else{
						throw new SAXException("Incorrect XML Format: "+qName);
					}
				}

				public void characters(char ch[], int start, int length) throws SAXException {
					if (numXPatterns) {
						fabricSize.width=Integer.parseInt(new String(ch, start, length));
						//System.out.println(new String(ch, start, length));

					}else if (numYPatterns){
						fabricSize.height=Integer.parseInt(new String(ch, start, length));
						//System.out.println(new String(ch, start, length));

					}else if (rows){
						tileSize.height=Integer.parseInt(new String(ch, start, length));
						//System.out.println(new String(ch, start, length));

					}else if (cols) {
						tileSize.width=Integer.parseInt(new String(ch, start, length));

						//initializing fabric tile
						tile=new Layout.Types[tileSize.height][tileSize.width];
						//System.out.println(new String(ch, start, length));
						i=-1;
					}else if (row){
						if (col){
							String temp=new String(ch, start, length);
							if (temp.equalsIgnoreCase("empty")){
								tile[i][j]=Layout.Types.Empty;
							}else if (temp.equalsIgnoreCase("basic")){
								tile[i][j]=Layout.Types.Basic;
							}else if (temp.equalsIgnoreCase("creation")){
								tile[i][j]=Layout.Types.Creation;
							}else if (temp.equalsIgnoreCase("interaction")){
								tile[i][j]=Layout.Types.Interaction;
							}else{
								throw new SAXException("Incorrect XML Format: undefined well type: "+temp);
							}
							j++;

						}
						//System.out.println(new String(ch, start, length));
					}//skipping over white spaces
					else if (new String(ch, start, length).trim().length()!=0){
						throw new SAXException("Incorrect XML Format: "+ new String(ch, start, length) + length);
					}
				}

			};


			File file = new File(addr);
			InputStream inputStream= new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream,"UTF-8");

			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");

			saxParser.parse(is, handler);
		}catch(FileNotFoundException e){
			System.err.println("Layout definition file not found!");
			return false;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


}
