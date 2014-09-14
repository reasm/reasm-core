package org.reasm.source;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.reasm.Architecture;
import org.reasm.Assembly;
import org.reasm.AssemblyBuilder;
import org.reasm.AssemblyCompletionStatus;
import org.reasm.Configuration;
import org.reasm.Environment;
import org.reasm.messages.ParseErrorMessage;
import org.reasm.testhelpers.DummyParseError;
import org.reasm.testhelpers.DummySourceNode;
import org.reasm.testhelpers.EquivalentAssemblyMessage;

import ca.fragag.text.Document;

/**
 * Test class for {@link SourceNode}.
 *
 * @author Francis Gagné
 */
public class SourceNodeTest {

    /**
     * Asserts that {@link SourceNode#assemble(AssemblyBuilder)} throws a {@link NullPointerException} when the <code>builder</code>
     * argument is <code>null</code>.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test(expected = NullPointerException.class)
    public void assembleNullBuilder() throws IOException {
        final SourceNode node = new DummySourceNode(132, null);
        node.assemble(null);
    }

    /**
     * Asserts that {@link SourceNode#assemble(AssemblyBuilder)} adds a {@link ParseErrorMessage} to the assembly when the
     * {@link SourceNode} has a parse error and doesn't call {@link SourceNode#assembleCore(AssemblyBuilder)}.
     */
    @Test
    public void assembleParseError() {
        final ParseError parseError = new DummyParseError();

        final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile("", ""), new Architecture(null) {
            @Override
            public SourceNode parse(Document text) {
                return new DummySourceNode(text.length(), parseError);
            }
        });

        final Assembly assembly = new Assembly(configuration);
        assertThat(assembly.step(), is(AssemblyCompletionStatus.COMPLETE));
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new ParseErrorMessage(parseError))));
    }

    /**
     * Asserts that {@link SourceNode#assemble(AssemblyBuilder)} throws an {@link IllegalArgumentException} when the specified
     * builder's current step is on a different {@link SourceNode}.
     */
    @Test
    public void assembleWrongNode() {
        // I need a mutable boolean
        final AtomicBoolean enteredAssembleCore = new AtomicBoolean();

        final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile("", ""), new Architecture(null) {
            @Override
            public SourceNode parse(Document text) {
                return new SourceNode(text.length(), null) {
                    @Override
                    protected void assembleCore(AssemblyBuilder builder) throws IOException {
                        enteredAssembleCore.set(true);

                        final SourceNode node = new DummySourceNode(132, null);

                        try {
                            node.assemble(builder);
                        } catch (IllegalArgumentException ex) {
                            return;
                        }

                        fail("SourceNode.assemble(AssemblyBuilder) should have thrown IllegalArgumentException");
                    }
                };
            }
        });

        final Assembly assembly = new Assembly(configuration);
        assembly.step();
        assertThat(enteredAssembleCore.get(), is(true));
    }

    /**
     * Asserts that {@link SourceNode#SourceNode(int, ParseError)} correctly initializes a {@link SourceNode}.
     */
    @Test
    public void sourceNode() {
        final ParseError parseError = new DummyParseError();
        final SourceNode node = new DummySourceNode(132, parseError);
        assertThat(node.getLength(), is(132));
        assertThat(node.getParseError(), is(parseError));
    }

    /**
     * Asserts that {@link SourceNode#SourceNode(int, ParseError)} throws an {@link IllegalArgumentException} when the
     * <code>length</code> argument is negative.
     */
    @Test(expected = IllegalArgumentException.class)
    public void sourceNodeNegativeLength() {
        new DummySourceNode(-132, null);
    }

    /**
     * Asserts that {@link SourceNode#SourceNode(int, ParseError)} doesn't throw an exception when the <code>parseError</code>
     * argument is <code>null</code>.
     */
    @Test
    public void sourceNodeNullParseError() {
        final SourceNode node = new DummySourceNode(132, null);
        assertThat(node.getLength(), is(132));
        assertThat(node.getParseError(), is(nullValue()));
    }

    /**
     * Asserts that {@link SourceNode#SourceNode(int, ParseError)} doesn't throw an exception when the <code>length</code> argument
     * is 0.
     */
    @Test
    public void sourceNodeZeroLength() {
        final ParseError parseError = new DummyParseError();
        final SourceNode node = new DummySourceNode(0, parseError);
        assertThat(node.getLength(), is(0));
        assertThat(node.getParseError(), is(parseError));
    }

}
