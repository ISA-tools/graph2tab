package org.isatools.tablib.export.graph2tab.minflow;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.isatools.tablib.export.graph2tab.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.collections.ObjectStore;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Aug 18, 2011</dd></dl>
 * @author brandizi
 *
 */
public class FlowManager
{
	// TODO demote debug messages to trace
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	private static class EgdeFlowInfo
	{
		int flow = 0;
	}

	private static class NodeFlowInfo
	{
		int deficit = 0;
	}
	
	private ObjectStore<Node, Node, EgdeFlowInfo> edgeFlowMap;
	private Map<Node, NodeFlowInfo> nodeFlowMap = new HashMap<Node, FlowManager.NodeFlowInfo> ();
  
	private Set<Node> nodes;
	SortedSet<Node> startNodes = null;
	private Set<Node> endNodes = null;

	public FlowManager ( Set<Node> nodes )
	{
		this.nodes = nodes;
	}
	
	public Set<Node> getNodes ()
	{
		return nodes;
	}


	public Set<Node> getStartNodes ()
	{
		if ( startNodes == null ) initFlow ();
		return startNodes;
	}

	
	public Set<Node> getEndNodes () 
	{
		if ( endNodes != null ) return endNodes;
		endNodes = new HashSet<Node> ();
		for ( Node n: nodes ) getEndNodes ( n );
		return endNodes;
	}
	
	private void getEndNodes ( Node node ) 
	{
		Set<Node> outs = node.getOutputs ();
		
		if ( outs.isEmpty () ) {
			endNodes.add ( node );
			return;
		}
		
		for ( Node out: outs ) 
			getEndNodes ( out );
		
		return;
	}
	

	private EgdeFlowInfo getEdgeFlowInfo ( Node n1, Node n2 )
	{
		if ( edgeFlowMap == null ) initFlow ();
		
		EgdeFlowInfo einfo = edgeFlowMap.get ( n1, n2 );
		if ( einfo != null ) return einfo;
		
		edgeFlowMap.put ( n1, n2, einfo = new EgdeFlowInfo () );
		
		// Initialise the deficits (will be quick if already done)
		getNodeFlowInfo ( n1 );
		getNodeFlowInfo ( n2 );
		
		return einfo;
	}
	
	public int getFlow ( Node n1, Node n2 )
	{
		return getEdgeFlowInfo ( n1, n2 ).flow;
	}
	
	private NodeFlowInfo getNodeFlowInfo ( Node n )
	{
		if ( nodeFlowMap == null ) initFlow ();
		
		NodeFlowInfo ninfo = nodeFlowMap.get ( n );
		if ( ninfo != null ) return ninfo;
		
		nodeFlowMap.put ( n, ninfo = new NodeFlowInfo () );
		return ninfo;
	}
	
	public int increaseFlow ( Node n1, Node n2, int delta )
	{
		EgdeFlowInfo einfo = getEdgeFlowInfo ( n1, n2 );
		if ( delta == 0 ) return einfo.flow;
		int newFlow = ( einfo.flow += delta );
		getNodeFlowInfo ( n1 ).deficit += delta;
		getNodeFlowInfo ( n2 ).deficit -= delta;
		if ( log.isDebugEnabled () )
			log.debug ( "Flow in '" + n1 + "' => '" + n2 + "' increased to " + newFlow 
					+ " (def1 = " + getNodeFlowInfo ( n1 ).deficit + ", def2 = " + getNodeFlowInfo ( n2 ).deficit + ")");
		return newFlow;
	}
	
	public int updateFlow ( Node n1, Node n2, int newFlow )
	{
		EgdeFlowInfo einfo = getEdgeFlowInfo ( n1, n2 );
		if ( einfo.flow == newFlow ) return 0;
		int delta = newFlow - einfo.flow;
		einfo.flow = newFlow;
		getNodeFlowInfo ( n1 ).deficit += delta;
		getNodeFlowInfo ( n2 ).deficit -= delta;
		if ( log.isDebugEnabled () )
			log.debug ( "Flow in '" + n1 + "' => '" + n2 + "' set to " + newFlow 
				+ " (def1 = " + getNodeFlowInfo ( n1 ).deficit + ", def2 = " + getNodeFlowInfo ( n2 ).deficit + ")" );
		return delta;
	}
	
