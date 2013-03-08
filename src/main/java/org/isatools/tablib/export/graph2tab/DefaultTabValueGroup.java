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
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * A default implementation for {@link TabValueGroup}. This simply keeps in memory an header, value and tail.
 *
 * <dl><dt>date</dt><dd>Jun 21, 2011</dd></dl>
 * @author brandizi
 *
 */
public class DefaultTabValueGroup implements TabValueGroup
{

	private final String header, value;
	private final List<TabValueGroup> tail = new ArrayList<TabValueGroup> ();

	public DefaultTabValueGroup ( String header, String value )
	{
		this.header = header;
		this.value = value;
	}

	/**
	 * Defines pair of header/value and append an existing one to it. This allows top-down definitions of nested groups
	 * (see examples).
	 * 
	 */
	public DefaultTabValueGroup ( String header, String value, TabValueGroup tailValueGroup )
	{
		this.header = header;
		this.value = value;
		this.append ( tailValueGroup );
	}

	public String getHeader ()
	{
		return header;
	}

	public String getValue ()
	{
		return value;
	}

	public List<TabValueGroup> getTail ()
	{
		return tail;
	}

	public void append ( TabValueGroup tail )
	{
		this.tail.add ( tail );
	}

	/**
	 * Uses header, value, tail, i.e. two structured columns are considered identical if they have the same 
	 * header and value, including the {@link #getTail()}.
	 * 
	 * This may be useful in rearrangement operations, before outputting the final table. 
	 */
	@Override
	public int hashCode ()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( header == null ) ? 0 : header.hashCode () );
		result = prime * result + ( ( value == null ) ? 0 : value.hashCode () );
		result = prime * result + ( ( tail == null ) ? 0 : tail.hashCode () );
		return result;
	}

	/**
	 * Uses header, value, tail, i.e. two structured columns are considered identical if they have the same 
	 * header and value, including the {@link #getTail()}.
	 * 
	 * This may be useful in rearrangement operations, before outputting the final table. 
	 */
	@Override
	public boolean equals ( Object obj )
	{
		if ( this == obj ) return true;
		if ( obj == null ) return false;
		if ( getClass () != obj.getClass () ) return false;
		DefaultTabValueGroup other = (DefaultTabValueGroup) obj;
		
		if ( header == null ) {
			if ( other.header != null ) return false;
		} 
		else if ( !header.equals ( other.header ) ) return false;
		
		if ( value == null ) {
			if ( other.value != null ) return false;
		}
		else if ( !value.equals ( other.value ) ) return false;

		if ( tail == null ) {
			if ( other.tail != null ) return false;
		} 
		else if ( !tail.equals ( other.tail ) ) return false;
		
		return true;
	}
	
}
