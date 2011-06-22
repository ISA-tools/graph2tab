package org.isatools.tablib.export.graph_algorithm;

import java.util.List;

public interface TabValueGroup
{
	public String getHeader();
	public String getValue();
	public List<TabValueGroup> getTail ();
}
