package org.isatools.tablib.export.graph2tab.layering_tests.wrappers;

import java.util.List;

import org.isatools.tablib.export.graph2tab.TabValueGroup;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioSample;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.node_wrappers.BioMaterialWrapper;

public class BioSampleWrapper extends BioMaterialWrapper
{

	BioSampleWrapper ( BioSample base, NodeFactory nodeFactory )
	{
		super ( base, nodeFactory );
	}

	public List<TabValueGroup> getTabValues ()
	{
		return super.getTabValues ( "Sample Name", "Characteristic" );
	}

}
