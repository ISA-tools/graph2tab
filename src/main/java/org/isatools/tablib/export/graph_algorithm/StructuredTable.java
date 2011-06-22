package org.isatools.tablib.export.graph_algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.ebi.utils.collections.ListUtils;

/**
 * 
 * TODO: Comment me!
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

	public StructuredTable ( TabValueGroup tbg, int rowsSize )
	{
		this.header = tbg.getHeader ();
		addRow ( tbg.getValue (), rowsSize );
		
		for ( TabValueGroup tailTbg: tbg.getTail () )
			this.tail.add ( new StructuredTable ( tailTbg, rowsSize ) );
	}
	
	
	public String getHeader () {
		return header;
	}

	
	public void addRow ( String value, int newSize ) {
		ListUtils.set ( this.rows, newSize - 1, value );
	}
	
	public void addTable ( StructuredTable table ) {
		this.tail.add ( table );
	}


	public List<String> getRows () {
		return Collections.unmodifiableList ( rows );
	}


	public List<StructuredTable> getTail ()
	{
		return Collections.unmodifiableList ( tail );
	}
	
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
					table.addRow ( tbg.getValue (), newSize );
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

	
	private static void mergeNullTabValues ( List<StructuredTable> tables, int newSize )
	{
		for ( StructuredTable table: tables )
		{
			table.addRow ( null, newSize );
			mergeNullTabValues ( table.tail, newSize );
		}
	}

	public void addAllHeaders ( List<String> existingHeaders )
	{
		existingHeaders.add ( header );
		for ( StructuredTable tailTb: tail )
			tailTb.addAllHeaders ( existingHeaders );
	}

	public void addAllRows ( List<String> existingRow, int irow )
	{
		existingRow.add ( ListUtils.get ( this.rows, irow ) );
		for ( StructuredTable tailTb: tail )
			tailTb.addAllRows ( existingRow, irow );
	}
}
