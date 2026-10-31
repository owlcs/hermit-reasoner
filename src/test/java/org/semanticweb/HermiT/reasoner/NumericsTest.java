package org.semanticweb.HermiT.reasoner;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

//Unit tests for HermiT as a blackbox -- the quick subset.
@Tag("AllQuickTests")
class NumericsTest extends AbstractReasonerTest {

    @Test void testIntegerRange1() throws Exception {
        assertDRSatisfiable(false, DR("xsd:int"), NOT(DR("xsd:integer")));
    }

    @Test void testIntegerRange2_1() throws Exception {
        assertDRSatisfiable(true, NOT(DR("xsd:int")), DR("xsd:integer"));
    }

    @Test void testIntegerRange2_2() throws Exception {
        assertDRSatisfiable(false, NOT(DR("xsd:int")), DR("xsd:integer"), OO(INT("0")));
    }

    @Test void testIntegerRange2_3() throws Exception {
        assertDRSatisfiable(false, NOT(DR("xsd:int")), DR("xsd:integer"), OO(INT("2147483647")));
    }

    @Test void testIntegerRange2_4() throws Exception {
        assertDRSatisfiable(true, NOT(DR("xsd:int")), DR("xsd:integer"), OO(INT("2147483648")));
    }

    @Test void testDecimalNotInteger_1() throws Exception {
        assertDRSatisfiable(false, DR("xsd:decimal"), NOT(DR("xsd:integer")), OO(INT("2147483648")));
    }

    @Test void testDecimalNotInteger_2() throws Exception {
        assertDRSatisfiable(false, DR("xsd:decimal"), NOT(DR("xsd:integer")), OO(DEC("2147483648.0")));
    }

    @Test void testDecimalNotInteger_3() throws Exception {
        assertDRSatisfiable(true, DR("xsd:decimal"), NOT(DR("xsd:integer")), OO(DEC("2147483648.1")));
    }

    @Test void testRealNotDecimal() throws Exception {
        assertDRSatisfiable(true, DR("owl:real"), NOT(DR("xsd:decimal")), OO(RAT("1", "3")));
    }

    @Test void testRealNotDecimal_2() throws Exception {
        assertDRSatisfiable(false, DR("owl:real"), NOT(DR("xsd:decimal")), OO(RAT("5", "2")));
    }

    @Test void testMinInclusiveInt() throws Exception {
        assertDRSatisfiable(false, DR("xsd:integer", "xsd:minInclusive", DEC("2.2")), OO(INT("2")));
    }

    @Test void testMinInclusiveInt_2() throws Exception {
        assertDRSatisfiable(true, DR("xsd:integer", "xsd:minInclusive", DEC("2.2")), OO(INT("3")));
    }

    @Test void testMinExclusiveDec() throws Exception {
        assertDRSatisfiable(false, DR("xsd:decimal", "xsd:minExclusive", DEC("2.2")), OO(DEC("2.2")));
    }

    @Test void testMaxInclusiveInt() throws Exception {
        assertDRSatisfiable(false, DR("xsd:integer", "xsd:maxInclusive", DEC("2.2")), OO(INT("3")));
    }

    @Test void testMaxInclusiveInt_2() throws Exception {
        assertDRSatisfiable(true, DR("xsd:integer", "xsd:maxInclusive", DEC("2.2")), OO(INT("2")));
    }

    @Test void testMaxExclusiveDec() throws Exception {
        assertDRSatisfiable(false, DR("xsd:decimal", "xsd:maxExclusive", DEC("2.2")), OO(DEC("2.2")));
    }

    @Test void testEnumInt_1() throws Exception {
        assertDRSatisfiable(true, DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2")),
                NOT(OO(INT("3"), INT("4"))));
    }

    @Test void testEnumInt_2() throws Exception {
        assertDRSatisfiable(false, DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2")),
                NOT(OO(INT("3"), INT("4"), INT("5"))));
    }

    @Test void testEnumInt_3() throws Exception {
        assertDRSatisfiable(true, DR("xsd:integer", "xsd:minInclusive", DEC("2.2")),
                NOT(OO(INT("3"), INT("4"), INT("5"), INT("6"), INT("7"), INT("8"))));
    }

    @Test void testEnumInt_4() throws Exception {
        assertDRSatisfiable(true, DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", INT("10000")),
                NOT(OO(INT("3"), INT("4"), INT("5"), INT("6"), INT("7"), INT("8"))));
    }

