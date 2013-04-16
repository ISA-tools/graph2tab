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
package org.isatools.tablib.export.graph2tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.export.graph2tab.minflow.MinFlowCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>The layer builder</h2>
 * 
 * <p>We try to explain below what the layering of an experimental work flow is, if you look at examples, one image
 * tells more than a thousand words. For instance, have a look at {@link LayeringTest#testUnevenGraph1()}</p>
 * 
 * <p>This class is optionally used by {@link TableBuilder}. It is needed if you expect an input experimental work flow 
 * graph that may be "uneven", i.e., that may contain paths from sources to end nodes of different length. 
 * This usually happen because some step in the experimental work flow is missing or omitted (e.g., a data item is 
 * achieved directly from a sample, while another has a specified extract material and an extraction protocol). 
 * If that is the case, the graph need to be "layered", in addition to being transformed into a set of source-to-sink
 * paths (by {@link MinFlowCalculator}). For any node, an integer layer index is computed. The layers span from left 
 * to right, from sources to end nodes (or sinks).</p>
 *  
 * <p>For every node, its layer is at least the topological highest distance from its reachable sources, possibly
 * increased so that, for each layer, all the nodes in it have the same type {@link Node#getType()}. Layers
 * essentially represent columns in a tabular format that are about homogeneous entity types (e.g., the columns for 
 * all the samples).</p>  
 * 
 * <p>This basically uses the approach described in section 3.3.2 of the 
 * <a href = "http://annotare.googlecode.com/files/MAGE-TABv1.1.pdf">MAGE-TAB specification</a>, adding several details 
 * that are not addressed in that document, such as the usage of the {@link Node#getOrder() nodes' order property} to 
 * sort out ambiguities that may arise.</p>    
 * 
 * <dl><dt>date</dt><dd>Feb 23, 2011</dd></dl>
 * @author brandizi
 *
 */
public class LayersBuilder
{
	private boolean isInitialized = false;

	private final Set<Node> endNodes;
	
	/**
	 * Allows to know all the nodes in a given layer, which is needed for completing the layering computation.
	 */
	private SortedMap<Integer, SortedSet<Node>> layer2Nodes = new TreeMap<Integer, SortedSet<Node>> ();
	
	/**
	 * Allows to know the layer a node is associated to. This is initially null.
	 */
	private Map<Node, Integer> node2Layer = new HashMap<Node, Integer> (); 
	
	/**
	 * The max layer index that was computed.
	 */
	private int maxLayer = -1;

	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	/**
	 * It expects the end nodes (sinks) of the graph to be layered. This is computed via inside {@link MinFlowCalculator}.
	 * 
	 */
	public LayersBuilder ( Set<Node> endNodes )
	{
		this.endNodes = endNodes;
	}

	
	/**
	 * Set the layer for a node, which means all the two internal structures used for that are updated.
	 * 
	 */
	private void setLayer ( Node node, int layer ) 
	{
		// Remove from the old layer
		Integer oldLayer = node2Layer.get ( node );
		if ( oldLayer != null )
		{
			SortedSet<Node> oldLayerNodes = layer2Nodes.get ( oldLayer );
			if ( oldLayerNodes.remove ( node ) && oldLayerNodes.isEmpty () && oldLayer == maxLayer )
				maxLayer--;
		}
		
		// Add to the new layer
		SortedSet<Node> lnodes = layer2Nodes.get ( layer );
		if ( lnodes == null ) {
			lnodes = new TreeSet<Node> ();
			layer2Nodes.put ( layer, lnodes );
		}
		lnodes.add ( node );
		node2Layer.put ( node, layer );
		if ( layer > maxLayer ) maxLayer = layer;
	}
	
	/**
	 * The first stage of the layering algorithm, layer indexes are computed by walking the graph upstream, i.e.: 
	 * layer ( n ) = max ( layer ( in ) ) for each in in {@link Node#getInputs()}. This is the recursive step.   
	 * 
	 */
	private int computeUntypedLayer ( Node node )
	{
		Integer result = node2Layer.get ( node );
		if ( result != null ) return result;
		
		result = -1;
		SortedSet<Node> ins = node.getInputs ();
		
		if ( !ins.isEmpty () ) 
			for ( Node in: ins )
			{
				int il = computeUntypedLayer ( in );
				if ( result < il ) result = il;
		}
		
		setLayer ( node, ++result );
		return result;
	}

	/**
	 * The first stage of the layering algorithm, layer indexes are computed by walking the graph upstream, i.e.: 
	 * layer ( n ) = max ( layer ( in ) ) for each in in {@link Node#getInputs()}.   
	 * 
	 */
	private void computeUntypedLayers ()
	{
		for ( Node sink: endNodes )
			computeUntypedLayer ( sink );
	}

	
	/**
	 * Computes the typed layers. It first invoked {@link #computeUntypedLayers()} and then walks all the layers, 
	 * adjusting all the nodes that have not the same types of the others. {@link Node#getOrder()} is used to determine
	 * which nodes have to shift on the right. Look at the code for details!
	 * 
	 */
	private void computeTypedLayers ()
	{
		computeUntypedLayers ();
		
		// Go through all the layers.
		for ( int layer = 0; layer <= maxLayer; layer++ ) 
		{
			if ( layer > 0 && log.isTraceEnabled () )
				log.trace ( "layering algo, layer " + ( layer - 1 ) + "/" + maxLayer + " processed" );
			log.trace ( "layering algo, working on the layer " + layer + "/" + maxLayer );
			
			List<Node> layerNodes = new ArrayList<Node> ( layer2Nodes.get ( layer ) );
			int nn = layerNodes.size ();
			
			// First, compute the minimum and the min-order on the left and on the right
			//
			int minOrder = -1;
			String firstType = null, minType = null;
			boolean hasMultipleTypes = false, isFirst = true, hasOrderUndefinedNodes = false;

			for ( int i = 0; i < nn; i++ )
			{
				Node node = layerNodes.get ( i );
				int order = node.getOrder ();
				String type = StringUtils.trimToNull ( node.getType () );

				if ( !hasOrderUndefinedNodes ) hasOrderUndefinedNodes = order == -1;
				
				if ( order != -1 && ( minOrder == -1 || order < minOrder ) ) {
					minOrder = order;
					minType = type;
				}
				
				if ( isFirst ) {
					firstType = type;
					isFirst = false;
				}
				else if ( !hasMultipleTypes && ( type == null && firstType != null || !type.equals ( firstType ) ) )
					hasMultipleTypes = true;
				
			} // for node

			// Now go over the nodes again and see which ones need to be shifted on the right
			//
			
			// In this case, we don't need to continue, all the nodes have the same type  
			if ( !hasMultipleTypes ) continue;
			
			int minOrderLeft = minOrderLeft ( layer ), minOrderRight = minOrderRight ( layer );
			
			boolean closer2Right = 
			  // If either side is order-irrelevant let's keep here the node with lowest order and send all others right
				minOrderRight == -1 || minOrderLeft == -1 
				// Is the min-order node closer to the right or to the left (according to the order)?
				|| minOrderRight - minOrder < minOrder - minOrderLeft;
			
			for ( int i = 0; i < nn; i++ )
			{
				Node node = layerNodes.get ( i );
				int order = node.getOrder ();
				String type = StringUtils.trimToNull ( node.getType () );
				
				if ( 
					// All nodes are order-irrelevant, so conventionally move all that have type != minType
				  minOrder == -1 && !StringUtils.equals ( type, minType )
				  // All nodes having order > of a defined min-order go to the right
				  || order > minOrder
				  // Nodes of undefined order that have a different type go to the right if the min-order nodes are closer 
				  // to the left, they're kept here otherwise
				  || minOrder != -1 && !closer2Right && order == -1 && !StringUtils.equals ( type, minType )
				  // Nodes of minimal order goes to the right if they're closer to that side and there are some order-undefined 
				  // node, which remain here.
				  || minOrder != -1 && closer2Right && hasOrderUndefinedNodes && order == minOrder
				)
					shift2Right ( node );
			
			} // for node
			
		} // for layer
		
		isInitialized = true;
	}
	
	
	/**
	 * Shifts the node to the right (i.e.: increase its layer index) and starts the propagation of that on the right side, 
	 * by invoking {@link #shift2Right(Node, List, int) shift2Right ( n, null, -1 )}.
	 * 
	 */
	private void shift2Right ( Node n ) {
		shift2Right ( n, -1, new HashSet<Node> () );
	}


	/**
	 * Shifts the node to the right (i.e.: increase its layer index) and recursively propagates that on the right side.
	 * visitedNodes allows it to stop the propagation on nodes that were already touched by this recursion. 

	 * @param n the node to be shifted, the method will recurse over n.getOutputs()
	 * @param prevNewLayer is the layer index that was computed by the previous recursive call (initially it is -1). This
	 * method recurse until the node is moved in a empty layer
	 * @param visitedNodes allows it to stop the propagation on nodes that were already touched by this recursion
	 * 
	 */
	private void shift2Right ( Node n, int prevNewLayer, Set<Node> visitedNodes )
	{
		// Visited, give up
		if ( visitedNodes.contains ( n ) ) return;
		
		int oldlayer = node2Layer.get ( n );

		// The previous shift had enough room, give up
		if ( prevNewLayer != -1 &&  oldlayer - prevNewLayer > 0 ) return;
		
		setLayer ( n, ++oldlayer );
		visitedNodes.add ( n );
		
		for ( Node out: n.getOutputs () )
			shift2Right ( out, oldlayer, visitedNodes );
	}
	
	/**
	 * The minimal order on the left side of the node. That is: 
	 * <ul>
	 *   <li>l0 = min ( nl.getOrder() != -1 and for all nl in m:layer(m) = layer(n) - 1)</li>
	 *   <li>if no node with order != -1 exist on the immediate left, layer(n) - 2,3,4... are evaluated the same way, 
	 *   the final result is -1 if we reach the leftmost side of the graph</li>
	 * </ul>
	 * 
	 * This means that we seek for the node having a significant order that is closer to the current node, if no such 
	 * node exists, we signal that by returning -1 (proper decisions are taken in {@link #computeTypedLayers()} in 
	 * such cases, see there).
	 *   
	 */
	private int minOrderLeft ( int layer )
	{
		while ( --layer >= 0 ) 
		{
			SortedSet<Node> lNodes = layer2Nodes.get ( layer );
			if ( !lNodes.isEmpty () ) 
			{ 
				int lno = lNodes.first ().getOrder ();
				// Ignore "doesn't matter" nodes
				if ( lno > -1 ) return lno;  
			}
		}
		return -1;
	}
	
	/**
	 * Computes the minimal order on the right side of the node, using the same approach described in 
	 * {@link #minOrderLeft(Node)}. 
	 * 
	 */
	private int minOrderRight ( int layer )
	{
		while ( ++layer <= maxLayer ) 
		{
			SortedSet<Node> rNodes = layer2Nodes.get ( layer );
			if ( !rNodes.isEmpty () ) 
			{
				int result = -1;
				for ( Node rnode: rNodes ) 
				{
					int ro = rnode.getOrder ();
					// Ignore "doesn't matter" nodes and keep searching until you find "good" nodes
					if ( ro < 0 ) continue;
					if ( result == -1 || ro < result ) result = ro;
				}
				// Ignore "doesn't matter" nodes and keep going right until you find "good" nodes
				if ( result > -1 ) return result;
			}
		}
		return -1;
	}

	/**
	 * Exposes the layer index to the world.
	 */
	public int getLayer ( Node n ) 
	{
		if ( !isInitialized ) computeTypedLayers ();
		return node2Layer.get ( n );
	}
	
	/**
	 * Exposes the layer nodes to the world, the returned set is unmodifiable.
	 */
	public SortedSet<Node> getLayerNodes ( int layer ) 
	{
		if ( !isInitialized ) computeTypedLayers ();
		return Collections.unmodifiableSortedSet ( layer2Nodes.get ( layer ) );
	}

	/**
	 * The highest layer index that was computed, minimal is 0 and hence there are maxLayer + 1 layer, every layer has
	 * at least a node, which also means there is at least one source-to-sink path that touches it.
	 * 
	 */
	public int getMaxLayer ()
	{
		if ( !isInitialized ) computeTypedLayers ();
		return maxLayer;
	}

		
	/**
	 * A representation of the current graph layering, useful for debugging.
	 * TODO: Move somewhere else?
	 *  
	 */
	@Override
	public String toString ()
	{
		String result = ""; 
		for ( int layer = 0; layer <= this.maxLayer; layer++ )
		{
			result += "LAYER " + layer + ":\n";
			result += "  ";
			Set<Node> nodes = layer2Nodes.get ( layer );
			if ( nodes == null ) continue;
			for ( Node n: nodes )
				result += n + "  ";
			result += "\n\n";
		}
		
		return result;
	}

}
