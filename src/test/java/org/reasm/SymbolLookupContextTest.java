package org.reasm;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.reasm.AssemblyTestsCommon.createAssembly;
import static org.reasm.AssemblyTestsCommon.createNodeThatDefinesASymbol;
import static org.reasm.AssemblyTestsCommon.createNodeThatDoesNothing;
import static org.reasm.AssemblyTestsCommon.createNodeThatExitsANamespace;
import static org.reasm.AssemblyTestsCommon.step;

import java.io.IOException;
import java.util.ArrayList;

import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceNode;
import org.reasm.testhelpers.NullArchitecture;
import org.reasm.testhelpers.TestSourceNode;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link SymbolLookupContext}.
 *
 * @author Francis Gagné
 */
public class SymbolLookupContextTest extends ObjectHashCodeEqualsContract {

    private static final Value ONE = new UnsignedIntValue(1L);

    private static final Object MAIN_OBJECT;
    private static final Object OTHER_EQUAL_OBJECT;
    private static final Object ANOTHER_EQUAL_OBJECT;
    private static final Object DIFFERENT_OBJECT_0;
    private static final Object DIFFERENT_OBJECT_1;
    private static final Object DIFFERENT_OBJECT_2;
    private static final Object DIFFERENT_OBJECT_3;
    private static final Object DIFFERENT_OBJECT_4;

    static {
        final TestSourceNode nodeThatDefinesAForwardAnonymousSymbol = createNodeThatDefinesASymbol("+", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatDefinesABackwardAnonymousSymbol = createNodeThatDefinesASymbol("-", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatDefinesAForwardAnonymousSymbolConditionally = createNodeThatDefinesASymbolIfAnotherSymbolHasADeterminedValue(
                "+", "B");
        final TestSourceNode nodeThatDefinesABackwardAnonymousSymbolConditionally = createNodeThatDefinesASymbolIfAnotherSymbolHasADeterminedValue(
                "-", "A");
        final TestSourceNode nodeThatDefinesSymbolF = createNodeThatDefinesASymbolBasedOnAnotherSymbol("F", "E");
        final TestSourceNode nodeThatDefinesSymbolD = createNodeThatDefinesASymbolBasedOnAnotherSymbol("D", "C");
        final TestSourceNode nodeThatDefinesSymbolC = createNodeThatDefinesASymbolBasedOnAnotherSymbol("C", "B");
        final TestSourceNode nodeThatDefinesSymbolB = createNodeThatDefinesASymbolBasedOnAnotherSymbol("B", "A");
        final TestSourceNode nodeThatDefinesSymbolA = createNodeThatDefinesASymbol("A", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatDefinesSymbolEConditionally = createNodeThatDefinesASymbolIfAnotherSymbolHasADeterminedValue(
                "E", "D");

        final TestSourceNode nodeThatEntersANamespace = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final Object value = builder.resolveSymbolReference(SymbolContext.VALUE, "F", false, null, null).getValue();
                if (value == null) {
                    builder.enterNamespace("N");
                } else {
                    builder.enterNamespace("N2");
                }
            }
        };

        final TestSourceNode nodeThatExitsANamespace = createNodeThatExitsANamespace();
        final TestSourceNode nodeThatDoesNothing = createNodeThatDoesNothing();

        final ArrayList<SourceNode> childNodes = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            childNodes.add(nodeThatDefinesAForwardAnonymousSymbol);
            childNodes.add(nodeThatDefinesABackwardAnonymousSymbol);
        }

        childNodes.add(nodeThatDefinesAForwardAnonymousSymbol);
        childNodes.add(nodeThatDefinesABackwardAnonymousSymbolConditionally);
        childNodes.add(nodeThatDefinesAForwardAnonymousSymbolConditionally);
        childNodes.add(nodeThatDefinesSymbolF);
        childNodes.add(nodeThatDefinesSymbolD);
        childNodes.add(nodeThatDefinesSymbolC);
        childNodes.add(nodeThatDefinesSymbolB);
        childNodes.add(nodeThatDefinesSymbolA);
        childNodes.add(nodeThatDefinesSymbolEConditionally);
        childNodes.add(nodeThatEntersANamespace);
        childNodes.add(nodeThatExitsANamespace);
        childNodes.add(nodeThatDoesNothing);

        final SourceNode rootNode = new SimpleCompositeSourceNode(childNodes);
        final Assembly assembly = createAssembly(rootNode);
        final int pendingStepsPerPassBeforeCapture = 23;
        final int pendingStepsPerPassAfterCapture = 1;

        manyPendingSteps(assembly, pendingStepsPerPassBeforeCapture);

        MAIN_OBJECT = assembly.getCurrentSymbolLookupContext();
        OTHER_EQUAL_OBJECT = assembly.getCurrentSymbolLookupContext();
        assertThat(OTHER_EQUAL_OBJECT, is(not(sameInstance(MAIN_OBJECT))));
        ANOTHER_EQUAL_OBJECT = assembly.getCurrentSymbolLookupContext();

        manyPendingSteps(assembly, pendingStepsPerPassAfterCapture);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        manyPendingSteps(assembly, pendingStepsPerPassBeforeCapture);

        // This SymbolLookupContext has a different backCounter.
        DIFFERENT_OBJECT_0 = assembly.getCurrentSymbolLookupContext();

        manyPendingSteps(assembly, pendingStepsPerPassAfterCapture);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        manyPendingSteps(assembly, pendingStepsPerPassBeforeCapture);

        // This SymbolLookupContext has a different forwCounter.
        DIFFERENT_OBJECT_1 = assembly.getCurrentSymbolLookupContext();

        manyPendingSteps(assembly, pendingStepsPerPassAfterCapture);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        manyPendingSteps(assembly, pendingStepsPerPassBeforeCapture);

        // This SymbolLookupContext has a different scopeKey.
        DIFFERENT_OBJECT_2 = assembly.getCurrentSymbolLookupContext();

        manyPendingSteps(assembly, pendingStepsPerPassAfterCapture);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        manyPendingSteps(assembly, pendingStepsPerPassBeforeCapture);

        // This SymbolLookupContext has a different namespace.
        DIFFERENT_OBJECT_3 = assembly.getCurrentSymbolLookupContext();

        manyPendingSteps(assembly, pendingStepsPerPassAfterCapture);
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        final int numberOfPasses = 5;
        nodeThatDefinesAForwardAnonymousSymbol.assertAssembleCount(7 * numberOfPasses);
        nodeThatDefinesABackwardAnonymousSymbol.assertAssembleCount(6 * numberOfPasses);
        nodeThatDefinesAForwardAnonymousSymbolConditionally.assertAssembleCount(1 * numberOfPasses);
        nodeThatDefinesABackwardAnonymousSymbolConditionally.assertAssembleCount(1 * numberOfPasses);
        nodeThatDefinesSymbolF.assertAssembleCount(1 * numberOfPasses);
        nodeThatDefinesSymbolD.assertAssembleCount(1 * numberOfPasses);
        nodeThatDefinesSymbolC.assertAssembleCount(1 * numberOfPasses);
        nodeThatDefinesSymbolB.assertAssembleCount(1 * numberOfPasses);
        nodeThatDefinesSymbolA.assertAssembleCount(1 * numberOfPasses);
        nodeThatDefinesSymbolEConditionally.assertAssembleCount(1 * numberOfPasses);
        nodeThatEntersANamespace.assertAssembleCount(1 * numberOfPasses);
        nodeThatExitsANamespace.assertAssembleCount(1 * numberOfPasses);
        nodeThatDoesNothing.assertAssembleCount(1 * numberOfPasses);
    }

