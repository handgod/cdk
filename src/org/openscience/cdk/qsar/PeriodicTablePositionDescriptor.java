/*
 *  $RCSfile$
 *  $Author$
 *  $Date$
 *  $Revision$
 *
 *  Copyright (C) 2004-2005  The Chemistry Development Kit (CDK) project
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.openscience.cdk.qsar;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomType;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.qsar.result.*;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.graph.*;
import org.openscience.cdk.graph.matrix.*;
import java.util.Hashtable;

/**
 *  This class returns the period in the periodic table of an atom belonging to an atom container
 *
 *@author         mfe4
 *@cdk.created    2004-11-13
 *@cdk.module     qsar
 *@cdk.set        qsar-descriptors
 */
public class PeriodicTablePositionDescriptor implements Descriptor {

	private int targetPosition = 0;
	private LoggingTool logger;
	public Hashtable periodicTable;
	
	/**
	 *  Constructor for the PeriodicTablePositionDescriptor object
	 */
	public PeriodicTablePositionDescriptor() {
		logger = new LoggingTool(this);
				if (periodicTable == null) { 
		periodicTable = new Hashtable();
		periodicTable.put("H", new Integer(1));
		periodicTable.put("Li", new Integer(2));
		periodicTable.put("Be", new Integer(2));
		periodicTable.put("B", new Integer(2));
		periodicTable.put("C", new Integer(2));
		periodicTable.put("N", new Integer(2));
		periodicTable.put("O", new Integer(2));
		periodicTable.put("F", new Integer(2));
		periodicTable.put("Na", new Integer(3));
		periodicTable.put("Mg", new Integer(3));
		periodicTable.put("Al", new Integer(3));
		periodicTable.put("Si", new Integer(3));
		periodicTable.put("P", new Integer(3));
		periodicTable.put("S", new Integer(3));
		periodicTable.put("Cl", new Integer(3));
		periodicTable.put("K", new Integer(4));
		periodicTable.put("Ca", new Integer(4));
		periodicTable.put("Ga", new Integer(4));
		periodicTable.put("Ge", new Integer(4));
		periodicTable.put("As", new Integer(4));
		periodicTable.put("Se", new Integer(4));
		periodicTable.put("Br", new Integer(4));
		periodicTable.put("Rb", new Integer(5));
		periodicTable.put("Sr", new Integer(5));
		periodicTable.put("In", new Integer(5));
		periodicTable.put("Sn", new Integer(5));
		periodicTable.put("Sb", new Integer(5));
		periodicTable.put("Te", new Integer(5));
		periodicTable.put("I", new Integer(5));
		periodicTable.put("Cs", new Integer(6));
		periodicTable.put("Ba", new Integer(6));
		periodicTable.put("Tl", new Integer(6));
		periodicTable.put("Pb", new Integer(6));
		periodicTable.put("Bi", new Integer(6));
		periodicTable.put("Po", new Integer(6));
		periodicTable.put("At", new Integer(6));
		periodicTable.put("Fr", new Integer(7));
		periodicTable.put("Ra", new Integer(7));	    
	    }
	}


	/**
	 *  Gets the specification attribute of the PeriodicTablePositionDescriptor object
	 *
	 *@return    The specification value
	 */
	public DescriptorSpecification getSpecification() {
		return new DescriptorSpecification(
				"http://qsar.sourceforge.net/dicts/qsar-descriptors:period",
				this.getClass().getName(),
				"$Id$",
				"The Chemistry Development Kit");
	}


	/**
	 *  Sets the parameters attribute of the PeriodicTablePositionDescriptor object
	 *
	 *@param  params            The parameter is the atom position
	 *@exception  CDKException  Description of the Exception
	 */
	public void setParameters(Object[] params) throws CDKException {
		if (params.length > 1) {
			throw new CDKException("PeriodicTablePositionDescriptor only expects one parameter");
		}
		if (!(params[0] instanceof Integer)) {
			throw new CDKException("The parameter must be of type Integer");
		}
		targetPosition = ((Integer) params[0]).intValue();
	}


	/**
	 *  Gets the parameters attribute of the PeriodicTablePositionDescriptor object
	 *
	 *@return    The parameters value
	 */
	public Object[] getParameters() {
		Object[] params = new Object[1];
		params[0] = new Integer(targetPosition);
		return params;
	}


	/**
	 *  This method calculates the period of an atom.
	 *
	 *@param  container         Parameter is the atom container.
	 *@return                   The period
	 *@exception  CDKException  Description of the Exception
	 */

	public DescriptorValue calculate(AtomContainer container) throws CDKException {
		int period = 0;
		String symbol = container.getAtomAt(targetPosition).getSymbol();
		period = ((Integer)periodicTable.get(symbol)).intValue();
		return new DescriptorValue(getSpecification(), getParameterNames(), getParameters(), new IntegerResult(period));
	}


	/**
	 *  Gets the parameterNames attribute of the PeriodicTablePositionDescriptor object
	 *
	 *@return    The parameterNames value
	 */
	public String[] getParameterNames() {
		String[] params = new String[1];
		params[0] = "The position of the target atom";
		return params;
	}


	/**
	 *  Gets the parameterType attribute of the PeriodicTablePositionDescriptor object
	 *
	 *@param  name  Description of the Parameter
	 *@return       The parameterType value
	 */
	public Object getParameterType(String name) {
		Object[] paramTypes = new Object[1];
		paramTypes[0] = new Integer(1);
		return paramTypes;
	}
}

