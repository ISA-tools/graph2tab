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
package org.isatools.tablib.export.graph2tab.simple_biomodel_tests.node_wrappers;

import org.isatools.tablib.export.graph2tab.DefaultAbstractNode;
import org.isatools.tablib.export.graph2tab.DefaultTabValueGroup;
import org.isatools.tablib.export.graph2tab.Node;
import org.isatools.tablib.export.graph2tab.TabValueGroup;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.Annotation;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.ExperimentNode;
import org.isatools.tablib.export.graph2tab.simple_biomodel_tests.model.OntoTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>The basic wrapper of the exported object model. Basically we implement {@link #getInputs()} and {@link #getOutputs()}
 * , plus the way the node provides its table cells, i.e., {@link #getTabValues()}.</p>
 * 
 * <dl>
 * <dt>date</dt>
 * <dd>Jun 1, 2010</dd>
 * </dl>
 * 
 * @author brandizi
 */
public abstract class ExpNodeWrapper extends DefaultAbstractNode
{
	/**
	 * We use either this model or an extension of it (see package org.isatools.tablib.export.graph2tab.layering_tests.model), 
	 * A different factory is needed for either case. This is not a typical case, probably it will be more common to get the
	 * factory via some static getInstance() method.
	 *  
	 */
	private final NodeFactory nodeFactory; 
		
	/**
	 * This is used in {@link #getOrder()} to establish the order of a node. Basically {@link Node#getType()} is used to 
	 * lookup this map and get the order. This is a typical simple method to establish the order of a node, another may
	 * be tracking the column of the spreadsheet the node comes from.
	 */
	@SuppressWarnings ( "serial" )
	public static final Map<String, Integer> TYPE_ORDER = new HashMap<String, Integer> () 
	{{
		put ( "Source Name", 0 );
		put ( "Sample Name", 1 );
		put ( "Extract Name", 2 );
		put ( "Labeled Extract Name", 3 );
		put ( "Assay Name", 4 );
		put ( "Data File Name", 5 );
	}};
	
	private ExperimentNode base;

	/**
	 * This should be used only by your custom factory, {@link NodeFactory} in this example. Here we pass the factory,
	 * because these nodes are used in two different packages with two different factories and wrappers, but of course 
	 * it could be a static variable in simpler cases.
	 *  
	 */
	ExpNodeWrapper ( ExperimentNode base, NodeFactory nodeFactory )
	{
		this.base = base;
		this.nodeFactory = nodeFactory;
	}


	/**
	 * In this case we're able to write a generic method that is customised by the descendants. The methods shows how to
	 * add an ontology term to a free text value. This is done by keeping all into the same {@link TabValueGroup}.
	 * 
	 * @param nameHeader
	 *          eg: "BioMaterial Name", "Protocol REF"
	 * @param annHeaderPrefix
	 *          eg: "Characteristic", "Parameter Value", here headers will be built with the schema annHeaderPrefix [Êtype
	 *          ], eg: Characteristic [ÊOrganism ]
	 */
	protected List<TabValueGroup> getTabValues ( String nameHeader, String annHeaderPrefix )
	{
		List<TabValueGroup> result = new ArrayList<TabValueGroup> ();
		result.add ( new DefaultTabValueGroup ( nameHeader, base.getName () ) );

		for ( Annotation annotation: base.getAnnotations () )
		{
			DefaultTabValueGroup tbg = new DefaultTabValueGroup ( annHeaderPrefix + " [ " + annotation.getType () + " ]",
					annotation.getValue () );
			OntoTerm ot = annotation.getOntoTerm ();
			if ( ot != null )
			{
				tbg.append ( 
					new DefaultTabValueGroup ( "Term Accession Number", ot.getAcc (),
						new DefaultTabValueGroup ( "Term Source REF", ot.getSource () )
				));
			}
			result.add ( tbg );
		}
		return result;
	}

	/**
	 * If it's not a clone produced by {@link Node#createIsolatedClone()}, it uses {@link NodeFactory} to build wrappers
	 * for the input nodes of the base and to return them.
	 * <p/>
	 * This is the typical way this method is implemented by.
	 */
	@Override
	public SortedSet<Node> getInputs ()
	{
		if ( inputs != null )
		{
			return super.getInputs ();
		}
		inputs = new TreeSet<Node> ();
		for ( ExperimentNode in: base.getInputs () )
		{
			inputs.add ( nodeFactory.getNode ( in ) );
		}
		return super.getInputs ();
	}

	/**
	 * If it's not a clone produced by {@link Node#createIsolatedClone()}, it uses {@link NodeFactory} to build wrappers
	 * for the output nodes of the base and to return them.
	 * <p/>
	 * This is the typical way this method is implemented by.
	 */
	@Override
	public SortedSet<Node> getOutputs ()
	{
		if ( outputs != null )
		{
			return super.getOutputs ();
		}
		outputs = new TreeSet<Node> ();
		for ( ExperimentNode out: base.getOutputs () )
		{
			outputs.add ( nodeFactory.getNode ( out ) );
		}
		return super.getOutputs ();
	}

	/**
	 * This is used in the layering_tests package only. 
	 * 
	 * As explained above, this is used to re-order nodes of different types and solve certain ambiguities, e.g., to know
	 * that "Sample Name" comes after "Source Name". 
	 *  
	 * It uses a typical implementation, consisting in looking at an type order table, like {@link #TYPE_ORDER}, using the
	 * first header as key. -1 is returned if nothing is found in this table.
	 *  
	 */
	@Override
	public int getOrder ()
	{
		String header = getType();
		Integer order = TYPE_ORDER.get ( header );
		return order == null ? -1 : order;
	}

}
