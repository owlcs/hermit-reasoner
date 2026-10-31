/* Copyright 2009 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
 */
// An update for the tests (all.rdf) should regularly be downloaded to the
// ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import org.opentest4j.AssertionFailedError;

import org.semanticweb.owlapi.functional.renderer.OWLFunctionalSyntaxRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
@SuppressWarnings("javadoc")
public abstract class AbstractTest {
    public static int TIMEOUT = 300000;

    protected File m_dumpTestDataDirectory;
    protected WGTestDescriptor m_wgTestDescriptor;
    protected OWLOntologyManager m_ontologyManager;
    protected OWLOntology m_premiseOntology;
    protected Reasoner m_reasoner;
    protected boolean m_useDisjunctionLearning;

    public void prepareTest(String name, WGTestDescriptor wgTestDescriptor, File dumpTestDataDirectory,
            boolean useDisjunctionLearning) {
        m_wgTestDescriptor = wgTestDescriptor;
        m_dumpTestDataDirectory = dumpTestDataDirectory;
        m_useDisjunctionLearning = useDisjunctionLearning;
    }

    protected void registerMappingToResource(String ontologyIRI, String physicalResource)  {
        IRI physicalIRI;
        try {
            physicalIRI = IRI.create(getClass().getResource(physicalResource).toURI());
        } catch (URISyntaxException e) {
          throw new RuntimeException(e);
        }
        IRI logicalIRI = IRI.create(ontologyIRI);
        m_ontologyManager.getIRIMappers().add(new SimpleIRIMapper(logicalIRI, physicalIRI));
    }

    protected void registerImportedReosurces() {
        registerMappingToResource("http://www.w3.org/2002/03owlt/miscellaneous/consistent001",
                "ontologies/consistent001.rdf");
        registerMappingToResource("http://www.w3.org/2002/03owlt/miscellaneous/consistent002",
                "ontologies/consistent002.rdf");
        registerMappingToResource("http://www.w3.org/2002/03owlt/imports/support011-A", "ontologies/support011-A.rdf");
    }

    @AfterEach
    void tearDown() {
        m_wgTestDescriptor = null;
        m_ontologyManager = null;
        m_premiseOntology = null;
        m_reasoner = null;
        m_dumpTestDataDirectory = null;
    }

    public void runTest() throws Exception {
        dumpTestData();
        m_reasoner = new Reasoner(getConfiguration(), m_premiseOntology, null);
        InterruptTimer timer = new InterruptTimer(TIMEOUT, m_reasoner);
        timer.start();
        try {
            doTest();
        } catch (ReasonerInterruptedException e) {
            fail("Test timed out. "+e.getMessage());
        } catch (OutOfMemoryError e) {
            m_reasoner = null;
            Runtime.getRuntime().gc();
            fail("Test ran out of memory. "+e.getMessage());
        } catch (AssertionFailedError e) {
            fail("Test failed: " + e.getMessage());
        } catch (Exception e) {
            throw e;
        } finally {
            timer.stopTiming();
            timer.join();
        }
    }

    protected void dumpTestData() throws Exception {
        if (m_dumpTestDataDirectory != null)
            saveOntology(m_premiseOntology, new File(m_dumpTestDataDirectory, "premise.owl"));
    }

    protected Configuration getConfiguration() {
        Configuration c = new Configuration();
        c.throwInconsistentOntologyException = false;
        c.useDisjunctionLearning = m_useDisjunctionLearning;
        return c;
    }

    protected void saveOntology(OWLOntology ontology, File file) throws Exception {
        try (FileWriter out = new FileWriter(file);
                BufferedWriter out2 = new BufferedWriter(out);
                PrintWriter writer = new PrintWriter(out2);) {
            OWLFunctionalSyntaxRenderer renderer = new OWLFunctionalSyntaxRenderer();
            renderer.render(ontology, writer);
        }
    }

    protected abstract void doTest() throws Exception;

    protected static class InterruptTimer extends Thread {
        protected final int m_timeout;
        protected final Reasoner m_reasoner;
        protected boolean m_timingStopped;

        public InterruptTimer(int timeout, Reasoner reasoner) {
            super("HermiT Interrupt Thread");
            setDaemon(true);
            m_timeout = timeout;
            m_reasoner = reasoner;
            m_timingStopped = false;
        }

        @Override
        public synchronized void run() {
            try {
                if (!m_timingStopped) {
                    wait(m_timeout);
                    if (!m_timingStopped)
                        m_reasoner.interrupt();
                }
            } catch (@SuppressWarnings("unused") InterruptedException stopped) {
            }
        }

        public synchronized void stopTiming() {
            m_timingStopped = true;
            notifyAll();
        }
    }
}
