package org.isatools.tablib.export.graph2tab.layering_tests.wrappers;

import java.util.List;

import org.isatools.tablib.export.graph2tab.TabValueGroup;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioSource;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.node_wrappers.BioMaterialWrapper;

public class BioSourceWrapper extends BioMaterialWrapper
{

	BioSourceWrapper ( BioSource base, NodeFactory nodeFactory )
	{
		super ( base, nodeFactory );
	}

	@Override
	public List<TabValueGroup> getTabValues ()
	{
		return super.getTabValues ( "Source Name", "Characteristic" );
	}

}
