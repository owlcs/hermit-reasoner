package org.semanticweb.HermiT;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractHermiTTest {

    protected Set<String> getStrings(String resourceName) throws Exception {
        Set<String> strings = new HashSet<>();
        
        try (InputStream in = getClass().getResourceAsStream(resourceName);
        InputStreamReader in2 = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(in2);) {
            String line = reader.readLine();
            while (line != null) {
                strings.add(line);
                line = reader.readLine();
            }
        }
        return strings;
    }

    protected String getResourceText(String resourceName) throws Exception {
        if (resourceName == null)
            return null;
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter output = new PrintWriter(buffer);
        
        try (InputStream in = getClass().getResourceAsStream(resourceName);
                InputStreamReader in2 = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(in2);) {
            String line = reader.readLine();
            while (line != null) {
                output.println(line);
                line = reader.readLine();
            }
        }
        output.flush();
        return buffer.toString();
    }

    @SuppressWarnings("all")
    public static <T> void assertContainsAll(Collection<T> actual, T... control) {
        assertEquals(control.length, actual.size());
        for (int i = 0; i < control.length; i++)
            assertTrue(actual.contains(control[i]), control[i]+ " not in "+actual);
    }
}
