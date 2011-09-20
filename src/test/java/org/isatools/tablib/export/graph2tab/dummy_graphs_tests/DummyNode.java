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
package org.isatools.tablib.export.graph2tab.dummy_graphs_tests;

import org.isatools.tablib.export.graph2tab.DefaultAbstractNode;
import org.isatools.tablib.export.graph2tab.DefaultTabValueGroup;
import org.isatools.tablib.export.graph2tab.Node;
import org.isatools.tablib.export.graph2tab.TabValueGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * A sample implementation of {@link Node}. This is is really simple and based on the straight extension of
 * {@link DefaultAbstractNode}. Typically you won't work like in this simple example, you'll will want to build
 * node wrappers and a node wrapper factory, see TODO.
 * <p/>
 * Note that the comparison/equivalence/hashing methods of {@link DefaultAbstractNode} are fine for this
 * implementation as well, because, they compares by node identity and sort by using the first value in
 * {@link #getTabValues()}.
 * <p/>
 * <dl><dt>date</dt><dd>May 31, 2010</dd></dl>
 *
 * @author brandizi
 */
public class DummyNode extends DefaultAbstractNode {
	private final int column;
	private final String value;

	/**
	 * This will result in a {@link TabValueGroup} with a single pair in it, where header = "Foo Header $column" and
	 * value is the value provided here.
	 */
	DummyNode(int column, String value) {
		this.column = column;
		this.value = value;
		this.inputs = new TreeSet<Node>();
		this.outputs = new TreeSet<Node>();
	}

	/**
	 * Just verify that it's a {@link DummyNode}, used by addXXX().
	 */
	private void checkNodeType(Node node) {
		if (!(node instanceof DummyNode)) {
			throw new IllegalArgumentException("DummyNode(s) works only with other DummyNode(s)");
		}
	}

	/**
	 * Just checks that the input is of DummyNode
	 */
	public boolean addInput(Node input) {
		checkNodeType(input);
		return super.addInput(input);
	}

	/**
	 * Just checks that the output is of DummyNode
	 */
	public boolean addOutput(Node output) {
		checkNodeType(output);
		return super.addOutput(output);
	}

	/**
	 * See the constructor.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * See the constructor.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return a list of {@link TabValueGroup}, representing the contribute of this node to the final table.  
	 */
	public List<TabValueGroup> getTabValues () {
		List<TabValueGroup> result = new ArrayList<TabValueGroup>();
		result.add ( new DefaultTabValueGroup ( "Foo Header "+ column, value ) );
		return result;
	}

}
