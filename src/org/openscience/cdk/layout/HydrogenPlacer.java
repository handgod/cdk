/* $RCSfile$    
 * $Author$    
 * $Date$    
 * $Revision$
 * 
 * Copyright (C) 2003  The Chemistry Development Kit (CDK) project
 * 
 * Contact: cdk-devel@lists.sourceforge.net
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  
 */
package org.openscience.cdk.layout;

import org.openscience.cdk.*;
import org.openscience.cdk.ringsearch.*;
import org.openscience.cdk.geometry.*;
import org.openscience.cdk.tools.*;
import org.openscience.cdk.graph.PathTools;
import javax.vecmath.*;
import java.util.Vector;
import java.lang.Math;
import java.awt.*;

/**
 * This is a wrapper class for some existing methods in AtomPlacer. It helps
 * you to layout 2D and 3D coordinates for hydrogen atoms added to a molecule
 * which already has coordinates for the rest of the atoms.
 *
 * @author   Christop Steinbeck
 * @created  2003-08-06
 */
public class HydrogenPlacer {
	public static boolean debug = false;
	public static boolean debug1 = false;
	
	public static void placeHydrogens2D(AtomContainer atomContainer)
	{
        LoggingTool logger = new LoggingTool("org.openscience.cdk.layout.HydrogenPlacer");
		Atom atom = null; 
		for (int f = 0; f < atomContainer.getAtomCount();f++)
		{
			atom = atomContainer.getAtomAt(f);
			if (!atom.getSymbol().equals("H"))
			{
				if (debug1) System.out.println("Now placing hydrogens at atom " + f);
                logger.debug("Now placing hydrogens at atom " + f);
				placeHydrogens2D(atomContainer, atom);
			}
		}
	}
	
	public static void placeHydrogens2D(AtomContainer atomContainer, Atom atom)
	{
        LoggingTool logger = new LoggingTool("org.openscience.cdk.layout.HydrogenPlacer");
		double bondLength = GeometryTools.getScaleFactor(atomContainer, 1.0);
		double startAngle = 0.0;
		double addAngle = 0.0; 
		AtomPlacer atomPlacer = new AtomPlacer();
		atomPlacer.setMolecule((Molecule)atomContainer);
		Vector atomVector = new Vector();
		if (debug) System.out.println("bondLength" + bondLength);
        logger.debug("bondLength" + bondLength);
		Atom[] connectedAtoms = atomContainer.getConnectedAtoms(atom);
		AtomContainer placedAtoms = new AtomContainer();
		AtomContainer unplacedAtoms = new AtomContainer();
		for (int f = 0; f < connectedAtoms.length; f++)
		{
			if (connectedAtoms[f].getSymbol().equals("H"))
			{
				unplacedAtoms.addAtom(connectedAtoms[f]);
			}
			else
			{
				placedAtoms.addAtom(connectedAtoms[f]);
			}
		}

		if (placedAtoms.getAtomCount() > 1)
		{
			atomPlacer.distributePartners(atom, placedAtoms, placedAtoms.get2DCenter(), unplacedAtoms, bondLength);
		}
		else if (placedAtoms.getAtomCount() == 1)
		{
			for (int f = 0; f < unplacedAtoms.getAtomCount(); f++)
			{
				atomVector.addElement(unplacedAtoms.getAtomAt(f));
			}

			addAngle = Math.PI * 2 / unplacedAtoms.getAtomCount() - 1;
			/* IMPORTANT: At this point we need a calculation of the
			   start angle. 
			   Not done yet.
			   */
			atomPlacer.populatePolygonCorners(atomVector, new Point2d(atom.getPoint2D()), startAngle, addAngle, bondLength);	
		}
		
		if (debug) System.out.println("unplacedAtoms: " + unplacedAtoms);
		if (debug) System.out.println("placedAtoms: " + placedAtoms);
        logger.debug("unplacedAtoms: " + unplacedAtoms);
        logger.debug("placedAtoms: " + placedAtoms);
				
	}
}
