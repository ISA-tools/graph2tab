package org.isatools.tablib.export.graph2tab.minflow;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.isatools.tablib.export.graph2tab.Node;
import org.isatools.tablib.export.graph2tab.dummy_graphs_tests.DummyGraph;
import org.junit.Test;

/**
 * 
 * Tests for the minimum flow computed by {@link MinFlowCalculator}. 
 *
 * <dl><dt>date</dt><dd>Aug 29, 2011</dd></dl>
 * @author brandizi
 *
 */
public class MinFlowCalculatorTest
{
	/**
	 * Tests {@link DummyGraph#getG1()}
	 */
	@Test
	public void testG1 () throws Exception
	{
		out.println("\n\n_______ MIN FLOW  ON G1 __________ ");

		Set<Node> nodes = new HashSet<Node>();

		DummyGraph g = DummyGraph.getG1 ();
		nodes.add ( g.getNode ( 2, "D" ) );
		
		MinFlowCalculator minFlowCalc = new MinFlowCalculator ( nodes );		
		
		// Test the internals
    Method findMinFlowMethod = MinFlowCalculator.class.getDeclaredMethod ( "findMinFlow" );
    findMinFlowMethod.setAccessible ( true );
    findMinFlowMethod.invoke ( minFlowCalc );

    Field flowMgrField = MinFlowCalculator.class.getDeclaredField ( "flowMgr" );
    flowMgrField.setAccessible ( true );
    FlowManager flowMgr = (FlowManager) flowMgrField.get ( minFlowCalc );

		assertEquals ( "Wrong flow in (A, C) + (B, C)!", 3, 
				flowMgr.getFlow ( g.getNode ( 0, "B" ), g.getNode ( 1, "C" ) ) 
				+ flowMgr.getFlow ( g.getNode ( 0, "A" ), g.getNode ( 1, "C" ) ) );
		
		assertEquals ( "Wrong flow in (C, D)!", 3, (int) flowMgr.getFlow ( g.getNode ( 1, "C" ), g.getNode ( 2, "D" ) ) );
		assertEquals ( "Wrong flow in (D, G)!", 2, (int) flowMgr.getFlow ( g.getNode ( 2, "D" ), g.getNode ( 3, "G" ) ) );

		assertEquals ( "Wrong flow in (G, H)!", 1, (int) flowMgr.getFlow ( g.getNode ( 3, "G" ), g.getNode ( 4, "H" ) ) );
		assertEquals ( "Wrong flow in (G, I)!", 1, (int) flowMgr.getFlow ( g.getNode ( 3, "G" ), g.getNode ( 4, "I" ) ) );
		assertEquals ( "Wrong flow in (E, F)!", 1, (int) flowMgr.getFlow ( g.getNode ( 3, "E" ), g.getNode ( 4, "F" ) ) );
		
	}
	
	/** <p>Example Graph G3.</p>
	 * <p>{@link DummyGraph#getG3()}</p>
	 */
	@Test
	public void testG3 () throws Exception
	{
		out.println("\n\n_______ MIN FLOW  ON G3 __________ ");

		Set<Node> nodes = new HashSet<Node>();
		DummyGraph g = DummyGraph.getG3 ();
		nodes.add ( g.getNode ( 0, "A" ) );
		nodes.add ( g.getNode ( 0, "B" ) );
		
		MinFlowCalculator minFlowCalc = new MinFlowCalculator ( nodes );		
		
		// Test the internals
    Method findMinFlowMethod = MinFlowCalculator.class.getDeclaredMethod ( "findMinFlow" );
    findMinFlowMethod.setAccessible ( true );
    findMinFlowMethod.invoke ( minFlowCalc );

    Field flowMgrField = MinFlowCalculator.class.getDeclaredField ( "flowMgr" );
    flowMgrField.setAccessible ( true );
    FlowManager flowMgr = (FlowManager) flowMgrField.get ( minFlowCalc );
		
		int fHI = flowMgr.getFlow ( g.getNode ( 2, "H" ), g.getNode ( 3, "I" ) );
		int fGI = flowMgr.getFlow ( g.getNode ( 2, "G" ), g.getNode ( 3, "I" ) );
		int fIL = flowMgr.getFlow ( g.getNode ( 3, "I" ), g.getNode ( 4, "L" ) );
		int fIK = flowMgr.getFlow ( g.getNode ( 3, "I" ), g.getNode ( 4, "K" ) );
		
		out.println ( "->I = " + fHI + ", " + fGI );
		out.println ( "I-> = " + fIL + ", " + fIK ); 
		
		assertEquals ( "I is not balanced!", 0, fIL + fIK - fHI - fGI );
		assertEquals ( "Flow entering I is not minimum!", 4, fHI + fGI );
		assertEquals ( "Flow exiting I is not minimum! ", 4, fIL + fIK );

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
	
}
