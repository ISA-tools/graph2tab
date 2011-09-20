package org.isatools.tablib.export.graph2tab.minflow;

import java.util.HashMap;
import java.util.Map;

import org.isatools.tablib.export.graph2tab.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.collections.ObjectStore;

/**
 * 
 * A flow manager, used to compute the minimum flow by {@link MinFlowCalculator}. It just maps nodes and arcs of a
 * graph to node deficits and arc flows. It doesn't manage flow bounds, since in our scope of graph-to-table conversion
 * the lower bound is implicitly 1 (or 0, when virtual source and virtual sink are involved) and the upper bound is 
 * infinite.
 *
 * <dl><dt>date</dt><dd>Aug 18, 2011</dd></dl>
 * @author brandizi
 *
 */
class FlowManager
{
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private ObjectStore<Node, Node, Integer> flows = new ObjectStore<Node, Node, Integer> ();
	private Map<Node, Integer> deficits = new HashMap<Node, Integer> ();
  
	/**
	 * The flow through an edge. Our graphs have at most one arc between any two distinct nodes, so we don't need an
	 * explicit representation of edges.
	 * 
	 * The result is an object and not a primitive integer for internal reasons about performance.
	 * 
	 */
	Integer getFlow ( Node n1, Node n2 )
	{
		Integer flow = flows.get ( n1, n2 );
		if ( flow != null ) return flow;
		flows.put ( n1, n2, 0 );
		return 0;
	}
		
	/**
	 * The flow deficit at node n. This is defined as outgoing-flow(n) - incoming-flow(n), where outgoing flow is 
	 * Sum ( flow( n -&gt; j ) ) and incoming flow is Sum ( flow( i -&gt; n ). We have chosen this definition according
	 * to <i><a href = 'http://basilo.kaist.ac.kr/mathnet/kms_tex/981523.pdf'>Ciurea and Ciupal&atilde;, 2004</a></i>.
	 * 
	 * In order to have a good performance, the deficits are kept in an internal structure and updated by 
	 * flow-changing methods. They're never computed.
	 * 
	 * The result is an object and not a primitive integer for internal reasons about performance.
	 * 
	 */
	Integer getDeficit ( Node n )
	{
		Integer deficit = deficits.get ( n );
		if ( deficit != null ) return deficit;
		deficits.put ( n, 0 );
		return 0;
	}
	
	/**
	 * Increase the flow in the (n1, n2) arc by delta. If delta is negative, the flow is automatically decreased instead.
	 * This updates the deficits at the edge ends too.
	 * 
	 */
	int increaseFlow ( Node n1, Node n2, int delta )
	{
		int flow = getFlow ( n1, n2 );
		if ( delta == 0 ) return flow;

		int newFlow = flow + delta;
		flows.put ( n1, n2, newFlow );

		int deficit1New = getDeficit ( n1 ) + delta; deficits.put ( n1, deficit1New );
		int deficit2New = getDeficit ( n2 ) - delta; deficits.put ( n2, deficit2New );
				
		if ( log.isDebugEnabled () )
			log.trace ( "Flow in '" + n1 + "' => '" + n2 + "' increased to " + newFlow 
					+ " (def1 = " + deficit1New + ", def2 = " + deficit2New + ")");
		
		return newFlow;
	}
	
	/**
	 * Set the flow through the (n1, n2) arc to the new flow.
	 * This updates the deficits at the edge ends too.
	 * 
	 */
	int updateFlow ( Node n1, Node n2, int newFlow )
	{
		int flow = getFlow ( n1, n2 );
		
		if ( flow == newFlow ) return 0;
		int delta = newFlow - flow;
		flows.put ( n1, n2, newFlow );
		
		int deficit1New = getDeficit ( n1 ) + delta; deficits.put ( n1, deficit1New );
		int deficit2New = getDeficit ( n2 ) - delta; deficits.put ( n2, deficit2New );

		if ( log.isDebugEnabled () )
			log.trace ( "Flow in '" + n1 + "' => '" + n2 + "' set to " + newFlow 
				+ " (def1 = " + deficit1New + ", def2 = " + deficit2New + ")" );
		return delta;
	}
	
	
	// TODO if needed. Requires revision and the addition of flow information.
	// 
///**
//* A facility useful for debugging. Outputs a syntax that can be used by GraphViz to show the graph being built.
//*/
//public void outDot ( PrintStream out, Node currentNode )
//{
//	Map<Node, Integer> ids = new HashMap<Node, Integer> ();
//	Set<Node> visited = new HashSet<Node> ();
//
//	out.println ( "strict digraph ExperimentalPipeline {" );
//	out.println ( "  graph [rankdir=LR];" );
//
//	for ( Node node: startNodes )
//		outDot ( out, ids, visited, node, currentNode );
//
//	// Adds up the layers if available
//	if ( layersBuilder != null )
//	{
//		out.println ();
//
//		int maxLayer = layersBuilder.getMaxLayer ();
//		for ( int layer = 0; layer <= maxLayer; layer++ )
//		{
//			Set<Node> lnodes = layersBuilder.getLayerNodes ( layer );
//			if ( lnodes == null || lnodes.isEmpty () )
//				continue;
//
//			out.println ( "    // layer " + layer );
//			out.print ( "    { rank = same" );
//			for ( Node node: lnodes ) {
//				int nodeid = ids.get ( node );
//				out.print ( "; " + nodeid );
//			}
//			out.println ( " }\n" );
//		}
//		out.println ();
//	}
//
//	out.println ( "}" );
//}
//
///**
//* @see #outDot(PrintStream).
//*/
//private void outDot ( PrintStream out, Map<Node, Integer> ids, Set<Node> visited, Node node, Node currentNode )
//{
//	if ( visited.contains ( node ) )
//		return;
//	visited.add ( node );
//
//	// The rainbow can help in tracking the graph manually.
//	final String[] colors = { "black", "red", "blue", "magenta", "green", "orange", "purple", "turquoise" };
//
//	String nodelbl = node.toString ();
//	Integer nodeid = ids.get ( node );
//	if ( nodeid == null )
//	{
//		nodeid = ids.size ();
//		ids.put ( node, nodeid );
//		String bgcolor = node.equals ( currentNode ) ? "yellow" : "white";
//		String color = colors[nodeid % colors.length];
//		out.println ( "  " + nodeid + 
//			"[label = \"" + nodelbl + "\", style = filled, color = " + color + ", fillcolor = " + bgcolor + "];" );
//	}
//
//	for ( Node nout: node.getOutputs () )
//	{
//		Integer outid = ids.get ( nout );
//		if ( outid == null )
//		{
//			outid = ids.size ();
//			ids.put ( nout, outid );
//			String outlbl = nout.toString ();
//			String bgcolor = nout.equals ( currentNode ) ? "yellow" : "white";
//			String color = colors[outid % colors.length];
//			out.println ( "  " + outid + 
//				"[label = \"" + outlbl + "\", style = filled, color = " + color + ", fillcolor = " + bgcolor + "];" );
//		}
//
//		String color = colors[ ( nodeid + outid ) % colors.length];
//		out.println ( "  " + nodeid + " -> " + outid + "[color = " + color + "];" );
//		outDot ( out, ids, visited, nout, currentNode );
//	}
//}	

}
