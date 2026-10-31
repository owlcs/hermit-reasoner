package org.semanticweb.HermiT.reasoner;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

//Unit tests for HermiT as a blackbox -- the quick subset.
@Tag("AllQuickTests")
class SimpleRolesTest extends AbstractReasonerTest {

    @Test void testSimpleRoles1() throws Exception {
        String axioms = "TransitiveObjectProperty(:R) " + "SubObjectPropertyOf(:R :P) "
                + "SubClassOf(:C ObjectMinCardinality(2 :P))";
        assertSimple(axioms, false);
    }

    @Test void testSimpleRoles2() throws Exception {
        String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q) :R) " + "SubObjectPropertyOf(:R :P) "
                + "SubClassOf(:C ObjectMaxCardinality(2 :P))";
        assertSimple(axioms, false);
    }

    @Test void testSimpleRoles3() throws Exception {
        String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q) :R) " + "SubObjectPropertyOf(:R :S) "
                + "InverseObjectProperties(:S :S-) " + "SubClassOf(:C ObjectMaxCardinality(2 :S-))";
        assertSimple(axioms, false);
    }

    @Test void testSimpleRoles4() throws Exception {
        String axioms = "TransitiveObjectProperty(:R-) " + "SubObjectPropertyOf(:R :P) " + "SubObjectPropertyOf(:P :S) "
                + "InverseObjectProperties(:R :R-) " + "InverseObjectProperties(:S :S-) "
                + "SubClassOf(:C ObjectMaxCardinality(2 :S-))";
        assertSimple(axioms, false);
    }
}
