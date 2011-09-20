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
