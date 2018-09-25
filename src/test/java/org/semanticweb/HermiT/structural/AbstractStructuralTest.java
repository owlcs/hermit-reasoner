package org.semanticweb.HermiT.structural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import junit.framework.AssertionFailedError;

import org.semanticweb.HermiT.AbstractOntologyTest;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.owlapi.model.IRI;
@SuppressWarnings("javadoc")
public abstract class AbstractStructuralTest extends AbstractOntologyTest {

    protected static void assertContainsAll(String testName, Collection<String> actual, String[] control) {
        try {
            assertEquals(Arrays.stream(control).sorted().collect(Collectors.joining("\n")),actual.stream().sorted().collect(Collectors.joining("\n")));
        } catch (AssertionFailedError e) {
            System.out.println("Test " + testName + " failed!");
                System.out.println("Control set (" + control.length + " elements):");
                System.out.println("------------------------------------------");
                for (String object : control)
                    System.out.println(object.toString());
                System.out.println("------------------------------------------");
            System.out.println("Actual set (" + actual.size() + " elements):");
            System.out.println("------------------------------------------");
            for (Object object : actual)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.flush();
            throw e;
        }
    }

    /**
     * tests that the set have equal length and that the actual set contains all
     * objects from the control set, otherwise the test fails and the contents
     * of the control and the actual set are printed
     */
    protected static <T> void assertContainsAll(String testName, Collection<T> actual, Collection<T> control) {
        try {
            assertEquals(control.size(), actual.size());
            for (T contr : control)
                assertTrue(actual.contains(contr));
        } catch (AssertionFailedError e) {
            System.out.println("Test " + testName + " failed!");
            System.out.println("Control set (" + control.size() + " elements):");
            System.out.println("------------------------------------------");
            for (T object : control)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.println("Actual set (" + actual.size() + " elements):");
            System.out.println("------------------------------------------");
            for (Object object : actual)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.flush();
            throw e;
        }
    }

    protected static String[] S(String... strings) {
        return strings;
    }

    protected List<String> getDLClauses() {
        OWLClausification clausifier = new OWLClausification(new Configuration());
        Set<DescriptionGraph> noDescriptionGraphs = Collections.emptySet();
        DLOntology dlOntology = (DLOntology) clausifier.preprocessAndClausify(m_ontology, noDescriptionGraphs)[1];
        String ontologyIRI = m_ontology.getOntologyID().getDefaultDocumentIRI().orElse(IRI.create( "urn:hermit:kb")).toString();
        List<String> actualStrings = new ArrayList<>();
        Prefixes prefixes = new Prefixes();
        prefixes.declareSemanticWebPrefixes();
        Set<String> individualIRIs = new HashSet<>();
        Set<String> anonIndividualIRIs = new HashSet<>();
        for (Individual individual : dlOntology.getAllIndividuals())
            if (individual.isAnonymous())
                addIRI(individual.getIRI(), anonIndividualIRIs);
            else
                addIRI(individual.getIRI(), individualIRIs);
        prefixes.declareInternalPrefixes(individualIRIs, anonIndividualIRIs);
        prefixes.declareDefaultPrefix(ontologyIRI + "#");
        for (DLClause dlClause : dlOntology.getDLClauses())
            actualStrings.add(toOrderedString(dlClause, prefixes));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getPositiveFacts())
            actualStrings.add(atom.toString(prefixes));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getNegativeFacts())
            actualStrings.add("not " + atom.toString(prefixes));
        return actualStrings;
    }

    protected static String toOrderedString(DLClause dlClause, Prefixes prefixes) {
        StringBuilder buffer = new StringBuilder();
        SortedSet<Atom> headAtoms = new TreeSet<>(AbstractStructuralTest::atomLexicalCompare);
        for (int atomIndex = 0; atomIndex < dlClause.getHeadLength(); atomIndex++)
            headAtoms.add(dlClause.getHeadAtom(atomIndex));
        boolean isFirstAtom = true;
        for (Atom atom : headAtoms) {
            if (isFirstAtom)
                isFirstAtom = false;
            else
                buffer.append(" v ");
            buffer.append(atom.toString(prefixes));
        }
        buffer.append(" :- ");
        SortedSet<Atom> bodyAtoms = new TreeSet<>(AbstractStructuralTest::atomLexicalCompare);
        for (int atomIndex = 0; atomIndex < dlClause.getBodyLength(); atomIndex++)
            bodyAtoms.add(dlClause.getBodyAtom(atomIndex));
        isFirstAtom = true;
        for (Atom atom : bodyAtoms) {
            if (isFirstAtom)
                isFirstAtom = false;
            else
                buffer.append(", ");
            buffer.append(atom.toString(prefixes));
        }
        return buffer.toString();
    }

    protected void addIRI(String uri, Set<String> prefixIRIs) {
        if (!Prefixes.isInternalIRI(uri)) {
            int lastHash = uri.lastIndexOf('#');
            if (lastHash != -1) {
                String prefixIRI = uri.substring(0, lastHash + 1);
                prefixIRIs.add(prefixIRI);
            }
        }
    }

    protected static int atomLexicalCompare(Atom a1, Atom a2) {
        if (a1 == a2)
            return 0;
        int comparison = a1.getDLPredicate().toString().compareTo(a2.getDLPredicate().toString());
        if (comparison != 0)
            return comparison;
        comparison = a1.getArity() - a2.getArity();
        if (comparison != 0)
            return comparison;
        for (int i = 0; i < a1.getArity(); i++) {
            comparison = a1.getArgument(i).toString().compareTo(a2.getArgument(i).toString());
            if (comparison != 0)
                return comparison;
        }
        return 0;
    }
}
