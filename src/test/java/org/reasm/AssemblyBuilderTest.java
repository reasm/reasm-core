package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.reasm.AssemblyTestsCommon.FORTY_TWO;
import static org.reasm.AssemblyTestsCommon.NODE_THAT_SHOULD_NOT_BE_REACHED;
import static org.reasm.AssemblyTestsCommon.createAssembly;
import static org.reasm.AssemblyTestsCommon.step;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.reasm.source.AbstractSourceFile;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNode;
import org.reasm.testhelpers.NullArchitecture;
import org.reasm.testhelpers.TestSourceNode;

import com.google.common.collect.ImmutableList;

/**
 * Test class for {@link AssemblyBuilder}.
 *
 * @author Francis Gagné
 */
public class AssemblyBuilderTest {

    private static abstract class TestNullPointerExceptionSourceNode<T> extends TestSourceNode {

        private final String method;

        TestNullPointerExceptionSourceNode(@Nonnull String method) {
            this.method = method;
        }

        @Override
        protected final void assembleCore2(@Nonnull AssemblyBuilder builder) throws IOException {
            try {
                this.assembleCore3(builder, null);
            } catch (NullPointerException e) {
                return;
            }

            fail("AssemblyBuilder." + this.method + " should have thrown a NullPointerException");
        }

        // The nil parameter is just to bypass FindBugs's null analysis.
        abstract void assembleCore3(@Nonnull AssemblyBuilder builder, T nil) throws IOException;

    }

    static final byte[] ARBITRARY_DATA = new byte[] { 1, 4, 9, 16 };

    static final CustomAssemblyData DUMMY_CUSTOM_ASSEMBLY_DATA = new CustomAssemblyData() {
        @Override
        public void completed() {
            fail();
        }

        @Override
        public void startedNewPass() {
            fail();
        }
    };

