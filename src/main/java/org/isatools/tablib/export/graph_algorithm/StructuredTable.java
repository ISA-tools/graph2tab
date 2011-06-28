package org.isatools.tablib.export.graph_algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.ebi.utils.collections.ListUtils;

/**
 * 
 * A Structured table. This is used internally by {@link TableBuilder#getTable()}. Essentially it allows to represent
 * a piece of the final resulting table as a list of nested columns (similarly to what it is done in {@link TabValueGroup})
 * This way it's easy to build the final table by progressively mering the nodes that are found the graph of chains that
 * {@link ChainsBuilder} obtains from the initial input. A structured table is composed of an header, an array of values
 * for that header (ie, the column) and optionally a tail, which represent linked headers and their values. The header
 * columns and the tail columns need to be kept together when new header/values are merged into an existing structured
 * table. A structured table is built by merging multiple {@link TabValueGroup}.
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
	 * Creates a new structured table that contains the same header of {@link TabValueGroup#getHeader()} and adds 
	 * the value coming from the tab value group ( {@link TabValueGroup#getValue()} ) as last row of the table
	 * {@link StructuredTable#getRows()}. Moreover, the tail of the table group {@link TabValueGroup#getTail()} becomes
	 * the tail of the new structured table {@link #getTail()}, by applying the same composition criteria (ie, this 
	 * cosntructor recursively calls itself over the tails it finds). 
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
	 * The table initial column header
	 */
	public String getHeader () {
		return header;
	}

	/**
	 * Adds a value to the row, given the information on what is the new desired size for the column represented by head of
	 * this structured table, ie, a new value for this {@link #getHeader() table's header}. This is used by 
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
	 * The table values for this header (ie, the values for a single column).
	 */
	public List<String> getRows () {
		return Collections.unmodifiableList ( rows );
	}


	/**
	 * The nested tables, ie, the tables that goes together with the top-level column. See {@link TabValueGroup}.
	 */
	public List<StructuredTable> getTail ()
	{
		return Collections.unmodifiableList ( tail );
	}
	
	/**
	 * Merge into exising structured tables a set of {@link TabValueGroup}s, typically the values provided with by 
	 * {@link Node#getTabValues()}, ie, the contribute that a node gives to the final resulting table.
	 * For instance, if so fare we have built a (structured) table with the headers: 
	 * Sample Name, Characteristics [ÊOrganism ], Characteristics [ Organ Part ]Ê and a new node
	 * arrives that contains: Sample Name, ( Characteristics [ Organism ], ( Term Source REF, ( Term Accession ) ) ), 
	 * Characteristics [ Sex ] the layer is changed so that it contains the new structure: 
	 * Sample Name, ( Characteristics [ Organism ], ( Term Source REF, ( Term Accession ) ) ), Characteristics [ Organ Part ],  
	 * Characteristics [ Sex ]. Note the usage of () to mark the nesting of new StructuredTable(s)
	 * 
	 * A new row is added to the {@link #getRows() row property} of the corresponding StructuredTable 
	 * that reflects the values of the new node (so, the row is {@link #getRows() this.getRow()} or some other row that can 
	 * be found by recursing over the {@link #getTail() tail}). 
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
				if ( !rowHeader.equals ( table.getHeader () ) ) continue;
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
	 * Collects all the headers ({@link #getHeader()} and all the objects in {@link #getTail()}) into a plain list).
	 * Obviously recurses on {@link #getTail()}. 
	 */
	public void exportAllHeaders ( List<String> existingHeaders )
	{
		existingHeaders.add ( header );
		for ( StructuredTable tailTb: tail )
			tailTb.exportAllHeaders ( existingHeaders );
	}

	/**
	 * Collects all the rows ({@link #getRows()} and all the objects in {@link #getTail()}) into a plain list).
	 * Obviously recurses on {@link #getTail()}. 
	 */
	public void exportAllRows ( List<String> existingRow, int irow )
	{
		existingRow.add ( ListUtils.get ( this.rows, irow ) );
		for ( StructuredTable tailTb: tail )
			tailTb.exportAllRows ( existingRow, irow );
	}
}
