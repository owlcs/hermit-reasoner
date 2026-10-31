package org.semanticweb.HermiT.reasoner;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

//Unit tests for HermiT as a blackbox -- the quick subset.
@Tag("AllQuickTests")
class FloatDoubleTest extends AbstractReasonerTest {

    @Test void testINF() throws Exception {
        String axioms = "SubClassOf(:A DataAllValuesFrom(:dp owl:real))"
                + "SubClassOf(:A DataSomeValuesFrom(:dp DataOneOf(\"-INF\"^^xsd:float \"-0\"^^xsd:integer)))"
                + "ClassAssertion(:A :a)" + "NegativeDataPropertyAssertion(:dp :a \"0\"^^xsd:unsignedInt)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    @Test void testFloatRange() throws Exception {
        assertDRSatisfiable(false, DR("xsd:float"), NOT(DR("xsd:float")));
    }

    @Test void testFloatZeroRange_1() throws Exception {
        assertDRSatisfiable(true, DR("xsd:float", "xsd:minInclusive", FLT("+0"), "xsd:maxInclusive", FLT("+0")),
                OO(FLT("-0")));
    }

    @Test void testFloatZeroRange_2() throws Exception {
        assertDRSatisfiable(true, 2, DR("xsd:float", "xsd:minInclusive", FLT("+0"), "xsd:maxInclusive", FLT("+0")));
    }

    @Test void testFloatZeroRange_3() throws Exception {
        assertDRSatisfiable(false, 3, DR("xsd:float", "xsd:minInclusive", FLT("+0"), "xsd:maxInclusive", FLT("+0")));
    }

    @Test void testFloatRangeEnum() throws Exception {
        assertDRSatisfiableNEQ(false, S(FLT("-0"), FLT("+0")),
                DR("xsd:float", "xsd:minInclusive", FLT("+0"), "xsd:maxInclusive", FLT("+0")));
    }

    @Test void testFloatNaN_1() throws Exception {
        assertDRSatisfiable(true, DR("xsd:float"), OO(FLT("NaN")));
    }

    @Test void testFloatNaN_2() throws Exception {
        assertDRSatisfiable(false, DR("xsd:float", "xsd:minInclusive", FLT("2.0")), OO(FLT("NaN")));
    }

    @Test void testFloatNaN_3() throws Exception {
        assertDRSatisfiable(true, NOT(DR("xsd:float", "xsd:minInclusive", FLT("2.0"))), OO(FLT("NaN")));
    }

    @Test void testNumberOfFloats_1() throws Exception {
        assertDRSatisfiable(true, 5, DR("xsd:float"));
    }

    @Test void testNumberOfFloats_2() throws Exception {
        assertDRSatisfiable(true, 5,
                DR("xsd:float", "xsd:minInclusive", FLT("1.0"), "xsd:maxInclusive", FLT("1.0000005")));
    }

    @Test void testNumberOfFloats_3() throws Exception {
        assertDRSatisfiable(false, 6,
                DR("xsd:float", "xsd:minInclusive", FLT("1.0"), "xsd:maxInclusive", FLT("1.0000005")));
    }

    @Test void testFloatAndDouble() throws Exception {
        assertDRSatisfiable(false, DR("xsd:float"), DR("xsd:double"));
    }

    @Test void testDoubleRange() throws Exception {
        assertDRSatisfiable(false, DR("xsd:double"), NOT(DR("xsd:double")));
    }

    @Test void testDoubleZeroRange_1() throws Exception {
        assertDRSatisfiable(true, DR("xsd:double", "xsd:minInclusive", DBL("+0"), "xsd:maxInclusive", DBL("+0")),
                OO(DBL("-0")));
    }

    @Test void testDoubleZeroRange_2() throws Exception {
        assertDRSatisfiable(true, 2, DR("xsd:double", "xsd:minInclusive", DBL("+0"), "xsd:maxInclusive", DBL("+0")));
    }

    @Test void testDoubleZeroRange_3() throws Exception {
        assertDRSatisfiable(false, 3, DR("xsd:double", "xsd:minInclusive", DBL("+0"), "xsd:maxInclusive", DBL("+0")));
    }

    @Test void testDoubleRangeEnum() throws Exception {
        assertDRSatisfiableNEQ(false, S(DBL("-0"), DBL("+0")),
                DR("xsd:double", "xsd:minInclusive", DBL("+0"), "xsd:maxInclusive", DBL("+0")));
    }

    @Test void testDoubleNaN_1() throws Exception {
        assertDRSatisfiable(true, DR("xsd:double"), OO(DBL("NaN")));
    }

    @Test void testDoubleNaN_2() throws Exception {
        assertDRSatisfiable(false, DR("xsd:double", "xsd:minInclusive", DBL("2.0")), OO(DBL("NaN")));
    }

    @Test void testDoubleNaN_3() throws Exception {
        assertDRSatisfiable(true, NOT(DR("xsd:double", "xsd:minInclusive", DBL("2.0"))), OO(DBL("NaN")));
    }

    @Test void testNumberOfDoubles_1() throws Exception {
        assertDRSatisfiable(true, 5, DR("xsd:double"));
    }

    @Test void testNumberOfDoubles_2() throws Exception {
        assertDRSatisfiable(true, 5,
                DR("xsd:double", "xsd:minInclusive", DBL("1.0"), "xsd:maxInclusive", DBL("1.0000000000000009")));
    }

    @Test void testNumberOfDoubles_3() throws Exception {
        assertDRSatisfiable(false, 6,
                DR("xsd:double", "xsd:minInclusive", DBL("1.0"), "xsd:maxInclusive", DBL("1.0000000000000009")));
    }
}
