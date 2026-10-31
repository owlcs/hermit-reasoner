package org.semanticweb.HermiT.reasoner;

import org.junit.jupiter.api.Tag;
import org.semanticweb.HermiT.Configuration;

//Unit tests for HermiT as a blackbox -- the quick subset.
@Tag("AllQuickTests")
class ReasonerIndividualReuseTest extends ReasonerTest {

    @Override
    protected Configuration getConfiguration() {
        Configuration configuration = super.getConfiguration();
        configuration.existentialStrategyType = Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        return configuration;
    }
}
