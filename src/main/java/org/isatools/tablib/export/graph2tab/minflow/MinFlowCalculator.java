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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.isatools.tablib.export.graph2tab.LayersBuilder;
import org.isatools.tablib.export.graph2tab.Node;
import org.isatools.tablib.export.graph2tab.TableBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The minimum flow calculator. This contains an implementation of the Ford-Fulkerson (FF) algorithm for computing the
 * minimum flow from a source to a sink in a directed graph. This algorithm is adapted to our particular case and purpose,
 * which means:
 * 
 * <ul>
 *   <li>every real edge has the requirement of 1 as minimum flow on the edge and infinite as maximum</li>
 *   <li>the source is virtual and connects left-most nodes, those nodes having no input, with arcs having 0 and infinite
 *   as flow boundaries</li>
 *   <li>similarly, every right-most node (those with no output) connects a virtual sink with through a virtual edge having
 *   0/infinite as boundaries</li>
 * </ul>
 *
 * <p>In other words, computing a minimum flow under the conditions above ensures that a minimum set of paths covering all
 * the edges and all the nodes in the graph (see {@link #getMinPathCover()}) can be easily found from such flow.</p>
 * 
 * <p>Notes:</p>
 * 
 * <ul>
 *   <li>The choice of the FF algorithm depends on the fact that it performs well in the typical case of 
 *   experimental flow graphs. This will be explained in further documentation about this library.</li>
 *    
 *   <li>Isolated nodes are ignored in practice, you need to pass them to the constructor of this class and they will be
 * part of the minimum path set that is returned as final solution by {@link #getMinPathCover()}.</li>
 * 
 *   <li>If you look at the code, we don't actually create virtual parts (source, sink, their edges), because these can
 *   be managed in an implicit way. For instance, the residue of (s,n) is always equal to the current flow of such arc 
 *   and equal to the total flow outgoing from n.</li>
 * </ul>
 * 
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

	/**
	 * You need to pass a set of nodes from which all the graph is reachable, see {@link TableBuilder} for details.
	 */
	public MinFlowCalculator ( Set<Node> nodes )
	{
		initialiser = new FlowInitialiser ( nodes );
	}
	
	/**
	 * FF step where a decreasing path is found by attempting to go from any source to any sink. If such a path is found, 
	 * it will be in result and the method will return the minimum decrease that such path makes it possible.
	 * 
	 * If it returns -2, it means no decreasing path was found, it returns -1 when it gets an end/sink node as parameter. 
	 * These parameters are used during the recursion and by {@link #findMinFlow()}, to know that there isn't any path 
	 * any more. 
	 * 
	 */
	private int findPath ( Node n, Deque<Node> result )
	{
		Set<Node> nouts = n.getOutputs ();
		
		// Path search ends with sink nodes (-1 as return value).
		if ( nouts.isEmpty () ) {
			result.push ( n );
			return -1;
		}
		
		for ( Node nout: nouts )
		{
			// the dual (nout, n) arc has an infinite capacity, so ti can receive as much flow as needed, hence the residue
			// flow reduces to this formula in our context
			int residualFlow = flowMgr.getFlow ( n, nout ) - 1;
			if ( residualFlow == 0 ) continue;
			int forwardMin = findPath ( nout, result );
			
			// No path could be built, so backtrack the failure
			if ( forwardMin == -2 ) return -2;
			
			// Otherwise, add a step to the path and try to go ahead, mark the minimum computed so far.
			result.push ( n );
			return forwardMin == -1 || residualFlow < forwardMin ? residualFlow : forwardMin;
		}
		// -2 = dead end
		return -2;
	}

	/**
	 * A wrapper of {@link #findPath(Node, Deque)} that starts from all the source nodes, until a valid decreasing path is 
	 * found. In such a case it returns the minimum residual for such a path (i.e., the same result returned by 
	 * {@link #findPath(Node, Deque) findPath( source, empty-queue) }). Returns -2 when no such path exists.
	 * 
	 */
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
	
	/**
	 * The main loop of the FF algorithm. It keep finding decreasing paths from sources to sinks and decreases the flow along
	 * such paths, until no such decreasing path exists and no further flow decrease is possible. It has been proven that 
	 * this approach finds a correct solution at least when it deals with integer values.
	 * 
	 */
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
  
	/**
	 * After having found the minimum flow via {@link #findMinFlow()}, it builds the corresponding minimum set of paths
	 * that covers all the edges and all the nodes in the graph. Doing this is simple from the minimum flow: start from any
	 * source that has an outgoing edge with positive flow, go through that edge and decrease its flow at the same time, keep
	 * going toward right as long as possible, mark a new solution when you reach any sink, stop when you cannot build any
	 * further path this way.
	 * 
	 * Note that the above procedure adds isolated nodes to the solution automatically, as long as these are present in the 
	 * initial set of nodes passed to the constructor of this class. 
	 * 
	 */
	public List<List<Node>> getMinPathCover ()
	{
		if ( isInitialised ) return minPathCover;
		
		findMinFlow ();
		
		minPathCover = new LinkedList<List<Node>> ();
		// While you have paths, add them up
		for ( List<Node> path; ( path = findMinPath () ) != null; minPathCover.add ( path ) );
		
		isInitialised = true;
		
		return minPathCover;
	}
	
	/**
	 * The recursive step of {@link #getMinPathCover()}, see there.
	 */
	private List<Node> findMinPath ( Node n  )
	{
		Set<Node> nouts = n.getOutputs ();
		
		// End of travel, this is a new solution, start building it.
		if ( nouts.isEmpty () ) 
		{
			List<Node> result = new LinkedList<Node> ();
			result.add ( 0, n );
			return result;
		}

		for ( Node nout: nouts )
		{
			if ( flowMgr.getFlow ( n, nout ) == 0 ) continue;
			// Go through the first edge still having a positive flow, decrease it so that one more visit is traced.
			flowMgr.increaseFlow ( n, nout, -1 );
			
			List<Node> path = findMinPath ( nout );
			if ( path == null ) continue;

			// Compute the solution as the current node plus what it was found by the right-ward recursion.
			path.add ( 0, n );
			return path;
		}
		
		// No solution if we reach this point, return null to mark that.
		return null;
	}
	
	/**
	 * A wrapper of {@link #findMinPath(Node)} that starts from the graph sources and return the first path that is able
	 * to find (or null if none). 
	 * 
	 */
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

	/**
	 * The nodes initially passed to the constructor of this class. This is actually a wrapper of 
	 * {@link FlowInitialiser#getNodes()}.
	 * 
	 */
	public Set<Node> getNodes ()
	{
		return initialiser.getNodes ();
	}

	/**
	 * The left-most, source nodes, i.e., those nodes that have no real input. This is actually a wrapper of 
	 * {@link FlowInitialiser#getStartNodes()}.
	 */
	public SortedSet<Node> getStartNodes ()
	{
		return initialiser.getStartNodes ();
	}

	/**
	 * The right-most, sink nodes, i.e., those nodes that have no real output. This is actually a wrapper of 
	 * {@link FlowInitialiser#getEndNodes()}.
	 * 
	 */
	public Set<Node> getEndNodes () 
	{
		return initialiser.getEndNodes ();
	}

	/** 
	 * A wrapper of {@link FlowInitialiser#outDot(String, LayersBuilder)}.
	 */
	public void outDot ( String filePath, LayersBuilder layersBuilder ) throws FileNotFoundException
	{
		initialiser.outDot ( filePath, layersBuilder );
	}
	
	/**
	 * A wrapper of {@link FlowInitialiser#outDot(PrintStream, LayersBuilder)}.
	 */
	public void outDot ( PrintStream out, LayersBuilder layersBuilder )
	{
		initialiser.outDot ( out, layersBuilder );
	}
	
}
