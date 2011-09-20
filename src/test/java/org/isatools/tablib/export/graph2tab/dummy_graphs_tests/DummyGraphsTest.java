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

import org.isatools.tablib.export.graph2tab.Node;
import org.isatools.tablib.export.graph2tab.TableBuilder;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests on a basic implementation of the {@link Node} interface and rather complex graphs.
 * 
 * <dl><dt>date</dt><dd>Jun 1, 2010</dd></dl>
 *
 * @author brandizi
 */
public class DummyGraphsTest {
	private void assertContains(List<List<String>> paths, String V1, String V2) {
		assertContains(paths, V1, V2, true);
	}

	private void assertDoesntContain(List<List<String>> paths, String V1, String V2) {
		assertContains(paths, V1, V2, false);
	}

	/**
	 * Verifies that a matrix returned by {@link TableBuilder#getTable()} (doesn't) contains the edge V1-&gt;V2, ie
	 * two subsequent cells in the same column, having the parameter values.
	 * This is used in the tests below.
	 */
	private void assertContains(List<List<String>> paths, String V1, String V2, boolean wanted) {
		int lastColIdx = paths.get(0).size() - 1;
		for (int i = 0; i < paths.size(); i++) {
			List<String> path = paths.get(i);
			assertEquals("Error in table sizes!: " + i + ": " + path, lastColIdx, path.size() - 1);
			for (int j = 0; j < lastColIdx;) {
				if (V1.equals(path.get(j++)) && V2.equals(path.get(j))) {
					if (wanted) {
						return;
					}
					fail("Table Error! should not exist: " + V1 + " -> " + V2);
				}
			}
		}
		if (wanted) {
			fail("Table Error! should exist: " + V1 + " -> " + V2);
		}
	}

	/**
	 * <p> Tests {@link DummyGraph#getG1()}</p>
	 */
	@Test
	public void testG1() {
		out.println("_______ PATH COVER TEST 1 __________ ");

		Set<Node> nodes = new HashSet<Node>();
		DummyGraph g = DummyGraph.getG1 ();
		nodes.add ( g.getNode ( 2, "D" ) );

		TableBuilder tb = new TableBuilder ( nodes );
		out.println(tb.report());

		List<List<String>> paths = tb.getTable();

		assertEquals("N. rows Error!", 4, paths.size());
		assertEquals("N. cols Error!", 5, paths.get(0).size());

		assertContains(paths, "C", "D");
		assertContains(paths, "G", "H");
		assertContains(paths, "B", "C");
		assertContains(paths, "D", "E");

		assertDoesntContain(paths, "A", "B");
		assertDoesntContain(paths, "E", "H");
		assertDoesntContain(paths, "E", "I");
		assertDoesntContain(paths, "G", "F");

	}


	/**
	 * <p> Tests This example graph:</p>
	 * <p/>
	 * <img src = "exp_graph2.png">
	 */
	@Test
	public void testG2() {
		out.println("_______ PATHS COVER TEST 2 __________ ");

		Set<Node> nodes = new HashSet<Node>();
		DummyGraph g = DummyGraph.getG2 ();
		nodes.add(g.getNode(2, "D"));

		TableBuilder tb = new TableBuilder ( nodes );
		out.println(tb.report());

		List<List<String>> paths = tb.getTable();

		assertEquals("N. rows Error!", 4, paths.size());
		assertEquals("N. cols Error!", 5, paths.get(2).size());

		assertContains(paths, "H", "G");
		assertContains(paths, "I", "G");
		assertContains(paths, "C", "A");
		assertContains(paths, "C", "B");
		assertContains(paths, "D", "C");
		assertContains(paths, "F", "E");

		assertDoesntContain(paths, "I", "E");
		assertDoesntContain(paths, "F", "G");

	}