    private static void appendAssembledDataByteArrayIntIntIndexOutOfBounds(final int offset, final int length) {
        final TestSourceNode node = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                try {
                    builder.appendAssembledData(ARBITRARY_DATA, offset, length);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }

                fail("AssemblyBuilder.appendAssembledData(byte[], int, int) should have thrown an IndexOutOfBoundsException");
            }
        };

        assembleNode(node);
    }

    private static void assembleNode(@Nonnull final TestSourceNode node) {
        final Assembly assembly = createAssembly(node);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        node.assertAssembleCount(1);
    }

    // Note: the parameters voluntarily have no @Nonnull annotation
    private static <TValue> TestSourceNode createNodeThatDefinesASymbolIncorrectly(final SymbolContext<TValue> context,
            final String symbolName, final SymbolType symbolType, final TValue value) {
        return new TestNullPointerExceptionSourceNode<Object>("defineSymbol()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, Object nil) {
                builder.defineSymbol(context, symbolName, false, symbolType, value);
            }
        };
    }

    /**
     * Asserts that {@link AssemblyBuilder#addMessage(AssemblyMessage)} throws a {@link NullPointerException} when the
     * <code>message</code> argument is <code>null</code>.
     */
    @Test
    public void addMessageNullMessage() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<AssemblyMessage>("addMessage()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, AssemblyMessage nil) {
                builder.addMessage(nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#addTentativeMessage(AssemblyMessage)} throws a {@link NullPointerException} when the
     * <code>message</code> argument is <code>null</code>.
     */
    @Test
    public void addTentativeMessageNullMessage() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<AssemblyMessage>("addTentativeMessage()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, AssemblyMessage nil) {
                builder.addTentativeMessage(nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#appendAssembledData(byte[], int, int)} throws an {@link IndexOutOfBoundsException} when
     * the sum of the <code>offset</code> parameter and of the <code>length</code> argument is greater than the data array's length.
     */
    @Test
    public void appendAssembledDataByteArrayIntIntLengthTooHigh() {
        appendAssembledDataByteArrayIntIntIndexOutOfBounds(3, 2);
    }

    /**
     * Asserts that {@link AssemblyBuilder#appendAssembledData(byte[], int, int)} throws an {@link IndexOutOfBoundsException} when
     * the <code>length</code> argument is less than zero.
     */
    @Test
    public void appendAssembledDataByteArrayIntIntLengthTooLow() {
        appendAssembledDataByteArrayIntIntIndexOutOfBounds(0, -1);
    }

    /**
     * Asserts that {@link AssemblyBuilder#appendAssembledData(byte[], int, int)} throws a {@link NullPointerException} when the
     * <code>data</code> argument is <code>null</code>.
     */
    @Test
    public void appendAssembledDataByteArrayIntIntNullData() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<byte[]>("appendAssembledData(byte[], int, int)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, byte[] nil) throws IOException {
                builder.appendAssembledData(nil, 0, 0);
            };
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#appendAssembledData(byte[], int, int)} throws an {@link IndexOutOfBoundsException} when
     * the <code>offset</code> argument is greater than the data array's length.
     */
    @Test
    public void appendAssembledDataByteArrayIntIntOffsetTooHigh() {
        appendAssembledDataByteArrayIntIntIndexOutOfBounds(5, 0);
    }

    /**
     * Asserts that {@link AssemblyBuilder#appendAssembledData(byte[], int, int)} throws an {@link IndexOutOfBoundsException} when
     * the <code>offset</code> argument is less than zero.
     */
    @Test
    public void appendAssembledDataByteArrayIntIntOffsetTooLow() {
        appendAssembledDataByteArrayIntIntIndexOutOfBounds(-1, 0);
    }

    /**
     * Asserts that {@link AssemblyBuilder#appendAssembledData(byte[])} throws a {@link NullPointerException} when the
     * <code>data</code> argument is <code>null</code>.
     */
    @Test
    public void appendAssembledDataByteArrayNullData() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<byte[]>("appendAssembledData(byte[])") {
            @Override
            void assembleCore3(AssemblyBuilder builder, byte[] nil) throws IOException {
                builder.appendAssembledData(nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#appendAssembledData(ByteBuffer)} throws a {@link NullPointerException} when the
     * <code>data</code> argument is <code>null</code>.
     */
    @Test
    public void appendAssembledDataByteBufferNullData() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<ByteBuffer>("appendAssembledData(ByteBuffer)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, ByteBuffer nil) throws IOException {
                builder.appendAssembledData(nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that (one of) {@link AssemblyBuilder}'s mutator methods throw an {@link IllegalStateException} when the assembly it
     * was created for is complete.
     */
    @Test
    public void checkStateNullAssembly() {
        final AtomicReference<AssemblyBuilder> builderRef = new AtomicReference<>();

        final TestSourceNode node = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builderRef.set(builder);
            }
        };

        final Assembly assembly = createAssembly(node);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        try {
            builderRef.get().setProgramCounter(0);
        } catch (IllegalStateException e) {
            return;
        }

        fail("AssemblyBuilder.setProgramCounter() should have thrown an IllegalStateException");
    }

    /**
     * Asserts that (one of) {@link AssemblyBuilder}'s mutator methods throw an {@link IllegalStateException} when the assembly it
     * was created for is not performing a step.
     */
    @Test
    public void checkStateNullStep() {
        final AtomicReference<AssemblyBuilder> builderRef = new AtomicReference<>();

        final TestSourceNode node = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builderRef.set(builder);
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(node, NODE_THAT_SHOULD_NOT_BE_REACHED));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        try {
            builderRef.get().setProgramCounter(0);
        } catch (IllegalStateException e) {
            return;
        }

        fail("AssemblyBuilder.setProgramCounter() should have thrown an IllegalStateException");
    }

    /**
     * Asserts that {@link AssemblyBuilder#defineSymbol(SymbolContext, String, boolean, SymbolType, Object)} throws a
     * {@link NullPointerException} when the <code>context</code> argument is <code>null</code>.
     */
    @Test
    public void defineSymbolNullContext() {
        assembleNode(createNodeThatDefinesASymbolIncorrectly(null, "foo", SymbolType.CONSTANT, FORTY_TWO));
    }

    /**
     * Asserts that {@link AssemblyBuilder#defineSymbol(SymbolContext, String, boolean, SymbolType, Object)} throws a
     * {@link NullPointerException} when the <code>symbolName</code> argument is <code>null</code>.
     */
    @Test
    public void defineSymbolNullSymbolName() {
        assembleNode(createNodeThatDefinesASymbolIncorrectly(SymbolContext.VALUE, null, SymbolType.CONSTANT, FORTY_TWO));
    }

    /**
     * Asserts that {@link AssemblyBuilder#defineSymbol(SymbolContext, String, boolean, SymbolType, Object)} throws a
     * {@link NullPointerException} when the <code>symbolType</code> argument is <code>null</code>.
     */
    @Test
    public void defineSymbolNullSymbolType() {
        assembleNode(createNodeThatDefinesASymbolIncorrectly(SymbolContext.VALUE, "foo", null, FORTY_TWO));
    }

    /**
     * Asserts that {@link AssemblyBuilder#enterBlock(Iterable, AssemblyStepIterationController, boolean, BlockEvents)} throws a
     * {@link NullPointerException} when the <code>sourceLocations</code> argument is <code>null</code>.
     */
    @Test
    public void enterBlockNullSourceLocations() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<Iterable<SourceLocation>>("enterBlock()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, Iterable<SourceLocation> nil) {
                builder.enterBlock(nil, null, false, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#enterFile(AbstractSourceFile, Architecture)} throws a {@link NullPointerException} when
     * the <code>file</code> argument is <code>null</code>.
     */
    @Test
    public void enterFileNullFile() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<AbstractSourceFile<?>>("enterFile()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, AbstractSourceFile<?> nil) {
                builder.enterFile(nil, NullArchitecture.DEFAULT);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#enterNamespace(String)} throws a {@link NullPointerException} when the <code>name</code>
     * argument is <code>null</code>.
     */
    @Test
    public void enterNamespaceNullName() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<String>("enterNamespace()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, String nil) {
                builder.enterNamespace(nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#enterTransformationBlock(OutputTransformation)} throws a {@link NullPointerException}
     * when the <code>transformation</code> argument is <code>null</code>.
     */
    @Test
    public void enterTransformationBlockNullTransformation() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<OutputTransformation>("enterTransformationBlock()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, OutputTransformation nil) {
                builder.enterTransformationBlock(nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#getCustomAssemblyData(Object)} throws a {@link NullPointerException} when the
     * <code>key</code> argument is <code>null</code>.
     */
    @Test
    public void getCustomAssemblyDataNullKey() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<Object>("getCustomAssemblyData()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, Object nil) {
                builder.getCustomAssemblyData(nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#processIOException(IOException)} throws a {@link NullPointerException} when the
     * <code>exception</code> argument is <code>null</code>.
     */
    @Test
    public void processIOExceptionNullException() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<IOException>("processIOException()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, IOException nil) {
                builder.processIOException(nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>contexts</code> argument contains a <code>null</code> element.
     */
    @Test
    public void resolveSymbolReferenceListStringBooleanBooleanSymbolResolutionFallbackNullContext() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<SymbolContext<?>>(
                "resolveSymbolReference(List, String, boolean, boolean, SymbolResolutionFallback)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, SymbolContext<?> nil) {
                builder.resolveSymbolReference(Arrays.asList(nil), "foo", false, false, null, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>contexts</code> argument is <code>null</code>.
     */
    @Test
    public void resolveSymbolReferenceListStringBooleanBooleanSymbolResolutionFallbackNullContexts() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<List<SymbolContext<?>>>(
                "resolveSymbolReference(List, String, boolean, boolean, SymbolResolutionFallback)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, List<SymbolContext<?>> nil) {
                builder.resolveSymbolReference(nil, "foo", false, false, null, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>name</code> argument is <code>null</code>.
     */
    @Test
    public void resolveSymbolReferenceListStringBooleanBooleanSymbolResolutionFallbackNullName() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<String>(
                "resolveSymbolReference(List, String, boolean, boolean, SymbolResolutionFallback)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, String nil) {
                builder.resolveSymbolReference(ImmutableList.of(SymbolContext.VALUE), nil, false, false, null, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>contexts</code> argument contains a <code>null</code> element.
     */
    @Test
    public void resolveSymbolReferenceSymbolContextArrayStringBooleanBooleanSymbolResolutionFallbackNullContext() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<SymbolContext<?>>(
                "resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolResolutionFallback)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, SymbolContext<?> nil) {
                builder.resolveSymbolReference(new SymbolContext<?>[] { nil }, "foo", false, false, null, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>contexts</code> argument is <code>null</code>.
     */
    @Test
    public void resolveSymbolReferenceSymbolContextArrayStringBooleanBooleanSymbolResolutionFallbackNullContexts() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<SymbolContext<?>[]>(
                "resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolResolutionFallback)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, SymbolContext<?>[] nil) {
                builder.resolveSymbolReference(nil, "foo", false, false, null, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>name</code> argument is <code>null</code>.
     */
    @Test
    public void resolveSymbolReferenceSymbolContextArrayStringBooleanBooleanSymbolResolutionFallbackNullName() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<String>(
                "resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolResolutionFallback)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, String nil) {
                builder.resolveSymbolReference(new SymbolContext[] { SymbolContext.VALUE }, nil, false, false, null, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>context</code> argument is <code>null</code>.
     */
    @Test
    public void resolveSymbolReferenceSymbolContextStringBooleanBooleanSymbolResolutionFallbackNullContext() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<SymbolContext<?>>(
                "resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolResolutionFallback)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, SymbolContext<?> nil) {
                builder.resolveSymbolReference(nil, "foo", false, false, null, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>name</code> argument is <code>null</code>.
     */
    @Test
    public void resolveSymbolReferenceSymbolContextStringBooleanBooleanSymbolResolutionFallbackNullName() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<String>(
                "resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolResolutionFallback)") {
            @Override
            void assembleCore3(AssemblyBuilder builder, String nil) {
                builder.resolveSymbolReference(SymbolContext.VALUE, nil, false, false, null, null);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#setCurrentEncoding(Charset)} throws a {@link NullPointerException} when the
     * <code>encoding</code> argument is <code>null</code>.
     */
    @Test
    public void setCurrentEncodingNull() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<Charset>("setCurrentEncoding()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, Charset nil) {
                builder.setCurrentEncoding(nil);
            };
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#setCustomAssemblyData(Object, CustomAssemblyData)} throws a {@link NullPointerException}
     * when the <code>customAssemblyData</code> argument is <code>null</code>.
     */
    @Test
    public void setCustomAssemblyDataNullCustomAssemblyData() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<CustomAssemblyData>("setCustomAssemblyData()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, CustomAssemblyData nil) throws IOException {
                builder.setCustomAssemblyData(AssemblyBuilderTest.class, nil);
            }
        };

        assembleNode(node);
    }

    /**
     * Asserts that {@link AssemblyBuilder#setCustomAssemblyData(Object, CustomAssemblyData)} throws a {@link NullPointerException}
     * when the <code>key</code> argument is <code>null</code>.
     */
    @Test
    public void setCustomAssemblyDataNullKey() {
        final TestSourceNode node = new TestNullPointerExceptionSourceNode<CustomAssemblyData>("setCustomAssemblyData()") {
            @Override
            void assembleCore3(AssemblyBuilder builder, CustomAssemblyData nil) throws IOException {
                builder.setCustomAssemblyData(nil, DUMMY_CUSTOM_ASSEMBLY_DATA);
            }
        };

        assembleNode(node);
    }

}
