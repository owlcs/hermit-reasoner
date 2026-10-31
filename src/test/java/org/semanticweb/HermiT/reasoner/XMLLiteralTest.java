package org.semanticweb.HermiT.reasoner;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

//Unit tests for HermiT as a blackbox -- the quick subset.
@Tag("AllQuickTests")
class XMLLiteralTest extends AbstractReasonerTest {

    @Test void testCanonicalization_1() throws Exception {
        assertDRSatisfiable(false, OO(XMLL("abc<a/>")), NOT(OO(XMLL("abc<a></a>"))));
    }

    @Test void testCanonicalization_2() throws Exception {
        assertDRSatisfiable(true, OO(XMLL("abc<a/>d")), NOT(OO(XMLL("abc<a></a>"))));
    }

    @Test void testRange_1() throws Exception {
        assertDRSatisfiable(true, 100, DR("rdf:XMLLiteral"));
    }

    @Test void testRange_2() throws Exception {
        assertDRSatisfiable(false, DR("rdf:XMLLiteral"), NOT(DR("rdf:XMLLiteral")));
    }

    @Test void testRange_3() throws Exception {
        assertDRSatisfiable(false, DR("rdf:XMLLiteral"), DR("xsd:boolean"));
    }

    @Test void testMembership_1() throws Exception {
        assertDRSatisfiable(true, DR("rdf:XMLLiteral"), OO(XMLL("<a>bla</a>")));
    }

    @Test void testMembership_2() throws Exception {
        assertDRSatisfiable(false, NOT(DR("rdf:XMLLiteral")), OO(XMLL("<a>bla</a>")));
    }
}
