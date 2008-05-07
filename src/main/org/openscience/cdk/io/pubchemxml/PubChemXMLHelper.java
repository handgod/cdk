/* $Revision$ $Author$ $Date$
 * 
 * Copyright (C) 2008  Egon Willighagen <egonw@users.sf.net>
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.io.pubchemxml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.xmlpull.v1.XmlPullParser;

/**
 * Helper class to parse PubChem XML documents.
 *
 * @cdk.module io
 * @cdk.svnrev $Revision$
 *
 * @author       Egon Willighagen <egonw@users.sf.net>
 * @cdk.created  2008-05-05
 */
public class PubChemXMLHelper {

	private IChemObjectBuilder builder;
	private IsotopeFactory factory;
	
	/**
     * @throws java.io.IOException if there is error in getting the {@link IsotopeFactory}
     */
	public PubChemXMLHelper(IChemObjectBuilder builder) throws IOException {
		this.builder = builder;
		factory = IsotopeFactory.getInstance(builder);
	};

	// general elements
	public final static String EL_PCCOMPOUND = "PC-Compound";
	public final static String EL_PCCOMPOUNDS = "PC-Compounds";
	public final static String EL_PCSUBSTANCE = "PC-Substance";
	public final static String EL_PCSUBSTANCE_SID = "PC-Substance_sid";
	public final static String EL_PCID_ID = "PC-ID_id";

	// atom block elements
	public final static String EL_ATOMBLOCK = "PC-Atoms";
	public final static String EL_ATOMSELEMENT = "PC-Atoms_element";
	public final static String EL_ELEMENT = "PC-Element";
	
	// bond block elements
	public final static String EL_BONDBLOCK = "PC-Bonds";
	public final static String EL_BONDID1 = "PC-Bonds_aid1";
	public final static String EL_BONDID2 = "PC-Bonds_aid2";
	public final static String EL_BONDORDER = "PC-Bonds_order";
	
    public IMoleculeSet parseCompoundsBlock(XmlPullParser parser) throws Exception {
    	IMoleculeSet set = builder.newMoleculeSet();
    	// assume the current element is PC-Compounds
    	if (!parser.getName().equals(EL_PCCOMPOUNDS)) {
    		return null;
    	}

    	while (parser.next() != XmlPullParser.END_DOCUMENT) {
    		if (parser.getEventType() == XmlPullParser.END_TAG) {
    			if (EL_PCCOMPOUNDS.equals(parser.getName())) {
    				break; // done parsing compounds block
    			}
    		} else if (parser.getEventType() == XmlPullParser.START_TAG) {
    			if (EL_PCCOMPOUND.equals(parser.getName())) {
    				IMolecule molecule = parseMolecule(parser, builder);
    				if (molecule.getAtomCount() > 0) {
    					// skip empty PC-Compound's
    					set.addMolecule(molecule);
    				}
    			}
    		}
    	}
		return set;
    }

    public IChemModel parseSubstance(XmlPullParser parser) throws Exception {
    	IChemModel model = builder.newChemModel();
    	// assume the current element is PC-Compound
    	if (!parser.getName().equals("PC-Substance")) {
    		return null;
    	}

    	while (parser.next() != XmlPullParser.END_DOCUMENT) {
    		if (parser.getEventType() == XmlPullParser.END_TAG) {
    			if (EL_PCSUBSTANCE.equals(parser.getName())) {
    				break; // done parsing the molecule
    			}
    		} else if (parser.getEventType() == XmlPullParser.START_TAG) {
    			if (EL_PCCOMPOUNDS.equals(parser.getName())) {
    				IMoleculeSet set = parseCompoundsBlock(parser);
    				model.setMoleculeSet(set);
    			} else if (EL_PCSUBSTANCE_SID.equals(parser.getName())) {
    				String sid = getSID(parser);
    				model.setProperty(CDKConstants.TITLE, sid);
    			}
    		}
    	}
		return model;
    }
	
