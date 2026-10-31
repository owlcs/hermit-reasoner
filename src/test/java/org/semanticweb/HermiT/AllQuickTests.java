package org.semanticweb.HermiT;

class AllQuickTests {

    public static void suite() throws Exception {
//        TestSuite suite = new TestSuite("All HermiT tests");
        // $JUnit-BEGIN$

        // not running because the tests fail so often due to different concept
        // definitions introduced for complex concepts depending on the parse
        // order
        // Tests the normalization and clausification, when loading via the
        // OWLAPI.
        // suite.addTest(org.semanticweb.HermiT.structural.AllTests.suite());

        // Tests Hermit as a blackbox, i.e., by only using the official
        // interface.
        //@Tag("AllQuickTests")

        // Tests for the internals of the reasoner.
        //@Tag("tableau")
        // Tests the graph library.
        //@Tag("Graph")
        // Runs the WG tests.
        //AllNonRejectedNonExtracreditWGTests

    }
}