	/**
	 * <p>Tests This example graph:</p>
	 * <p/>
	 * <img src = "exp_graph3.png">
	 * 
	 */
	@Test
	public void testG3 () {
		out.println("_______ PATH COVER TEST 3 __________ ");

		Set<Node> nodes = new HashSet<Node>();
		DummyGraph g = DummyGraph.getG3 ();
		nodes.add ( g.getNode ( 0, "A" ) );
		nodes.add ( g.getNode ( 0, "B" ) );

		TableBuilder tb = new TableBuilder ( nodes );
		out.println(tb.report());

		List<List<String>> paths = tb.getTable();

		assertEquals ( "N. rows Error!", 5, paths.size () );
		assertEquals ( "N. cols Error!", 8, paths.get(2).size() );

		assertContains ( paths, "L", "O" );
		assertContains ( paths, "Q", "T" );
		assertContains ( paths, "H", "I" );
		assertContains ( paths, "I", "K" );
		assertContains ( paths, "A", "D" );
		assertContains ( paths, "Q", "T" );
		assertContains ( paths, "N", "Q" );
		
		assertDoesntContain ( paths, "Q", "U" );
		assertDoesntContain ( paths, "G", "K" );
		assertDoesntContain ( paths, "L", "M" );
		assertDoesntContain ( paths, "K", "O" );
	}
	
	
	/**
	 * <p>Tests This example graph:</p>
	 * <p/>
	 * <img src = "exp_graph4.png">
	 * 
	 */
	@Test
	public void testG4 () {
		out.println("_______ PATH COVER TEST 4 __________ ");

		Set<Node> nodes = new HashSet<Node>();

		DummyGraph g = DummyGraph.getG4 ();
		nodes.add ( g.getNode ( 1, "G" ) );

		TableBuilder tb = new TableBuilder ( nodes );
		out.println ( tb.report() );

		List<List<String>> paths = tb.getTable();

		// Must be 7+headers cause the max cut should be SP, OP, LP, LJ, KI, HI, CD. 
		assertEquals ( "N. rows Error!", 8, paths.size () );
		assertEquals ( "N. cols Error!", 5, paths.get(2).size() );
		
		for ( DummyNode[] edge: g.getEdges () )
			assertContains ( paths, edge[ 0 ].getValue (), edge[ 1 ].getValue () );
		
		assertDoesntContain ( paths, "M", "N" );
		assertDoesntContain ( paths, "P", "J" );
		assertDoesntContain ( paths, "M", "H" );
		assertDoesntContain ( paths, "N", "K" );
	}
	
	
	/**
	 * <p>Tests This example graph:</p>
	 * <p/>
	 * <img src = "exp_graph5.png">
	 * 
	 */
	@Test
	public void testG5 () {
		out.println("_______ PATH COVER TEST 5 __________ ");

		Set<Node> nodes = new HashSet<Node>();

		DummyGraph g = DummyGraph.getG5 ();
		nodes.add ( g.getNode ( 0, "A" ) );
		nodes.add ( g.getNode ( 0, "H" ) );
		nodes.add ( g.getNode ( 0, "L" ) );

		TableBuilder tb = new TableBuilder ( nodes );
		out.println ( tb.report() );

		List<List<String>> paths = tb.getTable();

		// Must be 7+headers cause the max cut should be BC, EC, FD, FG, FK, JK, JO, NO. 
		assertEquals ( "N. rows Error!", 9, paths.size () );
		assertEquals ( "N. cols Error!", 4, paths.get(2).size() );
		
		for ( DummyNode[] edge: g.getEdges () )
			assertContains ( paths, edge[ 0 ].getValue (), edge[ 1 ].getValue () );
		
		assertDoesntContain ( paths, "C", "F" );
		assertDoesntContain ( paths, "F", "J" );
		assertDoesntContain ( paths, "A", "A" );
		assertDoesntContain ( paths, "H", "C" );
		assertDoesntContain ( paths, "N", "K" );
		assertDoesntContain ( paths, "N", "K" );
	}
	
}
