/**

 The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

 Exhibit A
 The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1

 "The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"). You may not use this file except in compliance with the License.
 You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

 The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
 2007-2011 ISA Team. All Rights Reserved.

 Contributor(s):
 Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
 Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
 supporting standards-compliant experimental annotation and enabling curation at the community level.
 Bioinformatics 2010;26(18):2354-6.

 Alternatively, the contents of this file may be used under the terms of either the GNU General
 Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
 http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
 or the LGPL are applicable instead of those above. If you wish to allow use of your version
 of this file only under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your decision by deleting
 the provisions above and replace them with the notice and other provisions required by the
 GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
 of this file under the terms of any one of the MPL, the GPL or the LGPL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
 (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
 (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

 */

package org.isatools.tablib.export.graph2tab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * The table builder. This is the thing to (possibly extend) and invoke to produce the table exported from the
 * experimental graph. This class invokes {@link ChainsBuilder} and produce a matrix of strings from the chains that
 * this class creates from the input graph.
 * <p/>
 * Columns belonging in different group types are exported in no particular order. This occasionally screw the grouping 
 * of headers of the same type, eg, a factor value could fall between two characteristics[] column blocks. 
 * We will implement a solution to that in future. For details about that, see inside {@link #addNode(int, Node)}.
 * <p/>
 * 
 * <dl>
 * <dt>date</dt>
 * <dd>May 10, 2010</dd>
 * </dl>
 * 
 * @author brandizi
 */
public class TableBuilder
{
	protected Set<Node> nodes;
	protected final boolean isLayeringRequired; 
	protected List<List<String>> table = null;

	/**
	 * This defaults isLayeringRequired to true and keeps the nodes to work with uninitialised, you have to do it in your
	 * specific constructor. Hence, it is advisable that you setup this.nodes with the sinks after the call to this 
	 * constructor.
	 *  
	 */
	protected TableBuilder ()
	{
		this ( null, true );
	}

	/**
	 * This defaults isLayeringRequired to true, hence, it is advisable that you pass the sinks to this constructor.
	 */
	public TableBuilder ( Set<Node> nodes )
	{
		this ( nodes, true );
	}

	/**
	 * Nodes can be all the nodes in the graph or a subset like this:
	 *  
	 * <ul>
	 *   <li>in case isLayeringRequired = true, you can pass the sinks only, 
	 *   ie: all the right-most nodes, which have no output and allow you to reach the rest of the graph</li>
	 *   <li>in case isLayeringRequired = false, you can pass the sources only, ie those nodes on the left-most side of
	 *   the graph, which don't have inputs and allow you to reach the rest of the graph</li>
	 * </ul>
	 * 
	 * Passing the node subsets described will speed things up a little. 
	 * 
	 * @parameter isLayeringRequired true means that the graph may be uneven (with missing steps in the path from sources to sinks)
	 * and therefore it will require that layers are computed via {@link LayersBuilder}. Set this parameter to false
	 * <b>ONLY</b> if you are sure your graph has no such missing steps. 
	 */
	public TableBuilder ( Set<Node> nodes, boolean isLayeringRequired )
	{
		this.nodes = nodes;
		this.isLayeringRequired = isLayeringRequired;
	}

	
	/**
	 * The exported table, as a matrix of strings.
	 */
	public List<List<String>> getTable ()
	{

		if ( this.table != null )
			return this.table;

		LayersBuilder layersBuilder = null;

		if ( isLayeringRequired ) {
			layersBuilder = new LayersBuilder ( nodes );
			// It has the start nodes, so let's speed up things this way
			nodes = layersBuilder.getStartNodes ();
		}
		
		ChainsBuilder chainsBuilder = new ChainsBuilder ( nodes, layersBuilder );
		TableContents tableContents = new TableContents ();
		int nrows = 1; 
		
		for ( Node node: chainsBuilder.getStartNodes () )
		{
			int layer = 0, prevLayer = -1; 
			while ( true )
			{
				if ( isLayeringRequired ) 
				{
					layer = layersBuilder.getLayer ( node );
					// Start from the previous'node layer and fill-in-the-blanks until you reach the current layer
					for ( int layeri = prevLayer + 1; layeri < layer; layeri++ )
						tableContents.mergeNode ( layeri, null, nrows ); 
					prevLayer = layer;
				}
				
				tableContents.mergeNode ( layer, node, nrows );
				
				SortedSet<Node> outs = node.getOutputs ();
				if ( outs.isEmpty () ) break;
	
				node = outs.first ();
	
				// This flag is final, so javac optimises a bit here
				if ( !isLayeringRequired ) layer++;
				
			} // while on node chain 

			if ( isLayeringRequired )
			{
				// Fill-in-the-blanks until the last layer
				int maxLayer = layersBuilder.getMaxLayer ();
				for ( int layeri = prevLayer + 1; layeri <= maxLayer; layeri++ )
					tableContents.mergeNode ( layeri, null, nrows ); 
			}
			
			nrows++;
		} // for chain
		
		return this.table = tableContents.getTable ();
	}
	
	/**
	 * @return A string representation of {@link #getTable()}, to be used for debugging purposes.
	 */
	public String report ()
	{
		StringWriter sout = new StringWriter ();
		PrintWriter out = new PrintWriter ( sout );

		for ( List<String> row: getTable () )
		{
			for ( String v: row )
			{
				out.printf ( "%30.30s | ", v );
			}
			out.println ();
		}
		out.println ();
		return sout.toString ();
	}

	/**
	 * Writes the resulting table in CSV/TSV (table-separated values or other) format. Uses a CSVWriter from the OpenCSV
	 * library, which will take care of many details, like quoting line returns etc. 
	 * 
	 * This version allows you to first define the writer (and trigger your options) and then pass it.
	 * More abstract versions available below.
	 */
	public void reportTSV ( CSVWriter out ) throws IOException
	{
		for ( List<String> row: getTable () )
			out.writeNext ( (String[]) row.toArray ( new String [0] ) );
		out.flush ();
	}
	
	/**
	 * A wrapper of {@link #reportTSV(CSVWriter)}. Writes the resulting table in TSV format. 
	 * 
	 * This version receives a generic writer and sets up a CSVWriter that uses '\t' as separator and '"' as wrap/escape 
	 * character.
	 * 
	 */
	public void reportTSV ( Writer out ) throws IOException
	{
		reportTSV ( new CSVWriter ( out, '\t', '"' ) );
	}
	
	/**
	 * A wrapper of {@link #reportTSV(CSVWriter)}. Writes the resulting table in TSV format. 
	 * 
	 * This version uses '\t' as separator and '"' as wrap/escape character (invokes {@link #reportTSV(Writer)}) 
	 * and returns the result as a string.
	 * 
	 */
	public String reportTSV () throws IOException
	{
		StringWriter sout = new StringWriter ();
		reportTSV ( new CSVWriter ( sout, '\t', '"' ) );
		return sout.toString ();
	}

	/**
	 * A wrapper of {@link #reportTSV(CSVWriter)}. Writes the resulting table in TSV format. 
	 * 
	 * This version receives a file and then sets up a CSVWriter that uses '\t' as separator and '"' as wrap/escape 
	 * character (invoking {@link #reportTSV(Writer)}. 
	 * 
	 */
	public void reportTSV ( File file ) throws IOException
	{
		reportTSV ( new FileWriter ( file ) );
	}

	/**
	 * Reports the resulting table as a TSV file in the specified path. It's a wrapper of {@link #reportTSV(File)}.
	 */
	public void reportTSV ( String filePath ) throws IOException
	{
		reportTSV ( new File ( filePath ) );
	}

}