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
package org.isatools.tablib.export.graph2tab.minflow;

import static java.lang.System.out;

import java.util.HashSet;
import java.util.Set;

import org.isatools.tablib.export.graph2tab.Node;
import org.isatools.tablib.export.graph2tab.dummy_graphs_tests.DummyGraph;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic tests for the {@link FlowInitialiser}.
 * 
 * <dl><dt>date</dt><dd>Aug 19, 2011</dd></dl>
 * @author brandizi
 *
 */
public class FlowInitialiserTest
{
	@Test
	
	/**
	 * <p> Tests {@link DummyGraph#getG1()}</p>
	 */
	public void testInitialFlowG1 ()
	{
		out.println("\n\n_______ INIT FLOW  ON G1 __________ ");

		Set<Node> nodes = new HashSet<Node>();

		DummyGraph g = DummyGraph.getG1 ();
		nodes.add ( g.getNode ( 2, "D" ) );
		
		FlowInitialiser flowInitialiser = new FlowInitialiser ( nodes );
		FlowManager flowMgr = flowInitialiser.getFlowManager ();
		
		assertEquals ( "Wrong flow in (A, C) + (B, C)!", 3, 
				flowMgr.getFlow ( g.getNode ( 0, "B" ), g.getNode ( 1, "C" ) ) 
				+ flowMgr.getFlow ( g.getNode ( 0, "A" ), g.getNode ( 1, "C" ) ) );
		
		assertEquals ( "Wrong flow in (C, D)!", 3, (int) flowMgr.getFlow ( g.getNode ( 1, "C" ), g.getNode ( 2, "D" ) ) );
		assertEquals ( "Wrong flow in (D, G)!", 2, (int) flowMgr.getFlow ( g.getNode ( 2, "D" ), g.getNode ( 3, "G" ) ) );

		assertEquals ( "Wrong flow in (G, H)!", 1, (int) flowMgr.getFlow ( g.getNode ( 3, "G" ), g.getNode ( 4, "H" ) ) );
		assertEquals ( "Wrong flow in (G, I)!", 1, (int) flowMgr.getFlow ( g.getNode ( 3, "G" ), g.getNode ( 4, "I" ) ) );
		assertEquals ( "Wrong flow in (E, F)!", 1, (int) flowMgr.getFlow ( g.getNode ( 3, "E" ), g.getNode ( 4, "F" ) ) );
	}
	
	@Test
	public void testInitialFlowG2 ()
	{
		out.println("\n\n_______ INIT FLOW  ON G2 __________ ");

		Set<Node> nodes = new HashSet<Node>();

		DummyGraph g = DummyGraph.getG2 ();
		nodes.add ( g.getNode ( 2, "D" ) );
		
		FlowInitialiser flowInitialiser = new FlowInitialiser ( nodes );
		FlowManager flowMgr = flowInitialiser.getFlowManager ();

		assertEquals ( "Wrong flow in (C, A) + (C, B)!", 3, 
				flowMgr.getFlow ( g.getNode ( 3, "C" ), g.getNode ( 4, "A" ) ) 
				+ flowMgr.getFlow ( g.getNode ( 3, "C" ), g.getNode ( 4, "B" ) ) );
		
		assertEquals ( "Wrong flow in (D, C)!", 3, (int) flowMgr.getFlow ( g.getNode ( 2, "D" ), g.getNode ( 3, "C" ) ) );
		assertEquals ( "Wrong flow in (G, D)!", 2, (int) flowMgr.getFlow ( g.getNode ( 1, "G" ), g.getNode ( 2, "D" ) ) );

		assertEquals ( "Wrong flow in (H, G)!", 1, (int) flowMgr.getFlow ( g.getNode ( 0, "H" ), g.getNode ( 1, "G" ) ) );
		assertEquals ( "Wrong flow in (I, G)!", 1, (int) flowMgr.getFlow ( g.getNode ( 0, "I" ), g.getNode ( 1, "G" ) ) );
		assertEquals ( "Wrong flow in (F, E)!", 1, (int) flowMgr.getFlow ( g.getNode ( 0, "F" ), g.getNode ( 1, "E" ) ) );
	}
	
	@Test
	public void testInitialFlowG3 ()
	{
		out.println("\n\n_______ INIT FLOW  ON G3 __________ ");

		Set<Node> nodes = new HashSet<Node>();
		DummyGraph g = DummyGraph.getG3 ();
		nodes.add ( g.getNode ( 0, "A" ) );
		nodes.add ( g.getNode ( 0, "B" ) );
		
		FlowInitialiser flowInitialiser = new FlowInitialiser ( nodes );
		FlowManager flowMgr = flowInitialiser.getFlowManager ();
		
		int fHI = flowMgr.getFlow ( g.getNode ( 2, "H" ), g.getNode ( 3, "I" ) );
		int fGI = flowMgr.getFlow ( g.getNode ( 2, "G" ), g.getNode ( 3, "I" ) );
		int fIL = flowMgr.getFlow ( g.getNode ( 3, "I" ), g.getNode ( 4, "L" ) );
		int fIK = flowMgr.getFlow ( g.getNode ( 3, "I" ), g.getNode ( 4, "K" ) );
		
		out.println ( "->I = " + fHI + ", " + fGI );
		out.println ( "I-> = " + fIL + ", " + fIK ); 
		
		assertEquals ( "I is not balanced!", 0, fIL + fIK - fHI - fGI );
		assertTrue ( "Input Flow is < minimum required, it is " + fHI + fGI + "< 4", fHI + fGI >= 4 );
		assertTrue ( "Output Flow is < minimum required, it is " + fIL + fIK + "< 4", fIL + fIK  >= 4 );

		int fSU = flowMgr.getFlow ( g.getNode ( 6, "S" ), g.getNode ( 7, "U" ) );
		int fQT = flowMgr.getFlow ( g.getNode ( 6, "Q" ), g.getNode ( 7, "T" ) );
		int fQR = flowMgr.getFlow ( g.getNode ( 6, "Q" ), g.getNode ( 7, "R" ) );

		out.println ( "SU = " + fSU + ", QT = " + fQT + ", QR = " + fQR ); 

		assertEquals ( "Flow in SU, QT, QR != flow in I", 0, fIL + fIK, fSU + fQT + fQR );
		
		int fBE = flowMgr.getFlow ( g.getNode ( 0, "B" ), g.getNode ( 1, "E" ) );
		int fBF = flowMgr.getFlow ( g.getNode ( 0, "B" ), g.getNode ( 1, "F" ) );
		int fAD = flowMgr.getFlow ( g.getNode ( 0, "A" ), g.getNode ( 1, "D" ) );
		int fAC = flowMgr.getFlow ( g.getNode ( 0, "A" ), g.getNode ( 1, "C" ) );
		
		out.println ( "BE = " + fBE + ", BF = " + fBF + ", AD = " + fAD + ", AC = " + fAC );
		assertEquals ( "Flow out from B and A != flow in I", 0, fIL + fIK, fBE + fBF + fAD + fAC );

	}
	
	// TODO: G4, G5
}
