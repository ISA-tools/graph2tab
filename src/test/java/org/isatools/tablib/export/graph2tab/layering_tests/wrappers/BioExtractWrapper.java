package org.isatools.tablib.export.graph2tab.layering_tests.wrappers;

import java.util.List;

import org.isatools.tablib.export.graph2tab.TabValueGroup;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioExtract;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.node_wrappers.BioMaterialWrapper;

public class BioExtractWrapper extends BioMaterialWrapper
{

	BioExtractWrapper ( BioExtract base, NodeFactory nodeFactory )
	{
		super ( base, nodeFactory );
	}


	public List<TabValueGroup> getTabValues ()
	{
		return super.getTabValues ( "Extract Name", "Characteristic" );
	}

}
