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


import java.util.*;

import uk.ac.ebi.utils.collections.ListUtils;


/**
 * Provides a table-like representation of the layers in the graph. A layer is a set of homogeneous nodes having the 
 * same distance from the sources (eg: the initial column of sources or all the samples after the source column).
 * 
 * <p/>
 * Basically we allow to represent the structure &lt;layer no.&gt; =&gt; List&lt;{@link StructuredTable}&gt;. That is, 
 * the piece of table corresponding to a given layer is represented by a list of nested columns. This class is used by
 * {@link TableBuilder#getTable()} to create the final result table from the layered graph of node chains that was 
 * obtained from the initial input experimental work flow.  
 * <p/>
 * <dl>
 * <dt>date</dt>
 * <dd>Jun 27, 2011</dd>
 * </dl>
 * 
 * @author brandizi
 */
class TableContents
{
	/**
	 * All the contents
	 */
	private final Map<Integer, List<StructuredTable>> layerContents = new HashMap<Integer, List<StructuredTable>> ();
	private int nrows = 0;
	
	/**
	 * The list of layers which were set, in no particular order
	 */
	public Set<Integer> getLayers ()
	{
		return layerContents.keySet ();
	}

	/**
	 * The the contents of this layer, in the form of a list of nested columns (ie, {@link StructuredTable}). 
	 * Creates an empty structure if noting exists yet.
	 *  
	 */
	public List<StructuredTable> getLayerContent ( int layer )
	{
		List<StructuredTable> layerCont = layerContents.get ( layer );
		if ( layerCont != null )
		{
			return layerCont;
		}
		layerCont = new ArrayList<StructuredTable> ();
		layerContents.put ( layer, layerCont );
		return layerCont;
	}

	/**
	 * This merges a new node into one of the layers that are being built, ie, builds a piece of the final result table. 
	 * See {@link StructuredTable#mergeTabValues(List, int, List)} for details.
	 * 
	 * The node is supposed to belong in this layer (ie, is put under it by the {@link LayersBuilder} and therfore it has
	 * the same {@link Node#getType() type} of the existing added nodes. newRowsSize tells us which row we are filling, this
	 * is some information that the caller (ie, {@link TableBuilder#getTable()}) knows.
	 *  
	 */
	public void mergeNode ( int layer, Node node, int newRowsSize )
	{
		StructuredTable.mergeTabValues ( getLayerContent ( layer ), newRowsSize, node == null ? null : node.getTabValues () );
		if ( newRowsSize > nrows ) nrows = newRowsSize;
	}
	
	/**
	 * @return report of current contents
	 * 
	 */
	@Override
	public String toString ()
	{
		StringBuilder sb = new StringBuilder ();
		sb.append ( "-- ROWS: " + nrows + "\n\n" );
		for ( int layer: layerContents.keySet () )
		{
			List<StructuredTable> layerCont = layerContents.get ( layer );
			sb.append ( "---- LAYER: " + layer + "\n" );
			if ( layerCont.isEmpty () )
				sb.append ( "  --empty--" );
			else
			{
				// All the headers with \t\t sep
				toStringHeaders ( sb, layerCont );
				sb.append ( "\n" );

				// All the rows, with \t\t sep and \n after each row
				int nrows = layerCont.get ( 0 ).getRows ().size ();
				for ( int irow = 0; irow < nrows; irow++ ) {
					toStringRows ( sb, layerCont, irow );
					sb.append ( "\n" );
				}
			}
			sb.append ( "\n" );
		}
		return sb.toString ();
	}
	
	/**
	 * Used by {@link #toString()} to report all the headers in the nested structure inside a list of structured tables.
	 * It does that by going through the recursive structure of {@link StructuredTable}. 
	 * 
	 */
	private void toStringHeaders ( StringBuilder sb, List<StructuredTable> tables )
	{
		if ( tables == null || tables.isEmpty () ) return;
		for ( StructuredTable tb: tables )
		{
			sb.append ( "\t\t" ).append ( tb.getHeader () );
			List<StructuredTable> tail = tb.getTail ();
			if ( !tail.isEmpty () )
				toStringHeaders ( sb, tail );
		}
	}

	/**
	 * Used by {@link #toString()} to report all the row values in the nested structure inside a list of structured tables.
	 * It does that by going through the recursive structure of {@link StructuredTable}. 
	 *  
	 */
	private void toStringRows ( StringBuilder sb, List<StructuredTable> tables, int irow )
	{
		if ( tables == null || tables.isEmpty () ) return;
		for ( StructuredTable tb: tables )
		{
			sb.append ( "\t\t" ).append ( ListUtils.get ( tb.getRows (), irow ) );
			List<StructuredTable> tail = tb.getTail ();
			if ( !tail.isEmpty () )
				toStringRows ( sb, tail, irow );
		}
	}

	/**
	 * Reports all the headers inside the nested structures (ie, list of {@link StructuredTable}) that are associated to 
	 * the layers. That is, the headers for the final result. The result is computed at every call no caching.
	 * 
	 */
	public List<String> getHeaders ()
	{
		List<String> result = new LinkedList<String> ();
		for ( int layer: getLayers () )
			for ( StructuredTable table: getLayerContent ( layer ) )
				table.exportAllHeaders ( result );
		return result;
	}

	/**
	 * Reports all the row values inside the nested structures (ie, list of {@link StructuredTable}) that are associated to 
	 * the layers, for a given row index. That is, the row for the final result. 
	 * It does that by going through the recursive structure of {@link StructuredTable}. 
	 * The result is computed at every call no caching.
	 * 
	 */
	private List<String> getRow ( int irow )
	{
		List<String> result = new LinkedList<String> ();
		for ( int layer: getLayers () )
			for ( StructuredTable table: getLayerContent ( layer ) )
				table.exportAllRows ( result, irow );
		return result;
	}
	
	/**
	 * Reports all the row values inside the nested structures (ie, list of {@link StructuredTable}) that are associated to 
	 * the layers. That is, the rows for the final result. 
	 * It does that by going through the recursive structure of {@link StructuredTable}. 
	 * The result is computed at every call no caching.
	 */
	public List<List<String>> getRows ()
	{
		List<List<String>> result = new LinkedList<List<String>> ();
		for ( int irow = 0; irow < nrows; irow++ )
			result.add ( getRow ( irow ) );
		return result;
	}
	
	
	/**
	 * Reports all the row values inside the nested structures (ie, list of {@link StructuredTable}) that are associated to 
	 * the layers. That is, the rows for the final result. 
	 * It does that by going through the recursive structure of {@link StructuredTable}. Note that it doesn't call
	 * {@link #getRows()} but redo a loop over {@link #getRow(int)}, in order to save a bit of memory. 
	 * The result is computed at every call no caching.
	 */
	public List<List<String>> getTable ()
	{
		List<List<String>> result = new LinkedList<List<String>> ();

		result.add ( getHeaders () );
		
		for ( int irow = 0; irow < nrows; irow++ )
			result.add ( getRow ( irow ) );

		return result;
		
	}
}
