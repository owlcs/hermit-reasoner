/*
 * Reasoner
 * 
 * Version 0.5.0
 *
 * 2008-08-19
 * 
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.HermiT.blocking.AncestorBlocking;
import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairwiseDirectBlockingCheckerWithReflexivity;
import org.semanticweb.HermiT.blocking.SingleDirectBlockingChecker;
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.DepthFirstStrategy;
import org.semanticweb.HermiT.existentials.ExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.hierarchy.NaiveHierarchyPosition;
import org.semanticweb.HermiT.hierarchy.TranslatedHierarchyPosition;
import org.semanticweb.HermiT.hierarchy.PositionTranslator;
import org.semanticweb.HermiT.hierarchy.SubsumptionHierarchy;
import org.semanticweb.HermiT.hierarchy.SubsumptionHierarchyNode;
import org.semanticweb.HermiT.hierarchy.TableauSubsumptionChecker;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.monitor.TableauMonitorFork;
import org.semanticweb.HermiT.monitor.Timer;
import org.semanticweb.HermiT.monitor.TimerWithPause;
import org.semanticweb.HermiT.owlapi.structural.OwlClausification;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.HermiT.util.TranslatedMap;
import org.semanticweb.HermiT.util.TranslatedSet;
import org.semanticweb.HermiT.util.Translator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.HermiT.hierarchy.StandardClassificationManager;

/**
 * Answers queries about the logical implications of a particular knowledge base.
 * A Reasoner is associated with a single knowledge base, which is "loaded" when
 * the reasoner is constructed. By default a full classification of all atomic
 * terms in the knowledge base is also performed at this time (which can take
 * quite a while for large or complex ontologies), but this behavior can be
 * disabled as a part of the Reasoner configuration.
 * Internal details of the loading and reasoning algorithms can be configured
 * in the Reasoner constructor and do not change over the lifetime of the
 * Reasoner object---internal data structures and caches are optimized for a
 * particular configuration. By default, HermiT will use the set of options
 * which provide optimal performance.
 */
public class Reasoner implements Serializable {
	private static final long serialVersionUID=-8277117863937974032L;

    public static enum TableauMonitorType {
        NONE, TIMING, TIMING_WITH_PAUSE,
        DEBUGGER_NO_HISTORY, DEBUGGER_HISTORY_ON
    };
    public static enum DirectBlockingType {
        SINGLE, PAIR_WISE, PAIR_WISE_REFLEXIVE, OPTIMAL
    };
    public static enum BlockingStrategyType { ANYWHERE, ANCESTOR };
    public static enum BlockingSignatureCacheType { CACHED, NOT_CACHED };
    public static enum ExistentialStrategyType {
        CREATION_ORDER, DEPTH_FIRST, EL, INDIVIDUAL_REUSE
    };
    public static enum ParserType { KAON2, OWLAPI };
    public static enum SubsumptionCacheStrategyType {
        IMMEDIATE, JUST_IN_TIME, ON_REQUEST
    };
    
    public static class Configuration {
        public TableauMonitorType tableauMonitorType;
        public DirectBlockingType directBlockingType;
        public BlockingStrategyType blockingStrategyType;
        public BlockingSignatureCacheType blockingSignatureCacheType;
        public ExistentialStrategyType existentialStrategyType;
        public ParserType parserType;
        public SubsumptionCacheStrategyType subsumptionCacheStrategyType;
        public boolean clausifyTransitivity;
        public boolean checkClauses;
        public boolean prepareForExpressiveQueries;
        public TableauMonitor monitor;
        public final Map<String,Object> parameters;
    
        public Configuration() {
            tableauMonitorType = TableauMonitorType.NONE;
            directBlockingType = DirectBlockingType.OPTIMAL;
            blockingStrategyType = BlockingStrategyType.ANYWHERE;
            blockingSignatureCacheType = BlockingSignatureCacheType.CACHED;
            existentialStrategyType = ExistentialStrategyType.CREATION_ORDER;
            parserType = ParserType.OWLAPI;
            subsumptionCacheStrategyType =
                SubsumptionCacheStrategyType.IMMEDIATE;
            clausifyTransitivity = false;
            checkClauses = true;
            prepareForExpressiveQueries = false;
            monitor = null;
            parameters = new HashMap<String,Object>();
        }

