package org.isatools.tablib.export.graph2tab.layering_tests;

import static java.lang.System.out;

import java.util.HashSet;
import java.util.Set;

import org.isatools.tablib.export.graph2tab.TableBuilder;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioSample;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioSource;
import org.isatools.tablib.export.graph2tab.layering_tests.wrappers.LayeringModelTableBuilder;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.Data;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.ExperimentNode;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.ProtocolRef;
import org.junit.Test;

public class BigGraphTest
{
	/**
	 * Written to see how it behaves with big graph. It's kept at 100*5 nodes to avoid too time wasting during 
	 * automatic building, however, we have done tests with higher values (up to 10000*5) 
	 */
	@Test
	public void testBigGraph ()
	{
		out.println ( "_______ BIG GRAPH TEST ________ " );

		Set<ExperimentNode> nodes = new HashSet<ExperimentNode> ();

		for ( int line = 0; line < 100; line++ )
		{
		
			BioSource src1 = new BioSource ( "source 1" );
			src1.addCharacteristic ( "Organism", "Mus Musculus", "123", "NCBI" );
			src1.addCharacteristic ( "Age", "10 weeks", null, null );

			ProtocolRef proto1 = new ProtocolRef ( "sampling protocol 1" );
			proto1.addParameter ( "Sampling Quantity", "10 ml", null, null );

			src1.addOutput ( proto1 );

			BioSample sample1 = new BioSample ( "sample" );
			sample1.addCharacteristic ( "Material Type", "RNA", "RNA", "MGED-Ontology" );

			sample1.addInput ( proto1 );
		
			ProtocolRef proto2 = new ProtocolRef ( "scanning protocol 2" );
			proto2.addInput ( sample1 );

			Data data1 = new Data ( "file1.txt" );
			data1.addAnnotation ( "Image Correction Method", "intensity average", "123", "OBI" );
			data1.addInput ( proto2 );

			nodes.add ( src1 );
		}

		TableBuilder tb = new LayeringModelTableBuilder ( nodes );
		tb.report ();
	}
}
