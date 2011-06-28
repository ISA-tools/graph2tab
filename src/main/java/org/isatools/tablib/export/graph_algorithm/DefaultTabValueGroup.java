package org.isatools.tablib.export.graph_algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * A default implementation for {@link TabValueGroup}. This simply keeps in memory an header, value and tail.
 *
 * <dl><dt>date</dt><dd>Jun 21, 2011</dd></dl>
 * @author brandizi
 *
 */
public class DefaultTabValueGroup implements TabValueGroup
{

	private final String header, value;
	private final List<TabValueGroup> tail = new ArrayList<TabValueGroup> ();

	public DefaultTabValueGroup ( String header, String value )
	{
		this.header = header;
		this.value = value;
	}

	/**
	 * Defines pair of header/value and append an existing one to it. This allows top-down definitions of nested groups
	 * (see examples).
	 * 
	 */
	public DefaultTabValueGroup ( String header, String value, TabValueGroup tailValueGroup )
	{
		this.header = header;
		this.value = value;
		this.append ( tailValueGroup );
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

	public void append ( TabValueGroup tail )
	{
		this.tail.add ( tail );
	}

}
