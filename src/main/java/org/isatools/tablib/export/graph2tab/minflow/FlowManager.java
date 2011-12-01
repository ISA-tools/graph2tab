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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	 * In order to ensure good performance, the deficits are kept in an internal structure and updated by 
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
	
}
