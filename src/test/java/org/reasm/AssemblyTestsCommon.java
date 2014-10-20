package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.reasm.messages.InternalAssemblerErrorMessage;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceNode;
import org.reasm.testhelpers.TestArchitecture;
import org.reasm.testhelpers.TestSourceNode;

final class AssemblyTestsCommon {

    static final UnsignedIntValue FORTY_TWO = new UnsignedIntValue(42L);

    static final SourceFile EMPTY_SOURCE_FILE = new SourceFile("", null);

    static final SourceNode NODE_THAT_SHOULD_NOT_BE_REACHED = new SourceNode(0, null) {
        @Override
        protected void assembleCore(AssemblyBuilder builder) {
            fail("This node should not be assembled.");
        }
    };

    static void checkOutput(@Nonnull Assembly assembly, @Nonnull byte[] expectedOutput) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(expectedOutput.length);
        assembly.writeAssembledDataTo(out);
        final byte[] actualOutput = out.toByteArray();
        assertThat(actualOutput, is(expectedOutput));
    }

    static void checkOutput(@Nonnull AssemblyStep step, @Nonnull byte[] expectedOutput) throws IOException {
        final int assembledDataLength = (int) step.getAssembledDataLength();
        final byte[] actualOutput = new byte[assembledDataLength];
        step.getOutput().read(step.getAssembledDataStart(), actualOutput, 0, assembledDataLength);
        assertThat(actualOutput, is(expectedOutput));
    }

    static Assembly createAssembly(@Nonnull SourceNode testNode) {
        return new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, new TestArchitecture(testNode)));
    }

    static TestSourceNode createNodeThatDefinesASymbol(@Nonnull String name, boolean isLocalSymbol, @Nonnull SymbolType type,
            @CheckForNull Value value) {
        return createNodeThatDefinesASymbol(SymbolContext.VALUE, name, isLocalSymbol, type, value);
    }

    static TestSourceNode createNodeThatDefinesASymbol(@Nonnull String name, @Nonnull SymbolType type, @CheckForNull Value value) {
        return createNodeThatDefinesASymbol(SymbolContext.VALUE, name, false, type, value);
    }

    static <TValue> TestSourceNode createNodeThatDefinesASymbol(@Nonnull SymbolContext<TValue> context, @Nonnull String name,
            @Nonnull SymbolType type, @CheckForNull TValue value) {
        return createNodeThatDefinesASymbol(context, name, false, type, value);
    }

    static TestSourceNode createNodeThatDoesNothing() {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
            }
        };
    }

    static TestSourceNode createNodeThatEmitsData(final byte[] data) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.appendAssembledData(data);
            }
        };
    }

    static TestSourceNode createNodeThatEntersANamespace(@Nonnull final String name) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.enterNamespace(name);
            }
        };
    }

    static TestSourceNode createNodeThatEntersATransformationBlock(@Nonnull final OutputTransformation transformation) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.enterTransformationBlock(transformation);
            }
        };
    }

    static TestSourceNode createNodeThatExitsANamespace() {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.exitNamespace();
            }
        };
    }

    static TestSourceNode createNodeThatExitsATransformationBlock() {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.exitTransformationBlock();
            }
        };
    }

    static void step(@Nonnull Assembly assembly, @Nonnull AssemblyCompletionStatus expectedStatus) {
        final AssemblyCompletionStatus actualStatus = assembly.step();
        if (actualStatus != expectedStatus) {
            final StringBuilder reasonBuilder = new StringBuilder();
            Throwable cause = null;

            for (AssemblyMessage message : assembly.getMessages()) {
                reasonBuilder.append(message.getText()).append(System.lineSeparator());
                if (message instanceof InternalAssemblerErrorMessage) {
                    cause = ((InternalAssemblerErrorMessage) message).getThrowable();
                    reasonBuilder.append(cause.getMessage()).append(System.lineSeparator());
                }
            }

            final Description description = new StringDescription();
            final Matcher<AssemblyCompletionStatus> matcher = is(expectedStatus);
            description.appendText(reasonBuilder.toString()).appendText("Expected: ").appendDescriptionOf(matcher)
                    .appendText("\n     but: ");
            matcher.describeMismatch(actualStatus, description);

            throw new AssertionError(description.toString(), cause);
        }
    }

    private static <TValue> TestSourceNode createNodeThatDefinesASymbol(@Nonnull final SymbolContext<TValue> context,
            @Nonnull final String name, final boolean isLocalSymbol, @Nonnull final SymbolType type,
            @CheckForNull final TValue value) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.defineSymbol(context, name, isLocalSymbol, type, value);
            }
        };
    }

    // This class is not meant to be instantiated.
    private AssemblyTestsCommon() {
    }

}
