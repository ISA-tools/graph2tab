package org.isatools.tablib.export.graph_algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Jun 21, 2011</dd></dl>
 * @author brandizi
 *
 */
public class DefaultTableGroup implements TabValueGroup
{

	private final String header, value;
	private final List<TabValueGroup> tail = new ArrayList<TabValueGroup> ();

	public DefaultTableGroup ( String header, String value )
	{
		this.header = header;
		this.value = value;
	}

	public DefaultTableGroup ( String header, String value, TabValueGroup parentGroup )
	{
		this.header = header;
		this.value = value;
		this.append ( parentGroup );
	}

	public String getHeader ()
	{
		return header;
	}

	public String getValue ()
	{
		return value;
	}

	public List<TabValueGroup> getTail ()
	{
		return tail;
	}

	public void append ( TabValueGroup row )
	{
		this.tail.add ( row );
	}

}
