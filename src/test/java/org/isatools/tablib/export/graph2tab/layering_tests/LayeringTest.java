/*

The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

Exhibit A
The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
1.1/GPL version 2.0/LGPL version 2.1

"The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"). You may not use this file except in compliance with the License.
You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
2007-2011 ISA Team. All Rights Reserved.

Contributor(s):
Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
supporting standards-compliant experimental annotation and enabling curation at the community level.
Bioinformatics 2010;26(18):2354-6.

Alternatively, the contents of this file may be used under the terms of either the GNU General
Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
or the LGPL are applicable instead of those above. If you wish to allow use of your version
of this file only under the terms of either the GPL or the LGPL, and not to allow others to
use your version of this file under the terms of the MPL, indicate your decision by deleting
the provisions above and replace them with the notice and other provisions required by the
GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
of this file under the terms of any one of the MPL, the GPL or the LGPL.

Sponsors:
The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
(http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
(http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

*/
package org.isatools.tablib.export.graph2tab.layering_tests;

import static java.lang.System.out;

import java.util.HashSet;
import java.util.Set;

import org.isatools.tablib.export.graph2tab.TableBuilder;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioExtract;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioSample;
import org.isatools.tablib.export.graph2tab.layering_tests.model.BioSource;
import org.isatools.tablib.export.graph2tab.layering_tests.wrappers.LayeringModelTableBuilder;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.Data;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.ExperimentNode;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.ProtocolRef;
import org.junit.Test;

/**
 * Tests for cases of uneven graphs, which use the {@link org.isatools.tablib.export.graph2tab.LayersBuilder}. 
 *  
 * TODO: results are inspected manually, we need to write validation. 
 * 
 * <dl><dt>date</dt><dd>Mar 2, 2011</dd></dl>
 * @author brandizi
 *
 */
public class LayeringTest
{
	/**
	 * Tests this example, implemented as an instance of the sample bio-model:
	 * <p/>
	 * 
	 * <pre>
	 * src1   proto1   sample                      data1
	 * src2   proto1   sample   sample1   proto2   data2
	 * </pre>
	 */
	@Test
	public void testPipeline1 ()
	{
		out.println ( "_______ TEST LAYERING 1 ________ " );

		BioSource src1 = new BioSource ( "source 1" );
		src1.addCharacteristic ( "Organism", "Mus Musculus", "123", "NCBI" );
		src1.addCharacteristic ( "Age", "10 weeks", null, null );

		BioSource src2 = new BioSource ( "source 2" );
		src2.addCharacteristic ( "Organism", "Mus Musculus", "123", "NCBI" );
		src2.addCharacteristic ( "Age", "20 weeks", null, null );

		ProtocolRef proto1 = new ProtocolRef ( "sampling protocol 1" );
		proto1.addParameter ( "Sampling Quantity", "10 ml", null, null );

		src1.addOutput ( proto1 );
		src2.addOutput ( proto1 );

		BioSample sample = new BioSample ( "sample" );
		sample.addCharacteristic ( "Material Type", "RNA", "RNA", "MGED-Ontology" );

		sample.addInput ( proto1 );
		
		BioSample sample1 = new BioSample ( "sample 1" );
		sample1.addInput ( sample );

		ProtocolRef proto2 = new ProtocolRef ( "scanning protocol 2" );
		proto2.addInput ( sample1 );

		Data data1 = new Data ( "file1.txt" );
		data1.addAnnotation ( "Image Correction Method", "intensity average", "123", "OBI" );
		data1.addInput ( sample );

		Data data2 = new Data ( "file2.txt" );
		data2.addAnnotation ( "Image Amplification", "10x", null, null );
		data2.addInput ( proto2 );

		Set<ExperimentNode> nodes = new HashSet<ExperimentNode> ();
		nodes.add ( sample );

		TableBuilder tb = new LayeringModelTableBuilder ( nodes );
		out.println ( tb.report () );
	}
	
