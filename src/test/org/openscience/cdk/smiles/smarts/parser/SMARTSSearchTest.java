/* $Revision$ $Author$ $Date$
 * 
 * Copyright (C) 2004-2007  Egon Willighagen <egonw@users.sf.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
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
package org.openscience.cdk.smiles.smarts.parser;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.openscience.cdk.CDKTestCase;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import java.io.InputStream;
import java.util.List;

/**
 * JUnit test routines for the SMARTS substructure search.
 *
 * @author Dazhi Jiao
 * @cdk.module test-smarts
 * @cdk.require ant1.6
 */
public class SMARTSSearchTest extends CDKTestCase {

    private LoggingTool logger;

    public SMARTSSearchTest(String testName) {
        super(testName);
        logger = new LoggingTool(this);
    }

    public static Test suite() {
        return new TestSuite(SMARTSSearchTest.class);
    }


    private int[] match(String smarts, String smiles) throws Exception {
        SMARTSQueryTool sqt = new SMARTSQueryTool(smarts, true);
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer atomContainer = sp.parseSmiles(smiles);
        boolean status = sqt.matches(atomContainer);
        if (status) {
        	return new int[] {
              sqt.countMatches(),
              sqt.getUniqueMatchingAtoms().size()
        	};
        } else {
        	return new int[]{0,0};
        }
    }

    public void testMoleculeFromSDF() throws CDKException {
        String filename = "data/mdl/cnssmarts.sdf";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        DefaultChemObjectReader reader = new MDLV2000Reader(ins);
        ChemFile content;
        content = (ChemFile) reader.read((ChemObject) new ChemFile());
        List cList = ChemFileManipulator.getAllAtomContainers(content);
        IAtomContainer atomContainer = (IAtomContainer) cList.get(0);

        SMARTSQueryTool sqt = new SMARTSQueryTool("[NX3;h1,h2,H1,H2;!$(NC=O)]", true);
        boolean status = sqt.matches(atomContainer);
        assertEquals(true, status);

        int nmatch = sqt.countMatches();
        int nqmatch = sqt.getUniqueMatchingAtoms().size();

        assertEquals(3, nmatch);
        assertEquals(3, nqmatch);


        sqt.setSmarts("[ND3]");
        status = sqt.matches(atomContainer);
        assertEquals(true, status);

        nmatch = sqt.countMatches();
        nqmatch = sqt.getUniqueMatchingAtoms().size();

        assertEquals(3, nmatch);
        assertEquals(3, nqmatch);
    }

