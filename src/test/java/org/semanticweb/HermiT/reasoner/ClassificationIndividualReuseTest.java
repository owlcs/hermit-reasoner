package org.semanticweb.HermiT.reasoner;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.semanticweb.HermiT.Configuration;
//Unit tests for HermiT as a blackbox -- the heavy subset.
@Tag("AllHeavyTests")
class ClassificationIndividualReuseTest extends AbstractReasonerTest {

    @Test void testDolce() throws Exception {
        loadReasonerFromResource("res/dolce_all.xml");
        assertHierarchies("res/dolce_all.xml.txt");
    }

    @Override
    protected Configuration getConfiguration() {
        Configuration configuration = super.getConfiguration();
        configuration.existentialStrategyType = Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        return configuration;
    }
}
