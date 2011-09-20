package org.isatools.tablib.export.graph2tab.layering_tests.wrappers;

import java.util.List;

import org.isatools.tablib.export.graph2tab.TabValueGroup;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioLabeledExtract;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.node_wrappers.BioMaterialWrapper;

public class BioLabeledExtractWrapper extends BioMaterialWrapper
{

	BioLabeledExtractWrapper ( BioLabeledExtract base, NodeFactory nodeFactory )
	{
		super ( base, nodeFactory );
	}


	public List<TabValueGroup> getTabValues ()
	{
		return super.getTabValues ( "Labeled Extract Name", "Characteristic" );
	}

}
