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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.HermiT.owl_wg_tests.WGTestDescriptor.TestType;
import org.semanticweb.owlapi.apibinding.OWLManager;

class ConsistencyTest extends AbstractTest {

    static List<String> slowCases = Arrays.asList(
            "WebOnt-miscellaneous-001",
            "WebOnt-miscellaneous-011",
            "WebOnt-I4.5-002",
            "WebOnt-I5.8-001",
            "WebOnt-I5.8-002",
            "WebOnt-I5.8-003",
            "WebOnt-I5.8-004",
            "WebOnt-I5.8-005",
            "WebOnt-unionOf-003",
            "WebOnt-disjointWith-007",
            "WebOnt-disjointWith-008",
            "WebOnt-description-logic-008",
            "WebOnt-description-logic-014",
            "WebOnt-description-logic-018",
            "WebOnt-description-logic-020",
            "WebOnt-description-logic-202",
            "WebOnt-description-logic-203",
            "WebOnt-description-logic-204",
            "WebOnt-description-logic-206",
            "WebOnt-description-logic-208",
            "WebOnt-description-logic-209",
            "WebOnt-description-logic-501",
            "WebOnt-description-logic-502",
            "WebOnt-description-logic-503",
            "WebOnt-description-logic-661",
            "WebOnt-description-logic-662",
            "WebOnt-description-logic-663",
            "WebOnt-description-logic-664",
            "WebOnt-description-logic-903",
            "WebOnt-description-logic-904",
            "WebOnt-description-logic-905",
            "WebOnt-description-logic-906",
            "WebOnt-description-logic-907",
            "WebOnt-description-logic-909",
            "WebOnt-description-logic-910");

    protected boolean m_positive;

    @ParameterizedTest
    @MethodSource("params")
    void consistencyTest(WGTestDescriptor wgTestDescriptor, boolean positive, File dumpTestDataDirectory,
            boolean useDisjunctionLearning) throws Exception {
        assumeFalse(slowCases.contains(wgTestDescriptor.identifier));
        m_positive = positive;
        prepareTest(wgTestDescriptor.identifier + (positive ? "-consistency" : "-inconsistency"), wgTestDescriptor,
                dumpTestDataDirectory, useDisjunctionLearning);
        m_ontologyManager = OWLManager.createOWLOntologyManager();
        registerImportedReosurces();
        m_premiseOntology = m_wgTestDescriptor.getPremiseOntology(m_ontologyManager);
        assertTimeoutPreemptively(Duration.ofSeconds(1), ()-> runTest(), wgTestDescriptor.identifier);
    }

    @Override
    protected void doTest() {
        assertEquals(m_positive, m_reasoner.isConsistent());
    }

    static List<Arguments> params() throws Exception {
        List<Arguments> list=new ArrayList<>();
        list.addAll(AllApprovedWGTests.arguments(TestType.CONSISTENCY));
        list.addAll(AllApprovedWGTests.arguments(TestType.INCONSISTENCY));
        list.addAll(AllExtracreditWGTests.arguments(TestType.CONSISTENCY));
        list.addAll(AllExtracreditWGTests.arguments(TestType.INCONSISTENCY));
        list.addAll(AllNonRejectedNonExtracreditWGTests.arguments(TestType.CONSISTENCY));
        list.addAll(AllNonRejectedNonExtracreditWGTests.arguments(TestType.INCONSISTENCY));
        list.addAll(AllProposedWGTests.arguments(TestType.CONSISTENCY));
        list.addAll(AllProposedWGTests.arguments(TestType.INCONSISTENCY));
        list.addAll(AllWGTests.arguments(TestType.CONSISTENCY));
        list.addAll(AllWGTests.arguments(TestType.INCONSISTENCY));
        list.addAll(AllWGTestsWithoutStatus.arguments(TestType.CONSISTENCY));
        list.addAll(AllWGTestsWithoutStatus.arguments(TestType.INCONSISTENCY));
        list.addAll(AllWGTestsWithStatus.arguments(TestType.CONSISTENCY));
        list.addAll(AllWGTestsWithStatus.arguments(TestType.INCONSISTENCY));
        list.addAll(FailingWGTestDebug.arguments(TestType.CONSISTENCY));
        list.addAll(FailingWGTestDebug.arguments(TestType.INCONSISTENCY));
        return list;
    }
}