    static {
        final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile("", null),
                NullArchitecture.DEFAULT);
        final Assembly assembly = new Assembly(configuration);

        // This SymbolLookupContext has a different assembly.
        DIFFERENT_OBJECT_4 = assembly.getCurrentSymbolLookupContext();
    }

    private static TestSourceNode createNodeThatDefinesASymbolBasedOnAnotherSymbol(final String nameOfSymbolToDefine,
            final String nameOfReferencedSymbol) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final Object value = builder.resolveSymbolReference(SymbolContext.VALUE, nameOfReferencedSymbol, false, null, null)
                        .getValue();

                if (value != null) {
                    assertThat(value, is(instanceOf(Value.class)));
                }

                builder.defineSymbol(SymbolContext.VALUE, nameOfSymbolToDefine, false, SymbolType.CONSTANT, (Value) value);
            }
        };
    }

    private static TestSourceNode createNodeThatDefinesASymbolIfAnotherSymbolHasADeterminedValue(final String nameOfSymbolToDefine,
            final String nameOfReferencedSymbol) {
        final Value one = ONE;
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final Object value = builder.resolveSymbolReference(SymbolContext.VALUE, nameOfReferencedSymbol, false, null, null)
                        .getValue();
                if (value != null) {
                    builder.defineSymbol(SymbolContext.VALUE, nameOfSymbolToDefine, false, SymbolType.CONSTANT, one);
                }
            }
        };
    }

    private static void manyPendingSteps(Assembly assembly, int steps) {
        for (int i = 0; i < steps; i++) {
            step(assembly, AssemblyCompletionStatus.PENDING);
        }
    }

    /**
     * Initializes a new SymbolLookupContextTest.
     */
    public SymbolLookupContextTest() {
        super(MAIN_OBJECT, OTHER_EQUAL_OBJECT, ANOTHER_EQUAL_OBJECT, new Object[] { DIFFERENT_OBJECT_0, DIFFERENT_OBJECT_1,
                DIFFERENT_OBJECT_2, DIFFERENT_OBJECT_3, DIFFERENT_OBJECT_4, new Object() });
    }

}