	public String getSID(XmlPullParser parser) throws Exception {
		String sid = "unknown";
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() == XmlPullParser.END_TAG) {
    			if (EL_PCSUBSTANCE_SID.equals(parser.getName())) {
    				break; // done parsing the atom block
    			}
    		} else if (parser.getEventType() == XmlPullParser.START_TAG) {
    			if (EL_PCID_ID.equals(parser.getName())) {
    				sid = parser.nextText();
    			}
    		}
		}
	    return sid;
    }

	public void parseAtomElements(XmlPullParser parser, IMolecule molecule) throws Exception {
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() == XmlPullParser.END_TAG) {
    			if (EL_ATOMSELEMENT.equals(parser.getName())) {
    				break; // done parsing the atom elements
    			}
    		} else if (parser.getEventType() == XmlPullParser.START_TAG) {
    			if (EL_ELEMENT.equals(parser.getName())) {
    				int atomicNumber = Integer.parseInt(parser.nextText());
    				IElement element = factory.getElement(atomicNumber);
    				if (element == null) {
    					IAtom atom = molecule.getBuilder().newPseudoAtom();
    					molecule.addAtom(atom);
    				} else {
    					IAtom atom = molecule.getBuilder().newAtom(element.getSymbol());
    					atom.setAtomicNumber(element.getAtomicNumber());
    					molecule.addAtom(atom);
    				}
    			}
    		}
		}
	}
	
	public void parserAtomBlock(XmlPullParser parser, IMolecule molecule) throws Exception {
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() == XmlPullParser.END_TAG) {
    			if (EL_ATOMBLOCK.equals(parser.getName())) {
    				break; // done parsing the atom block
    			}
    		} else if (parser.getEventType() == XmlPullParser.START_TAG) {
    			if (EL_ATOMSELEMENT.equals(parser.getName())) {
    				parseAtomElements(parser, molecule);
    			}
    		}
		}
	}
	
    public IMolecule parseMolecule(XmlPullParser parser, IChemObjectBuilder builder) throws Exception {
    	IMolecule molecule = builder.newMolecule();
    	// assume the current element is PC-Compound
    	if (!parser.getName().equals("PC-Compound")) {
    		return null;
    	}

    	while (parser.next() != XmlPullParser.END_DOCUMENT) {
    		if (parser.getEventType() == XmlPullParser.END_TAG) {
    			if (EL_PCCOMPOUND.equals(parser.getName())) {
    				break; // done parsing the molecule
    			}
    		} else if (parser.getEventType() == XmlPullParser.START_TAG) {
    			if (EL_ATOMBLOCK.equals(parser.getName())) {
    				parserAtomBlock(parser, molecule);
    			} else if (EL_BONDBLOCK.equals(parser.getName())) {
    				parserBondBlock(parser, molecule);
    			}
    		}
    	}
		return molecule;
    }


	public void parserBondBlock(XmlPullParser parser, IMolecule molecule) throws Exception {
		List<String> id1s = new ArrayList<String>();
		List<String> id2s = new ArrayList<String>();
		List<String> orders = new ArrayList<String>();
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() == XmlPullParser.END_TAG) {
    			if (EL_BONDBLOCK.equals(parser.getName())) {
    				break; // done parsing the atom block
    			}
    		} else if (parser.getEventType() == XmlPullParser.START_TAG) {
    			if (EL_BONDID1.equals(parser.getName())) {
    				id1s = parseValues(parser, EL_BONDID1, "PC-Bonds_aid1_E");
    			} else if (EL_BONDID2.equals(parser.getName())) {
    				id2s = parseValues(parser, EL_BONDID2, "PC-Bonds_aid2_E");
    			} else if (EL_BONDORDER.equals(parser.getName())) {
    				orders = parseValues(parser, EL_BONDORDER, "PC-BondType");
    			}
    		}
		}
		// aggregate information
		if (id1s.size() != id2s.size()) {
			throw new CDKException("Inequal number of atom identifier in bond block.");
		}
		if (id1s.size() != orders.size()) {
			throw new CDKException("Number of bond orders does not match number of bonds in bond block.");
		}
		for (int i=0; i<id1s.size(); i++) {
			IAtom atom1 = molecule.getAtom(Integer.parseInt(id1s.get(i))-1);
			IAtom atom2 = molecule.getAtom(Integer.parseInt(id2s.get(i))-1);
			IBond bond = molecule.getBuilder().newBond(atom1, atom2);
			int order = Integer.parseInt(orders.get(i));
			if (order == 1) {
				bond.setOrder(IBond.Order.SINGLE);
				molecule.addBond(bond);
			} else if (order == 2) {
				bond.setOrder(IBond.Order.DOUBLE);
				molecule.addBond(bond);
			} if (order == 3) {
				bond.setOrder(IBond.Order.TRIPLE);
				molecule.addBond(bond);
			} else {
				// unknown bond order, skip
			}
		}
	}

	private List<String> parseValues(XmlPullParser parser, String endTag, String fieldTag) throws Exception {
		List<String> values = new ArrayList<String>();
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() == XmlPullParser.END_TAG) {
    			if (endTag.equals(parser.getName())) {
    				// done parsing the values
    				break;
    			}
    		} else if (parser.getEventType() == XmlPullParser.START_TAG) {
    			if (fieldTag.equals(parser.getName())) {
    				String value = parser.nextText();
    				values.add(value);
    			}
    		}
		}
		return values;
	}
	
}