	/**
	 * <p>Tests the graph:<br/><br/> <img src = 'ex_uneven_graph_1.gif' />.</p>
	 *  
	 * <p>Which yelds this:</p>
	 * <p> 
	 * <pre>
	 *  Source Name |                    Sample Name |                    Sample Name |                   Protocol REF |                   Protocol REF |                   Extract Name | 
	 *         null |                       Sample 1 |                       Sample 2 |                           null |                           null |                             x1 | 
	 *         null |                       Sample 1 |                           null |            treating protocol 1 |            treating protocol 2 |                             x1 | 
	 *     source 1 |                           null |                       Sample 2 |                           null |                           null |                             x1 | 
	 *     source 1 |                       Sample 3 |                           null |            sampling protocol 3 |                           null |                           null | 
	 *     source 1 |                           null |                           null |                           null |                           null |                             x2 | 	 
	 * </pre>
	 * </p>
	 */
	@Test
	public void testUnevenGraph1 () throws Exception
	{
		out.println ( "_______ TEST UNEVEN GRAPH 1 ________ " );

		BioSource sr1 = new BioSource ( "source 1" );
		sr1.addCharacteristic ( "Organism", "Mus Musculus", "123", "NCBI" );
		sr1.addCharacteristic ( "Age", "10 weeks", null, null );

		BioSample sm1 = new BioSample ( "Sample 1" );
		sm1.addCharacteristic ( "Material Type", "RNA", "RNA", "MGED-Ontology" );
		
		BioSample sm3 = new BioSample ( "Sample 3" );

		sm3.addInput ( sr1 );
		
		
		ProtocolRef p3 = new ProtocolRef ( "sampling protocol 3" );
		p3.addParameter ( "Sampling Quantity", "10 ml", null, null );

		p3.addInput ( sm3 );
		
		BioSample sm2 = new BioSample ( "Sample 2" );
		sm2.addCharacteristic ( "Organism Part", "liver", null, null );
		
		sm2.addInput ( sm1 );
		sm2.addInput ( sr1 );
		
		ProtocolRef p1 = new ProtocolRef ( "treating protocol 1" );

		p1.addInput ( sm1 );
		
		ProtocolRef p2 = new ProtocolRef ( "treating protocol 2" );
		
		p2.addInput ( p1 );
		
		BioExtract x1 = new BioExtract ( "x1" );

		BioExtract x2 = new BioExtract ( "x2" );
		x2.addCharacteristic ( "Material Type", "DNA", "123", "OBI" );

		x1.addInput ( p2 );
		x1.addInput ( sm2 );
		x2.addInput ( sr1 );

		Set<ExperimentNode> nodes = new HashSet<ExperimentNode> ();
		nodes.add ( x1 );
		nodes.add ( x2 );
		nodes.add ( p3 );

		TableBuilder tb = new LayeringModelTableBuilder ( nodes );
		out.println ( tb.report () );
		tb.reportTSV ( "target/uneven_graph_1.csv" );
	}
	
	/**
	 * Tests the case:
	 * <pre> 
	 *  |  Sample1  |  Sample2  |  Extract1  |
	 *  |  Sample1  |  Proto1   |  Sample4   |
	 * </pre>
	 * 
	 * which should yeld: 
	 * 
	 * <pre>
	 *  |  Sample1  |           |  Sample2  |  Extract1  |
	 *  |  Sample1  |  Proto1   |  Sample4  |            |
	 * </pre>
	 * 
	 * It's the sample that is shifted, because it's semantic closeness to what we have on the right layer. 
	 */
	@Test
	public void testCloseSameTypes ()
	{
		out.println ( "_______ TEST FOR CLOSE SAME TYPES ________ " );

		BioSample sample1 = new BioSample ( "sample 1" );
		sample1.addCharacteristic ( "Material Type", "RNA", "RNA", "MGED-Ontology" );

		BioSample sample2 = new BioSample ( "sample 2" );
		sample2.addInput ( sample1 );
		
		ProtocolRef proto1 = new ProtocolRef ( "treatment protocol 1" );
		proto1.addParameter ( "Foo Quantity", "10 ml", null, null );
		proto1.addInput ( sample1 );
		
		BioSample sample4 = new BioSample ( "sample 4" );
		sample4.addInput ( proto1 );
		
		BioExtract xtract1 = new BioExtract ( "extract 1" );
		xtract1.addInput ( sample2 );

		Set<ExperimentNode> nodes = new HashSet<ExperimentNode> ();
		nodes.add ( sample4 );
		nodes.add ( xtract1 );

		TableBuilder tb = new LayeringModelTableBuilder ( nodes );
		out.println ( tb.report () );
	}
	
}
