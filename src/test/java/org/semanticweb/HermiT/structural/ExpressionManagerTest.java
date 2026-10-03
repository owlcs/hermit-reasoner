package org.semanticweb.HermiT.structural;

import org.semanticweb.HermiT.AbstractOntologyTest;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

@SuppressWarnings("javadoc")
public class ExpressionManagerTest extends AbstractOntologyTest {
    private ExpressionManager expressionManager;
    private OWLClass cls;
    private OWLObjectProperty property;

    @Override
    protected void setUp() {
        super.setUp();
        expressionManager=new ExpressionManager(m_dataFactory);
        cls=NS_C("A");
        property=m_dataFactory.getOWLObjectProperty(NS("p"));
    }

    public void testEmptyUnionSimplifiesToNothing() {
        assertEquals(m_dataFactory.getOWLNothing(),expressionManager.getSimplified(
            m_dataFactory.getOWLObjectUnionOf(
                m_dataFactory.getOWLNothing(),
                m_dataFactory.getOWLObjectSomeValuesFrom(property,m_dataFactory.getOWLNothing()))));
    }

    public void testSingletonUnionSimplifiesToItsOperand() {
        assertEquals(cls,expressionManager.getSimplified(
            m_dataFactory.getOWLObjectUnionOf(cls,m_dataFactory.getOWLNothing())));
    }

    public void testEmptyIntersectionSimplifiesToThing() {
        assertEquals(m_dataFactory.getOWLThing(),expressionManager.getSimplified(
            m_dataFactory.getOWLObjectIntersectionOf(
                m_dataFactory.getOWLThing(),
                m_dataFactory.getOWLObjectAllValuesFrom(property,m_dataFactory.getOWLThing()))));
    }

    public void testSingletonIntersectionSimplifiesToItsOperand() {
        assertEquals(cls,expressionManager.getSimplified(
            m_dataFactory.getOWLObjectIntersectionOf(cls,m_dataFactory.getOWLThing())));
    }
}
