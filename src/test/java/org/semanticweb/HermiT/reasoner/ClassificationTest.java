package org.semanticweb.HermiT.reasoner;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

//Unit tests for HermiT as a blackbox -- the heavy subset.
@Tag("AllHeavyTests")
class ClassificationTest extends AbstractReasonerTest {

    @Test void testWine() throws Exception {
        loadReasonerFromResource("res/wine.xml");
        assertHierarchies("res/wine.xml.txt");
    }

    @Test void testGalenIansFullUndoctored() throws Exception {
        loadReasonerFromResource("res/galen-ians-full-undoctored.xml");
        assertHierarchies("res/galen-ians-full-undoctored.xml.txt");
    }

    @Test void testPizza() throws Exception {
        loadReasonerFromResource("res/pizza.xml");
        assertHierarchies("res/pizza.xml.txt");
    }

    @Test void testPropreo() throws Exception {
        loadReasonerFromResource("res/propreo.xml");
        assertHierarchies("res/propreo.xml.txt");
    }
}