	private void initFlow ()
	{
		if ( edgeFlowMap != null ) return;
		
		edgeFlowMap = new ObjectStore<Node, Node, EgdeFlowInfo> ();
		startNodes = new TreeSet<Node> ();
		
		Deque<Node> reviewNodes = new LinkedList<Node> ();
		for ( Node n: getEndNodes () ) setInitialFlowLeft ( n, reviewNodes );
		while ( !reviewNodes.isEmpty () ) setInitialFlowRight ( reviewNodes.pop () );
	}

	
	private void setInitialFlowLeft ( Node node, Deque<Node> reviewNodes )
	{
		SortedSet<Node> ins = node.getInputs (), outs = node.getOutputs ();
		int nins = ins.size (), nouts = outs.size ();
		
		// Rare case of isolated node, nothing to do.
		if ( nins == 0 && nouts == 0 ) return;

		if ( nins == 0 ) {
			// Source, end of leftward travel
			startNodes.add ( node );
			return;
		}	
			
		if ( nouts == 0 )
		{
			// Sink, load all your incoming edges with 1, unless they've already been already loaded by another path 
			for ( Node nin: ins )
				if ( getEdgeFlowInfo ( nin, node ).flow == 0 )
					updateFlow ( nin, node, 1 );

			// Then continue leftward
			for ( Node nin: ins ) setInitialFlowLeft ( nin, reviewNodes );
			return;
		}

		// nins != nouts and it's a middle node, let's work to the flow loading propagation 
		
		// First, saturate the incoming edges with the minimum flow, unless this was already done in another path
		//
		if ( log.isDebugEnabled () ) log.debug ( "Loading Inputs for '" + node + "'" );
		boolean flowChanged = false;
		for ( Node nin: ins )
			if ( getEdgeFlowInfo ( nin, node ).flow == 0 ) {
				updateFlow ( nin, node, 1 );
				flowChanged = true;
		}
		
		// Then, let's see what deficit we have at the node 
		//
		int deficit = getNodeFlowInfo ( node ).deficit;
		if ( log.isDebugEnabled () ) log.debug ( "Working deficit of " + deficit + " for '" + node + "'" );
		
		// If nothing happened and the node is balanced, we don't have to go ahead with this path, all the left graph
		// won't change anyway
		if ( !flowChanged && deficit == 0 ) return;
		
		if ( deficit < 0 )
		{
			// We cannot balance the left graph with the flow coming from the right (there isn't enough, at least so far), 
			// so let's review this later, in a rigth-ward walk (via calls to setInitialFlowRight ())
			reviewNodes.push ( node );
		}
		else if ( deficit > 0 )
		{
			// Distribute the excess of output over the inputs. Try to do an even distribution, in order to maximise the 
			// probability that we have a minimum flow as soon as the initialisation is finished.
			//
			if ( log.isDebugEnabled () ) log.debug ( "Distributing excess of output for '" + node + "'" );
			int dquota = deficit / nins, rquota = deficit % nins;
			for ( Node nin: ins )
			{
				increaseFlow ( nin, node, dquota );
				if ( rquota-- > 0 ) increaseFlow ( nin, node, 1 );
			}
		}
		// else if deficit == 0 after input loading, propagate the right change(s) to the full left graph

		// Propagate the above flow changes to the left
		for ( Node nin: ins ) setInitialFlowLeft ( nin, reviewNodes );
		
	}

	private void setInitialFlowRight ( Node node )
	{
		int deficit = getNodeFlowInfo ( node ).deficit;
		
		// 0 means It was initially not balanceable (with the flow accumulated up to the point where it was added to 
		// reviewNodes), but then it was balanced by some other paths. We don't need to continue toward right from this 
		// particular node, if there is still some un-balanced node on its right graph, it will be dealt with by a call
		// that picks up the node from reviewNodes (in initFlow() )
		if ( deficit == 0 ) return;
		
		// We have a formal proof that this doesn't happen at this point, but this is the real world, let's check
		// to be really sure
		if ( deficit > 0 )
			throw new IllegalStateException ( 
				"Internal error, I found a node with deficit > 0 during the rightward phase of initialisation, this should " +
				"never happen, likely there is a bug"
			);
		
		
		// deficit < 0
		// Distribute the excess of inputs over the outputs, so that it spreads toward the sinks and the node is balanced
		//
		
		Set<Node> outs = node.getOutputs ();
		int nouts = outs.size ();
		if ( nouts == 0 )
			// Sink, we've finished and it's normal that deficit < 0 here 
			return; 
		
		deficit = -deficit;
		if ( log.isDebugEnabled () ) log.debug ( "Distributing excess of input " + deficit + " for '" + node + "'" );

		// Same approach as above
		int dquota = deficit / nouts, rquota = deficit % nouts;
		for ( Node nout: outs )
		{
			increaseFlow ( node, nout, dquota );
			if ( rquota-- > 0 ) increaseFlow ( node, nout, 1 );
		}

		// Propagate the above changes to the right
		for ( Node nout: outs ) setInitialFlowRight ( nout );
	}
}
