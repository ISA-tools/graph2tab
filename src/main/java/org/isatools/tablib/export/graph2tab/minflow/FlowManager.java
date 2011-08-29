package org.isatools.tablib.export.graph2tab.minflow;

import java.util.HashMap;
import java.util.Map;

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
class FlowManager
{
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private ObjectStore<Node, Node, Integer> flows = new ObjectStore<Node, Node, Integer> ();
	private Map<Node, Integer> deficits = new HashMap<Node, Integer> ();
  

	FlowManager ()
	{
	}

	Integer getFlow ( Node n1, Node n2 )
	{
		Integer flow = flows.get ( n1, n2 );
		if ( flow != null ) return flow;
		flows.put ( n1, n2, 0 );
		return 0;
	}
		
	
	Integer getDeficit ( Node n )
	{
		Integer deficit = deficits.get ( n );
		if ( deficit != null ) return deficit;
		deficits.put ( n, 0 );
		return 0;
	}
	
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
	
}
