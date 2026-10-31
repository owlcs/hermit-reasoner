package org.semanticweb.HermiT.structural;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ClausificationDatatypesTest extends AbstractStructuralTest {

    @ParameterizedTest
    @MethodSource("tests")
    void clausification(String name, String axioms, String[] expected) throws Exception {
        loadOntologyWithAxioms(axioms);
        List<String> clauses = getDLClauses();
        assertContainsAll(name, clauses, expected);
    }

    static Stream<Arguments> tests() {
        String hasAge = "Declaration(DataProperty(:hasAge))";
        String eighteen = "Declaration(Class(:Eighteen))";
        String dp = "Declaration(DataProperty(:dp))";
        String a = "Declaration(Class(:A))";
        return Stream.of(
                Arguments.of("testDataPropertiesHasValue1",
                        eighteen + " " + hasAge + " SubClassOf(:Eighteen DataHasValue(:hasAge \"18\"^^xsd:integer))",
                        S("atLeast(1 :hasAge { \"18\"^^xsd:integer })(X) :- :Eighteen(X)")),
                Arguments.of("testDataPropertiesHasValue2",
                        eighteen + " " + hasAge + " SubClassOf(DataHasValue(:hasAge \"18\"^^xsd:integer) :Eighteen)",
                        S(":Eighteen(X) v not({ \"18\"^^xsd:integer })(Y) :- :hasAge(X,Y)")),
                Arguments.of("testDataPropertiesAll1() throws Exception",
                        a + " " + dp + " SubClassOf(:A DataAllValuesFrom(:dp xsd:integer))",
                        S("xsd:integer(Y) :- :A(X), :dp(X,Y)")),
                Arguments.of("testDataPropertiesAll2",
                        a + " " + dp + " SubClassOf(DataAllValuesFrom(:dp xsd:integer) :A)",
                        S(":A(X) v atLeast(1 :dp not(xsd:integer))(X) :- owl:Thing(X)")),
                Arguments.of("testDataPropertiesSome1",
                        a + " " + dp + " SubClassOf(DataSomeValuesFrom(:dp xsd:string) :A)",
                        S(":A(X) v not(xsd:string)(Y) :- :dp(X,Y)")),
                Arguments.of("testDataPropertiesSome2",
                        a + " " + dp + " SubClassOf(:A DataSomeValuesFrom(:dp xsd:string))",
                        S("atLeast(1 :dp xsd:string)(X) :- :A(X)")),
                Arguments.of("testDataPropertiesDataOneOf1", a + " " + dp
                        + " SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"Peter\"^^xsd:string \"19\"^^xsd:integer)))",
                        S("{ \"19\"^^xsd:integer \"Peter\"^^xsd:string }(Y) :- :A(X), :dp(X,Y)")),
                Arguments.of("testDataPropertiesDataOneOf2", a + " " + dp
                        + " SubClassOf(DataAllValuesFrom(:dp DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)) :A)",
                        S(":A(X) v atLeast(1 :dp not({ \"18\"^^xsd:integer \"19\"^^xsd:integer }))(X) :- owl:Thing(X)")),
                Arguments.of("testDataPropertiesDataOneOf3", a + " " + dp
                        + " SubClassOf(DataAllValuesFrom(:dp DataOneOf(\"18\"^^xsd:integer \"abc\"^^xsd:string)) :A)",
                        S(":A(X) v atLeast(1 :dp not({ \"18\"^^xsd:integer \"abc\"^^xsd:string }))(X) :- owl:Thing(X)")),
                Arguments.of("testDataPropertiesDataOneOf4", a + " " + dp
                        + " SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"18\"^^xsd:integer \"abc\"^^xsd:string)))",
                        S("{ \"18\"^^xsd:integer \"abc\"^^xsd:string }(Y) :- :A(X), :dp(X,Y)")),
                Arguments.of("testDataPropertiesDataComplementOf1", a + " " + dp
                        + " SubClassOf(:A DataAllValuesFrom(:dp DataComplementOf(DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)))))",
                        S("{ \"18\"^^xsd:integer \"19\"^^xsd:integer }(Y) :- :A(X), :dp(X,Y)")),
                Arguments.of("testDataPropertiesDataComplementOf2", a + " " + dp
                        + " SubClassOf(:A DataAllValuesFrom(:dp DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer))))",
                        S("not({ \"18\"^^xsd:integer \"19\"^^xsd:integer })(Y) :- :A(X), :dp(X,Y)")),
                Arguments.of("testDataPropertiesMax1",
                        a + " " + dp + " SubClassOf(:A DataMaxCardinality(1 :dp xsd:string))",
                        S("Y1 == Y2 v not(xsd:string)(Y1) v not(xsd:string)(Y2) :- :A(X), :dp(X,Y1), :dp(X,Y2)")),
                Arguments.of("testDataPropertiesMax2",
                        a + " " + dp + " SubClassOf(DataMaxCardinality(1 :dp xsd:string) :A)",
                        S(":A(X) v atLeast(2 :dp xsd:string)(X) :- owl:Thing(X)")),
                Arguments.of("testDataPropertiesMax3",
                        a + " " + dp + " SubClassOf(:A DataMaxCardinality(3 :dp xsd:integer))",
                        S("Y1 == Y2 v Y1 == Y3 v Y1 == Y4 v Y2 == Y3 v Y2 == Y4 v Y3 == Y4 v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) v not(xsd:integer)(Y4) :- :A(X), :dp(X,Y1), :dp(X,Y2), :dp(X,Y3), :dp(X,Y4)")),
                Arguments.of("testDataPropertiesMax4",
                        a + " " + dp + " SubClassOf(DataMaxCardinality(3 :dp xsd:integer) :A)",
                        S(":A(X) v atLeast(4 :dp xsd:integer)(X) :- owl:Thing(X)")),
                Arguments.of("testDataPropertiesMin1",
                        a + " " + dp + " SubClassOf(DataMinCardinality(1 :dp xsd:string) :A)",
                        S(":A(X) v not(xsd:string)(Y) :- :dp(X,Y)")),
                Arguments.of("testDataPropertiesMin2",
                        a + " " + dp + " SubClassOf(DataMinCardinality(3 :dp xsd:string) :A)",
                        S(":A(X) v Y1 == Y2 v Y1 == Y3 v Y2 == Y3 v not(xsd:string)(Y1) v not(xsd:string)(Y2) v not(xsd:string)(Y3) :- :dp(X,Y1), :dp(X,Y2), :dp(X,Y3)")),
                Arguments.of("testDataPropertiesMin3",
                        a + " " + dp + " SubClassOf(:A DataMinCardinality(1 :dp xsd:string))",
                        S("atLeast(1 :dp xsd:string)(X) :- :A(X)")),
                Arguments.of("testDataPropertiesMin4",
                        a + " " + dp + " SubClassOf(:A DataMinCardinality(5 :dp xsd:string))",
                        S("atLeast(5 :dp xsd:string)(X) :- :A(X)")),
                Arguments.of("testDataPropertiesExact1",
                        a + " " + dp + " SubClassOf(:A DataExactCardinality(1 :dp xsd:integer))",
                        S("Y1 == Y2 v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) :- :A(X), :dp(X,Y1), :dp(X,Y2)",
                                "atLeast(1 :dp xsd:integer)(X) :- :A(X)")),
                Arguments.of("testDataPropertiesExact2",
                        a + " " + dp + " SubClassOf(:A DataExactCardinality(3 :dp xsd:integer))",
                        S("Y1 == Y2 v Y1 == Y3 v Y1 == Y4 v Y2 == Y3 v Y2 == Y4 v Y3 == Y4 v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) v not(xsd:integer)(Y4) :- :A(X), :dp(X,Y1), :dp(X,Y2), :dp(X,Y3), :dp(X,Y4)",
                                "atLeast(3 :dp xsd:integer)(X) :- :A(X)")),
                Arguments.of("testDataPropertiesExact3",
                        a + " " + dp + " SubClassOf(DataExactCardinality(1 :dp xsd:integer) :A)",
                        S(":A(X) v atLeast(2 :dp xsd:integer)(X) v not(xsd:integer)(Y) :- :dp(X,Y)")),
                Arguments.of("testDataPropertiesExact4",
                        a + " " + dp + " SubClassOf(DataExactCardinality(3 :dp xsd:integer) :A)",
                        S(":A(X) v Y1 == Y2 v Y1 == Y3 v Y2 == Y3 v atLeast(4 :dp xsd:integer)(X) v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) :- :dp(X,Y1), :dp(X,Y2), :dp(X,Y3)")),
                Arguments.of("testDataIntersectionOf",
                        "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger))) ClassAssertion(DataMinCardinality(2 :dp rdfs:Literal) :a)",
                        S("def:a0(:a)", "xsd:nonNegativeInteger(X) :- defdata:a0(X)",
                                "atLeast(2 :dp rdfs:Literal)(X) :- def:a0(X)", "defdata:a0(Y) :- :dp(X,Y)",
                                "xsd:nonPositiveInteger(X) :- defdata:a0(X)")),
                Arguments.of("testDataIntersectionOf2",
                        "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataIntersectionOf(rdfs:Literal DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger)))) ClassAssertion(DataMinCardinality(2 :dp rdfs:Literal) :a)",
                        S("def:a0(:a)", "xsd:nonNegativeInteger(X) :- defdata:a0(X)",
                                "atLeast(2 :dp rdfs:Literal)(X) :- def:a0(X)", "defdata:a0(Y) :- :dp(X,Y)",
                                "xsd:nonPositiveInteger(X) :- defdata:a0(X)")),
                Arguments.of("testDataIntersectionOf3",
                        "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataIntersectionOf(xsd:decimal xsd:nonNegativeInteger xsd:nonPositiveInteger))) ClassAssertion(DataMinCardinality(2 :dp rdfs:Literal) :a)",
                        S("def:a0(:a)", "xsd:nonNegativeInteger(X) :- defdata:a0(X)", "xsd:decimal(X) :- defdata:a0(X)",
                                "atLeast(2 :dp rdfs:Literal)(X) :- def:a0(X)", "defdata:a0(Y) :- :dp(X,Y)",
                                "xsd:nonPositiveInteger(X) :- defdata:a0(X)")),
                Arguments.of("testDataUnionOf1",
                        "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataUnionOf(xsd:nonNegativeInteger xsd:double)))",
                        S("defdata:a0(Y) :- :dp(X,Y)", "xsd:double(X) v xsd:nonNegativeInteger(X) :- defdata:a0(X)")),
                Arguments.of("testDataComplementOf1",
                        "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataComplementOf(DataUnionOf(xsd:nonNegativeInteger xsd:double))))",
                        S("not(xsd:nonNegativeInteger)(X) :- defdata:a0(X)", "defdata:a0(Y) :- :dp(X,Y)",
                                "not(xsd:double)(X) :- defdata:a0(X)")),
                Arguments.of("testDataComplementOf2",
                        "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataComplementOf(DataIntersectionOf(xsd:nonNegativeInteger xsd:double))))",
                        S("defdata:a0(Y) :- :dp(X,Y)",
                                "not(xsd:double)(X) v not(xsd:nonNegativeInteger)(X) :- defdata:a0(X)")),
                Arguments.of("testDataComplementOf3",
                        "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataComplementOf(DataIntersectionOf(DataOneOf(\"5\"^^xsd:nonNegativeInteger \"4.3\"^^xsd:double) DataOneOf(\"5\"^^xsd:integer)))))",
                        S("defdata:a0(Y) :- :dp(X,Y)",
                                "not({ \"4.3\"^^xsd:double \"5\"^^xsd:nonNegativeInteger })(X) v not({ \"5\"^^xsd:integer })(X) :- defdata:a0(X)")),
                Arguments.of("testDataComplementOf4",
                        "SubClassOf(DataAllValuesFrom(:dp DataComplementOf(DataIntersectionOf(DataOneOf(\"5\"^^xsd:nonNegativeInteger \"4.3\"^^xsd:double) DataOneOf(\"5\"^^xsd:integer)))) :A)",
                        S("{ \"4.3\"^^xsd:double \"5\"^^xsd:nonNegativeInteger }(X) :- defdata:a0(X)",
                                "{ \"5\"^^xsd:integer }(X) :- defdata:a0(X)",
                                ":A(X) v atLeast(1 :dp defdata:a0)(X) :- owl:Thing(X)")));
    }
}
