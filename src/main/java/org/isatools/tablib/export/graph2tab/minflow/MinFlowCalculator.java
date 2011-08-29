package org.isatools.tablib.export.graph2tab.minflow;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.isatools.tablib.export.graph2tab.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO: Comment me!
 * TODO: all this isInitalised is to be reviewed
 *
 * <dl><dt>date</dt><dd>Aug 25, 2011</dd></dl>
 * @author brandizi
 *
 */
public class MinFlowCalculator
{
	private final FlowInitialiser initialiser;
	private FlowManager flowMgr;
	private List<List<Node>> minPathCover = new LinkedList<List<Node>> ();

	private boolean isInitialised = false; 
	
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	public MinFlowCalculator ( Set<Node> nodes )
	{
		initialiser = new FlowInitialiser ( nodes );
	}
	
	private int findPath ( Node n, Deque<Node> result )
	{
		Set<Node> nouts = n.getOutputs ();
		
		// Path search ends with sink nodes.
		if ( nouts.isEmpty () ) {
			result.push ( n );
			return -1;
		}
		
		for ( Node nout: nouts )
		{
			int residualFlow = flowMgr.getFlow ( n, nout ) - 1;
			if ( residualFlow == 0 ) continue;
			int forwardMin = findPath ( nout, result );
			
			// No path could be built, so pop failure up
			if ( forwardMin == -2 ) return -2;
			
			result.push ( n );
			return forwardMin == -1 || residualFlow < forwardMin ? residualFlow : forwardMin;
		}
		return -2;
	}

	private int findPath ( Deque<Node> result )
	{
		for ( Node src: initialiser.getStartNodes () )
		{
			int srcMin = findPath ( src, result );
			if ( srcMin > 0 ) {
				log.trace ( "Returng residual path of value " + srcMin + ": " + result );
				return srcMin;
			}
		}
		return -2;
	}
	
	private void findMinFlow ()
	{
		flowMgr = initialiser.getFlowManager ();
		
		Deque<Node> path = new LinkedList<Node> ();
		for ( int minResidual; ( minResidual = -findPath ( path ) ) < 0; )
		{
			Node prevNode = null;
			for ( Iterator<Node> pItr = path.iterator (); pItr.hasNext (); )
			{
				if ( prevNode == null ) { prevNode = pItr.next (); continue; }

				Node node = pItr.next ();
				flowMgr.increaseFlow ( prevNode, node, minResidual );
				prevNode = node;
			}
			path.clear ();
		}
	}
  
	
	public List<List<Node>> getMinPathCover ()
	{
		if ( isInitialised ) return minPathCover;
		
		findMinFlow ();
		
		minPathCover = new LinkedList<List<Node>> ();
		for ( List<Node> path; ( path = findMinPath () ) != null; minPathCover.add ( path ) );
		
		isInitialised = true;
		
		return minPathCover;
	}
	
	private List<Node> findMinPath ( Node n  )
	{
		Set<Node> nouts = n.getOutputs ();
		
		if ( nouts.isEmpty () ) 
		{
			List<Node> result = new LinkedList<Node> ();
			result.add ( 0, n );
			return result;
		}

		for ( Node nout: nouts )
		{
			if ( flowMgr.getFlow ( n, nout ) == 0 ) continue; 
			flowMgr.increaseFlow ( n, nout, -1 );
			List<Node> path = findMinPath ( nout );
			if ( path == null ) continue;
			
			path.add ( 0, n );
			return path;
		}
		return null;
	}
	
	private List<Node> findMinPath ()
	{
		for ( Node src: initialiser.getStartNodes () )
		{
			List<Node> path = findMinPath ( src );
			if ( path == null ) continue;
			return path;
		}
		return null;
	}

	public Set<Node> getNodes ()
	{
		return initialiser.getNodes ();
	}

	public SortedSet<Node> getStartNodes ()
	{
		return initialiser.getStartNodes ();
	}

	public Set<Node> getEndNodes () 
	{
		return initialiser.getEndNodes ();
	}

}
