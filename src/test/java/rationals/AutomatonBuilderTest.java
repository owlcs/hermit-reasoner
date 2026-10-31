package rationals;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AutomatonBuilderTest {
    @Test
    void testStateLabellingYieldUniqueStates() {
        Automaton a = new Automaton();
        State s = a.state("init");
        State s2 = a.state("init");
        assertSame(s, s2, "should be same object");
        assertEquals(s, s2, "objects should be equals");
    }
}
