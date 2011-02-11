package org.openscience.cdk.applications.taverna.qsar.descriptors.molecular;

/* $RCSfile$
 * $Author: thomaskuhn $
 * $Date: 2008-05-30 14:32:43 +0200 (Fr, 30 Mai 2008) $
 * $Revision: 11221 $
 * 
 * Copyright (C) 2006 - 2007 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

import org.openscience.cdk.applications.taverna.qsar.AbstractMolecularDescriptor;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.IPMolecularLearningDescriptor;

/**
 * Class which provides the implementation for a cdk-taverna molecular qsar
 * descriptor.
 * 
 * @author Thomas Kuhn
 * 
 */
public class IPMolecularLearning extends AbstractMolecularDescriptor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openscience.cdk.applications.taverna.qsar.descriptors.bond.
	 * AbstractBondDescriptor#getDescriptor()
	 */
	public IMolecularDescriptor getDescriptor() {
		return new IPMolecularLearningDescriptor();
	}

}