        protected void setIndividualReuseStrategyReuseAlways
            (Set<? extends LiteralConcept> concepts) {
            parameters.put("IndividualReuseStrategy.reuseAlways", concepts);
        }

        public void loadIndividualReuseStrategyReuseAlways(File file)
            throws IOException {
            Set<AtomicConcept> concepts = loadConceptsFromFile(file);
            setIndividualReuseStrategyReuseAlways(concepts);
        }

        protected void setIndividualReuseStrategyReuseNever
            (Set<? extends LiteralConcept> concepts) {
            parameters.put("IndividualReuseStrategy.reuseNever", concepts);
        }

        public void loadIndividualReuseStrategyReuseNever(File file)
            throws IOException {
            Set<AtomicConcept> concepts = loadConceptsFromFile(file);
            setIndividualReuseStrategyReuseNever(concepts);
        }

        protected Set<AtomicConcept> loadConceptsFromFile(File file)
            throws IOException {
            Set<AtomicConcept> result = new HashSet<AtomicConcept>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line = reader.readLine();
                while (line != null) {
                    result.add(AtomicConcept.create(line));
                    line = reader.readLine();
                }
                return result;
            } finally {
                reader.close();
            }
        }
        
    } // end Configuration class

    private final Configuration m_config; // never null
    private DLOntology m_dlOntology; // never null
    private Namespaces namespaces; // never null
    private Tableau m_tableau; // never null
    private TableauSubsumptionChecker m_subsumptionChecker; // never null
    private Map<AtomicConcept, HierarchyPosition<AtomicConcept>>
        atomicConceptHierarchy; // may be null; use getAtomicConceptHierarchy
    private Map<AtomicConcept, Set<Individual>> realization;
        // may be null; use getRealization
    private SubsumptionHierarchy oldHierarchy;
        // only null when atomicConceptHierarchy is
    
    private OwlClausification clausifier; // null if loaded through KAON2
    
    public Reasoner(String ontologyURI)
        throws Clausifier.LoadingException, OWLException {
        m_config = new Configuration();
        loadOntology(URI.create(ontologyURI));
    }
    
    public Reasoner(java.net.URI ontologyURI)
        throws Clausifier.LoadingException, OWLException {
        m_config = new Configuration();
        loadOntology(ontologyURI);
    }
    
    public Reasoner(java.net.URI ontologyURI, Configuration config)
        throws Clausifier.LoadingException, OWLException {
        m_config = config;
        loadOntology(ontologyURI);
    }
    
    public Reasoner(OWLOntology ontology, Configuration config)
        throws OWLException {
        m_config = config;
        // FIXME: do the identities of the manager and factory matter?
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        loadOwlOntology(ontology, manager.getOWLDataFactory(),
                        (Set<DescriptionGraph>) null);
    }

    public Reasoner(OWLOntology ontology, Configuration config,
                  Set<DescriptionGraph> graphs)
        throws OWLException {
        m_config = config;
        // FIXME: do the identities of the manager and factory matter?
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        loadOwlOntology(ontology, manager.getOWLDataFactory(), graphs);
    }

    public DLOntology getDLOntology() {
        return m_dlOntology;
    }
    
    public boolean isConsistent() {
        return m_tableau.isABoxSatisfiable();
    }
    
    /**
     * Return `true` iff `classUri` occured in the loaded knowledge base.
     */
    public boolean isClassNameDefined(String classUri) {
        return m_dlOntology.getAllAtomicConcepts()
            .contains(AtomicConcept.create(classUri));
    }

    /**
     * Check whether `classUri` is satisfiable.
     * Note that classes which were not defined in the input ontology
     * are satisfiable if and only if the ontology as a whole is
     * consistent.
     */
    public boolean isClassSatisfiable(String classUri) {
        return m_subsumptionChecker.isSatisfiable(
            AtomicConcept.create(classUri)
        );
    }

    public void seedSubsumptionCache() {
        getClassTaxonomy();
    }
    
    public boolean isSubsumptionCacheSeeded() {
        return atomicConceptHierarchy != null;
    }

    public boolean isClassSubsumedBy(String childName,
                                     String parentName) {
        return m_subsumptionChecker.isSubsumedBy(
            AtomicConcept.create(childName), AtomicConcept.create(parentName)
        );
    }
    
    public SubsumptionHierarchy getSubsumptionHierarchy() {
        try {
            return new SubsumptionHierarchy(m_subsumptionChecker);
        } catch (SubsumptionHierarchy.SubusmptionCheckerException e) {
            throw new RuntimeException(
                "Unable to compute subsumption hierarchy.", e);
        }
    }
    
    protected Map<AtomicConcept, HierarchyPosition<AtomicConcept>>
        getAtomicConceptHierarchy() {
        if (atomicConceptHierarchy == null) {
            assert oldHierarchy == null;
            try {
                 oldHierarchy = new SubsumptionHierarchy(m_subsumptionChecker);
            } catch (SubsumptionHierarchy.SubusmptionCheckerException e) {
                throw new RuntimeException(
                    "Unable to compute subsumption hierarchy.");
            }
        
            Map<AtomicConcept, HierarchyPosition<AtomicConcept>> newHierarchy =
                new HashMap<AtomicConcept, HierarchyPosition<AtomicConcept>>();
        
            Map<AtomicConcept, NaiveHierarchyPosition<AtomicConcept>> newNodes
                = new HashMap<AtomicConcept,
                                NaiveHierarchyPosition<AtomicConcept>>();
            // First just create all the new hierarchy nodes:
            for (SubsumptionHierarchyNode oldNode : oldHierarchy) {
                NaiveHierarchyPosition<AtomicConcept> newNode =
                    new NaiveHierarchyPosition<AtomicConcept>();
                newNodes.put(oldNode.getRepresentative(), newNode);
                for (AtomicConcept concept : oldNode.getEquivalentConcepts()) {
                    newNode.labels.add(concept);
                    if (newHierarchy.put(concept, newNode) !=
                        null) {
                        throw new RuntimeException("The '" + concept.getURI() +
                            "' concept occurs in two different places" +
                            " in the taxonomy.");
                    }
                }
            }
            // Now connect them together:
            for (SubsumptionHierarchyNode oldNode : oldHierarchy) {
                NaiveHierarchyPosition<AtomicConcept> newNode =
                    newNodes.get(oldNode.getRepresentative());
                for (SubsumptionHierarchyNode parent
                        : oldNode.getParentNodes()) {
                    newNode.parents.add(
                        newNodes.get(parent.getRepresentative())
                    );
                }
                for (SubsumptionHierarchyNode child
                        : oldNode.getChildNodes()) {
                    newNode.children.add(
                        newNodes.get(child.getRepresentative())
                    );
                }
            }
            // Construction finished; set our member cache:
            atomicConceptHierarchy = newHierarchy;
        }
        return atomicConceptHierarchy;
    }
    
    protected HierarchyPosition<AtomicConcept>
        getPosition(AtomicConcept c) {
        getAtomicConceptHierarchy();
        assert oldHierarchy != null;
        StandardClassificationManager classifier =
            new StandardClassificationManager(oldHierarchy,
                                                m_subsumptionChecker);
        try {
            classifier.findPosition(c);
        } catch (SubsumptionHierarchy.SubusmptionCheckerException e) {
            throw new RuntimeException(
                "Unable to classify concept.", e);
        }
        if (classifier.m_topSet.equals(classifier.m_bottomSet)) {
            assert classifier.m_topSet.size() == 1;
            return getAtomicConceptHierarchy().get
                        (classifier.m_topSet.get(0).getRepresentative());
        }
        NaiveHierarchyPosition<AtomicConcept> out =
            new NaiveHierarchyPosition<AtomicConcept>();
        for (SubsumptionHierarchyNode parent : classifier.m_topSet) {
            out.parents.add(getAtomicConceptHierarchy().get(
                                parent.getRepresentative()));
        }
        for (SubsumptionHierarchyNode child : classifier.m_bottomSet) {
            out.children.add(getAtomicConceptHierarchy().get(
                                child.getRepresentative()));
        }
        return out;
    }
    
    protected HierarchyPosition<AtomicConcept>
        getPosition(OWLDescription desc) {
        Set<DLClause> clauses = new HashSet<DLClause>();
        Set<Atom> positiveFacts = new HashSet<Atom>();
        Set<Atom> negativeFacts = new HashSet<Atom>();
        if (clausifier == null) {
            throw new RuntimeException(
                "Complex concept queries require parsing by the OWL API.");
        }
        AtomicConcept c =
            clausifier.define(desc, clauses, positiveFacts, negativeFacts);
        m_tableau.extendWithDefinitions(clauses, positiveFacts, negativeFacts);
        return getPosition(c);
    }

    static class ConceptToString implements Translator<AtomicConcept, String> {
        public String translate(AtomicConcept c) {
            return c.getURI();
        }
        public boolean equals(Object o) {
            return o instanceof ConceptToString;
        }
        public int hashCode() {
            return 0;
        }
    }
    static class IndividualToString implements Translator<Individual, String> {
        public String translate(Individual i) {
            return i.getURI();
        }
        public boolean equals(Object o) {
            return o instanceof IndividualToString;
        }
        public int hashCode() {
            return 0;
        }
    }
    static class ConceptTranslator implements Translator<Object, AtomicConcept> {
        public AtomicConcept translate(Object o) {
            return AtomicConcept.create(o.toString());
        }
        public boolean equals(Object o) {
            return o instanceof ConceptTranslator;
        }
        public int hashCode() {
            return 0;
        }
    }

    public Map<String, HierarchyPosition<String>> getClassTaxonomy() {
        return new TranslatedMap<
                AtomicConcept, String, HierarchyPosition<AtomicConcept>,
                HierarchyPosition<String>
            >(getAtomicConceptHierarchy(), new ConceptToString(),
                new ConceptTranslator(),
                new PositionTranslator<AtomicConcept, String>(
                    new ConceptToString()));
    }
    
    public HierarchyPosition<String>
        getClassTaxonomyPosition(String className) {
        if (!isClassNameDefined(className)) {
            throw new RuntimeException(
                "unrecognized class name '" + className + "'"
            );
        }
        return getClassTaxonomy().get(className);
    }
    
    public HierarchyPosition<String>
        getClassTaxonomyPosition(OWLDescription description) {
        return new TranslatedHierarchyPosition<AtomicConcept, String>(
            getPosition(description), new ConceptToString());
    }
    
    public HierarchyPosition<String>
        getMemberships(String individual) {
        if (clausifier == null) {
            throw new RuntimeException(
                "Individual queries require parsing by the OWL API.");
        }
        return getClassTaxonomyPosition(
            clausifier.factory.getOWLObjectOneOf(
                clausifier.factory.getOWLIndividual(URI.create(individual))));
    }
    
    private Map<AtomicConcept, Set<Individual>> getRealization() {
        if (realization == null) {
            realization = new HashMap<AtomicConcept, Set<Individual>>();
            for (Individual i : m_dlOntology.getAllIndividuals()) {
                HierarchyPosition<AtomicConcept> p =
                    getPosition(
                        clausifier.factory.getOWLObjectOneOf(
                            clausifier.factory.getOWLIndividual(
                                URI.create(i.getURI()))));
                Set<AtomicConcept> parents = p.getEquivalents();
                if (parents.isEmpty()) {
                    parents = new HashSet<AtomicConcept>();
                    for (HierarchyPosition<AtomicConcept> parentPos
                            : p.getParentPositions()) {
                        parents.addAll(p.getEquivalents());
                    }
                }
                for (AtomicConcept c : parents) {
                    if (!realization.containsKey(c)) {
                        realization.put(c, new HashSet<Individual>());
                    }
                    realization.get(c).add(i);
                }
            }
        }
        return realization;
    }
    
    public Set<String> getDirectMembers(String className) {
        return new TranslatedSet<Individual, String>(
            getRealization().get(AtomicConcept.create(className)),
            new IndividualToString()
        );
    }
    
    public Set<String> getMembers(String className) {
        Set<String> out = new HashSet<String>();
        for (AtomicConcept c :
            getPosition(AtomicConcept.create(className)).getDescendants()) {
            for (Individual i : getRealization().get(c)) {
                out.add(i.getURI());
            }
        }
        return out;
    }
    
    public Set<String> getMembers(OWLDescription description) {
        Set<String> out = new HashSet<String>();
        for (AtomicConcept c :
            getPosition(description).getDescendants()) {
            for (Individual i : getRealization().get(c)) {
                out.add(i.getURI());
            }
        }
        return out;
    }

    public Set<String> getDirectMembers(OWLDescription description) {
        Set<String> out = new HashSet<String>();
        HierarchyPosition<AtomicConcept> p = getPosition(description);
        Set<AtomicConcept> children = p.getEquivalents();
        if (children.isEmpty()) {
            for (HierarchyPosition<AtomicConcept> childPos
                    : p.getChildPositions()) {
                children.addAll(childPos.getEquivalents());
            }
        }
        for (AtomicConcept c : children) {
            for (Individual i : getRealization().get(c)) {
                out.add(i.getURI());
            }
        }
        return out;
    }
    
    public void printSortedAncestorLists(PrintWriter output) {
        printSortedAncestorLists(output, getClassTaxonomy());
    }
    
    public static void printSortedAncestorLists(
        PrintWriter output, Map<String, HierarchyPosition<String>> taxonomy) {
        Map<String, Set<String>> flat = new TreeMap<String, Set<String>>();
        for (Map.Entry<String, HierarchyPosition<String>> e :
                taxonomy.entrySet()) {
            flat.put(e.getKey(),
                     new TreeSet<String>(e.getValue().getAncestors()));
        }
        try {
            for (Map.Entry<String, Set<String>> e : flat.entrySet()) {
                output.println("'" + e.getKey() + "' ancestors:");
                for (String ancestor : e.getValue()) {
                    output.println("\t" + ancestor);
                }
                output.println("--------------------------------"); // 32
            }
            output.println("! THE END !");
        } finally {
            output.flush();
        }
    }

    protected void loadOntology(URI physicalURI)
        throws Clausifier.LoadingException, OWLException {
        loadOntology(physicalURI, null);
    }
    
    protected void loadOntology(URI physicalURI,
                               Set<DescriptionGraph> descriptionGraphs)
        throws Clausifier.LoadingException, OWLException {
        Clausifier clausifier = null;
        switch (m_config.parserType) {
            case KAON2: {
                try {
                    clausifier = (Clausifier)
                        Class.forName("org.semanticweb.HermiT.kaon2.Clausifier")
                            .newInstance();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to load KAON2 library", e);
                } catch (NoClassDefFoundError e) {
                    // This seems to be the one that comes up with no KAON2 available
                    throw new RuntimeException("Unable to load KAON2 library", e);
                } catch (InstantiationException e) {
                    throw new RuntimeException("Unable to load KAON2 library", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to load KAON2 library", e);
                }
                loadDLOntology(clausifier.loadFromURI(physicalURI, null));
            } break;
            case OWLAPI: {
                OWLOntologyManager manager =
                    OWLManager.createOWLOntologyManager();
                OWLOntology o =
                    manager.loadOntologyFromPhysicalURI(physicalURI);
                loadOwlOntology(o, manager.getOWLDataFactory(),
                                descriptionGraphs);
            } break;
            default:
                throw new IllegalArgumentException(
                    "unknown parser library requested");
        }
    }
    
    protected void loadOwlOntology(OWLOntology ontology,
                                   OWLDataFactory factory,
                                   Set<DescriptionGraph> descriptionGraphs)
        throws OWLException {
        if (descriptionGraphs == null) {
            descriptionGraphs = Collections.emptySet();
        }
        clausifier = new OwlClausification(factory);
        DLOntology d = clausifier.clausify(
            m_config, ontology, descriptionGraphs
        );
        loadDLOntology(d);
    }
    
    
    protected void loadDLOntology(File file) throws Exception {
        BufferedInputStream input =
            new BufferedInputStream(new FileInputStream(file));
        try {
            loadDLOntology(DLOntology.load(input));
        } finally {
            input.close();
        }
    }
    
    protected void loadDLOntology(DLOntology dlOntology)
        throws IllegalArgumentException {
        if (!dlOntology.canUseNIRule() &&
            dlOntology.hasAtMostRestrictions() &&
            dlOntology.hasInverseRoles() &&
            (m_config.existentialStrategyType ==
                ExistentialStrategyType.INDIVIDUAL_REUSE)) {
            throw new IllegalArgumentException(
                "The supplied DL-onyology is not compatible" +
                " with the individual reuse strategy.");
        }

        Map<String, String> namespaceDecl = new HashMap<String, String>();
        namespaceDecl.put("", dlOntology.getOntologyURI() + "#");
        namespaces = InternalNames.withInternalNamespaces
            (new Namespaces(namespaceDecl, Namespaces.semanticWebNamespaces));

        if (m_config.checkClauses) {
            Collection<DLClause> nonAdmissibleDLClauses =
                dlOntology.getNonadmissibleDLClauses();
            if (!nonAdmissibleDLClauses.isEmpty()) {
                String CRLF = System.getProperty("line.separator");
                StringBuffer buffer = new StringBuffer();
                buffer.append("The following DL-clauses in the DL-ontology" +
                              " are not admissible:");
                buffer.append(CRLF);
                for (DLClause dlClause : nonAdmissibleDLClauses) {
                    buffer.append(dlClause.toString(namespaces));
                    buffer.append(CRLF);
                }
                throw new IllegalArgumentException(buffer.toString());
            }
        }
        m_dlOntology = dlOntology;
        
        TableauMonitor wellKnownTableauMonitor = null;
        switch (m_config.tableauMonitorType) {
        case NONE:
            wellKnownTableauMonitor = null;
            break;
        case TIMING:
            wellKnownTableauMonitor = new Timer();
            break;
        case TIMING_WITH_PAUSE:
            wellKnownTableauMonitor = new TimerWithPause();
            break;
        case DEBUGGER_HISTORY_ON:
            wellKnownTableauMonitor = new Debugger(namespaces, true);
            break;
        case DEBUGGER_NO_HISTORY:
            wellKnownTableauMonitor = new Debugger(namespaces, false);
            break;
        default:
            throw new IllegalArgumentException("Unknown monitor type");
        }
        
        TableauMonitor tableauMonitor = null;
        if (m_config.monitor == null) {
            tableauMonitor = wellKnownTableauMonitor;
        } else if (wellKnownTableauMonitor == null) {
            tableauMonitor = m_config.monitor;
        } else {
            tableauMonitor = new TableauMonitorFork(wellKnownTableauMonitor,
                                                    m_config.monitor);
        }
        
        DirectBlockingChecker directBlockingChecker = null;
        switch (m_config.directBlockingType) {
        case OPTIMAL:
            if (m_config.prepareForExpressiveQueries) {
                directBlockingChecker =
                    new PairwiseDirectBlockingCheckerWithReflexivity();
            } else if (m_dlOntology.hasAtMostRestrictions() &&
                m_dlOntology.hasInverseRoles()) {
                if (m_dlOntology.hasReflexifity()) {
        			directBlockingChecker =
        			    new PairwiseDirectBlockingCheckerWithReflexivity();
        		} else {
        			directBlockingChecker =
        			    new PairWiseDirectBlockingChecker();
    			}
        	} else {
        		directBlockingChecker = new SingleDirectBlockingChecker();
        	}
        	break;
        case SINGLE:
            directBlockingChecker = new SingleDirectBlockingChecker();
            break;
        case PAIR_WISE:
        	directBlockingChecker = new PairWiseDirectBlockingChecker();
            break;
        case PAIR_WISE_REFLEXIVE:
    		directBlockingChecker =
        		    new PairwiseDirectBlockingCheckerWithReflexivity();
        	break;
        default:
            throw new IllegalArgumentException(
                "Unknown direct blocking type.");
        }
        
        BlockingSignatureCache blockingSignatureCache = null;
        if (!dlOntology.hasNominals()) {
            switch (m_config.blockingSignatureCacheType) {
            case CACHED:
                blockingSignatureCache =
                    new BlockingSignatureCache(directBlockingChecker);
                break;
            case NOT_CACHED:
                blockingSignatureCache = null;
                break;
            default:
                throw new IllegalArgumentException(
                    "Unknown blocking cache type.");
            }
        }
        
        BlockingStrategy blockingStrategy = null;
        switch (m_config.blockingStrategyType) {
        case ANCESTOR:
            blockingStrategy = new AncestorBlocking(directBlockingChecker,
                                                    blockingSignatureCache);
            break;
        case ANYWHERE:
            blockingStrategy = new AnywhereBlocking(directBlockingChecker,
                                                    blockingSignatureCache);
            break;
        default:
            throw new IllegalArgumentException(
                "Unknown blocking strategy type.");
        }
        
        ExpansionStrategy existentialsExpansionStrategy = null;
        switch (m_config.existentialStrategyType) {
        case CREATION_ORDER:
            existentialsExpansionStrategy =
                new CreationOrderStrategy(blockingStrategy);
            break;
        case DEPTH_FIRST:
            existentialsExpansionStrategy =
                new DepthFirstStrategy(blockingStrategy);
            break;
        case EL:
            existentialsExpansionStrategy =
                new IndividualReuseStrategy(blockingStrategy,true);
            break;
        case INDIVIDUAL_REUSE:
            existentialsExpansionStrategy =
                new IndividualReuseStrategy(blockingStrategy,false);
            break;
        default:
            throw new IllegalArgumentException(
                "Unknown expansion strategy type.");
        }
        
        m_tableau = new Tableau(tableauMonitor,
                                existentialsExpansionStrategy,
                                m_dlOntology,
                                m_config.parameters);
        m_subsumptionChecker = new TableauSubsumptionChecker(m_tableau);
        if (m_config.subsumptionCacheStrategyType ==
            SubsumptionCacheStrategyType.IMMEDIATE) {
            getClassTaxonomy();
        }
    }
    
    public void outputClauses(PrintWriter output, Namespaces namespaces) {
        output.println(m_dlOntology.toString(namespaces));
    }
    
    public Namespaces getNamespaces() {
        return namespaces;
    }

    public void save(File file) throws IOException {
        OutputStream outputStream =
            new BufferedOutputStream(new FileOutputStream(file));
        try {
            save(outputStream);
        } finally {
            outputStream.close();
        }
    }
    
    public void save(OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream =
            new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
    }
    
    public static Reasoner load(InputStream inputStream) throws IOException {
        try {
            ObjectInputStream objectInputStream =
                new ObjectInputStream(inputStream);
            return (Reasoner) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            IOException error=new IOException();
            error.initCause(e);
            throw error;
        }
    }
    public static Reasoner load(File file) throws IOException {
        InputStream inputStream =
            new BufferedInputStream(new FileInputStream(file));
        try {
            return load(inputStream);
        } finally {
            inputStream.close();
        }
    }

}