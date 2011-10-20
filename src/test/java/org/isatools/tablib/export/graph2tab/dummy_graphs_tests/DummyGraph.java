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

package org.isatools.tablib.export.graph2tab.dummy_graphs_tests;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.isatools.tablib.export.graph2tab.Node;

/**
 * This is used by the tests, mainly to have a quick way to generate graphs from their topology
 * specification (see {@link #addChain(int, String)}).
 * <p/>
 * <dl><dt>date</dt><dd>Jun 1, 2010</dd></dl>
 *
 * @author brandizi
 */
public class DummyGraph 
{
	private Map<String, DummyNode> nodeMap = new HashMap<String, DummyNode>();

	/**
	 * The node, where the column is used to build a value like "Foo Header $column": $value   
	 */
	public DummyNode getNode(int column, String value) {
		String key = "" + column + value;
		DummyNode node = nodeMap.get("" + column + value);
		if (node != null) {
			return node;
		}
		node = new DummyNode(column, value);
		nodeMap.put(key, node);
		return node;
	}

	/**
	 * Creates a chain of nodes that start at the layer startLayer. Each node has a single-character label,
	 * so that the chain is represented by the chLabels string, eg: "ABC" creates the chain
	 * A-&gt;B-&gt;C chain.
	 *
	 * @return the first node in the created chain ( DummyNode ( startNode, "A" ) in the example above).
	 */
	public DummyNode addChain(int startLayer, String chLabels) {
		DummyNode result = null;
		char[] chars = chLabels.toCharArray();
		for (int i = 0; i < chars.length - 1;) {
			String in = "" + chars[i], out = "" + chars[++i];
			DummyNode inn = getNode(startLayer, in), outn = getNode(++startLayer, out);
			inn.addOutput(outn);
			if (i == 1) {
				result = inn;
			}
		}
		return result;
	}

	/**
	 * All the nodes in the graph
	 */
	public Collection<DummyNode> getNodes ()
	{
		return nodeMap.values ();
	}

	/**
	 * All the edges in the graph, each in a 2-sided array. 
	 */
	public Collection<DummyNode[]> getEdges ()
	{
		List<DummyNode[]> result = new LinkedList<DummyNode[]> ();
		for ( DummyNode n: getNodes () )
			for ( Node nout: n.getOutputs () )
				result.add ( new DummyNode[] { n, (DummyNode) nout } );
		
		return result;
	}
	
	private static DummyGraph g1 = null;
	
	/** <p>Example Graph G1.</p>
	 * <p><img src = "exp_graph1.png"></p>
	 */
	public static DummyGraph getG1 ()
	{
		if ( g1 != null ) return g1;
		
		DummyGraph g = new DummyGraph();

		g.addChain(0, "ACDEF");
		g.addChain(0, "BC");
		g.addChain(2, "DGH");
		g.addChain(3, "GI");
		
		return g1 = g;
	}

	private static DummyGraph g2 = null;
	
	/** <p>Example Graph G2.</p>
	 * <p><img src = "exp_graph2.png"></p>
	 */
	public static DummyGraph getG2 ()
	{
		if ( g2 != null ) return g2;

		DummyGraph g = new DummyGraph();

		g.addChain(0, "FEDCA");
		g.addChain(0, "HGDCB");
		g.addChain(0, "IGD");
		
		return g2 = g;
	}


	private static DummyGraph g3 = null;
	
	/** <p>Example Graph G3.</p>
	 * <p><img src = "exp_graph3.png"></p>
	 */
	public static DummyGraph getG3 ()
	{
		if ( g3!= null ) return g3;

		DummyGraph g = new DummyGraph();

		g.addChain ( 0, "ACGIKMQR" );
		g.addChain ( 0, "ADGIKNQT" );
		g.addChain ( 0, "BEHILOSU" );
		g.addChain ( 0, "BFHILPS" );
		
		return g3 = g;
	}


	private static DummyGraph g4 = null;
	
	/** <p>Example Graph G4.</p>
	 * <p><img src = "exp_graph4.png"></p>
	 */
	public static DummyGraph getG4 ()
	{
		if ( g4!= null ) return g4;

		DummyGraph g = new DummyGraph();
		g.addChain ( 0, "ABCDE" );
		g.addChain ( 0, "AGHIE" );
		g.addChain ( 0, "FGKIE" );
		g.addChain ( 0, "MGKLJ" );
		g.addChain ( 0, "MGKLP" );
		g.addChain ( 0, "MGNOP" );
		g.addChain ( 0, "MQRSP" );
		
		return g4 = g;
	}

	
	private static DummyGraph g5 = null;

	/** <p>Example Graph G5.</p>
	 * <p><img src = "exp_graph5.png"></p>
	 */
	public static DummyGraph getG5 ()
	{
		if ( g5!= null ) return g5;

		DummyGraph g = new DummyGraph();
		g.addChain ( 0, "ABCD" );
		g.addChain ( 0, "AEC" );
		g.addChain ( 1, "ECD" );
		g.addChain ( 1, "EFD" );
		g.addChain ( 2, "FG" );
		g.addChain ( 2, "FK" );
		g.addChain ( 0, "HE" );
		g.addChain ( 0, "HIJK" );
		g.addChain ( 2, "JO" );
		g.addChain ( 0, "LMNO" );

		return g5 = g;
	}

	
	private static DummyGraph g6 = null;
    
	/** <p>Example Graph G6.</p>
	 * <p><img src = "exp_graph6.png"></p>
	 */
	public static DummyGraph getG6 ()
	{
		if ( g6!= null ) return g6;

		DummyGraph g = new DummyGraph();
		g.addChain ( 0, "A" );
		g.addChain ( 0, "B" );

		return g6 = g;
	}


}
