/* Copyright 2009 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.semanticweb.HermiT.owl_wg_tests;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asSet;

import java.io.File;
import java.time.Duration;

import org.semanticweb.HermiT.EntailmentChecker;
import org.semanticweb.HermiT.owl_wg_tests.WGTestDescriptor.TestType;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EntailmentTest extends AbstractTest {

    static List<String> slowCases = Arrays.asList(
            "WebOnt-description-logic-202",
            "WebOnt-description-logic-203",
            "WebOnt-description-logic-204", 
            "WebOnt-description-logic-206",
            "WebOnt-description-logic-208",
            "WebOnt-description-logic-209",
            "WebOnt-miscellaneous-011",
            "WebOnt-I5.8-004",
            "WebOnt-I5.8-005",
            "WebOnt-description-logic-661",
            "WebOnt-description-logic-662",
            "WebOnt-description-logic-663",
            "WebOnt-description-logic-664",
            "WebOnt-description-logic-903",
            "WebOnt-description-logic-904");

    protected boolean m_positive;
    protected OWLOntology m_conclusionOntology;

    @ParameterizedTest
    @MethodSource("params")
    void entailmentTest(WGTestDescriptor wgTestDescriptor, boolean positive, File dumpTestDataDirectory,
            boolean useDisjunctionLearning) throws Exception {
        assumeFalse(slowCases.contains(wgTestDescriptor.identifier));
        m_positive = positive;
        prepareTest(wgTestDescriptor.identifier + (positive ? "-entailment" : "-nonentailment"), wgTestDescriptor,
                dumpTestDataDirectory, useDisjunctionLearning);
        m_ontologyManager = OWLManager.createOWLOntologyManager();
        registerImportedReosurces();
        m_premiseOntology = m_wgTestDescriptor.getPremiseOntology(m_ontologyManager);
        m_conclusionOntology = m_wgTestDescriptor.getConclusionOntology(m_ontologyManager, m_positive);
        assertTimeoutPreemptively(Duration.ofSeconds(1), ()-> runTest(), wgTestDescriptor.identifier);
    }

    @AfterEach
    protected void tearDown() {
        super.tearDown();
        m_conclusionOntology = null;
    }

    @Override
    protected void doTest() {
        EntailmentChecker checker = new EntailmentChecker(m_reasoner, m_ontologyManager.getOWLDataFactory());
        boolean isEntailed = checker.entails(asSet(m_conclusionOntology.logicalAxioms()));
        if (m_positive)
            assertTrue( isEntailed,"Axioms should be entailed.");
        else
            assertTrue(!isEntailed, "At least one axiom should not be entailed by the premise ontology.");
    }

    @Override
    protected void dumpTestData() throws Exception {
        super.dumpTestData();
        if (m_dumpTestDataDirectory != null)
            saveOntology(m_conclusionOntology, new File(m_dumpTestDataDirectory, m_positive ? "conclusion.owl" : "nonconclusion.owl"));
    }

    static List<Arguments> params() throws Exception {
        
        List<Arguments> list=new ArrayList<>();
        list.addAll(AllApprovedWGTests.arguments(TestType.POSITIVE_ENTAILMENT));
        list.addAll(AllApprovedWGTests.arguments(TestType.NEGATIVE_ENTAILMENT));
        list.addAll(AllExtracreditWGTests.arguments(TestType.POSITIVE_ENTAILMENT));
        list.addAll(AllExtracreditWGTests.arguments(TestType.NEGATIVE_ENTAILMENT));
        list.addAll(AllNonRejectedNonExtracreditWGTests.arguments(TestType.POSITIVE_ENTAILMENT));
        list.addAll(AllNonRejectedNonExtracreditWGTests.arguments(TestType.NEGATIVE_ENTAILMENT));
        list.addAll(AllProposedWGTests.arguments(TestType.POSITIVE_ENTAILMENT));
        list.addAll(AllProposedWGTests.arguments(TestType.NEGATIVE_ENTAILMENT));
        list.addAll(AllWGTests.arguments(TestType.POSITIVE_ENTAILMENT));
        list.addAll(AllWGTests.arguments(TestType.NEGATIVE_ENTAILMENT));
        list.addAll(AllWGTestsWithoutStatus.arguments(TestType.POSITIVE_ENTAILMENT));
        list.addAll(AllWGTestsWithoutStatus.arguments(TestType.NEGATIVE_ENTAILMENT));
        list.addAll(AllWGTestsWithStatus.arguments(TestType.POSITIVE_ENTAILMENT));
        list.addAll(AllWGTestsWithStatus.arguments(TestType.NEGATIVE_ENTAILMENT));
        list.addAll(FailingWGTestDebug.arguments(TestType.POSITIVE_ENTAILMENT));
        list.addAll(FailingWGTestDebug.arguments(TestType.NEGATIVE_ENTAILMENT));
        return list;
    }
}
