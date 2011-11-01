/*

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.isatools.tablib.export.graph2tab.minflow.MinFlowCalculator;

import uk.ac.ebi.utils.collections.ListUtils;

/**
 * 
 * A Structured table. This is used internally by {@link TableBuilder#getTable()}. Essentially it allows to represent
 * a piece of the final resulting table as a list of nested columns (similarly to what it is done in {@link TabValueGroup})
 * This way it's easy to build the final table by progressively merging the nodes that are found in the set of paths that 
 * {@link MinFlowCalculator#getMinPathCover()} obtains from the initial input. 
 * A structured table is composed of an header, an array of values for that header (ie, the column) and optionally 
 * a tail, which represent linked headers and their values. The header columns and the tail columns need to be kept 
 * together when new header/values are merged into an existing structured table. A structured table is built by 
 * merging multiple {@link TabValueGroup}.
 *
 * <dl><dt>date</dt><dd>Jun 21, 2011</dd></dl>
 * @author brandizi
 *
 */
class StructuredTable
{

	private final String header;
	private final List<String> rows = new ArrayList<String> ();
	
	private final List<StructuredTable> tail = new ArrayList<StructuredTable> ();

	/**
	 * Creates a new structured table that contains the same header given by {@link TabValueGroup#getHeader() tbg.getHeader} 
	 * and adds the value ( {@link TabValueGroup#getValue() tbg.getValue} ) as last row 
	 * of the table, so this will appear in {@link StructuredTable#getRows()}. 
	 * Moreover, the tail of the table group {@link TabValueGroup#getTail()} becomes
	 * the tail of the new structured table {@link #getTail()}, by applying the same composition criteria (i.e., this 
	 * constructor recursively calls itself over the tails it finds). 
	 * 
	 * @param tbg
	 * @param rowsSize
	 */
	public StructuredTable ( TabValueGroup tbg, int rowsSize )
	{
		this.header = tbg.getHeader ();
		addRowValue ( tbg.getValue (), rowsSize );
		
		for ( TabValueGroup tailTbg: tbg.getTail () )
			this.tail.add ( new StructuredTable ( tailTbg, rowsSize ) );
	}
	
	/**
	 * The table's initial column header
	 */
	public String getHeader () {
		return header;
	}

	/**
	 * Adds a value to the row, given the information on what is the new desired size for the column represented by head of
	 * this structured table, i.e., a new value for this {@link #getHeader() table's header}. This is used by 
	 * {@link TableBuilder#getTable()}, which knows which row (== newSize) it is building. 
	 *  
	 */
	public void addRowValue ( String value, int newSize ) {
		ListUtils.set ( this.rows, newSize - 1, value );
	}
	
	/**
	 * Adds a nested table to the current header/column. For instance, A Term Source REF column can be added to an existing
	 * Characteristics[] column. See {@link TabValueGroup}.
	 */
	public void appendTable ( StructuredTable table ) {
		this.tail.add ( table );
	}


	/**
	 * The table values for this header (i.e., the values for the first single column).
	 */
	public List<String> getRows () {
		return Collections.unmodifiableList ( rows );
	}


	/**
	 * The nested tables, i.e., the tables that goes together with the top-level column. See {@link TabValueGroup}.
	 */
	public List<StructuredTable> getTail ()
	{
		return Collections.unmodifiableList ( tail );
	}
	
	/**
	 * Merges into existing structured tables a set of {@link TabValueGroup}s, typically the values provided with by 
	 * {@link Node#getTabValues()}, i.e., the contribute that a node gives to the final resulting table.
	 * For instance, if so fare we have built a (structured) table with the headers: 
	 * Sample Name, Characteristics [ÊOrganism ], Characteristics [ Organ Part ]Ê and a new node
	 * arrives that contains: Sample Name, ( Characteristics [ Organism ], ( Term Source REF, ( Term Accession ) ) ), 
	 * Characteristics [ Sex ] the layer is changed so that it contains the new structure: 
	 * Sample Name, ( Characteristics [ Organism ], ( Term Source REF, ( Term Accession ) ) ), Characteristics [ Organ Part ],  
	 * Characteristics [ Sex ]. Note the usage of () to mark the nesting of StructuredTable(s)
	 * 
	 * A new row is added to the {@link #getRows() rows property} of the corresponding StructuredTable 
	 * that reflects the values of the new node (so, the value is in {@link #getRows() this.getRows()} or in some other 
	 * StructuredTable that can be found by recursing over the {@link #getTail() tail}).
	 *  
	 * Existing headers for which the node has no value to provide with are left null in the new row. 
	 * 
	 */
	public static void mergeTabValues ( List<StructuredTable> tables, int newSize, List<TabValueGroup> tbvs )
	{
		if ( tables == null ) throw new RuntimeException ( 
			"mergeRows() expects a non null table array as parameter" 
		);
		
		if ( tbvs == null ) {
			mergeNullTabValues ( tables, newSize );
			return;
		}
			
		for ( TabValueGroup tbg: tbvs )
		{
			String rowHeader = tbg.getHeader ();
			boolean done = false;
			
			for ( StructuredTable table: tables )
			{
				if ( rowHeader == null || !rowHeader.equals ( table.getHeader () ) ) continue;
				if ( table.getRows ().size () < newSize )
				{
					// The header is still free for the row being built, so fill it. 
					table.addRowValue ( tbg.getValue (), newSize );
					mergeTabValues ( table.tail, newSize, tbg.getTail () );
					done = true;
					break;
				}				
			}

			if ( done ) continue;
			// else, we still have to fit the new header/value, so let's append it to the current array
			tables.add ( new StructuredTable ( tbg, newSize ) );
		} // for rows
	} // mergeRows()

	/**
	 * This is a facility similar to {@link #mergeTabValues(List, int, List)} (and actually used by it), adds a null 
	 * to every row that can be found in the parameter structured tables (it recurses on all the tails). 
	 * 
	 */
	private static void mergeNullTabValues ( List<StructuredTable> tables, int newSize )
	{
		for ( StructuredTable table: tables )
		{
			table.addRowValue ( null, newSize );
			mergeNullTabValues ( table.tail, newSize );
		}
	}

	/**
	 * Collects the first headers ({@link #getHeader()} and all the headers in {@link #getTail()}) into a plain list).
	 * Obviously recurses on {@link #getTail()}. 
	 */
	public void exportAllHeaders ( List<String> existingHeaders )
	{
		existingHeaders.add ( header );
		for ( StructuredTable tailTb: tail )
			tailTb.exportAllHeaders ( existingHeaders );
	}

	/**
	 * Collects all the rows for this header ({@link #getRows()} and all the rows in {@link #getTail()}) into a plain list).
	 * Obviously recurses on {@link #getTail()}.
	 */
	public void exportAllRows ( List<String> existingRow, int irow )
	{
		existingRow.add ( ListUtils.get ( this.rows, irow ) );
		for ( StructuredTable tailTb: tail )
			tailTb.exportAllRows ( existingRow, irow );
	}
}
