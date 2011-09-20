/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee:  BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. 
 * To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
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

		return g4 = g;
	}


}
