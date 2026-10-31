package org.semanticweb.HermiT.bugs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Disabled;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

@Disabled("to fix")
class InversePropertiesBadInferenceTest {

    @Test
    public void should() throws OWLOntologyCreationException {
        StringDocumentSource source = new StringDocumentSource(
                "Prefix: : <http://example.org/>\n" + "Ontology: <http://asdf>\n\n" 
                        + "ObjectProperty: hasBigPart\n"
                        + "    SubPropertyOf: hasPart, inverse (partOf)\n" 
                        + "ObjectProperty: hasPart\n"
                        + "    InverseOf: partOf\n" 
                        + "ObjectProperty: partOf\n" 
                        + "    Characteristics: Transitive\n"
                        + "    InverseOf: hasPart");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        OWLObjectProperty p1 = df.getOWLObjectProperty("http://example.org/hasBigPart");
        OWLObjectProperty p2 = df.getOWLObjectProperty("http://example.org/hasPart");
        OWLOntology ont = man.loadOntologyFromOntologyDocument(source);
        ont.axioms().forEach(System.out::println);

        OWLReasoner reasoner = new ReasonerFactory().createReasoner(ont);
        assertFalse(reasoner.isEntailed(df.getOWLEquivalentObjectPropertiesAxiom(p1,p2)));
        System.out.println(reasoner.getEquivalentObjectProperties(p1));
    }
}