    @Test void testEnumInt_5() throws Exception {
        assertDRSatisfiable(true,
                DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive",
                        INT("1000000000000000000000000000000000000000")),
                NOT(OO(INT("3"), INT("4"), INT("5"), INT("6"), INT("7"), INT("8"))));
    }

    @Test void testEnumIntNEQ_1() throws Exception {
        assertDRSatisfiableNEQ(true, S(INT("3"), INT("4")),
                DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2")));
    }

    @Test void testEnumIntNEQ_2() throws Exception {
        assertDRSatisfiableNEQ(false, S(INT("3"), INT("4"), INT("5")),
                DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2")));
    }

    @Test void testEnumIntNEQ_3() throws Exception {
        assertDRSatisfiableNEQ(true, S(INT("3"), INT("4"), INT("5"), INT("6"), INT("7"), INT("8")),
                DR("xsd:integer", "xsd:minInclusive", DEC("2.2")));
    }

    @Test void testEnumIntNEQ_4() throws Exception {
        assertDRSatisfiableNEQ(true, S(INT("3"), INT("4"), INT("5"), INT("6"), INT("7"), INT("8")),
                DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", INT("10000")));
    }

    @Test void testEnumIntNEQ_5() throws Exception {
        assertDRSatisfiableNEQ(true, S(INT("3"), INT("4"), INT("5"), INT("6"), INT("7"), INT("8")), DR("xsd:integer",
                "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", INT("1000000000000000000000000000000000000000")));
    }

    @Test void testMinMaxEqual_1() throws Exception {
        assertDRSatisfiable(false, DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("2.2")));
    }

    @Test void testMinMaxEqual_2() throws Exception {
        assertDRSatisfiable(true, DR("xsd:decimal", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("2.2")));
    }

    @Test void testMinMaxEqual_3() throws Exception {
        assertDRSatisfiable(false,
                DR("xsd:decimal", "xsd:minInclusive", RAT("1", "3"), "xsd:maxInclusive", RAT("1", "3")));
    }

    @Test void testMinMaxEqual_4() throws Exception {
        assertDRSatisfiable(false, DR("xsd:decimal", "xsd:minExclusive", DEC("2.2"), "xsd:maxInclusive", DEC("2.2")));
    }

    @Test void testInvalidMinMax() throws Exception {
        assertDRSatisfiable(false, DR("xsd:integer", "xsd:minInclusive", DEC("5.2"), "xsd:maxInclusive", DEC("2.2")));
    }

    @Test void testDecimalMinusInt_1() throws Exception {
        assertDRSatisfiable(false, DR("xsd:decimal", "xsd:minInclusive", DEC("1.2"), "xsd:maxInclusive", DEC("7.2")),
                NOT(DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2"))), OO(INT("4")));
    }

    @Test void testDecimalMinusInt_2() throws Exception {
        assertDRSatisfiable(true, DR("xsd:decimal", "xsd:minInclusive", DEC("1.2"), "xsd:maxInclusive", DEC("7.2")),
                NOT(DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2"))), DR("xsd:int"));
    }

    @Test void testDecimalMinusInt_3() throws Exception {
        assertDRSatisfiable(true, DR("xsd:decimal", "xsd:minInclusive", DEC("1.2"), "xsd:maxInclusive", DEC("7.2")),
                NOT(DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2"))), DR("xsd:int"),
                NOT(OO(DEC("6.0"), DEC("7.0"))));
    }

    @Test void testDecimalMinusInt_4() throws Exception {
        assertDRSatisfiable(false, DR("xsd:decimal", "xsd:minInclusive", DEC("1.2"), "xsd:maxInclusive", DEC("7.2")),
                NOT(DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2"))), DR("xsd:int"),
                NOT(OO(INT("2"), DEC("6.0"), DEC("7.0"))));
    }

    @Test void testDecimalMinusIntNEQ_1() throws Exception {
        assertDRSatisfiableNEQ(true, S(DEC("6.0"), DEC("7.0")),
                DR("xsd:decimal", "xsd:minInclusive", DEC("1.2"), "xsd:maxInclusive", DEC("7.2")),
                NOT(DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2"))), DR("xsd:int"));
    }

    @Test void testDecimalMinusIntNEQ_2() throws Exception {
        assertDRSatisfiableNEQ(true, S(DEC("6.0"), DEC("7.0")),
                DR("xsd:decimal", "xsd:minInclusive", DEC("1.2"), "xsd:maxInclusive", DEC("7.2")),
                NOT(DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2"))), DR("xsd:int"));
    }

    @Test void testDecimalMinusIntNEQ_3() throws Exception {
        assertDRSatisfiableNEQ(false, S(INT("2"), DEC("6.0"), DEC("7.0")),
                DR("xsd:decimal", "xsd:minInclusive", DEC("1.2"), "xsd:maxInclusive", DEC("7.2")),
                NOT(DR("xsd:integer", "xsd:minInclusive", DEC("2.2"), "xsd:maxInclusive", DEC("5.2"))), DR("xsd:int"));
    }

    @Test void testLargeRange1_1() throws Exception {
        assertDRSatisfiableUseCliqueOptimization(true, 255, DR("xsd:byte"));
    }

    @Test void testLargeRange1_2() throws Exception {
        assertDRSatisfiableUseCliqueOptimization(true, 256, DR("xsd:byte"));
    }

    @Test void testLargeRange1_3() throws Exception {
        assertDRSatisfiableUseCliqueOptimization(false, 257, DR("xsd:byte"));
    }

    @Test void testLargeRange2_1() throws Exception {
        assertDRSatisfiableUseCliqueOptimization(true, 127, DR("xsd:byte"), DR("xsd:nonNegativeInteger"));
    }

    @Test void testLargeRange2_2() throws Exception {
        assertDRSatisfiableUseCliqueOptimization(true, 128, DR("xsd:byte"), DR("xsd:nonNegativeInteger"));
    }

    @Test void testLargeRange2_3() throws Exception {
        assertDRSatisfiableUseCliqueOptimization(false, 129, DR("xsd:byte"), DR("xsd:nonNegativeInteger"));
    }

    @Test void testClique() throws Exception {
        assertDRSatisfiableUseCliqueOptimization(true, 2,
                DR("xsd:integer", "xsd:minInclusive", INT("1"), "xsd:maxInclusive", INT("2")), NOT(OO(INT("3"))));
    }
}