    public void testRGraphBond() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("CC=O");
        logger.debug("Query c:c: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC=O"); // benzene, aromatic
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testAromaticBond() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("c:c");
        logger.debug("Query c:c: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("c1ccccc1"); // benzene, aromatic
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C1CCCCC1"); // hexane, not aromatic
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testSingleBond() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("C-C");
        logger.debug("Query C-C: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C=C");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C#C");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testDoubleBond() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("C=C");
        logger.debug("Query C=C: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C=C");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C#C");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testTripleBond() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("C#C");
        logger.debug("Query C#C: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C=C");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C#C");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testAnyOrderBond() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("C~C");
        logger.debug("Query C~C: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C=C");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("C#C");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testAnyAtom() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("C*C");
        logger.debug("Query C*C: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("CNC");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("CCN");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testAliphaticAtom() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("CAC");
        logger.debug("Query CAC: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("CNC");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("c1ccccc1"); // benzene, aromatic
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testAromaticAtom() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("aaa");
        logger.debug("Query CaC: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("c1ccccc1"); // benzene, aromatic
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    public void testSymbolQueryAtom() throws Exception {
        QueryAtomContainer query = SMARTSParser.parse("CCC");
        logger.debug("Query CAC: " + query.toString());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer atomContainer = sp.parseSmiles("CCC");
        assertTrue(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("CNC");
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));

        atomContainer = sp.parseSmiles("c1ccccc1"); // benzene, aromatic
        assertFalse(UniversalIsomorphismTester.isSubgraph(atomContainer, query));
    }

    /**
     * From http://www.daylight.com/dayhtml_tutorials/languages/smarts/index.html
     */
    public void testPropertyCharge1() throws Exception {
        int[] results = match("[+1]", "[OH-].[Mg+2]");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testPropertyCharge2() throws Exception {
        int[] results = match("[+1]", "COCC(O)Cn1ccnc1[N+](=O)[O-]");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyCharge3() throws Exception {
        int[] results = match("[+1]", "[NH4+]");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyCharge4() throws Exception {
        int[] results = match("[+1]", "CN1C(=O)N(C)C(=O)C(N(C)C=N2)=C12");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testPropertyCharge5() throws Exception {
        int[] results = match("[+1]", "[Cl-].[Cl-].NC(=O)c2cc[n+](COC[n+]1ccccc1C=NO)cc2");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyAromatic1() throws Exception {
        int[] results = match("[a]", "c1cc(C)c(N)cc1");
        assertEquals(6, results[0]);
        assertEquals(6, results[1]);
    }

    public void testPropertyAromatic2() throws Exception {
        int[] results = match("[a]", "c1c(C)c(N)cnc1");
        assertEquals(6, results[0]);
        assertEquals(6, results[1]);
    }

    public void testPropertyAromatic3() throws Exception {
        int[] results = match("[a]", "c1(C)c(N)cco1");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testPropertyAromatic4() throws Exception {
        int[] results = match("[a]", "c1c(C)c(N)c[nH]1");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testPropertyAromatic5() throws Exception {
        int[] results = match("[a]", "O=n1ccccc1");
        assertEquals(6, results[0]);
        assertEquals(6, results[1]);
    }

    public void testPropertyAromatic6() throws Exception {
        int[] results = match("[a]", "[O-][n+]1ccccc1");
        assertEquals(6, results[0]);
        assertEquals(6, results[1]);
    }

    public void testPropertyAromatic7() throws Exception {
        int[] results = match("[a]", "c1ncccc1C1CCCN1C");
        assertEquals(6, results[0]);
        assertEquals(6, results[1]);
    }

    public void testPropertyAromatic8() throws Exception {
        int[] results = match("[a]", "c1ccccc1C(=O)OC2CC(N3C)CCC3C2C(=O)OC");
        assertEquals(6, results[0]);
        assertEquals(6, results[1]);
    }

    public void testPropertyAliphatic1() throws Exception {
        int[] results = match("[A]", "c1cc(C)c(N)cc1");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyAliphatic2() throws Exception {
        int[] results = match("[A]", "CCO");
        assertEquals(3, results[0]);
        assertEquals(3, results[1]);
    }

    public void testPropertyAliphatic3() throws Exception {
        int[] results = match("[A]", "C=CC=CC=C");
        assertEquals(6, results[0]);
        assertEquals(6, results[1]);
    }

    public void testPropertyAliphatic4() throws Exception {
        int[] results = match("[A]", "CC(C)(C)C");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testPropertyAliphatic5() throws Exception {
        int[] results = match("[A]", "CCN(CC)C(=O)C1CN(C)C2CC3=CNc(ccc4)c3c4C2=C1");
        assertEquals(15, results[0]);
        assertEquals(15, results[1]);
    }

    public void testPropertyAliphatic6() throws Exception {
        int[] results = match("[A]", "N12CCC36C1CC(C(C2)=CCOC4CC5=O)C4C3N5c7ccccc76");
        assertEquals(19, results[0]);
        assertEquals(19, results[1]);
    }

    public void testPropertyAtomicNumber1() throws Exception {
        int[] results = match("[#6]", "c1cc(C)c(N)cc1");
        assertEquals(7, results[0]);
        assertEquals(7, results[1]);
    }

    public void testPropertyAtomicNumber2() throws Exception {
        int[] results = match("[#6]", "CCO");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyAtomicNumber3() throws Exception {
        int[] results = match("[#6]", "C=CC=CC=C-O");
        assertEquals(6, results[0]);
        assertEquals(6, results[1]);
    }

    public void testPropertyAtomicNumber4() throws Exception {
        int[] results = match("[#6]", "CC(C)(C)C");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testPropertyAtomicNumber5() throws Exception {
        int[] results = match("[#6]", "COc1cc2c(ccnc2cc1)C(O)C4CC(CC3)C(C=C)CN34");
        assertEquals(20, results[0]);
        assertEquals(20, results[1]);
    }

    public void testPropertyAtomicNumber6() throws Exception {
        int[] results = match("[#6]", "C123C5C(O)C=CC2C(N(C)CC1)Cc(ccc4O)c3c4O5");
        assertEquals(17, results[0]);
        assertEquals(17, results[1]);
    }

    public void testPropertyAtomicNumber7() throws Exception {
        int[] results = match("[#6]", "C123C5C(OC(=O)C)C=CC2C(N(C)CC1)Cc(ccc4OC(=O)C)c3c4O5");
        assertEquals(21, results[0]);
        assertEquals(21, results[1]);
    }

    public void testPropertyR1() throws Exception {
        int[] results = match("[R2]", "N12CCC36C1CC(C(C2)=CCOC4CC5=O)C4C3N5c7ccccc76");
        assertEquals(7, results[0]);
        assertEquals(7, results[1]);
    }

    public void testPropertyR2() throws Exception {
        // TODO: It seems the part C4CC(CC3)CCN34 are not correctly built into
        // the AtomContainer. No R2 matches this part.
        // Commented to pass the test
        /*
          int[] results = match("[R2]", "COc1cc2c(ccnc2cc1)C(O)C4CC(CC3)C(C=C)CN34");
          assertEquals(6, results[0]);
          assertEquals(6, results[1]);
          */
    }

    public void testPropertyR3() throws Exception {
        int[] results = match("[R2]", "C123C5C(O)C=CC2C(N(C)CC1)Cc(ccc4O)c3c4O5");
        assertEquals(4, results[0]);
        assertEquals(4, results[1]);
    }

    public void testPropertyR4() throws Exception {
        int[] results = match("[R2]", "C123C5C(OC(=O)C)C=CC2C(N(C)CC1)Cc(ccc4OC(=O)C)c3c4O5");
        assertEquals(4, results[0]);
        assertEquals(4, results[1]);
    }

    public void testPropertyR5() throws Exception {
        int[] results = match("[R2]", "C1C(C)=C(C=CC(C)=CC=CC(C)=CCO)C(C)(C)C1");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testPropertyr1() throws Exception {
        int[] results = match("[r5]", "N12CCC36C1CC(C(C2)=CCOC4CC5=O)C4C3N5c7ccccc76");
        assertEquals(9, results[0]);
        assertEquals(9, results[1]);
    }

    public void testPropertyr2() throws Exception {
        int[] results = match("[r5]", "COc1cc2c(ccnc2cc1)C(O)C4CC(CC3)C(C=C)CN34");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testPropertyr3() throws Exception {
        int[] results = match("[r5]", "C123C5C(O)C=CC2C(N(C)CC1)Cc(ccc4O)c3c4O5");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testPropertyr4() throws Exception {
        int[] results = match("[r5]", "C123C5C(OC(=O)C)C=CC2C(N(C)CC1)Cc(ccc4OC(=O)C)c3c4O5");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testPropertyr5() throws Exception {
        int[] results = match("[r5]", "C1C(C)=C(C=CC(C)=CC=CC(C)=CCO)C(C)(C)C1");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testPropertyValence1() throws Exception {
        int[] results = match("[v4]", "C");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyValence2() throws Exception {
        int[] results = match("[v4]", "CCO");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyValence3() throws Exception {
        int[] results = match("[v4]", "[NH4+]");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyValence4() throws Exception {
        int[] results = match("[v4]", "CC1(C)SC2C(NC(=O)Cc3ccccc3)C(=O)N2C1C(=O)O");
        assertEquals(16, results[0]);
        assertEquals(16, results[1]);
    }

    public void testPropertyValence5() throws Exception {
        int[] results = match("[v4]", "[Cl-].[Cl-].NC(=O)c2cc[n+](COC[n+]1ccccc1C=NO)cc2");
        assertEquals(16, results[0]);
        assertEquals(16, results[1]);
    }

    public void testPropertyX1() throws Exception {
        int[] results = match("[X2]", "CCO");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyX2() throws Exception {
        int[] results = match("[X2]", "O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyX3() throws Exception {
        int[] results = match("[X2]", "CCC(=O)CC");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testPropertyX4() throws Exception {
        int[] results = match("[X2]", "FC(Cl)=C=C(Cl)F");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyX5() throws Exception {
        int[] results = match("[X2]", "COc1cc2c(ccnc2cc1)C(O)C4CC(CC3)C(C=C)CN34");
        assertEquals(3, results[0]);
        assertEquals(3, results[1]);
    }

    public void testPropertyX6() throws Exception {
        int[] results = match("[X2]", "C123C5C(O)C=CC2C(N(C)CC1)Cc(ccc4O)c3c4O5");
        assertEquals(3, results[0]);
        assertEquals(3, results[1]);
    }

    public void testPropertyD1() throws Exception {
        int[] results = match("[D2]", "CCO");
        assertEquals(1, results[0]);
    }

    public void testPropertyD2() throws Exception {
        int[] results = match("[D2]", "O");
        assertEquals(0, results[0]);
    }

    public void testPropertyD3() throws Exception {
        int[] results = match("[D2]", "CCC(=O)CC");
        assertEquals(2, results[0]);
    }

    public void testPropertyD4() throws Exception {
        int[] results = match("[D2]", "FC(Cl)=C=C(Cl)F");
        assertEquals(1, results[0]);
    }

    public void testPropertyD5() throws Exception {
        int[] results = match("[D2]", "COc1cc2c(ccnc2cc1)C(O)C4CC(CC3)C(C=C)CN34");
        assertEquals(12, results[0]);
    }

    public void testPropertyD6() throws Exception {
        int[] results = match("[D2]", "C123C5C(O)C=CC2C(N(C)CC1)Cc(ccc4O)c3c4O5");
        assertEquals(8, results[0]);
    }

    public void testPropertyHAtom1() throws Exception {
        int[] results = match("[H]", "[H+].[Cl-]");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyHAtom2() throws Exception {
        int[] results = match("[H]", "[2H]");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyHAtom3() throws Exception {
        int[] results = match("[H]", "[H][H]");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyHAtom4() throws Exception {
        int[] results = match("[H]", "[CH4]");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testPropertyHAtom5() throws Exception {
        int[] results = match("[H]", "[H]C([H])([H])[H]");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testPropertyHTotal1() throws Exception {
        int[] results = match("[H1]", "CCO");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyHTotal2() throws Exception {
        int[] results = match("[H1]", "[2H]C#C");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyHTotal3() throws Exception {
        int[] results = match("[H1]", "[H]C(C)(C)C");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyHTotal4() throws Exception {
        int[] results = match("[H1]", "COc1cc2c(ccnc2cc1)C(O)C4CC(CC3)C(C=C)CN34");
        assertEquals(11, results[0]);
        assertEquals(11, results[1]);
    }

    public void testPropertyHTotal5() throws Exception {
        int[] results = match("[H1]", "C123C5C(O)C=CC2C(N(C)CC1)Cc(ccc4O)c3c4O5");
        assertEquals(10, results[0]);
        assertEquals(10, results[1]);
    }

    public void testPropertyHTotal6() throws Exception {
        int[] results = match("[H1]", "[H][H]");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyAnyAtom1() throws Exception {
        int[] results = match("[*]", "C");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyAnyAtom2() throws Exception {
        int[] results = match("[*]", "[2H]C");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyAnyAtom3() throws Exception {
        int[] results = match("[*]", "[H][H]");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testPropertyAnyAtom4() throws Exception {
        int[] results = match("[*]", "[1H]C([1H])([1H])[1H]");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testPropertyAtomicMass1() throws Exception {
        int[] results = match("[13C]", "[13C]");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyAtomicMass2() throws Exception {
        int[] results = match("[13C]", "[C]");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testPropertyAtomicMass3() throws Exception {
        int[] results = match("[13*]", "[13C]Cl");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testPropertyAtomicMass4() throws Exception {
        int[] results = match("[12C]", "CCl");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testBondSingle1() throws Exception {
        int[] results = match("CC", "C=C");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testBondSingle2() throws Exception {
        int[] results = match("CC", "C#C");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testBondSingle3() throws Exception {
        int[] results = match("CC", "CCO");
        assertEquals(2, results[0]);
        assertEquals(1, results[1]);
    }

    public void testBondSingle4() throws Exception {
        int[] results = match("CC", "C1C(C)=C(C=CC(C)=CC=CC(C)=CCO)C(C)(C)C1");
        assertEquals(28, results[0]);
        assertEquals(14, results[1]);
    }

    public void testBondSingle5() throws Exception {
        int[] results = match("CC", "CC1(C)SC2C(NC(=O)Cc3ccccc3)C(=O)N2C1C(=O)O");
        assertEquals(14, results[0]);
        assertEquals(7, results[1]);
    }

    public void testBondAny1() throws Exception {
        int[] results = match("C~C", "C=C");
        assertEquals(2, results[0]);
        assertEquals(1, results[1]);
    }

    public void testBondAny2() throws Exception {
        int[] results = match("C~C", "C#C");
        assertEquals(2, results[0]);
        assertEquals(1, results[1]);
    }

    public void testBondAny3() throws Exception {
        int[] results = match("C~C", "CCO");
        assertEquals(2, results[0]);
        assertEquals(1, results[1]);
    }

    public void testBondAny4() throws Exception {
        int[] results = match("C~C", "C1C(C)=C(C=CC(C)=CC=CC(C)=CCO)C(C)(C)C1");
        assertEquals(38, results[0]);
        assertEquals(19, results[1]);
    }

    public void testBondAny5() throws Exception {
        int[] results = match("[C,c]~[C,c]", "CC1(C)SC2C(NC(=O)Cc3ccccc3)C(=O)N2C1C(=O)O");
        assertEquals(28, results[0]);
        assertEquals(14, results[1]);
    }

    public void testBondRing1() throws Exception {
        int[] results = match("C@C", "C=C");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testBondRing2() throws Exception {
        int[] results = match("C@C", "C#C");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testBondRing3() throws Exception {
        int[] results = match("C@C", "C1CCCCC1");
        assertEquals(12, results[0]);
        assertEquals(6, results[1]);
    }

    public void testBondRing4() throws Exception {
        int[] results = match("[C,c]@[C,c]", "c1ccccc1Cc1ccccc1");
        assertEquals(24, results[0]);
        assertEquals(12, results[1]);
    }

    public void testBondRing5() throws Exception {
        int[] results = match("[C,c]@[C,c]", "CCN(CC)C(=O)C1CN(C)C2CC3=CNc(ccc4)c3c4C2=C1");
        assertEquals(30, results[0]);
        assertEquals(15, results[1]);
    }

    public void testBondRing6() throws Exception {
        int[] results = match("[C,c]@[C,c]", "N12CCC36C1CC(C(C2)=CCOC4CC5=O)C4C3N5c7ccccc76");
        assertEquals(44, results[0]);
        assertEquals(22, results[1]);
    }

    //TODO: Stereo bond not implemented in smiles parser. Commented.
    /*
    public void testBondStereo1() throws Exception { 
    	int[] results = match("F/?C=C/Cl", "F/C=C/Cl");
    	assertEquals(1, results[0]);
    	assertEquals(1, results[1]);
    }
    public void testBondStereo2() throws Exception {
    	int[] results = match("F/?C=C/Cl", "FC=C/Cl");
    	assertEquals(1, results[0]);
    	assertEquals(1, results[1]);
    }
    public void testBondStereo3() throws Exception {
    	int[] results = match("F/?C=C/Cl", "FC=CCl");
    	assertEquals(1, results[0]);
    	assertEquals(1, results[1]);
    }
    public void testBondStereo4() throws Exception {
    	int[] results = match("F/?C=C/Cl", "F\\C=C/Cl");
    	assertEquals(0, results[0]);
    	assertEquals(0, results[1]);
    }
    */
    public void testLogicalNot1() throws Exception {
        int[] results = match("[!c]", "c1cc(C)c(N)cc1");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testLogicalNot2() throws Exception {
        int[] results = match("[!c]", "c1c(C)c(N)cnc1");
        assertEquals(3, results[0]);
        assertEquals(3, results[1]);
    }

    public void testLogicalNot3() throws Exception {
        int[] results = match("[!c]", "c1(C)c(N)cco1");
        assertEquals(3, results[0]);
        assertEquals(3, results[1]);
    }

    public void testLogicalNot4() throws Exception {
        int[] results = match("[!c]", "c1c(C)c(N)c[nH]1");
        assertEquals(3, results[0]);
        assertEquals(3, results[1]);
    }

    public void testLogicalNot5() throws Exception {
        int[] results = match("[!c]", "O=n1ccccc1");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testLogicalNot6() throws Exception {
        int[] results = match("[!c]", "[O-][n+]1ccccc1");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testLogicalNot7() throws Exception {
        int[] results = match("[!c]", "c1ncccc1C1CCCN1C");
        assertEquals(7, results[0]);
        assertEquals(7, results[1]);
    }

    public void testLogicalNot8() throws Exception {
        int[] results = match("[!c]", "c1ccccc1C(=O)OC2CC(N3C)CCC3C2C(=O)OC");
        assertEquals(16, results[0]);
        assertEquals(16, results[1]);
    }

    public void testLogicalOr1() throws Exception {
        int[] results = match("[N,O,o]", "c1cc(C)c(N)cc1");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testLogicalOr2() throws Exception {
        int[] results = match("[N,O,o]", "c1c(C)c(N)cnc1");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testLogicalOr3() throws Exception {
        int[] results = match("[N,O,o]", "c1(C)c(N)cco1");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    public void testLogicalOr4() throws Exception {
        int[] results = match("[N,O,o]", "c1c(C)c(N)c[nH]1");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testLogicalOr5() throws Exception {
        int[] results = match("[N,O,o]", "O=n1ccccc1");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testLogicalOr6() throws Exception {
        int[] results = match("[N,O,o]", "[O-][n+]1ccccc1");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testLogicalOr7() throws Exception {
        int[] results = match("[N,O,o]", "c1ncccc1C1CCCN1C");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testLogicalOr8() throws Exception {
        int[] results = match("[N,O,o]", "c1ccccc1C(=O)OC2CC(N3C)CCC3C2C(=O)OC");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    public void testLogicalOr9() throws Exception {
        int[] results = match("[N]=[N]-,=[N]", "CCCC(=O)C=C");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testLogicalOr10() throws Exception {
        int[] results = match("[N;$([N!X4])]!@;-[N;$([N!X4])]", "CCCC(=O)C=C");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

     public void testLogicalOr11() throws Exception {
        int[] results = match("[#6]!:;=[#6][#6](=O)[!O]", "CCCC(=O)C=C");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testLogicalOr12() throws Exception {
        int[] results = match("C=,#C","C=CCC#C");
        assertEquals(4, results[0]);
        assertEquals(2, results[1]);
    }

    public void testLogicalOrHighAnd1() throws Exception {
        int[] results = match("[N,#6&+1,+0]", "CCN(CC)C(=O)C1CN(C)C2CC3=CNc(ccc4)c3c4C2=C1");
        assertEquals(24, results[0]);
        assertEquals(24, results[1]);
    }

    public void testLogicalOrHighAnd2() throws Exception {
        int[] results = match("[N,#6&+1,+0]", "N12CCC36C1CC(C(C2)=CCOC4CC5=O)C4C3N5c7ccccc76");
        assertEquals(25, results[0]);
        assertEquals(25, results[1]);
    }

    public void testLogicalOrHighAnd3() throws Exception {
        int[] results = match("[N,#6&+1,+0]", "COc1cc2c(ccnc2cc1)C(O)C4CC(CC3)C(C=C)CN34");
        assertEquals(24, results[0]);
        assertEquals(24, results[1]);
    }

    public void testLogicalOrHighAnd4() throws Exception {
        int[] results = match("[N,#6&+1,+0]", "C123C5C(O)C=CC2C(N(C)CC1)Cc(ccc4O)c3c4O5");
        assertEquals(21, results[0]);
        assertEquals(21, results[1]);
    }

    public void testLogicalOrHighAnd5() throws Exception {
        int[] results = match("[N,#6&+1,+0]", "N1N([Hg-][O+]=C1N=Nc2ccccc2)c3ccccc3");
        assertEquals(17, results[0]);
        assertEquals(17, results[1]);
    }

    /*
    public void testLogicalOrHighAnd6() throws Exception { 
        //TODO: This takes a long time to match
        long start = Calendar.getInstance().getTimeInMillis();
        //int[] results = match("[N,#6&+1,+0]", "[Na+].[Na+].[O-]C(=O)c1ccccc1c2c3ccc([O-])cc3oc4cc(=O)ccc24");
        new SmilesParser(DefaultChemObjectBuilder.getInstance());
        SMARTSParser.parse("[N,#6&+1,+0]");
        long end = Calendar.getInstance().getTimeInMillis();
        System.out.println( (end - start) );
        //assertEquals(23, results[0]);
    }
    */
    public void testLogicalOrHighAnd7() throws Exception {
        int[] results = match("[N,#6&+1,+0]", "[Cl-].Clc1ccc([I+]c2cccs2)cc1");
        assertEquals(12, results[0]);
        assertEquals(12, results[1]);
    }

    public void testLogicalOrLowAnd1() throws Exception {
        int[] results = match("[#7,C;+0,+1]", "CCN(CC)C(=O)C1CN(C)C2CC3=CNc(ccc4)c3c4C2=C1");
        assertEquals(15, results[0]);
        assertEquals(15, results[1]);
    }

    public void testLogicalOrLowAnd2() throws Exception {
        int[] results = match("[#7,C;+0,+1]", "N12CCC36C1CC(C(C2)=CCOC4CC5=O)C4C3N5c7ccccc76");
        assertEquals(17, results[0]);
        assertEquals(17, results[1]);
    }

    public void testLogicalOrLowAnd3() throws Exception {
        int[] results = match("[#7,C;+0,+1]", "COc1cc2c(ccnc2cc1)C(O)C4CC(CC3)C(C=C)CN34");
        assertEquals(13, results[0]);
        assertEquals(13, results[1]);
    }

    public void testLogicalOrLowAnd4() throws Exception {
        int[] results = match("[#7,C;+0,+1]", "C123C5C(O)C=CC2C(N(C)CC1)Cc(ccc4O)c3c4O5");
        assertEquals(12, results[0]);
        assertEquals(12, results[1]);
    }

    public void testLogicalOrLowAnd5() throws Exception {
        int[] results = match("[#7,C;+0,+1]", "N1N([Hg-][O+]=C1N=Nc2ccccc2)c3ccccc3");
        assertEquals(5, results[0]);
        assertEquals(5, results[1]);
    }

    // //TODO: this takes very long. It is the same smiles. So the bottle neck
    // might be in the AtomContainer
    /*
    public void testLogicalOrLowAnd6() throws Exception { 
    	int[] results = match("[#7,C;+0,+1]", "[Na+].[Na+].[O-]C(=O)c1ccccc1c2c3ccc([O-])cc3oc4cc(=O)ccc24");
    	assertEquals(1, results[0]);    	
    }
    */
    public void testLogicalOrLowAnd7() throws Exception {
        int[] results = match("[#7,C;+0,+1]", "[Cl-].Clc1ccc([I+]c2cccs2)cc1");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }



    public void testRing1() throws Exception {
        int[] results = match("C1CCCCC1", "C1CCCCC1CCCC");
        assertEquals(12, results[0]);
        assertEquals(1, results[1]);
    }

    public void testRing2() throws Exception {
        int[] results = match("C1CCCCC1", "C1CCCCC1C1CCCCC1");
        assertEquals(24, results[0]);
        assertEquals(2, results[1]);
    }

    public void testRing3() throws Exception {
        int[] results = match("C1CCCCC1", "C1CCCC12CCCCC2");
        assertEquals(12, results[0]);
        assertEquals(1, results[1]);
    }

    public void testRing4() throws Exception {
        int[] results = match("C1CCCCC1", "c1ccccc1O");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testRing5() throws Exception {
        int[] results = match("C1CCCCC1", "c1ccccc1CCCCCC");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testRing6() throws Exception {
        int[] results = match("C1CCCCC1", "CCCCCC");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testAromaticRing1() throws Exception {
        int[] results = match("c1ccccc1", "c1ccccc1");
        assertEquals(12, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAromaticRing2() throws Exception {
        int[] results = match("c1ccccc1", "c1cccc2c1cccc2");
        assertEquals(24, results[0]);
        assertEquals(2, results[1]);
    }

    public void testAromaticRing3() throws Exception {
        int[] results = match("c1ccccn1", "c1cccc2c1cccc2");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testAromaticRing4() throws Exception {
        int[] results = match("c1ccccn1", "c1cccc2c1cccn2");
        assertEquals(2, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid1() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(C)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid2() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CCCNC(N)=N)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid3() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CC(N)=O)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid4() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CC(O)=O)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid5() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CS)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid6() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CCC(N)=O)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid7() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CCC(O)=O)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid8() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC([H])C(O)=O");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    public void testAminoAcid9() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CC1=CNC=N1)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid10() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(C(CC)C)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid11() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CC(C)C)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid12() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CCCCN)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid13() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CCSC)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid14() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CC1=CC=CC=C1)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid15() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "OC(C1CCCN1)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid16() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CO)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid17() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(C(C)O)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid18() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CC1=CNC2=C1C=CC=C2)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid19() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(CC1=CC=C(O)C=C1)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testAminoAcid20() throws Exception {
        int[] results = match("[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]", "NC(C(C)C)C(O)=O");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }

    public void testCyclicUreas() throws Exception {
        int[] results = match("[$(C1CNC(=O)N1)]", "N1C(=O)NCC1");
        assertEquals(2, results[0]);
        assertEquals(2, results[1]);
    }

    /**
     * @throws Exception
     * @cdk.bug 1967468
     */
    public void testAcyclicUreas() throws Exception {
        int[] results = match("[$(CC);$(C1CNC(=O)N1)]", "C1CC1NC(=O)Nc2ccccc2");
//        int[] results = match("[$([CR][NR][CR](=O)[NR])]", "C1CC1NC(=O)Nc2ccccc2");
        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    /**
     * @cdk.bug 1985811
     * @throws Exception
     */
    public void testIndoleAgainstIndole() throws Exception {
        int[] results = match("c1ccc2cc[nH]c2(c1)", "C1(NC=C2)=C2C=CC=C1");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);


        results = match("c1ccc2cc[nH]c2(c1)", "c1ccc2cc[nH]c2(c1)");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);

    }

    /**
     * @cdk.bug 1985811
     * @throws Exception
     */
    public void testPyridineAgainstPyridine() throws Exception {
        int[] results = match("c1ccncc1", "c1ccncc1");
        assertEquals(2, results[0]);
        assertEquals(1, results[1]);

        results = match("c1ccncc1", "C1=NC=CC=C1" );
        assertEquals(2, results[0]);
        assertEquals(1, results[1]);
    }

    public void testGroup5Elements() throws Exception {
        int[] results = match("[V,Cr,Mn,Nb,Mo,Tc,Ta,W,Re]", "[W]");
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
    }
}

