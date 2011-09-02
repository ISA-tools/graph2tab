package org.isatools.tablib.export.graph2tab.minflow;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.isatools.tablib.export.graph2tab.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Aug 25, 2011</dd></dl>
 * @author brandizi
 *
 */
class FlowInitialiser
{
	private final FlowManager flowMgr = new FlowManager ();
	private final Set<Node> nodes;
	
	private SortedSet<Node> startNodes = new TreeSet<Node> ();
	private Set<Node> endNodes = new HashSet<Node> ();

	private boolean isInitialised = false;

	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	FlowInitialiser ( Set<Node> nodes ) 
	{
		this.nodes = nodes;
	}
	
	Set<Node> getNodes ()
	{
		return nodes;
	}

	Set<Node> getEndNodes ()
	{
		if ( !isInitialised ) initFlow ();
		return endNodes;
	}
	
	SortedSet<Node> getStartNodes () 
	{
		if ( !isInitialised ) initFlow ();
		
		for ( Node n: nodes ) findStartNodes ( n );
		return startNodes;
	}
	
	private void findStartNodes ( Node node ) 
	{
		Set<Node> ins = node.getInputs ();
		
		if ( ins.isEmpty () ) {
			startNodes.add ( node );
			return;
		}
		
		for ( Node in: ins ) findStartNodes ( in );
		
		return;
	}

	FlowManager getFlowManager ()
	{
		if ( !isInitialised ) initFlow ();
		return flowMgr;
	}
	
	private void initFlow ()
	{
		if ( isInitialised ) return;
		
		isInitialised = true; // tell getEndNode() to go ahead
		
		Deque<Node> reviewNodes = new LinkedList<Node> ();
		for ( Node n: getStartNodes () ) initFlowRight ( n, reviewNodes );
		while ( !reviewNodes.isEmpty () ) initFlowLeft ( reviewNodes.pop () );		
	}

	
	private void initFlowRight ( Node node, Deque<Node> reviewNodes )
	{
		SortedSet<Node> ins = node.getInputs (), outs = node.getOutputs ();
		int nins = ins.size (), nouts = outs.size ();
		
		// Rare case of isolated node, nothing to do.
		if ( nins == 0 && nouts == 0 ) return;

		if ( nouts == 0 ) {
			// End node, end of rightward travel
			endNodes.add ( node );
			return;
		}	
		
		// First, saturate the outgoing edges with the minimum flow, unless this was already done in another visit
		//
		if ( log.isDebugEnabled () ) log.trace ( "Loading Outputs for '" + node + "'" );
		boolean flowChanged = false;
		for ( Node out: outs )
			if ( flowMgr.getFlow ( node, out ) == 0 ) {
				flowMgr.updateFlow ( node, out, 1 );
				flowChanged = true;
		}
			
		// Then, let's see what deficit we have at the node now 
		//
		int deficit = flowMgr.getDeficit ( node );
		if ( log.isDebugEnabled () ) log.trace ( "Working deficit of " + deficit + " for '" + node + "'" );
		
		// If nothing happened and the node is balanced, we don't have to go ahead with this path, all the left graph
		// won't change anyway
		if ( !flowChanged && deficit == 0 ) return;
		
		if ( deficit > 0 && nins != 0 )
		{
			// If it's not a source (for which the deficit is always >= 0), then 
			// we cannot balance the right graph with the flow that have come from the left side so far, so let's review 
			// this later, in a left-ward walk (via calls to setInitialFlowLeft())
			reviewNodes.push ( node );
		}
		else if ( deficit < 0 )
		{
			// Distribute the excess of input over the outputs. Try to do an even distribution, in order to maximise the 
			// likelihood that we have a minimum flow as soon as the initialisation is finished.
			//
			if ( log.isDebugEnabled () ) log.trace ( "Distributing excess of input for '" + node + "'" );
			
			deficit = -deficit;
			int dquota = deficit / nouts, rquota = deficit % nouts;
			for ( Node out: outs )
			{
				flowMgr.increaseFlow ( node, out, dquota );
				if ( rquota-- > 0 ) flowMgr.increaseFlow ( node, out, 1 );
			}
		}

		// else deficit became 0 after output loading, propagate the change(s) made on the right of the node to the full 
		// right graph
		for ( Node out: outs ) initFlowRight ( out, reviewNodes );
	}

	private void initFlowLeft ( Node node )
	{
		int deficit = flowMgr.getDeficit ( node );
		
		// 0 means It couldn't initially be balanced (with the flow accumulated up to the point where it was added to 
		// reviewNodes), but then it was by some other routes. We don't need to continue toward left from this 
		// particular node, if there is still some unbalanced node on its left graph, it will be dealt with by a call
		// that picks up that node from reviewNodes (in initFlow() )
		if ( deficit == 0 ) return;
		
		// We have a formal proof that this doesn't happen at this point. If it does, it is hardly due to the algorithm on
		// itself, can be because of underlining reasons (e.g., bad set of initial nodes, problem with Node implementation).
		//
		if ( deficit < 0 )
			throw new IllegalStateException ( 
				"Internal error, I found a node with deficit < 0 during the left-ward phase of initialisation, the theory " +
				"tells us that this never happens, but unfortunately the practice tells us bugs can be everywhere... " +
				"Check the way you customised graph2tab for your particular use case."
			);
		
		
		// deficit > 0
		// Distribute the excess of outputs over the inputs, so that it spreads toward the sources and the node is balanced
		//
		Set<Node> ins = node.getInputs ();
		int nins = ins.size ();
		// Source, we've finished and it's normal that deficit > 0 here 
		if ( nins == 0 ) return; 
		
		if ( log.isDebugEnabled () ) log.trace ( "Distributing excess of input " + deficit + " for '" + node + "'" );

		// Same approach as above
		int dquota = deficit / nins, rquota = deficit % nins;
		for ( Node in: ins )
		{
			flowMgr.increaseFlow ( in, node, dquota );
			if ( rquota-- > 0 ) flowMgr.increaseFlow ( in, node, 1 );
		}

		// Propagate the above changes to the right
		for ( Node in: ins ) initFlowLeft ( in );
	}

}
