package org.reasm;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.reasm.source.AbstractSourceFile;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceNode;
import org.reasm.testhelpers.DummySourceNode;
import org.reasm.testhelpers.NullArchitecture;

import ca.fragag.Consumer;
import ca.fragag.text.Document;

/**
 * Test class for {@link Architecture}.
 *
 * @author Francis Gagné
 */
public class ArchitectureTest {

    /**
     * Asserts that the default implementation of
     * {@link Architecture#evaluateExpression(CharSequence, Assembly, Consumer, Consumer)} returns <code>null</code>.
     */
    @Test
    public void architectureEvaluateExpression() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, new SourceFile("", null),
                NullArchitecture.DEFAULT));
        assertThat(NullArchitecture.DEFAULT.evaluateExpression("2+2", assembly, null, null), is(nullValue()));
    }

    /**
     * Asserts that {@link Architecture#Architecture(Collection)} initializes an architecture with the specified names and that
     * {@link Architecture#getNames()} returns an unmodifiable set.
     */
    @Test
    public void architectureNonNullNames() {
        final Architecture architecture = new NullArchitecture("a", "b", "c"); // calls super(Arrays.asList(names));
        final Set<String> names = architecture.getNames();
        assertThat(names, is(not(nullValue())));
        assertThat(names, containsInAnyOrder("a", "b", "c"));

        try {
            names.add("d"); // should throw UnsupportedOperationException
            fail("Set.add(T) on architecture.getNames() should have thrown UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // Exception is expected
        }

        try {
            names.remove("d"); // should throw UnsupportedOperationException
            fail("Set.remove(T) on architecture.getNames() should have thrown UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // Exception is expected
        }
    }

    /**
     * Asserts that {@link Architecture#Architecture(Collection)} initializes an architecture with no names when the
     * <code>names</code> argument is <code>null</code>.
     */
    @Test
    public void architectureNullNames() {
        final Architecture architecture = new NullArchitecture(); // calls super(null);
        assertThat(architecture.getNames(), is(Collections.<String> emptySet()));
    }

    /**
     * Asserts that the default implementation of {@link Architecture#reparse(Document, AbstractSourceFile, int, int, int)}
     * delegates to {@link Architecture#parse(Document)}.
     */
    @Test
    public void architectureReparse() {
        // I need a mutable boolean
        final AtomicBoolean enteredParse = new AtomicBoolean();

        final Architecture architecture = new Architecture(null) {
            @Override
            public SourceNode parse(Document text) {
                enteredParse.set(true);
                return new DummySourceNode(text.length(), null);
            }
        };

        architecture.reparse(new Document("foobaz"), new SourceFile("foobar", null), 5, 1, 1);
        assertThat(enteredParse.get(), is(true));
    }

}
