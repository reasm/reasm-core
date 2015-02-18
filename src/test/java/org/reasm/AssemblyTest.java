package org.reasm;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.reasm.AssemblyTestsCommon.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.reasm.messages.*;
import org.reasm.source.AbstractSourceFile;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNode;
import org.reasm.testhelpers.*;

import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

/**
 * Test class for {@link Assembly}, {@link AssemblyBuilder}, {@link AssemblyStep}, {@link AssemblyStepLocationGenerator},
 * {@link Namespace}, {@link Scope}, {@link SymbolDefinitionResolutionFallback}, {@link SymbolLookupContext},
 * {@link SymbolReference}, {@link TransformationBlock} and {@link UserSymbol}.
 *
 * @author Francis Gagné
 */
public class AssemblyTest {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static final EquivalentAssemblyMessage A_WARNING = new EquivalentAssemblyMessage(createWarning());
    private static final EquivalentAssemblyMessage AN_ERROR = new EquivalentAssemblyMessage(createError());

    static final UnsignedIntValue ONE = new UnsignedIntValue(1L);
    static final UnsignedIntValue TWO = new UnsignedIntValue(2L);
    static final UnsignedIntValue THREE = new UnsignedIntValue(3L);
    private static final SignedIntValue MINUS_SEVENTY_EIGHT = new SignedIntValue(-78L);
    private static final FloatValue THREE_POINT_TWENTY_FIVE = new FloatValue(3.25);

    static final SymbolContext<Object> DUMMY_SYMBOL_CONTEXT = new SymbolContext<>(Object.class);

    static AssemblyErrorMessage createError() {
        return new AssemblyErrorMessage("test") {
        };
    }

    static AssemblyWarningMessage createWarning() {
        return new AssemblyWarningMessage("test") {
        };
    }

    private static void checkSymbol(String symbolName, SymbolType symbolType, Assembly assembly,
            Collection<Matcher<? super UserSymbol>> symbols) {
        final SymbolReference symbolReference = assembly.resolveSymbolReference(SymbolContext.VALUE, symbolName, false, false,
                null, null);
        final Symbol symbol = symbolReference.getSymbol();
        assertThat(symbol, is(notNullValue()));
        assertThat(symbol, is(instanceOf(UserSymbol.class)));
        assertThat((UserSymbol) symbol, is(new UserSymbolMatcher<>(SymbolContext.VALUE, symbolName, symbolType, FORTY_TWO)));
        symbols.add(is(sameInstance(symbol)));
    }

    private static TestSourceNode createNodeThatAddsAnError() {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.addMessage(createError());
            }
        };
    }

    private static TestSourceNode createNodeThatAddsAWarning() {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.addMessage(createWarning());
            }
        };
    }

    private static TestSourceNode createNodeThatEmitsAByte(final byte b) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.appendAssembledData(b);
            }
        };
    }

    private static TestSourceNode createNodeThatEndsThePass() {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.endPass();
            }
        };
    }

    private static TestSourceNode createNodeThatReferencesASymbol(final String identifier) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.resolveSymbolReference(SymbolContext.VALUE, identifier, false, false, null, null);
            }
        };
    }

    private static TestSourceNode createNodeThatReferencesASymbolAndExpectsAUserSymbol(final String identifier,
            final String symbolName, final SymbolType type, final Value value) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final SymbolReference symbolReference = builder.resolveSymbolReference(SymbolContext.VALUE, identifier, false,
                        false, null, null);
                assertThat(symbolReference.isLocal(), is(false));
                final Symbol symbol = symbolReference.getSymbol();
                assertThat(symbol, is(notNullValue()));
                assertThat(symbol, is(instanceOf(UserSymbol.class)));
                assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, symbolName, type, value));
                assertThat(symbolReference.getValue(), is((Object) value));
            }
        };
    }

    private static TestSourceNode createNodeThatReferencesASymbolAndExpectsAUserSymbolOnTheSecondPassOnly(final String identifier,
            final String symbolName, final SymbolType type, final Value value) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final Symbol symbol = builder.resolveSymbolReference(SymbolContext.VALUE, identifier, false, false, null, null)
                        .getSymbol();
                if (builder.getAssembly().getCurrentPass() == 1) {
                    assertThat(symbol, is(nullValue()));
                } else {
                    assertThat(symbol, is(notNullValue()));
                    assertThat(symbol, is(instanceOf(UserSymbol.class)));
                    assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, symbolName, type, value));
                }
            }
        };
    }

    private static TestSourceNode createNodeThatReferencesASymbolAndExpectsNoSymbol(final String identifier) {
        return new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final SymbolReference symbolReference = builder.resolveSymbolReference(SymbolContext.VALUE, identifier, false,
                        false, null, null);
                assertThat(symbolReference.isLocal(), is(false));
                assertThat(symbolReference.getSymbol(), is(nullValue()));
                assertThat(symbolReference.getValue(), is(nullValue()));
            }
        };
    }

    private static void defineSymbol(@Nonnull final String sourceSymbolName, @Nonnull final String... actualSymbolNames) {
        defineSymbol(sourceSymbolName, SymbolType.CONSTANT, actualSymbolNames);
    }

    private static void defineSymbol(@Nonnull final String sourceSymbolName, @Nonnull final SymbolType symbolType,
            @Nonnull final String... actualSymbolNames) {
        final TestSourceNode nodeThatDefinesASymbol = createNodeThatDefinesASymbol(sourceSymbolName, symbolType, FORTY_TWO);
        final Assembly assembly = createAssembly(nodeThatDefinesASymbol);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatDefinesASymbol.assertAssembleCount(1);

        final Collection<Matcher<? super UserSymbol>> symbols = new ArrayList<>();
        for (String actualSymbolName : actualSymbolNames) {
            checkSymbol(actualSymbolName, symbolType, assembly, symbols);
        }

        assertThat(assembly.getSymbols(), containsInAnyOrder(symbols));
    }

    private static void defineSymbolIllegalName(@Nonnull final String name) {
        final TestSourceNode nodeThatDefinesASymbol = createNodeThatDefinesASymbol(name, SymbolType.CONSTANT, FORTY_TWO);
        final Assembly assembly = createAssembly(nodeThatDefinesASymbol);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new IllegalSymbolNameErrorMessage(name))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        nodeThatDefinesASymbol.assertAssembleCount(1);
        assertThat(assembly.getSymbols(), is(emptyIterable()));
    }

    private static void redefineError(@Nonnull SymbolType symbolTypeOnRedefinition,
            @Nonnull Function<UserSymbol, AssemblyMessage> redefinitionErrorMessageFactory) {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatDefinesTheFooSymbolAgain = createNodeThatDefinesASymbol("foo", symbolTypeOnRedefinition, TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatDefinesTheFooSymbolAgain));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        final Symbol fooSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null).getSymbol();
        assertThat(fooSymbol, is(notNullValue()));
        assertThat(fooSymbol, is(instanceOf(UserSymbol.class)));
        final UserSymbol fooUserSymbol = (UserSymbol) fooSymbol;
        assertThat(fooUserSymbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, ONE));

        final List<AssemblyMessage> messages = assembly.getMessages();
        final List<AssemblyStep> steps = assembly.getSteps();

        final EquivalentAssemblyMessage symbolRedefinedError = new EquivalentAssemblyMessage(
                redefinitionErrorMessageFactory.apply(fooUserSymbol));
        final EquivalentAssemblyMessage symbolDefineHereInformation = new EquivalentAssemblyMessage(
                new SymbolDefinedHereInformationMessage(fooUserSymbol));
        assertThat(messages,
                contains(ImmutableList.<Matcher<? super AssemblyMessage>> of(symbolRedefinedError, symbolDefineHereInformation)));
        assertThat(messages.get(0).getStep(), is(sameInstance(steps.get(2))));
        assertThat(messages.get(1).getStep(), is(sameInstance(steps.get(1))));

        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
        nodeThatDefinesTheFooSymbolAgain.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#Assembly(Configuration)} throws a {@link NullPointerException} when the
     * <code>configuration</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void assemblyNullConfiguration() {
        new Assembly(null);
    }

    /**
     * Asserts that {@link Assembly#Assembly(Configuration)} properly initializes an {@link Assembly}.
     */
    @Test
    public void constructorInitializesAssembly() {
        final Configuration configuration = new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT);
        final Assembly assembly = new Assembly(configuration);
        assertThat(assembly.getConfiguration(), is(sameInstance(configuration)));
        assertThat(assembly.getCurrentEncoding(), is(Charset.forName("UTF-8")));
        assertThat(assembly.getCurrentPass(), is(1));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getSymbols(), is(emptyIterable()));
        assertThat(assembly.getProgramCounter(), is(0L));
        assertThat(assembly.getSteps(), is(empty()));
    }

    /**
     * Asserts that {@link Assembly#Assembly(Configuration)}, given a configuration that has predefined symbols, defines user
     * symbols corresponding to the predefined symbols.
     */
    @Test
    public void constructorWithConfigurationWithPredefinedSymbolsDefinesCorrespondingUserSymbols() {
        final PredefinedSymbol predefinedSymbol1 = new PredefinedSymbol(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, FORTY_TWO);
        final PredefinedSymbol predefinedSymbol2 = new PredefinedSymbol(SymbolContext.VALUE, "bar", SymbolType.CONSTANT,
                MINUS_SEVENTY_EIGHT);
        final PredefinedSymbol predefinedSymbol3 = new PredefinedSymbol(SymbolContext.VALUE, "baz", SymbolType.VARIABLE,
                THREE_POINT_TWENTY_FIVE);
        final PredefinedSymbolTable predefinedSymbols = new PredefinedSymbolTable(Arrays.asList(predefinedSymbol1,
                predefinedSymbol2, predefinedSymbol3));
        final Configuration configuration = new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT)
                .setPredefinedSymbols(predefinedSymbols);
        final Assembly assembly = new Assembly(configuration);
        assertThat(assembly.getConfiguration(), is(sameInstance(configuration)));
        assertThat(assembly.getCurrentPass(), is(1));
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        assertThat(assembly.getSymbols(), containsInAnyOrder(ImmutableList.<Matcher<? super UserSymbol>> of(
                new UserSymbolFromPredefinedSymbolMatcher(predefinedSymbol1), new UserSymbolFromPredefinedSymbolMatcher(
                        predefinedSymbol2), new UserSymbolFromPredefinedSymbolMatcher(predefinedSymbol3))));
        assertThat(assembly.getProgramCounter(), is(0L));
        assertThat(assembly.getSteps(), is(empty()));
    }

    /**
     * Asserts that {@link Assembly#setCustomAssemblyData(Object, CustomAssemblyData)} adds a custom assembly data object to the
     * assembly, that {@link Assembly#getCustomAssemblyData(Object)} can retrieve it later, and that
     * {@link CustomAssemblyData#startedNewPass()} and {@link CustomAssemblyData#completed()} are called at the appropriate time.
     */
    @Test
    public void customAssemblyData() {
        // I need a couple of mutable booleans
        final AtomicBoolean ranStartedNewPass = new AtomicBoolean();
        final AtomicBoolean ranCompleted = new AtomicBoolean();

        final Object key = new Object();
        final CustomAssemblyData customAssemblyData = new CustomAssemblyData() {
            @Override
            public void completed() {
                assertThat(ranCompleted.get(), is(false));
                ranCompleted.set(true);
            }

            @Override
            public void startedNewPass() {
                assertThat(ranStartedNewPass.get(), is(false));
                ranStartedNewPass.set(true);
            }
        };

        final TestSourceNode nodeThatSetsCustomAssemblyData = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.setCustomAssemblyData(key, customAssemblyData);
            }
        };

        final TestSourceNode nodeThatGetsCustomAssemblyData = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                assertThat(builder.getCustomAssemblyData(key), is(customAssemblyData));
            }
        };

        final TestSourceNode nodeThatReferencesTheFooSymbolBeforeItsDefinition = createNodeThatReferencesASymbol("foo");

        final TestSourceNode nodeThatDefinesTheFooSymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.defineSymbol(SymbolContext.VALUE, "foo", false, SymbolType.CONSTANT, FORTY_TWO);
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatSetsCustomAssemblyData,
                nodeThatGetsCustomAssemblyData, nodeThatReferencesTheFooSymbolBeforeItsDefinition, nodeThatDefinesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(ranStartedNewPass.get(), is(false));
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        assertThat(ranStartedNewPass.get(), is(true));

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(ranCompleted.get(), is(false));
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(ranCompleted.get(), is(true));

        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatSetsCustomAssemblyData.assertAssembleCount(2);
        nodeThatGetsCustomAssemblyData.assertAssembleCount(2);
        nodeThatReferencesTheFooSymbolBeforeItsDefinition.assertAssembleCount(2);
        nodeThatDefinesTheFooSymbol.assertAssembleCount(2);
    }

    /**
     * Asserts that a symbol defined as a local symbol is defined in a scope rather than in the global symbol table.
     */
    @Test
    public void defineLocalSymbol() {
        final TestSourceNode nodeThatDefinesALocalSymbol = createNodeThatDefinesASymbol("l", true, SymbolType.CONSTANT, FORTY_TWO);
        final Assembly assembly = createAssembly(nodeThatDefinesALocalSymbol);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatDefinesALocalSymbol.assertAssembleCount(1);

        assertThat(assembly.getSymbols(), is(emptyIterable()));

        final Scope scope = assembly.getScopes().get(null);
        assertThat(scope, is(notNullValue()));

        assertThat(scope.getLocalSymbols(), contains(new UserSymbolMatcher<>(SymbolContext.VALUE, "l", SymbolType.CONSTANT,
                FORTY_TWO)));

        final SymbolReference symbolReference = assembly.resolveSymbolReference(SymbolContext.VALUE, "l", true, false, null, null);
        final Symbol symbol = symbolReference.getSymbol();
        assertThat(symbol, is(notNullValue()));
        assertThat(symbol, is(instanceOf(UserSymbol.class)));
        assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "l", SymbolType.CONSTANT, FORTY_TWO));
        assertThat(symbolReference.isLocal(), is(true));
        assertThat(symbolReference.getValue(), is((Object) FORTY_TWO));
    }

    /**
     * Asserts that a symbol defined as a local symbol after a non-local symbol is defined in a scope identified by the non-local
     * symbol.
     */
    @Test
    public void defineLocalSymbolAfterNonLocalSymbol() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol("bar", true, SymbolType.CONSTANT, FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatDefinesTheBarSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(1);

        final Symbol fooSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null).getSymbol();
        assertThat(assembly.getSymbols(), contains(fooSymbol));

        final AssemblyStepLocation assemblyStepLocation = assembly.getSteps().get(1).getLocation();
        assertThat(assembly.getScopes().keySet(), contains(assemblyStepLocation));

        final Scope scope = assembly.getScopes().get(assemblyStepLocation);
        assertThat(scope, is(notNullValue()));

        assertThat(scope.getLocalSymbols(), contains(new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT,
                FORTY_TWO)));
    }

    /**
     * Asserts that a symbol defined with a '.' as the first character of its name (a "suffix symbol") has the name of the last
     * non-suffix symbol prefixed to its name.
     */
    @Test
    public void defineSuffixSymbol() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", false, SymbolType.CONSTANT,
                FORTY_TWO);
        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol(".bar", false, SymbolType.CONSTANT,
                FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatDefinesTheBarSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(1);

        final Collection<Matcher<? super UserSymbol>> symbols = new ArrayList<>();

        checkSymbol("foo", SymbolType.CONSTANT, assembly, symbols);
        checkSymbol("foo.bar", SymbolType.CONSTANT, assembly, symbols);

        assertThat(assembly.getSymbols(), containsInAnyOrder(symbols));
    }

    /**
     * Asserts that a suffix symbol defined before any non-suffix symbol has been defined has nothing prefixed to its name.
     */
    @Test
    public void defineSuffixSymbolBeforeNonSuffixSymbol() {
        defineSymbol(".foo", ".foo");
    }

    /**
     * Asserts that a symbol defined with name "a" is not treated specially.
     */
    @Test
    public void defineSymbolA() {
        defineSymbol("a", "a");
    }

    /**
     * Asserts that a symbol defined with name "-" is defined as a backward anonymous reference.
     */
    @Test
    public void defineSymbolBackwardAnonymousReference() {
        defineSymbol("-", "__back1");
    }

    /**
     * Asserts that a symbol defined with name "/" is defined as a both a forward and a backward anonymous reference.
     */
    @Test
    public void defineSymbolBidirectionalAnonymousReference() {
        defineSymbol("/", "__forw1", "__back1");
    }

    /**
     * Asserts that a symbol defined with name "+" is defined as a forward anonymous reference.
     */
    @Test
    public void defineSymbolForwardAnonymousReference() {
        defineSymbol("+", "__forw1");
    }

    /**
     * Asserts that a symbol defined with name "--" is an error.
     */
    @Test
    public void defineSymbolMinusMinus() {
        defineSymbolIllegalName("--");
    }

    /**
     * Asserts that a symbol defined with name "++" is an error.
     */
    @Test
    public void defineSymbolPlusPlus() {
        defineSymbolIllegalName("++");
    }

    /**
     * Asserts that a symbol defined with name "//" is not defined as a pair of anonymous references.
     */
    @Test
    public void defineSymbolSlashSlash() {
        defineSymbol("//", "//");
    }

    /**
     * Asserts that two local symbols defined without a non-local symbol being defined in-between are defined in the same scope.
     */
    @Test
    public void defineTwoLocalSymbols() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", true, SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol("bar", true, SymbolType.CONSTANT, FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatDefinesTheBarSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(1);

        assertThat(assembly.getSymbols(), is(emptyIterable()));

        assertThat(assembly.getScopes().keySet(), contains(nullValue()));

        final Scope scope = assembly.getScopes().get(null);
        assertThat(scope, is(notNullValue()));

        assertThat(scope.getLocalSymbols(), containsInAnyOrder(ImmutableList.<Matcher<? super UserSymbol>> of(
                new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, FORTY_TWO), new UserSymbolMatcher<>(
                        SymbolContext.VALUE, "bar", SymbolType.CONSTANT, FORTY_TWO))));
    }

    /**
     * Asserts that two suffix symbols defined without a non-suffix symbol being defined in-between have the same prefix.
     */
    @Test
    public void defineTwoSuffixSymbols() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", false, SymbolType.CONSTANT,
                FORTY_TWO);
        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol(".bar", false, SymbolType.CONSTANT,
                FORTY_TWO);
        final TestSourceNode nodeThatDefinesTheBazSymbol = createNodeThatDefinesASymbol(".baz", false, SymbolType.CONSTANT,
                FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatDefinesTheBarSymbol, nodeThatDefinesTheBazSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(1);
        nodeThatDefinesTheBazSymbol.assertAssembleCount(1);

        final Collection<Matcher<? super UserSymbol>> symbols = new ArrayList<>();

        checkSymbol("foo", SymbolType.CONSTANT, assembly, symbols);
        checkSymbol("foo.bar", SymbolType.CONSTANT, assembly, symbols);
        checkSymbol("foo.baz", SymbolType.CONSTANT, assembly, symbols);

        assertThat(assembly.getSymbols(), containsInAnyOrder(symbols));
    }

    /**
     * Asserts that a symbol defined as a variable is defined as a variable.
     */
    @Test
    public void defineVariable() {
        defineSymbol("v", SymbolType.VARIABLE, "v");
    }

    /**
     * Asserts that a symbol that disappears on the second pass triggers a third pass.
     */
    @Test
    public void disappearingSymbol() {
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbol("foo");

        final TestSourceNode nodeThatDefinesASymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                if (builder.getAssembly().getCurrentPass() == 1) {
                    builder.defineSymbol(SymbolContext.VALUE, "foo", false, SymbolType.CONSTANT, ONE);
                } else {
                    builder.defineSymbol(SymbolContext.VALUE, "bar", false, SymbolType.CONSTANT, TWO);
                }
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatReferencesTheFooSymbol,
                nodeThatDefinesASymbol));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        assertThat(assembly.getMessages(),
                contains(new EquivalentAssemblyMessage(new UnresolvedSymbolReferenceErrorMessage("foo"))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
    }

    /**
     * Asserts that {@link Assembly#endPass(AssemblyStep)} causes the current pass to end immediately.
     */
    @Test
    public void endPass() {
        final TestSourceNode nodeThatEndsThePass = createNodeThatEndsThePass();

        final SimpleCompositeSourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEndsThePass,
                NODE_THAT_SHOULD_NOT_BE_REACHED));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING); // root node
        step(assembly, AssemblyCompletionStatus.COMPLETE); // end pass node
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatEndsThePass.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#endPass(AssemblyStep)} calls {@link Block#exitBlock()} on the blocks still in the block stack.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void endPassUnwindsBlockStack() throws IOException {
        final TestSourceNode nodeThatEntersATransformationBlock = createNodeThatEntersATransformationBlock(IdentityTransformation.INSTANCE);

        final TestSourceNode nodeThatEmitsData = createNodeThatEmitsAByte((byte) 1);

        final TestSourceNode nodeThatEndsThePass = createNodeThatEndsThePass();

        final TestSourceNode nodeThatExitsATransformationBlock = createNodeThatExitsATransformationBlock();

        final TestCompositeSourceNode block = new TestCompositeSourceNode(Arrays.asList(nodeThatEntersATransformationBlock,
                nodeThatEmitsData, nodeThatEndsThePass, NODE_THAT_SHOULD_NOT_BE_REACHED, nodeThatExitsATransformationBlock)) {
            @Override
            protected void assembleCore2(final AssemblyBuilder builder) throws IOException {
                builder.enterComposite(true, new BlockEvents() {
                    @Override
                    public void exitBlock() throws IOException {
                        builder.exitTransformationBlock();
                    }
                });
            }
        };

        final SimpleCompositeSourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(block));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        block.assertAssembleCount(1);
        nodeThatEntersATransformationBlock.assertAssembleCount(1);
        nodeThatEndsThePass.assertAssembleCount(1);
        nodeThatExitsATransformationBlock.assertAssembleCount(0);

        // A transformation block intercepts the output.
        // Output is only written to the assembly when exiting the transformation block.
        // This checks that the transformation block has been exited.
        checkOutput(assembly, new byte[] { 1 });
    }

    /**
     * Asserts that {@link Assembly#enterBlock(Iterable, AssemblyStepIterationController, AssemblyStep, boolean, BlockEvents)}, when
     * called with a non-null {@link BlockEvents}, enters a block that references that {@link BlockEvents}.
     */
    @Test
    public void enterBlockWithEvents() {
        final BlockEvents blockEvents = new BlockEvents();

        final TestSourceNode nodeThatChecksTheCurrentBlock = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) {
                final Block currentBlock = builder.getCurrentBlock();
                assertThat(currentBlock, is(not(nullValue())));
                assertThat(currentBlock.getEvents(), is(sameInstance(blockEvents)));
            }
        };

        final TestCompositeSourceNode nodeThatEntersABlockWithEvents = new TestCompositeSourceNode(
                Arrays.asList(nodeThatChecksTheCurrentBlock)) {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) {
                assertThat(builder.getCurrentBlock(), is(not(nullValue())));

                builder.enterBlock(builder.getStep().getLocation().getSourceLocation().getChildSourceLocations(), null, false,
                        blockEvents);
            }
        };

        final Assembly assembly = createAssembly(nodeThatEntersABlockWithEvents);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersABlockWithEvents.assertAssembleCount(1);
        nodeThatChecksTheCurrentBlock.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#enterBlock(Iterable, AssemblyStepIterationController, AssemblyStep, boolean, BlockEvents)}, when
     * called with a non-null {@link BlockEvents} that overrides {@link BlockEvents#exitBlock()}, enters a block that will call that
     * overridden method when exiting the block.
     */
    @Test
    public void enterBlockWithExitBlockEvent() {
        final AtomicBoolean ranExitBlock = new AtomicBoolean();

        final BlockEvents blockEvents = new BlockEvents() {
            @Override
            public void exitBlock() throws IOException {
                ranExitBlock.set(true);
            }
        };

        final TestSourceNode nodeThatChecksTheCurrentBlock = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) {
                final Block currentBlock = builder.getCurrentBlock();
                assertThat(currentBlock, is(not(nullValue())));
                assertThat(currentBlock.getEvents(), is(sameInstance(blockEvents)));
            }
        };

        final TestCompositeSourceNode nodeThatEntersABlockWithEvents = new TestCompositeSourceNode(
                Collections.singleton(nodeThatChecksTheCurrentBlock)) {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) {
                assertThat(builder.getCurrentBlock(), is(not(nullValue())));

                builder.enterBlock(builder.getStep().getLocation().getSourceLocation().getChildSourceLocations(), null, false,
                        blockEvents);
            }
        };

        final TestSourceNode nodeThatChecksThatTheBlockHasExited = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                assertThat(ranExitBlock.get(), is(true));
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersABlockWithEvents,
                nodeThatChecksThatTheBlockHasExited));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersABlockWithEvents.assertAssembleCount(1);
        nodeThatChecksTheCurrentBlock.assertAssembleCount(1);
        nodeThatChecksThatTheBlockHasExited.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#enterBlock(Iterable, AssemblyStepIterationController, AssemblyStep, boolean, BlockEvents)}, when
     * called with a non-null {@link AssemblyStepIterationController}, enters a block over a sequence of {@link SourceLocation}s
     * that can be assembled a variable number of times.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void enterBlockWithIterationController() throws IOException {
        final TestSourceNode nodeThatEmitsData = createNodeThatEmitsAByte((byte) 1);

        final TestCompositeSourceNode nodeThatEntersABlockWithAnIterationController = new TestCompositeSourceNode(
                Arrays.asList(nodeThatEmitsData)) {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final AssemblyStepIterationController iterationController = new AssemblyStepIterationController() {
                    private int count = -1;

                    @Override
                    public boolean hasNextIteration() {
                        return ++this.count < 3;
                    }
                };

                builder.enterBlock(builder.getStep().getLocation().getSourceLocation().getChildSourceLocations(),
                        iterationController, false, null);
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersABlockWithAnIterationController));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersABlockWithAnIterationController.assertAssembleCount(1);
        nodeThatEmitsData.assertAssembleCount(3);

        checkOutput(assembly, new byte[] { 1, 1, 1 });

        final List<AssemblyStep> steps = assembly.getSteps();
        final byte[] oneOne = new byte[] { 1 };
        checkOutput(steps.get(0), EMPTY_BYTE_ARRAY);
        checkOutput(steps.get(1), EMPTY_BYTE_ARRAY);
        checkOutput(steps.get(2), oneOne);
        checkOutput(steps.get(3), oneOne);
        checkOutput(steps.get(4), oneOne);
    }

    /**
     * Asserts that {@link Assembly#enterBlock(Iterable, AssemblyStepIterationController, AssemblyStep, boolean, BlockEvents)}, when
     * called with a non-null {@link AssemblyStepIterationController} whose
     * {@link AssemblyStepIterationController#hasNextIteration()} methods returns <code>false</code> on the first call, enters a
     * block over a sequence of {@link SourceLocation}s that is never assembled.
     */
    @Test
    public void enterBlockWithZeroIterations() {
        final TestCompositeSourceNode nodeThatEntersABlockWithAnIterationController = new TestCompositeSourceNode(
                Arrays.asList(NODE_THAT_SHOULD_NOT_BE_REACHED)) {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final AssemblyStepIterationController iterationController = new AssemblyStepIterationController() {
                    @Override
                    public boolean hasNextIteration() {
                        return false;
                    }
                };

                builder.enterBlock(builder.getStep().getLocation().getSourceLocation().getChildSourceLocations(),
                        iterationController, false, null);
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersABlockWithAnIterationController));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersABlockWithAnIterationController.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#enterComposite(AssemblyStep, boolean, BlockEvents)} enters a block for the child source
     * locations of a {@link CompositeSourceNode}.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void enterComposite() throws IOException {
        final TestSourceNode node1 = createNodeThatEmitsAByte((byte) 1);
        final TestSourceNode node2 = createNodeThatEmitsAByte((byte) 2);
        final TestSourceNode node3 = createNodeThatEmitsAByte((byte) 3);

        final SourceNode nonTransparentCompositeNode = new CompositeSourceNode(Arrays.asList(node2, node3), null) {
            @Override
            protected void assembleCore(AssemblyBuilder builder) throws IOException {
                builder.enterComposite(false, null);
            }
        };

        final SourceNode transparentCompositeNode = new SimpleCompositeSourceNode(Arrays.asList(node1, nonTransparentCompositeNode));

        final Assembly assembly = createAssembly(transparentCompositeNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        node1.assertAssembleCount(1);
        node2.assertAssembleCount(1);
        node3.assertAssembleCount(1);

        checkOutput(assembly, new byte[] { 1, 2, 3 });

        final List<AssemblyStep> steps = assembly.getSteps();
        AssemblyStep step;
        AssemblyStepLocation stepLocation;
        SourceLocation sourceLocation;

        step = steps.get(0);
        final AssemblyStepLocation parentStepLocation = step.getLocation();
        stepLocation = parentStepLocation;
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getSourceNode(), is(transparentCompositeNode));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(nullValue()));
        assertThat(stepLocation.isParentTransparent(), is(false));
        checkOutput(step, EMPTY_BYTE_ARRAY);

        step = steps.get(1);
        stepLocation = step.getLocation();
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getSourceNode(), is((SourceNode) node1));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(parentStepLocation));
        assertThat(stepLocation.isParentTransparent(), is(true));
        checkOutput(step, new byte[] { 1 });

        step = steps.get(2);
        final AssemblyStepLocation parentStepLocation2 = step.getLocation();
        stepLocation = parentStepLocation2;
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getSourceNode(), is(nonTransparentCompositeNode));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(parentStepLocation));
        assertThat(stepLocation.isParentTransparent(), is(true));
        checkOutput(step, EMPTY_BYTE_ARRAY);

        step = steps.get(3);
        stepLocation = step.getLocation();
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getSourceNode(), is((SourceNode) node2));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(parentStepLocation2));
        assertThat(stepLocation.isParentTransparent(), is(false));
        checkOutput(step, new byte[] { 2 });

        step = steps.get(4);
        stepLocation = step.getLocation();
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getSourceNode(), is((SourceNode) node3));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(parentStepLocation2));
        assertThat(stepLocation.isParentTransparent(), is(false));
        checkOutput(step, new byte[] { 3 });
    }

    /**
     * Asserts that {@link Assembly#enterFile(AbstractSourceFile, Architecture, AssemblyStep)} enters a block for an
     * {@link AbstractSourceFile}.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void enterFile() throws IOException {
        final SourceFile mainFile = new SourceFile("", "main.asm");
        final SourceFile childFile = new SourceFile("", "child.asm");

        final TestSourceNode nodeThatOutputsData = createNodeThatEmitsAByte((byte) 0x12);

        final Architecture childFileArchitecture = new TestArchitecture(nodeThatOutputsData);

        final TestSourceNode nodeThatEntersAFile = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.enterFile(childFile, childFileArchitecture);
            }
        };

        final Architecture mainFileArchitecture = new TestArchitecture(nodeThatEntersAFile);

        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, mainFile, mainFileArchitecture));

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersAFile.assertAssembleCount(1);
        nodeThatOutputsData.assertAssembleCount(1);
        checkOutput(assembly, new byte[] { 0x12 });

        final List<AssemblyStep> steps = assembly.getSteps();
        AssemblyStep step;
        AssemblyStepLocation stepLocation;
        SourceLocation sourceLocation;

        step = steps.get(0);
        final AssemblyStepLocation parentStepLocation = step.getLocation();
        stepLocation = parentStepLocation;
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getFile(), is((Object) mainFile));
        assertThat(sourceLocation.getArchitecture(), is(mainFileArchitecture));
        assertThat(sourceLocation.getSourceNode(), is((SourceNode) nodeThatEntersAFile));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(nullValue()));
        checkOutput(step, EMPTY_BYTE_ARRAY);

        step = steps.get(1);
        stepLocation = step.getLocation();
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getFile(), is((Object) childFile));
        assertThat(sourceLocation.getArchitecture(), is(childFileArchitecture));
        assertThat(sourceLocation.getSourceNode(), is((SourceNode) nodeThatOutputsData));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(parentStepLocation));
        checkOutput(step, new byte[] { 0x12 });
    }

    /**
     * Asserts that {@link Assembly#enterFile(AbstractSourceFile, Architecture, AssemblyStep)} uses the current architecture when
     * <code>null</code> is passed for the <code>architecture</code> parameter.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void enterFileSameArchitecture() throws IOException {
        final Architecture architecture = new Architecture(null) {
            @Override
            public SourceNode parse(Document text) {
                final List<SourceNode> nodes = new ArrayList<>();
                final DocumentReader reader = new DocumentReader(text);
                while (!reader.atEnd()) {
                    final char ch = reader.getCurrentChar();
                    if (ch == 'i') {
                        reader.advance();
                        nodes.add(new SourceNode(2, null) {
                            @Override
                            protected void assembleCore(AssemblyBuilder builder) throws IOException {
                                final CharSequence text = builder.getStep().getLocation().getSourceLocation().getText();
                                final FileFetcher fileFetcher = builder.getAssembly().getConfiguration().getFileFetcher();
                                assert fileFetcher != null;
                                final SourceFile childFile = fileFetcher.fetchSourceFile(Character.toString(text.charAt(1)));
                                assert childFile != null;
                                builder.enterFile(childFile, null);
                            }
                        });
                    } else if (ch >= '0' && ch <= '9') {
                        nodes.add(new SourceNode(1, null) {
                            @Override
                            protected void assembleCore(AssemblyBuilder builder) throws IOException {
                                final CharSequence text = builder.getStep().getLocation().getSourceLocation().getText();
                                builder.appendAssembledData((byte) (text.charAt(0) - '0'));
                            }
                        });
                    } else {
                        fail();
                    }

                    reader.advance();
                }

                return new SimpleCompositeSourceNode(nodes);
            }
        };

        final SourceFile fileA = new SourceFile("2", "a.asm");
        final FileFetcher fileFetcher = new FileFetcher() {
            @Override
            public byte[] fetchBinaryFile(String filePath) throws IOException {
                fail("not implemented");
                throw new FileNotFoundException();
            }

            @Override
            public SourceFile fetchSourceFile(String filePath) throws IOException {
                if ("a".equals(filePath)) {
                    return fileA;
                }

                throw new FileNotFoundException();
            }
        };

        final SourceFile mainFile = new SourceFile("1ia3", "main.asm");
        final Assembly assembly = new Assembly(
                new Configuration(Environment.DEFAULT, mainFile, architecture).setFileFetcher(fileFetcher));

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        checkOutput(assembly, new byte[] { 1, 2, 3 });

        final List<AssemblyStep> steps = assembly.getSteps();
        AssemblyStep step;
        AssemblyStepLocation stepLocation;
        SourceLocation sourceLocation;

        step = steps.get(0);
        final AssemblyStepLocation rootStepLocation = step.getLocation();
        stepLocation = step.getLocation();
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getFile(), is((Object) mainFile));
        assertThat(sourceLocation.getArchitecture(), is(architecture));
        assertThat(sourceLocation.getSourceNode(), is(notNullValue()));
        assertThat(sourceLocation.getTextPosition(), is(0));
        assertThat(sourceLocation.getLineNumber(), is(1));
        assertThat(sourceLocation.getLinePosition(), is(1));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(nullValue()));
        checkOutput(step, EMPTY_BYTE_ARRAY);

        step = steps.get(1);
        stepLocation = step.getLocation();
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getFile(), is((Object) mainFile));
        assertThat(sourceLocation.getArchitecture(), is(architecture));
        assertThat(sourceLocation.getSourceNode(), is(notNullValue()));
        assertThat(sourceLocation.getTextPosition(), is(0));
        assertThat(sourceLocation.getLineNumber(), is(1));
        assertThat(sourceLocation.getLinePosition(), is(1));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(rootStepLocation));
        checkOutput(step, new byte[] { 1 });

        step = steps.get(2);
        final AssemblyStepLocation parentStepLocation0 = step.getLocation();
        stepLocation = parentStepLocation0;
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getFile(), is((Object) mainFile));
        assertThat(sourceLocation.getArchitecture(), is(architecture));
        assertThat(sourceLocation.getSourceNode(), is(notNullValue()));
        assertThat(sourceLocation.getTextPosition(), is(1));
        assertThat(sourceLocation.getLineNumber(), is(1));
        assertThat(sourceLocation.getLinePosition(), is(2));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(rootStepLocation));
        checkOutput(step, EMPTY_BYTE_ARRAY);

        step = steps.get(3);
        final AssemblyStepLocation parentStepLocation1 = step.getLocation();
        stepLocation = parentStepLocation1;
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getFile(), is((Object) fileA));
        assertThat(sourceLocation.getArchitecture(), is(architecture));
        assertThat(sourceLocation.getSourceNode(), is(notNullValue()));
        assertThat(sourceLocation.getTextPosition(), is(0));
        assertThat(sourceLocation.getLineNumber(), is(1));
        assertThat(sourceLocation.getLinePosition(), is(1));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(parentStepLocation0));
        checkOutput(step, EMPTY_BYTE_ARRAY);

        step = steps.get(4);
        stepLocation = step.getLocation();
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getFile(), is((Object) fileA));
        assertThat(sourceLocation.getArchitecture(), is(architecture));
        assertThat(sourceLocation.getSourceNode(), is(notNullValue()));
        assertThat(sourceLocation.getTextPosition(), is(0));
        assertThat(sourceLocation.getLineNumber(), is(1));
        assertThat(sourceLocation.getLinePosition(), is(1));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(parentStepLocation1));
        checkOutput(step, new byte[] { 2 });

        step = steps.get(5);
        stepLocation = step.getLocation();
        sourceLocation = stepLocation.getSourceLocation();
        assertThat(sourceLocation.getFile(), is((Object) mainFile));
        assertThat(sourceLocation.getArchitecture(), is(architecture));
        assertThat(sourceLocation.getSourceNode(), is(notNullValue()));
        assertThat(sourceLocation.getTextPosition(), is(3));
        assertThat(sourceLocation.getLineNumber(), is(1));
        assertThat(sourceLocation.getLinePosition(), is(4));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(rootStepLocation));
        checkOutput(step, new byte[] { 3 });
    }

    /**
     * Asserts that entering a namespace twice doesn't define two namespaces with the same name.
     */
    @Test
    public void enterNamespaceTwice() {
        final TestSourceNode nodeThatEntersTheFooNamespace = createNodeThatEntersANamespace("foo");
        final TestSourceNode nodeThatExitsTheFooNamespace = createNodeThatExitsANamespace();
        final TestSourceNode nodeThatEntersTheFooNamespace2 = createNodeThatEntersANamespace("foo");
        final TestSourceNode nodeThatExitsTheFooNamespace2 = createNodeThatExitsANamespace();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersTheFooNamespace,
                nodeThatExitsTheFooNamespace, nodeThatEntersTheFooNamespace2, nodeThatExitsTheFooNamespace2));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersTheFooNamespace.assertAssembleCount(1);
        nodeThatExitsTheFooNamespace.assertAssembleCount(1);
        nodeThatEntersTheFooNamespace2.assertAssembleCount(1);
        nodeThatExitsTheFooNamespace2.assertAssembleCount(1);

        final Map<String, Namespace> namespaces = assembly.getNamespaces();
        assertThat(namespaces, IsMapWithSize.hasSize(1));
        final Map.Entry<String, Namespace> namespaceEntry = namespaces.entrySet().iterator().next();
        assertThat(namespaceEntry.getKey(), is("foo"));
        final Namespace namespace = namespaceEntry.getValue();
        assertThat(namespace.getName(), is("foo"));
        assertThat(namespace.getParent(), is(nullValue()));
    }

    /**
     * Asserts that {@linkplain AssemblyBuilder#addMessage(AssemblyMessage) adding} an {@link AssemblyErrorMessage} to an
     * {@link Assembly} causes the assembly to abort at the end of the current pass and sets the assembly's
     * {@linkplain Assembly#getGravity() gravity} correctly.
     */
    @Test
    public void errorMessage() {
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbolAndExpectsNoSymbol("foo");
        final TestSourceNode nodeThatAddsAnError = createNodeThatAddsAnError();
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatReferencesTheFooSymbol,
                nodeThatAddsAnError, nodeThatDefinesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);

        // root node
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        // node that references the foo symbol
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        // error node
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));

        // node that defines the foo symbol
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));

        nodeThatReferencesTheFooSymbol.assertAssembleCount(1);
        nodeThatAddsAnError.assertAssembleCount(1);
        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
    }

    /**
     * Asserts that calling {@link Assembly#addMessage(AssemblyMessage, AssemblyStep)} with an {@link AssemblyErrorMessage} sets the
     * assembly's {@linkplain Assembly#getGravity() gravity} to {@link MessageGravity#ERROR}, and that calling
     * {@link Assembly#addMessage(AssemblyMessage, AssemblyStep)} afterwards with an {@link AssemblyWarningMessage} doesn't affect
     * the assembly's {@linkplain Assembly#getGravity() gravity}.
     */
    @Test
    public void errorMessageThenWarningMessage() {
        final TestSourceNode nodeThatAddsAnError = createNodeThatAddsAnError();
        final TestSourceNode nodeThatAddsAWarning = createNodeThatAddsAWarning();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatAddsAnError, nodeThatAddsAWarning));
        final Assembly assembly = createAssembly(rootNode);

        // Root node
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        // Error node
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getMessages(), contains(AN_ERROR));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));

        // Warning node
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(ImmutableList.<Matcher<? super AssemblyMessage>> of(AN_ERROR, A_WARNING)));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));

        nodeThatAddsAnError.assertAssembleCount(1);
        nodeThatAddsAWarning.assertAssembleCount(1);
    }

    /**
     * Asserts that calling {@link Assembly#exitNamespace(AssemblyStep)} when there is no current namespace adds an error message to
     * the assembly.
     */
    @Test
    public void exitNamespaceWithoutCurrentNamespace() {
        final TestSourceNode nodeThatExitsANamespace = createNodeThatExitsANamespace();
        final Assembly assembly = createAssembly(nodeThatExitsANamespace);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(
                new ExitingNamespaceWithoutNamespaceErrorMessage())));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        nodeThatExitsANamespace.assertAssembleCount(1);
    }

    /**
     * Asserts that calling {@link Assembly#exitTransformationBlock(AssemblyStep)} when there is no open transformation block adds
     * an error message to the assembly.
     */
    @Test
    public void exitTransformationBlockWithoutCurrentTransformationBlock() {
        final TestSourceNode nodeThatExitsATransformationBlock = createNodeThatExitsATransformationBlock();
        final Assembly assembly = createAssembly(nodeThatExitsATransformationBlock);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new NoOpenTransformationBlockErrorMessage())));
        nodeThatExitsATransformationBlock.assertAssembleCount(1);
    }

    /**
     * Asserts that {@linkplain AssemblyBuilder#addMessage(AssemblyMessage) adding} an {@link AssemblyFatalErrorMessage} to an
     * {@link Assembly} causes the assembly to abort immediately and sets the assembly's {@linkplain Assembly#getGravity() gravity}
     * correctly.
     */
    @Test
    public void fatalErrorMessage() {
        final TestSourceNode nodeThatAddsAFatalError = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) {
                builder.addMessage(new AssemblyFatalErrorMessage("test") {
                });
            }
        };

        final SimpleCompositeSourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatAddsAFatalError,
                NODE_THAT_SHOULD_NOT_BE_REACHED));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING); // root node
        step(assembly, AssemblyCompletionStatus.COMPLETE); // fatal error node
        assertThat(assembly.getGravity(), is(MessageGravity.FATAL_ERROR));
        nodeThatAddsAFatalError.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#fetchBinaryFile(String)} returns and caches the contents of the specified binary file, or throws
     * a {@link FileNotFoundException} if the specified file was not found.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void fetchBinaryFile() throws IOException {
        final byte[] nil = null;

        Configuration configuration = new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT);
        configuration = configuration.setFileFetcher(new FileFetcher() {
            @Override
            public byte[] fetchBinaryFile(String filePath) throws IOException {
                if ("a".equals(filePath)) {
                    return new byte[] { 0x01 };
                }

                if ("b".equals(filePath)) {
                    return new byte[] { 0x02 };
                }

                return nil;
            }

            @Override
            public SourceFile fetchSourceFile(String filePath) throws IOException {
                throw new AssertionError();
            }
        });

        final Assembly assembly = new Assembly(configuration);
        byte[] file;

        file = assembly.fetchBinaryFile("a");
        assertThat(file, is(new byte[] { 0x01 }));
        assertThat(assembly.fetchBinaryFile("a"), is(sameInstance(file)));

        file = assembly.fetchBinaryFile("b");
        assertThat(file, is(new byte[] { 0x02 }));
        assertThat(assembly.fetchBinaryFile("b"), is(sameInstance(file)));

        try {
            assembly.fetchBinaryFile("c");
            fail("Assembly.fetchBinaryFile should have thrown a FileNotFoundException");
        } catch (FileNotFoundException e) {
        }
    }

    /**
     * Asserts that {@link Assembly#fetchBinaryFile(String)} throws a {@link FileNotFoundException} if the assembly's configuration
     * has no file fetcher.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void fetchBinaryFileNoFileFetcher() throws IOException {
        final Configuration configuration = new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT);
        final Assembly assembly = new Assembly(configuration);
        try {
            assembly.fetchBinaryFile("a");
            fail("Assembly.fetchBinaryFile should have thrown a FileNotFoundException");
        } catch (FileNotFoundException e) {
        }
    }

    /**
     * Asserts that {@link Assembly#fetchSourceFile(String)} returns and caches the contents of the specified source file, or throws
     * a {@link FileNotFoundException} if the specified file was not found.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void fetchSourceFile() throws IOException {
        final SourceFile nil = null;

        Configuration configuration = new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT);
        configuration = configuration.setFileFetcher(new FileFetcher() {
            @Override
            public byte[] fetchBinaryFile(String filePath) throws IOException {
                throw new AssertionError();
            }

            @Override
            public SourceFile fetchSourceFile(String filePath) throws IOException {
                if ("a".equals(filePath)) {
                    return new SourceFile("AAA", "a");
                }

                if ("b".equals(filePath)) {
                    return new SourceFile("BBB", "b");
                }

                return nil;
            }
        });

        final Assembly assembly = new Assembly(configuration);
        SourceFile file;

        file = assembly.fetchSourceFile("a");
        assertThat(file, is(notNullValue()));
        assertThat(file.getText().toString(), is("AAA"));
        assertThat(file.getFileName(), is("a"));
        assertThat(assembly.fetchSourceFile("a"), is(sameInstance(file)));

        file = assembly.fetchSourceFile("b");
        assertThat(file, is(notNullValue()));
        assertThat(file.getText().toString(), is("BBB"));
        assertThat(file.getFileName(), is("b"));
        assertThat(assembly.fetchSourceFile("b"), is(sameInstance(file)));

        try {
            assembly.fetchSourceFile("c");
            fail("Assembly.fetchSourceFile should have thrown a FileNotFoundException");
        } catch (FileNotFoundException e) {
        }
    }

    /**
     * Asserts that {@link Assembly#fetchSourceFile(String)} throws a {@link FileNotFoundException} if the assembly's configuration
     * has no file fetcher.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void fetchSourceFileNoFileFetcher() throws IOException {
        final Configuration configuration = new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT);
        final Assembly assembly = new Assembly(configuration);
        try {
            assembly.fetchSourceFile("a");
            fail("Assembly.fetchSourceFile should have thrown a FileNotFoundException");
        } catch (FileNotFoundException e) {
        }
    }

    /**
     * Asserts that {@link Assembly#getCurrentBlock()} throws an {@link IllegalStateException} when called after the assembly
     * process is complete.
     */
    @Test
    public void getCurrentBlockAfterComplete() {
        final Assembly assembly = createAssembly(new SimpleCompositeSourceNode(Collections.<SourceNode> emptySet()));
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        try {
            assembly.getCurrentBlock();
            fail("Assembly.getCurrentBlock() should have thrown an IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Asserts that {@link Assembly#isAnonymousSymbolReference(String)} returns the expected result for various inputs.
     */
    @Test
    public void isAnonymousSymbolReference() {
        assertThat(Assembly.isAnonymousSymbolReference(""), is(false));
        assertThat(Assembly.isAnonymousSymbolReference("A"), is(false));
        assertThat(Assembly.isAnonymousSymbolReference("+"), is(true));
        assertThat(Assembly.isAnonymousSymbolReference("+++"), is(true));
        assertThat(Assembly.isAnonymousSymbolReference("-"), is(true));
        assertThat(Assembly.isAnonymousSymbolReference("---"), is(true));
        assertThat(Assembly.isAnonymousSymbolReference("++-"), is(false));
        assertThat(Assembly.isAnonymousSymbolReference("/"), is(false));
    }

    /**
     * Asserts that {@link Assembly#isSuffixSymbolName(String)} returns the expected result for various inputs.
     */
    @Test
    public void isSuffixSymbolName() {
        assertThat(Assembly.isSuffixSymbolName(""), is(false));
        assertThat(Assembly.isSuffixSymbolName("A"), is(false));
        assertThat(Assembly.isSuffixSymbolName("ABC"), is(false));
        assertThat(Assembly.isSuffixSymbolName("."), is(true));
        assertThat(Assembly.isSuffixSymbolName(".A"), is(true));
        assertThat(Assembly.isSuffixSymbolName(".ABC"), is(true));
    }

    /**
     * Asserts that a local symbol defined in a namespace does not have a name derived from the namespace's name.
     */
    @Test
    public void localSymbolDefinedInNamespace() {
        final TestSourceNode nodeThatEntersTheFooNamespace = createNodeThatEntersANamespace("foo");
        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol("bar", true, SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatExitsTheFooNamespace = createNodeThatExitsANamespace();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersTheFooNamespace,
                nodeThatDefinesTheBarSymbol, nodeThatExitsTheFooNamespace));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersTheFooNamespace.assertAssembleCount(1);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(1);
        nodeThatExitsTheFooNamespace.assertAssembleCount(1);

        final Map<String, Namespace> namespaces = assembly.getNamespaces();
        assertThat(namespaces, IsMapWithSize.hasSize(1));
        final Map.Entry<String, Namespace> namespaceEntry = namespaces.entrySet().iterator().next();
        assertThat(namespaceEntry.getKey(), is("foo"));
        final Namespace namespace = namespaceEntry.getValue();
        assertThat(namespace.getName(), is("foo"));
        assertThat(namespace.getParent(), is(nullValue()));
        assertThat(namespace.getInnerNamespaces(), is(IsEmptyMap.empty()));

        final Symbol fooBarSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo.bar", false, false, null, null)
                .getSymbol();
        assertThat(fooBarSymbol, is(nullValue()));

        final Scope scope = assembly.getScopes().get(null);
        assertThat(scope, is(notNullValue()));

        final Iterator<UserSymbol> localSymbolsIterator = scope.getLocalSymbols().iterator();
        final UserSymbol barSymbol = localSymbolsIterator.next();
        assertThat(localSymbolsIterator.hasNext(), is(false));
        assertThat(barSymbol, is(notNullValue()));
        assertThat(barSymbol.getContext(), is(sameInstance((Object) SymbolContext.VALUE)));
        assertThat(barSymbol.getName(), is("bar"));
        assertThat(barSymbol.getType(), is(sameInstance(SymbolType.CONSTANT)));
        assertThat(barSymbol.getValue(), is((Object) FORTY_TWO));
    }

    /**
     * Asserts that {@link CustomAssemblyData#completed()} is called on all {@link CustomAssemblyData} objects even if one of them
     * throws an exception.
     */
    @Test
    public void misbehavingCustomAssemblyData() {
        // I need a couple of mutable booleans
        final AtomicBoolean ranCompleted1 = new AtomicBoolean();
        final AtomicBoolean ranCompleted2 = new AtomicBoolean();

        // Define objects with a specific hash code to influence the order of the entries in assembly.customAssemblyData
        final Object key1 = new Object() {
            @Override
            public int hashCode() {
                return 0x11111111;
            }
        };

        final Object key2 = new Object() {
            @Override
            public int hashCode() {
                return 0x22222222;
            }
        };

        final RuntimeException exception1 = new RuntimeException("misbehaving");
        final RuntimeException exception2 = new RuntimeException("misbehaving too");

        final TestSourceNode nodeThatAddsTwoCustomAssemblyDataObjects = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.setCustomAssemblyData(key1, new CustomAssemblyData() {
                    @Override
                    public void completed() {
                        assertThat(ranCompleted1.get(), is(false));
                        assertThat(ranCompleted2.get(), is(false));
                        ranCompleted1.set(true);

                        // We want to check that the other CustomAssemblyData's completed() method is called even though we throw an
                        // exception here.
                        throw exception1;
                    }

                    @Override
                    public void startedNewPass() {
                        fail("unreachable");
                    }
                });

                builder.setCustomAssemblyData(key2, new CustomAssemblyData() {
                    @Override
                    public void completed() {
                        assertThat(ranCompleted1.get(), is(true));
                        assertThat(ranCompleted2.get(), is(false));
                        ranCompleted2.set(true);

                        throw exception2;
                    }

                    @Override
                    public void startedNewPass() {
                        fail("unreachable");
                    }
                });
            }
        };

        final Assembly assembly = createAssembly(nodeThatAddsTwoCustomAssemblyDataObjects);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new InternalAssemblerErrorMessage(exception1))));
        assertThat(exception1.getSuppressed(), is(new Throwable[] { exception2 }));
        assertThat(assembly.getGravity(), is(MessageGravity.FATAL_ERROR));
        assertThat(ranCompleted1.get(), is(true));
        assertThat(ranCompleted2.get(), is(true));
    }

    /**
     * Asserts that an exception thrown by an implementation of {@link CustomAssemblyData#completed()} after an exception was thrown
     * from an implementation of {@link SourceNode#assemble(AssemblyBuilder)} is added as a suppressed exception to the exception
     * thrown by the {@link SourceNode}.
     */
    @Test
    public void misbehavingStepAndMisbehavingCustomAssemblyData() {
        // I need a mutable boolean
        final AtomicBoolean ranCompleted = new AtomicBoolean();

        final Object key = new Object();

        final RuntimeException exception1 = new RuntimeException("misbehaving");
        final RuntimeException exception2 = new RuntimeException("misbehaving too");

        final TestSourceNode nodeThatAddsACustomAssemblyDataObjectAndFails = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.setCustomAssemblyData(key, new CustomAssemblyData() {
                    @Override
                    public void completed() {
                        assertThat(ranCompleted.get(), is(false));
                        ranCompleted.set(true);

                        throw exception2;
                    }

                    @Override
                    public void startedNewPass() {
                        fail("unreachable");
                    }
                });

                throw exception1;
            }
        };

        final Assembly assembly = createAssembly(nodeThatAddsACustomAssemblyDataObjectAndFails);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new InternalAssemblerErrorMessage(exception1))));
        assertThat(exception1.getSuppressed(), is(new Throwable[] { exception2 }));
        assertThat(assembly.getGravity(), is(MessageGravity.FATAL_ERROR));
        assertThat(ranCompleted.get(), is(true));
    }

    /**
     * Asserts that {@link Assembly#processIOException(IOException, AssemblyStep)}, when called from {@link Assembly#step()} when an
     * {@link IOException} is thrown, adds an {@link IOErrorMessage} to the assembly.
     */
    @Test
    public void processIOExceptionFromStep() {
        final IOException ioException = new IOException("test");

        final TestSourceNode nodeThatThrowsIOException = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                throw ioException;
            }
        };

        final Assembly assembly = createAssembly(nodeThatThrowsIOException);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new IOErrorMessage(ioException))));
        assertThat(assembly.getGravity(), is(MessageGravity.FATAL_ERROR));
        nodeThatThrowsIOException.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#processIOException(IOException, AssemblyStep)}, when called manually through
     * {@link AssemblyBuilder#processIOException(IOException)}, adds an {@link IOErrorMessage} to the assembly.
     */
    @Test
    public void processIOExceptionManually() {
        final IOException ioException = new IOException("test");

        final TestSourceNode nodeThatThrowsIOException = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.processIOException(ioException);
            }
        };

        final Assembly assembly = createAssembly(nodeThatThrowsIOException);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new IOErrorMessage(ioException))));
        assertThat(assembly.getGravity(), is(MessageGravity.FATAL_ERROR));
        nodeThatThrowsIOException.assertAssembleCount(1);
    }

    /**
     * Asserts that defining the same constant twice in a pass causes an error.
     */
    @Test
    public void redefineConstant() {
        redefineError(SymbolType.CONSTANT, new Function<UserSymbol, AssemblyMessage>() {
            @Override
            public AssemblyMessage apply(UserSymbol input) {
                return new SymbolRedefinedErrorMessage(input);
            }
        });
    }

    /**
     * Asserts that defining the same variable twice in a pass does not cause an error.
     */
    @Test
    public void redefineVariable() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.VARIABLE, ONE);
        final TestSourceNode nodeThatDefinesTheFooSymbolAgain = createNodeThatDefinesASymbol("foo", SymbolType.VARIABLE, TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatDefinesTheFooSymbolAgain));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        final Symbol fooSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null).getSymbol();
        assertThat(fooSymbol, is(notNullValue()));
        assertThat(fooSymbol, is(instanceOf(UserSymbol.class)));
        final UserSymbol fooUserSymbol = (UserSymbol) fooSymbol;
        assertThat(fooUserSymbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.VARIABLE, TWO));

        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
        nodeThatDefinesTheFooSymbolAgain.assertAssembleCount(1);
    }

    /**
     * Asserts that defining a constant and a variable with the same name causes an error.
     */
    @Test
    public void redefineWithDifferentType() {
        redefineError(SymbolType.VARIABLE, new Function<UserSymbol, AssemblyMessage>() {
            @Override
            public AssemblyMessage apply(UserSymbol input) {
                return new SymbolRedefinedWithDifferentTypeErrorMessage(input);
            }
        });
    }

    /**
     * Asserts that a reentrant call to {@link Assembly#step()} fails.
     */
    @Test
    public void reentrantCallToStepFails() {
        final TestSourceNode testNode = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) {
                try {
                    builder.getAssembly().step();
                } catch (IllegalStateException e) {
                    return;
                }

                fail("Assembly.step() should have thrown an IllegalStateException");
            }
        };

        final Assembly assembly = createAssembly(testNode);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        testNode.assertAssembleCount(1);
    }

    /**
     * Asserts that a symbol reference after the definition of the corresponding symbol is resolved on the first pass.
     */
    @Test
    public void resolveSymbolReferenceAfterSymbolDefinition() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbolAndExpectsAUserSymbol("foo", "foo",
                SymbolType.CONSTANT, FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatReferencesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
        nodeThatReferencesTheFooSymbol.assertAssembleCount(1);
    }

    /**
     * Asserts that a symbol reference in an ambiguous context resolves to the first symbol found by searching the specified
     * contexts in order.
     */
    @Test
    public void resolveSymbolReferenceAmbiguousContext() {
        final Object fooDummyValue = new Object();

        final TestSourceNode nodeThatDefinesTheFooSymbolInTheValueContext = createNodeThatDefinesASymbol("foo",
                SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatDefinesTheFooSymbolInTheDummyContext = createNodeThatDefinesASymbol(DUMMY_SYMBOL_CONTEXT,
                "foo", SymbolType.CONSTANT, fooDummyValue);

        final TestSourceNode nodeThatReferencesTheFooSymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                {
                    final SymbolContext<?>[] contexts = new SymbolContext<?>[] { DUMMY_SYMBOL_CONTEXT, SymbolContext.VALUE };
                    final Symbol symbol = builder.resolveSymbolReference(contexts, "foo", false, false, null, null).getSymbol();
                    assertThat(symbol, is(notNullValue()));
                    assertThat(symbol, is(instanceOf(UserSymbol.class)));
                    assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(DUMMY_SYMBOL_CONTEXT, "foo", SymbolType.CONSTANT,
                            fooDummyValue));
                }

                {
                    final SymbolContext<?>[] contexts = new SymbolContext<?>[] { SymbolContext.VALUE, DUMMY_SYMBOL_CONTEXT };
                    final Symbol symbol = builder.resolveSymbolReference(contexts, "foo", false, false, null, null).getSymbol();
                    assertThat(symbol, is(notNullValue()));
                    assertThat(symbol, is(instanceOf(UserSymbol.class)));
                    assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.CONSTANT,
                            FORTY_TWO));
                }
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbolInTheValueContext,
                nodeThatDefinesTheFooSymbolInTheDummyContext, nodeThatReferencesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
    }

    /**
     * Asserts that a symbol reference with the name "---" references a backward anonymous symbol.
     */
    @Test
    public void resolveSymbolReferenceBackwardAnonymous() {
        final TestSourceNode nodeThatDefinesABackwardAnonymousSymbol1 = createNodeThatDefinesASymbol("-", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatDefinesABackwardAnonymousSymbol2 = createNodeThatDefinesASymbol("-", SymbolType.CONSTANT, TWO);
        final TestSourceNode nodeThatDefinesABackwardAnonymousSymbol3 = createNodeThatDefinesASymbol("-", SymbolType.CONSTANT,
                THREE);
        final TestSourceNode nodeThatReferencesABackwardAnonymousSymbol = createNodeThatReferencesASymbolAndExpectsAUserSymbol(
                "---", "__back1", SymbolType.CONSTANT, ONE);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesABackwardAnonymousSymbol1,
                nodeThatDefinesABackwardAnonymousSymbol2, nodeThatDefinesABackwardAnonymousSymbol3,
                nodeThatReferencesABackwardAnonymousSymbol));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatReferencesABackwardAnonymousSymbol.assertAssembleCount(1);
        nodeThatDefinesABackwardAnonymousSymbol1.assertAssembleCount(1);
        nodeThatDefinesABackwardAnonymousSymbol2.assertAssembleCount(1);
        nodeThatDefinesABackwardAnonymousSymbol3.assertAssembleCount(1);
    }

    /**
     * Asserts that a symbol reference before the definition of the corresponding symbol (which is a constant) is resolved on the
     * second pass.
     */
    @Test
    public void resolveSymbolReferenceBeforeSymbolDefinitionConstant() {
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbolAndExpectsAUserSymbolOnTheSecondPassOnly(
                "foo", "foo", SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatReferencesTheFooSymbol,
                nodeThatDefinesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatReferencesTheFooSymbol.assertAssembleCount(2);
        nodeThatDefinesTheFooSymbol.assertAssembleCount(2);
    }

    /**
     * Asserts that a reference to a constant defined with a value that changes between the first pass and the second pass causes a
     * third pass to be performed.
     */
    @Test
    public void resolveSymbolReferenceBeforeSymbolDefinitionConstantChangingValue() {
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbol("foo");

        final TestSourceNode nodeThatDefinesTheFooSymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final int value = builder.getAssembly().getCurrentPass() == 1 ? 1 : 2;
                builder.defineSymbol(SymbolContext.VALUE, "foo", false, SymbolType.CONSTANT, new UnsignedIntValue(value));
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatReferencesTheFooSymbol,
                nodeThatDefinesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatReferencesTheFooSymbol.assertAssembleCount(3);
        nodeThatDefinesTheFooSymbol.assertAssembleCount(3);
    }

    /**
     * Asserts that a symbol reference before the definition of the corresponding symbol (which is a variable) is not resolved and
     * causes an error.
     */
    @Test
    public void resolveSymbolReferenceBeforeSymbolDefinitionVariable() {
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbolAndExpectsNoSymbol("foo");
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.VARIABLE, FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatReferencesTheFooSymbol,
                nodeThatDefinesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(),
                contains(new EquivalentAssemblyMessage(new UnresolvedSymbolReferenceErrorMessage("foo"))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        nodeThatReferencesTheFooSymbol.assertAssembleCount(1);
        nodeThatDefinesTheFooSymbol.assertAssembleCount(1);
    }

    /**
     * Asserts that a symbol reference that appears only on the second pass before the definition of the corresponding symbol (which
     * is a variable) is not resolved and causes an error.
     */
    @Test
    public void resolveSymbolReferenceBeforeSymbolDefinitionVariableSecondPass() {
        final TestSourceNode nodeThatReferencesTheFooSymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                if (builder.getAssembly().getCurrentPass() >= 2) {
                    final Symbol symbol = builder.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null)
                            .getSymbol();
                    assertThat(symbol, is(nullValue()));
                }
            }
        };

        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.VARIABLE, FORTY_TWO);
        final TestSourceNode nodeThatReferencesTheBarSymbol = createNodeThatReferencesASymbol("bar");
        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol("bar", SymbolType.CONSTANT, ONE);

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatReferencesTheFooSymbol,
                nodeThatDefinesTheFooSymbol, nodeThatReferencesTheBarSymbol, nodeThatDefinesTheBarSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(),
                contains(new EquivalentAssemblyMessage(new UnresolvedSymbolReferenceErrorMessage("foo"))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        nodeThatReferencesTheFooSymbol.assertAssembleCount(2);
        nodeThatDefinesTheFooSymbol.assertAssembleCount(2);
        nodeThatReferencesTheBarSymbol.assertAssembleCount(2);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(2);
    }

    /**
     * Asserts that a symbol reference with the name "+++" references a forward anonymous symbol.
     */
    @Test
    public void resolveSymbolReferenceForwardAnonymous() {
        final TestSourceNode nodeThatReferencesAForwardAnonymousSymbol = createNodeThatReferencesASymbolAndExpectsAUserSymbolOnTheSecondPassOnly(
                "+++", "__forw3", SymbolType.CONSTANT, THREE);
        final TestSourceNode nodeThatDefinesAForwardAnonymousSymbol1 = createNodeThatDefinesASymbol("+", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatDefinesAForwardAnonymousSymbol2 = createNodeThatDefinesASymbol("+", SymbolType.CONSTANT, TWO);
        final TestSourceNode nodeThatDefinesAForwardAnonymousSymbol3 = createNodeThatDefinesASymbol("+", SymbolType.CONSTANT, THREE);

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatReferencesAForwardAnonymousSymbol,
                nodeThatDefinesAForwardAnonymousSymbol1, nodeThatDefinesAForwardAnonymousSymbol2,
                nodeThatDefinesAForwardAnonymousSymbol3));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatReferencesAForwardAnonymousSymbol.assertAssembleCount(2);
        nodeThatDefinesAForwardAnonymousSymbol1.assertAssembleCount(2);
        nodeThatDefinesAForwardAnonymousSymbol2.assertAssembleCount(2);
        nodeThatDefinesAForwardAnonymousSymbol3.assertAssembleCount(2);
    }

    /**
     * Asserts that
     * {@link AssemblyBuilder#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * correctly resolves a symbol reference when a {@link SymbolLookupContext} is specified.
     */
    @Test
    public void resolveSymbolReferenceInExplicitLookupContext() {
        // I need a mutable object reference
        final AtomicReference<SymbolLookupContext> lookupContextRef = new AtomicReference<>();

        final TestSourceNode nodeThatEntersTheANamespace = createNodeThatEntersANamespace("A");
        final TestSourceNode nodeThatDefinesTheAFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatCapturesASymbolLookupContext = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                lookupContextRef.set(builder.getAssembly().getCurrentSymbolLookupContext());
            }
        };
        final TestSourceNode nodeThatExitsTheANamespace = createNodeThatExitsANamespace();

        final TestSourceNode nodeThatEntersTheBNamespace = createNodeThatEntersANamespace("B");
        final TestSourceNode nodeThatDefinesTheBFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, TWO);
        final TestSourceNode nodeThatReferencesTheFooSymbolInTheCapturedSymbolLookupContext = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final SymbolLookupContext lookupContext = lookupContextRef.get();
                assertThat(lookupContext, is(not(nullValue())));
                final Symbol symbol = builder.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, lookupContext, null)
                        .getSymbol();
                assertThat(symbol, is(not(nullValue())));
                assertThat(symbol, is(instanceOf(UserSymbol.class)));
                assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "A.foo", SymbolType.CONSTANT, ONE));
            }
        };
        final TestSourceNode nodeThatExitsTheBNamespace = createNodeThatExitsANamespace();

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersTheANamespace,
                nodeThatDefinesTheAFooSymbol, nodeThatCapturesASymbolLookupContext, nodeThatExitsTheANamespace,
                nodeThatEntersTheBNamespace, nodeThatDefinesTheBFooSymbol,
                nodeThatReferencesTheFooSymbolInTheCapturedSymbolLookupContext, nodeThatExitsTheBNamespace));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws an {@link IllegalArgumentException} when the specified {@link SymbolLookupContext} belongs to a different assembly.
     */
    @Test
    public void resolveSymbolReferenceInForeignLookupContext() {
        final Assembly assembly0 = createAssembly(NODE_THAT_SHOULD_NOT_BE_REACHED);
        final Assembly assembly1 = createAssembly(NODE_THAT_SHOULD_NOT_BE_REACHED);
        final SymbolLookupContext lookupContext = assembly0.getCurrentSymbolLookupContext();

        try {
            assembly1.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, lookupContext, null);
        } catch (IllegalArgumentException e) {
            return;
        }

        fail("Assembly.resolveSymbolReference() should have thrown IllegalArgumentException");
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)} throws
     * a {@link NullPointerException} when the <code>contexts</code> argument contains a <code>null</code> element.
     */
    @Test(expected = NullPointerException.class)
    public void resolveSymbolReferenceListStringBooleanBooleanSymbolResolutionFallbackNullContext() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.resolveSymbolReference(Arrays.asList((SymbolContext<?>) null), "foo", false, false, null, null);
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)} throws
     * a {@link NullPointerException} when the <code>contexts</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void resolveSymbolReferenceListStringBooleanBooleanSymbolResolutionFallbackNullContexts() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.resolveSymbolReference((List<? extends SymbolContext<?>>) null, "foo", false, false, null, null);
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)} throws
     * a {@link NullPointerException} when the <code>name</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void resolveSymbolReferenceListStringBooleanBooleanSymbolResolutionFallbackNullName() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.resolveSymbolReference(ImmutableList.of(SymbolContext.VALUE), null, false, false, null, null);
    }

    /**
     * Asserts that a symbol reference in an ambiguous context resolves to a symbol in any of the specified contexts.
     */
    @Test
    public void resolveSymbolReferenceNonSourceArrayOfContexts() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);
        final Assembly assembly = createAssembly(nodeThatDefinesTheFooSymbol);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        final SymbolContext<?>[] contexts = new SymbolContext<?>[] { DUMMY_SYMBOL_CONTEXT, SymbolContext.VALUE };
        final Symbol symbol = assembly.resolveSymbolReference(contexts, "foo", false, false, null, null).getSymbol();
        assertThat(symbol, is(notNullValue()));
        assertThat(symbol, is(instanceOf(UserSymbol.class)));
        assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, FORTY_TWO));
    }

    /**
     * Asserts that a symbol reference in an ambiguous context resolves to a symbol in any of the specified contexts.
     */
    @Test
    public void resolveSymbolReferenceNonSourceListOfContexts() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);
        final Assembly assembly = createAssembly(nodeThatDefinesTheFooSymbol);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        final List<SymbolContext<?>> contexts = ImmutableList.of(DUMMY_SYMBOL_CONTEXT, SymbolContext.VALUE);
        final Symbol symbol = assembly.resolveSymbolReference(contexts, "foo", false, false, null, null).getSymbol();
        assertThat(symbol, is(notNullValue()));
        assertThat(symbol, is(instanceOf(UserSymbol.class)));
        assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, FORTY_TWO));
    }

    /**
     * Asserts that a symbol reference in an ambiguous context resolves to a symbol in any of the specified contexts.
     */
    @Test
    public void resolveSymbolReferenceSourceArrayOfContexts() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);

        final TestSourceNode nodeThatReferencesTheFooSymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final SymbolContext<?>[] contexts = new SymbolContext<?>[] { DUMMY_SYMBOL_CONTEXT, SymbolContext.VALUE };
                final Symbol symbol = builder.resolveSymbolReference(contexts, "foo", false, false, null, null).getSymbol();
                assertThat(symbol, is(notNullValue()));
                assertThat(symbol, is(instanceOf(UserSymbol.class)));
                assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, FORTY_TWO));
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatReferencesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
    }

    /**
     * Asserts that a symbol reference in an ambiguous context resolves to a symbol in any of the specified contexts.
     */
    @Test
    public void resolveSymbolReferenceSourceListOfContexts() {
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);

        final TestSourceNode nodeThatReferencesTheFooSymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final List<SymbolContext<?>> contexts = ImmutableList.of(DUMMY_SYMBOL_CONTEXT, SymbolContext.VALUE);
                final Symbol symbol = builder.resolveSymbolReference(contexts, "foo", false, false, null, null).getSymbol();
                assertThat(symbol, is(notNullValue()));
                assertThat(symbol, is(instanceOf(UserSymbol.class)));
                assertThat((UserSymbol) symbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, FORTY_TWO));
            }
        };

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbol,
                nodeThatReferencesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>contexts</code> argument contains a <code>null</code> element.
     */
    @Test(expected = NullPointerException.class)
    public void resolveSymbolReferenceSymbolContextArrayStringBooleanBooleanSymbolResolutionFallbackNullContext() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.resolveSymbolReference(new SymbolContext<?>[] { null }, "foo", false, false, null, null);
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>contexts</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void resolveSymbolReferenceSymbolContextArrayStringBooleanBooleanSymbolResolutionFallbackNullContexts() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.resolveSymbolReference((SymbolContext<?>[]) null, "foo", false, false, null, null);
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>name</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void resolveSymbolReferenceSymbolContextArrayStringBooleanBooleanSymbolResolutionFallbackNullName() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.resolveSymbolReference(new SymbolContext<?>[] { SymbolContext.VALUE }, null, false, false, null, null);
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>context</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void resolveSymbolReferenceSymbolContextStringBooleanBooleanSymbolResolutionFallbackNullContext() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.resolveSymbolReference((SymbolContext<?>) null, "foo", false, false, null, null);
    }

    /**
     * Asserts that
     * {@link Assembly#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * throws a {@link NullPointerException} when the <code>name</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void resolveSymbolReferenceSymbolContextStringBooleanBooleanSymbolResolutionFallbackNullName() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.resolveSymbolReference(SymbolContext.VALUE, null, false, false, null, null);
    }

    /**
     * Asserts that {@link Assembly#setCurrentEncoding(Charset)} sets the assembly's current encoding.
     */
    @Test
    public void setCurrentAssembly() {
        final Charset usAscii = Charset.forName("US-ASCII");

        final TestSourceNode nodeThatSetsTheCurrentEncoding = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.setCurrentEncoding(usAscii);
            }
        };

        final Assembly assembly = createAssembly(nodeThatSetsTheCurrentEncoding);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        assertThat(assembly.getCurrentEncoding(), is(sameInstance(usAscii)));
        nodeThatSetsTheCurrentEncoding.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#setProgramCounter(AssemblyStep, long)} sets the assembly's program counter.
     */
    @Test
    public void setProgramCounter() {
        final TestSourceNode nodeThatSetsTheProgramCounter = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.setProgramCounter(0x1234L);
            }
        };

        final TestSourceNode nodeThatEmitsData = createNodeThatEmitsAByte((byte) 1);

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatSetsTheProgramCounter, nodeThatEmitsData));
        final Assembly assembly = createAssembly(rootNode);
        final List<AssemblyStep> steps = assembly.getSteps();

        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getProgramCounter(), is(0L));
        assertThat(steps.get(0).getProgramCounter(), is(0L));

        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getProgramCounter(), is(0x1234L));
        assertThat(steps.get(1).getProgramCounter(), is(0L));

        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getProgramCounter(), is(0x1235L));
        assertThat(steps.get(2).getProgramCounter(), is(0x1234L));

        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        nodeThatSetsTheProgramCounter.assertAssembleCount(1);
        nodeThatEmitsData.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#setProgramCounter(AssemblyStep, long)} sets the assembly's program counter while also
     * considering the step's output.
     */
    @Test
    public void setProgramCounterAndEmitData() {
        final TestSourceNode nodeThatSetsTheProgramCounterAndEmitsData = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.appendAssembledData((byte) 1);
                builder.setProgramCounter(0x1234L);
                builder.appendAssembledData((byte) 2);
            }
        };

        final Assembly assembly = createAssembly(nodeThatSetsTheProgramCounterAndEmitsData);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        assertThat(assembly.getProgramCounter(), is(0x1236L));
        assertThat(assembly.getSteps().get(0).getProgramCounter(), is(0L));

        nodeThatSetsTheProgramCounterAndEmitsData.assertAssembleCount(1);
    }

    /**
     * Asserts that the state specific to the current pass is reset when a new pass is started.
     */
    @Test
    public void startNewPass() {
        final TestSourceNode nodeThatDefinesTheLocalBarSymbol = createNodeThatDefinesASymbol("bar", true, SymbolType.CONSTANT,
                FORTY_TWO);
        final TestSourceNode nodeThatDefinesTheSuffixBazSymbol = createNodeThatDefinesASymbol(".baz", false, SymbolType.CONSTANT,
                FORTY_TWO);
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbol("foo");
        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheLocalBarSymbol,
                nodeThatDefinesTheSuffixBazSymbol, nodeThatReferencesTheFooSymbol, nodeThatDefinesTheFooSymbol));
        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);

        assertThat(assembly.getSteps(), is(empty()));
        assertThat(assembly.getProgramCounter(), is(0L));
        assertThat(assembly.getCurrentEncoding(), is(Charset.forName("UTF-8")));
        assertThat(assembly.getCurrentNamespace(), is(nullValue()));
        assertThat(assembly.getCurrentPass(), is(2));

        // Check that the counters for anonymous symbols are reset.
        assertThat(assembly.resolveSymbolReference(SymbolContext.VALUE, "+", false, false, null, null).getName(), is("__forw1"));
        assertThat(assembly.resolveSymbolReference(SymbolContext.VALUE, "-", false, false, null, null).getName(), is("__back0"));

        // Check that the scope key is reset.
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getScope(null), is(notNullValue()));

        // Check that the last non-suffix symbol is reset.
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.resolveSymbolReference(SymbolContext.VALUE, ".baz", false, false, null, null).getName(), is(".baz"));
    }

    /**
     * Asserts that {@link Assembly#step()} generates {@link AssemblyStep} objects correctly and that
     * {@link Assembly#writeAssembledDataTo(OutputStream)} writes the assembled data to the specified {@link OutputStream}
     * correctly.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void stepBasic() throws IOException {
        final SourceFile mainSourceFile = new SourceFile("0122FC", "main.hex");
        final Architecture hexArchitecture = HexArchitecture.INSTANCE;
        final CompositeSourceNode rootSourceNode = (CompositeSourceNode) mainSourceFile.getParsed(hexArchitecture);
        final Configuration configuration = new Configuration(Environment.DEFAULT, mainSourceFile, hexArchitecture);
        final Assembly assembly = new Assembly(configuration);
        final List<AssemblyStep> steps = assembly.getSteps();
        AssemblyStep step;
        AssemblyStepLocation stepLocation;

        // Step 1 (root node)
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getCurrentPass(), is(1));
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        assertThat(assembly.getSymbols(), is(emptyIterable()));
        assertThat(assembly.getProgramCounter(), is(0L));
        assertThat(steps, hasSize(1));
        step = steps.get(0);
        final AssemblyStepLocation rootStepLocation = step.getLocation();
        stepLocation = rootStepLocation;
        assertThat(stepLocation.getSourceLocation().getFile(), is(sameInstance((Object) mainSourceFile)));
        assertThat(stepLocation.getSourceLocation().getArchitecture(), is(sameInstance(hexArchitecture)));
        assertThat(stepLocation.getSourceLocation().getSourceNode(), is(sameInstance((SourceNode) rootSourceNode)));
        assertThat(stepLocation.getSourceLocation().getTextPosition(), is(0));
        assertThat(stepLocation.getSourceLocation().getLineNumber(), is(1));
        assertThat(stepLocation.getSourceLocation().getLinePosition(), is(1));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(nullValue()));
        assertThat(stepLocation.getFullPath(), is("main.hex(1,1)"));
        assertThat(step.getProgramCounter(), is(0L));
        assertThat(step.getAssembledDataStart(), is(0L));
        assertThat(step.getAssembledDataLength(), is(0L));

        // Step 2 (1st child node)
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getCurrentPass(), is(1));
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        assertThat(assembly.getSymbols(), is(emptyIterable()));
        assertThat(assembly.getProgramCounter(), is(1L));
        assertThat(steps, hasSize(2));
        step = steps.get(1);
        stepLocation = step.getLocation();
        assertThat(stepLocation.getSourceLocation().getFile(), is(sameInstance((Object) mainSourceFile)));
        assertThat(stepLocation.getSourceLocation().getArchitecture(), is(sameInstance(hexArchitecture)));
        assertThat(stepLocation.getSourceLocation().getSourceNode(), is(sameInstance(rootSourceNode.getChildNodes().get(0))));
        assertThat(stepLocation.getSourceLocation().getTextPosition(), is(0));
        assertThat(stepLocation.getSourceLocation().getLineNumber(), is(1));
        assertThat(stepLocation.getSourceLocation().getLinePosition(), is(1));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(sameInstance(rootStepLocation)));
        assertThat(stepLocation.getFullPath(), is("main.hex(1,1)"));
        assertThat(step.getProgramCounter(), is(0L));
        assertThat(step.getAssembledDataStart(), is(0L));
        assertThat(step.getAssembledDataLength(), is(1L));

        // Step 3 (2nd child node)
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getCurrentPass(), is(1));
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        assertThat(assembly.getSymbols(), is(emptyIterable()));
        assertThat(assembly.getProgramCounter(), is(2L));
        assertThat(steps, hasSize(3));
        step = steps.get(2);
        stepLocation = step.getLocation();
        assertThat(stepLocation.getSourceLocation().getFile(), is(sameInstance((Object) mainSourceFile)));
        assertThat(stepLocation.getSourceLocation().getArchitecture(), is(sameInstance(hexArchitecture)));
        assertThat(stepLocation.getSourceLocation().getSourceNode(), is(sameInstance(rootSourceNode.getChildNodes().get(1))));
        assertThat(stepLocation.getSourceLocation().getTextPosition(), is(2));
        assertThat(stepLocation.getSourceLocation().getLineNumber(), is(1));
        assertThat(stepLocation.getSourceLocation().getLinePosition(), is(3));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(sameInstance(rootStepLocation)));
        assertThat(stepLocation.getFullPath(), is("main.hex(1,3)"));
        assertThat(step.getProgramCounter(), is(1L));
        assertThat(step.getAssembledDataStart(), is(1L));
        assertThat(step.getAssembledDataLength(), is(1L));

        // Step 4 (3rd child node)
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getCurrentPass(), is(1));
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        assertThat(assembly.getSymbols(), is(emptyIterable()));
        assertThat(assembly.getProgramCounter(), is(3L));
        assertThat(steps, hasSize(4));
        step = steps.get(3);
        stepLocation = step.getLocation();
        assertThat(stepLocation.getSourceLocation().getFile(), is(sameInstance((Object) mainSourceFile)));
        assertThat(stepLocation.getSourceLocation().getArchitecture(), is(sameInstance(hexArchitecture)));
        assertThat(stepLocation.getSourceLocation().getSourceNode(), is(sameInstance(rootSourceNode.getChildNodes().get(2))));
        assertThat(stepLocation.getSourceLocation().getTextPosition(), is(4));
        assertThat(stepLocation.getSourceLocation().getLineNumber(), is(1));
        assertThat(stepLocation.getSourceLocation().getLinePosition(), is(5));
        assertThat(stepLocation.getIterationNumber(), is(0L));
        assertThat(stepLocation.getParent(), is(sameInstance(rootStepLocation)));
        assertThat(stepLocation.getFullPath(), is("main.hex(1,5)"));
        assertThat(step.getProgramCounter(), is(2L));
        assertThat(step.getAssembledDataStart(), is(2L));
        assertThat(step.getAssembledDataLength(), is(1L));

        // Step after completion
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        // Check the output
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assembly.writeAssembledDataTo(out);
        assertThat(out.toByteArray(), is(new byte[] { 0x01, 0x22, (byte) 0xFC }));
    }

    /**
     * Asserts that a {@link Throwable} that is not an {@link OutOfMemoryError}, {@link ThreadDeath} or {@link IOException} causes
     * an {@link InternalAssemblerErrorMessage} to be added to the assembly.
     */
    @Test
    public void stepOtherThrowable() {
        final RuntimeException exception = new RuntimeException("I have no idea what I'm doing");

        final TestSourceNode nodeThatFails = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                throw exception;
            }
        };

        final Assembly assembly = createAssembly(nodeThatFails);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new InternalAssemblerErrorMessage(exception))));
        assertThat(assembly.getGravity(), is(MessageGravity.FATAL_ERROR));
    }

    /**
     * Asserts that an {@link OutOfMemoryError} is caught and that an {@link OutOfMemoryErrorMessage} is added to the assembly.
     */
    @Test
    public void stepOutOfMemory() {
        final TestSourceNode nodeThatRunsOutOfMemory = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                throw new OutOfMemoryError("did not actually run out of memory");
            }
        };

        final Assembly assembly = createAssembly(nodeThatRunsOutOfMemory);
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new OutOfMemoryErrorMessage())));
        assertThat(assembly.getGravity(), is(MessageGravity.FATAL_ERROR));
    }

    /**
     * Asserts that a {@link ThreadDeath} thrown by {@link Thread#stop()} causes an {@link AssemblyInterruptedErrorMessage} to be
     * added to the assembly, that the {@link ThreadDeath} is rethrown and that the thread dies as expected. The step is performed
     * in another thread.
     *
     * @throws InterruptedException
     *             the thread was interrupted while waiting for the child thread to finish
     */
    @SuppressWarnings("javadoc")
    @Test
    public void stepThreadDeath() throws InterruptedException {
        final TestSourceNode nodeThatDies = new TestSourceNode() {
            @Override
            @SuppressWarnings("deprecation")
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                Thread.currentThread().stop();
            }
        };

        final Assembly assembly = createAssembly(nodeThatDies);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                assembly.step();
                fail("Assembly.step() should not return");
            }
        });

        thread.start();
        thread.join();
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(new AssemblyInterruptedErrorMessage())));
        assertThat(assembly.getGravity(), is(MessageGravity.FATAL_ERROR));
    }

    /**
     * Asserts that a symbol defined in a namespace has a name derived from the namespace's name.
     */
    @Test
    public void symbolDefinedInNamespace() {
        final TestSourceNode nodeThatEntersTheFooNamespace = createNodeThatEntersANamespace("foo");
        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol("bar", SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatExitsTheFooNamespace = createNodeThatExitsANamespace();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersTheFooNamespace,
                nodeThatDefinesTheBarSymbol, nodeThatExitsTheFooNamespace));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersTheFooNamespace.assertAssembleCount(1);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(1);
        nodeThatExitsTheFooNamespace.assertAssembleCount(1);

        final Map<String, Namespace> namespaces = assembly.getNamespaces();
        assertThat(namespaces, IsMapWithSize.hasSize(1));
        final Map.Entry<String, Namespace> namespaceEntry = namespaces.entrySet().iterator().next();
        assertThat(namespaceEntry.getKey(), is("foo"));
        final Namespace namespace = namespaceEntry.getValue();
        assertThat(namespace.getName(), is("foo"));
        assertThat(namespace.getParent(), is(nullValue()));
        assertThat(namespace.getInnerNamespaces(), is(IsEmptyMap.empty()));

        final Symbol barSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo.bar", false, false, null, null)
                .getSymbol();
        assertThat(barSymbol, is(notNullValue()));
        assertThat(barSymbol, is(instanceOf(UserSymbol.class)));
        assertThat(barSymbol.getName(), is("foo.bar"));
        assertThat(barSymbol.getValue(), is((Object) FORTY_TWO));
        assertThat(assembly.getSymbols(), contains(barSymbol));
    }

    /**
     * Asserts that a symbol defined in a nested namespace has a name derived from the namespace's qualified name.
     */
    @Test
    public void symbolDefinedInNestedNamespace() {
        final TestSourceNode nodeThatEntersTheFooNamespace = createNodeThatEntersANamespace("foo");
        final TestSourceNode nodeThatEntersTheBarNamespace = createNodeThatEntersANamespace("bar");
        final TestSourceNode nodeThatDefinesTheBazSymbol = createNodeThatDefinesASymbol("baz", SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatExitsTheBarNamespace = createNodeThatExitsANamespace();
        final TestSourceNode nodeThatExitsTheFooNamespace = createNodeThatExitsANamespace();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersTheFooNamespace,
                nodeThatEntersTheBarNamespace, nodeThatDefinesTheBazSymbol, nodeThatExitsTheBarNamespace,
                nodeThatExitsTheFooNamespace));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersTheFooNamespace.assertAssembleCount(1);
        nodeThatEntersTheBarNamespace.assertAssembleCount(1);
        nodeThatDefinesTheBazSymbol.assertAssembleCount(1);
        nodeThatExitsTheBarNamespace.assertAssembleCount(1);
        nodeThatExitsTheFooNamespace.assertAssembleCount(1);

        final Map<String, Namespace> namespaces = assembly.getNamespaces();
        assertThat(namespaces, IsMapWithSize.hasSize(1));
        final Map.Entry<String, Namespace> namespaceEntry = namespaces.entrySet().iterator().next();
        assertThat(namespaceEntry.getKey(), is("foo"));
        final Namespace namespace = namespaceEntry.getValue();
        assertThat(namespace.getName(), is("foo"));
        assertThat(namespace.getParent(), is(nullValue()));

        final Map<String, Namespace> innerNamespaces = namespace.getInnerNamespaces();
        assertThat(innerNamespaces, IsMapWithSize.hasSize(1));
        final Map.Entry<String, Namespace> innerNamespaceEntry = innerNamespaces.entrySet().iterator().next();
        assertThat(innerNamespaceEntry.getKey(), is("bar"));
        final Namespace innerNamespace = innerNamespaceEntry.getValue();
        assertThat(innerNamespace.getName(), is("bar"));
        assertThat(innerNamespace.getParent(), is(namespace));

        final Symbol bazSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo.bar.baz", false, false, null, null)
                .getSymbol();
        assertThat(bazSymbol, is(notNullValue()));
        assertThat(bazSymbol, is(instanceOf(UserSymbol.class)));
        assertThat(bazSymbol.getName(), is("foo.bar.baz"));
        assertThat(bazSymbol.getValue(), is((Object) FORTY_TWO));
        assertThat(assembly.getSymbols(), contains(bazSymbol));
    }

    /**
     * Asserts that a symbol reference resolves to a different symbol when a better candidate is defined in the namespace in which
     * the reference appears.
     */
    @Test
    public void symbolReferenceResolutionAndNamespaces() {
        // I need mutable object references
        final AtomicReference<Symbol> barSymbolRef = new AtomicReference<>();
        final AtomicReference<Symbol> fooBarSymbolRef = new AtomicReference<>();

        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol("bar", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatEntersTheFooNamespace = createNodeThatEntersANamespace("foo");

        final TestSourceNode nodeThatReferencesTheBarSymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final Symbol barSymbol = builder.resolveSymbolReference(SymbolContext.VALUE, "bar", false, false, null, null)
                        .getSymbol();
                assertThat(barSymbol, is(notNullValue()));
                assertThat(barSymbol, is(instanceOf(UserSymbol.class)));

                if (builder.getAssembly().getCurrentPass() == 1) {
                    assertThat((UserSymbol) barSymbol,
                            new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, ONE));
                    barSymbolRef.set(barSymbol);
                } else {
                    assertThat((UserSymbol) barSymbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "foo.bar", SymbolType.CONSTANT,
                            TWO));
                    fooBarSymbolRef.set(barSymbol);
                }
            }
        };

        final TestSourceNode nodeThatDefinesTheFooBarSymbol = createNodeThatDefinesASymbol("bar", SymbolType.CONSTANT, TWO);
        final TestSourceNode nodeThatExitsTheFooNamespace = createNodeThatExitsANamespace();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheBarSymbol,
                nodeThatEntersTheFooNamespace, nodeThatReferencesTheBarSymbol, nodeThatDefinesTheFooBarSymbol,
                nodeThatExitsTheFooNamespace));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatDefinesTheBarSymbol.assertAssembleCount(2);
        nodeThatEntersTheFooNamespace.assertAssembleCount(2);
        nodeThatReferencesTheBarSymbol.assertAssembleCount(2);
        nodeThatDefinesTheFooBarSymbol.assertAssembleCount(2);
        nodeThatExitsTheFooNamespace.assertAssembleCount(2);

        assertThat(barSymbolRef.get(), is(notNullValue()));
        assertThat(fooBarSymbolRef.get(), is(notNullValue()));

        final Map<String, Namespace> namespaces = assembly.getNamespaces();
        assertThat(namespaces, IsMapWithSize.hasSize(1));
        final Map.Entry<String, Namespace> namespaceEntry = namespaces.entrySet().iterator().next();
        assertThat(namespaceEntry.getKey(), is("foo"));
        final Namespace namespace = namespaceEntry.getValue();
        assertThat(namespace.getName(), is("foo"));
        assertThat(namespace.getParent(), is(nullValue()));
        assertThat(namespace.getInnerNamespaces(), is(IsEmptyMap.empty()));

        final Symbol barSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "bar", false, false, null, null).getSymbol();
        assertThat(barSymbol, is(sameInstance(barSymbolRef.get())));

        final Symbol fooBarSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo.bar", false, false, null, null)
                .getSymbol();
        assertThat(fooBarSymbol, is(sameInstance(fooBarSymbolRef.get())));

        assertThat(assembly.getSymbols(), containsInAnyOrder(barSymbol, fooBarSymbol));
    }

    /**
     * Asserts that symbol references to symbols defined in the same namespace or in enclosing namespaces are resolved when using
     * the unqualified name, a partially qualified name and a fully qualified name.
     */
    @Test
    public void symbolReferencesAndNamespaces() {
        // I need mutable object references
        final AtomicReference<Symbol> aSymbolRef = new AtomicReference<>();
        final AtomicReference<Symbol> bSymbolRef = new AtomicReference<>();
        final AtomicReference<Symbol> cSymbolRef = new AtomicReference<>();

        final TestSourceNode nodeThatDefinesTheASymbol = createNodeThatDefinesASymbol("a", SymbolType.CONSTANT, ONE);
        final TestSourceNode nodeThatEntersTheXNamespace = createNodeThatEntersANamespace("X");
        final TestSourceNode nodeThatDefinesTheBSymbol = createNodeThatDefinesASymbol("b", SymbolType.CONSTANT, TWO);
        final TestSourceNode nodeThatEntersTheYNamespace = createNodeThatEntersANamespace("Y");
        final TestSourceNode nodeThatDefinesTheCSymbol = createNodeThatDefinesASymbol("c", SymbolType.CONSTANT, THREE);

        final TestSourceNode nodeThatReferencesTheSymbols = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final Symbol aSymbol = builder.resolveSymbolReference(SymbolContext.VALUE, "a", false, false, null, null)
                        .getSymbol();
                assertThat(aSymbol, is(notNullValue()));
                assertThat(aSymbol, is(instanceOf(UserSymbol.class)));
                assertThat((UserSymbol) aSymbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "a", SymbolType.CONSTANT, ONE));

                final Symbol bSymbol = builder.resolveSymbolReference(SymbolContext.VALUE, "b", false, false, null, null)
                        .getSymbol();
                assertThat(bSymbol, is(notNullValue()));
                assertThat(bSymbol, is(instanceOf(UserSymbol.class)));
                assertThat((UserSymbol) bSymbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "X.b", SymbolType.CONSTANT, TWO));

                final Symbol bSymbolQualified = builder
                        .resolveSymbolReference(SymbolContext.VALUE, "X.b", false, false, null, null).getSymbol();
                assertThat(bSymbolQualified, is(sameInstance(bSymbol)));

                final Symbol cSymbol = builder.resolveSymbolReference(SymbolContext.VALUE, "c", false, false, null, null)
                        .getSymbol();
                assertThat(cSymbol, is(notNullValue()));
                assertThat(cSymbol, is(instanceOf(UserSymbol.class)));
                assertThat((UserSymbol) cSymbol, new UserSymbolMatcher<>(SymbolContext.VALUE, "X.Y.c", SymbolType.CONSTANT, THREE));

                final Symbol cSymbolPartiallyQualified = builder.resolveSymbolReference(SymbolContext.VALUE, "Y.c", false, false,
                        null, null).getSymbol();
                assertThat(cSymbolPartiallyQualified, is(sameInstance(cSymbol)));

                final Symbol cSymbolFullyQualified = builder.resolveSymbolReference(SymbolContext.VALUE, "X.Y.c", false, false,
                        null, null).getSymbol();
                assertThat(cSymbolFullyQualified, is(sameInstance(cSymbol)));

                aSymbolRef.set(aSymbol);
                bSymbolRef.set(bSymbol);
                cSymbolRef.set(cSymbol);
            }
        };

        final TestSourceNode nodeThatExitsTheYNamespace = createNodeThatExitsANamespace();
        final TestSourceNode nodeThatExitsTheXNamespace = createNodeThatExitsANamespace();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheASymbol,
                nodeThatEntersTheXNamespace, nodeThatDefinesTheBSymbol, nodeThatEntersTheYNamespace, nodeThatDefinesTheCSymbol,
                nodeThatReferencesTheSymbols, nodeThatExitsTheYNamespace, nodeThatExitsTheXNamespace));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatDefinesTheASymbol.assertAssembleCount(1);
        nodeThatEntersTheXNamespace.assertAssembleCount(1);
        nodeThatDefinesTheBSymbol.assertAssembleCount(1);
        nodeThatEntersTheYNamespace.assertAssembleCount(1);
        nodeThatDefinesTheCSymbol.assertAssembleCount(1);
        nodeThatReferencesTheSymbols.assertAssembleCount(1);
        nodeThatExitsTheYNamespace.assertAssembleCount(1);
        nodeThatExitsTheXNamespace.assertAssembleCount(1);

        assertThat(aSymbolRef.get(), is(notNullValue()));
        assertThat(bSymbolRef.get(), is(notNullValue()));
        assertThat(cSymbolRef.get(), is(notNullValue()));

        final Map<String, Namespace> namespaces = assembly.getNamespaces();
        assertThat(namespaces, IsMapWithSize.hasSize(1));
        final Map.Entry<String, Namespace> namespaceEntry = namespaces.entrySet().iterator().next();
        assertThat(namespaceEntry.getKey(), is("X"));
        final Namespace namespace = namespaceEntry.getValue();
        assertThat(namespace.getName(), is("X"));
        assertThat(namespace.getParent(), is(nullValue()));

        final Map<String, Namespace> innerNamespaces = namespace.getInnerNamespaces();
        assertThat(innerNamespaces, IsMapWithSize.hasSize(1));
        final Map.Entry<String, Namespace> innerNamespaceEntry = innerNamespaces.entrySet().iterator().next();
        assertThat(innerNamespaceEntry.getKey(), is("Y"));
        final Namespace innerNamespace = innerNamespaceEntry.getValue();
        assertThat(innerNamespace.getName(), is("Y"));
        assertThat(innerNamespace.getParent(), is(namespace));

        final Symbol aSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "a", false, false, null, null).getSymbol();
        assertThat(aSymbol, is(sameInstance(aSymbolRef.get())));

        final Symbol bSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "X.b", false, false, null, null).getSymbol();
        assertThat(bSymbol, is(sameInstance(bSymbolRef.get())));

        final Symbol cSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "X.Y.c", false, false, null, null).getSymbol();
        assertThat(cSymbol, is(sameInstance(cSymbolRef.get())));

        assertThat(assembly.getSymbols(), containsInAnyOrder(aSymbol, bSymbol, cSymbol));
    }

    /**
     * Asserts that two symbols defined with a <code>null</code> value causes two
     * {@link SymbolDefinedWithUndeterminedValueErrorMessage}s to be added to the assembly.
     */
    @Test
    public void symbolsWithUndeterminedValue() {
        final TestSourceNode nodeThatDefinesTheFooSymbolWithAnUndeterminedValue = createNodeThatDefinesASymbol("foo",
                SymbolType.CONSTANT, null);
        final TestSourceNode nodeThatDefinesTheBarSymbolWithAnUndeterminedValue = createNodeThatDefinesASymbol("bar",
                SymbolType.CONSTANT, null);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesTheFooSymbolWithAnUndeterminedValue,
                nodeThatDefinesTheBarSymbolWithAnUndeterminedValue));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        final Symbol fooSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null).getSymbol();
        assertThat(fooSymbol, is(notNullValue()));
        assertThat(fooSymbol, is(instanceOf(UserSymbol.class)));

        final Symbol barSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "bar", false, false, null, null).getSymbol();
        assertThat(barSymbol, is(notNullValue()));
        assertThat(barSymbol, is(instanceOf(UserSymbol.class)));

        assertThat(assembly.getMessages(), containsInAnyOrder(ImmutableList.<Matcher<? super AssemblyMessage>> of(
                new EquivalentAssemblyMessage(new SymbolDefinedWithUndeterminedValueErrorMessage((UserSymbol) fooSymbol)),
                new EquivalentAssemblyMessage(new SymbolDefinedWithUndeterminedValueErrorMessage((UserSymbol) barSymbol)))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
    }

    /**
     * Asserts that a symbol defined with a <code>null</code> value causes a {@link SymbolDefinedWithUndeterminedValueErrorMessage}
     * to be added to the assembly.
     */
    @Test
    public void symbolWithUndeterminedValue() {
        final TestSourceNode nodeThatDefinesASymbolWithUndeterminedValue = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT,
                null);
        final Assembly assembly = createAssembly(nodeThatDefinesASymbolWithUndeterminedValue);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        final Symbol fooSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null).getSymbol();
        assertThat(fooSymbol, is(notNullValue()));
        assertThat(fooSymbol, is(instanceOf(UserSymbol.class)));
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(
                new SymbolDefinedWithUndeterminedValueErrorMessage((UserSymbol) fooSymbol))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        nodeThatDefinesASymbolWithUndeterminedValue.assertAssembleCount(1);
    }

    /**
     * Asserts that a symbol defined with a <code>null</code> value does not cause a
     * {@link SymbolDefinedWithUndeterminedValueErrorMessage} to be added to the assembly if another error was added to the
     * assembly.
     */
    @Test
    public void symbolWithUndeterminedValueAndError() {
        final TestSourceNode nodeThatDefinesASymbolWithUndeterminedValue = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT,
                null);
        final TestSourceNode nodeThatAddsAnError = createNodeThatAddsAnError();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatDefinesASymbolWithUndeterminedValue,
                nodeThatAddsAnError));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        final Symbol fooSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null).getSymbol();
        assertThat(fooSymbol, is(notNullValue()));
        assertThat(fooSymbol, is(instanceOf(UserSymbol.class)));
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(createError())));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        nodeThatDefinesASymbolWithUndeterminedValue.assertAssembleCount(1);
        nodeThatAddsAnError.assertAssembleCount(1);
    }

    /**
     * Asserts that a symbol defined with a <code>null</code> value causes a {@link SymbolDefinedWithUndeterminedValueErrorMessage}
     * to be added to the assembly.
     */
    @Test
    public void symbolWithUndeterminedValueAndNoDefinition() {
        final TestSourceNode dummyNode = createNodeThatDoesNothing();

        final PredefinedSymbol predefinedSymbol = new PredefinedSymbol(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, null);
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, new TestArchitecture(
                dummyNode)).setPredefinedSymbols(new PredefinedSymbolTable(Collections.singleton(predefinedSymbol))));
        step(assembly, AssemblyCompletionStatus.COMPLETE);

        final Symbol fooSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null).getSymbol();
        assertThat(fooSymbol, is(notNullValue()));
        assertThat(fooSymbol, is(instanceOf(UserSymbol.class)));
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(
                new SymbolDefinedWithUndeterminedValueErrorMessage((UserSymbol) fooSymbol))));
        assertThat(assembly.getMessages().get(0).getStep(), is(sameInstance(assembly.getSteps().get(0))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        dummyNode.assertAssembleCount(1);
    }

    /**
     * Asserts that a symbol defined with a <code>null</code> value in the first pass does not cause a
     * {@link SymbolDefinedWithUndeterminedValueErrorMessage} to be added to the assembly if a second pass is required.
     */
    @Test
    public void symbolWithUndeterminedValueInFirstPass() {
        final TestSourceNode nodeThatReferencesTheBarSymbolAndDefinesTheFooSymbol = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                // This symbol reference will be unresolved initially and will trigger a second pass.
                final Symbol symbol = builder.resolveSymbolReference(SymbolContext.VALUE, "bar", false, false, null, null)
                        .getSymbol();

                Value value = null;
                if (symbol != null) {
                    value = (Value) symbol.getValue();
                }

                builder.defineSymbol(SymbolContext.VALUE, "foo", false, SymbolType.CONSTANT, value);
            }
        };

        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol("bar", SymbolType.CONSTANT, FORTY_TWO);

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(
                nodeThatReferencesTheBarSymbolAndDefinesTheFooSymbol, nodeThatDefinesTheBarSymbol));

        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        final Symbol fooSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "foo", false, false, null, null).getSymbol();
        assertThat(fooSymbol, is(notNullValue()));
        assertThat(fooSymbol, is(instanceOf(UserSymbol.class)));
        assertThat(fooSymbol.getValue(), is((Object) FORTY_TWO));

        final Symbol barSymbol = assembly.resolveSymbolReference(SymbolContext.VALUE, "bar", false, false, null, null).getSymbol();
        assertThat(barSymbol, is(notNullValue()));
        assertThat(barSymbol, is(instanceOf(UserSymbol.class)));
        assertThat(barSymbol.getValue(), is((Object) FORTY_TWO));

        nodeThatReferencesTheBarSymbolAndDefinesTheFooSymbol.assertAssembleCount(2);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(2);
    }

    /**
     * Asserts that tentative messages (added with {@link Assembly#addTentativeMessage(AssemblyMessage, AssemblyStep)}) are ignored
     * when a new pass is required (e.g. tentative error messages do not cause the assembly to fail) and considered on the final
     * pass of an assembly.
     */
    @Test
    public void tentativeMessagesAreIgnoredWhenANewPassIsRequiredAndConsideredOnTheFinalPass() {
        final TestSourceNode nodeThatAddsATentativeErrorMessage = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.addTentativeMessage(createError());
            }
        };

        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbol("foo");

        final TestSourceNode nodeThatDefinesTheFooSymbol = createNodeThatDefinesASymbol("foo", SymbolType.CONSTANT, FORTY_TWO);

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatAddsATentativeErrorMessage,
                nodeThatReferencesTheFooSymbol, nodeThatDefinesTheFooSymbol));

        final Assembly assembly = createAssembly(rootNode);

        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.STARTED_NEW_PASS);
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(createError())));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));

        nodeThatAddsATentativeErrorMessage.assertAssembleCount(2);
        nodeThatReferencesTheFooSymbol.assertAssembleCount(2);
        nodeThatDefinesTheFooSymbol.assertAssembleCount(2);
    }

    /**
     * Asserts that transformation blocks transform the output correctly.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void transformationBlock() throws IOException {
        final byte[] zeroOneTwoThree = new byte[] { 0, 1, 2, 3 };

        final TestSourceNode nodeThatOutputsData = createNodeThatEmitsData(zeroOneTwoThree);

        final TestSourceNode nodeThatEntersATransformationBlock = createNodeThatEntersATransformationBlock(ReverseTransformation.INSTANCE);

        final TestSourceNode nodeThatExitsATransformationBlock = createNodeThatExitsATransformationBlock();

        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatOutputsData,
                nodeThatEntersATransformationBlock, nodeThatOutputsData, nodeThatExitsATransformationBlock, nodeThatOutputsData));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        nodeThatEntersATransformationBlock.assertAssembleCount(1);
        nodeThatExitsATransformationBlock.assertAssembleCount(1);
        nodeThatOutputsData.assertAssembleCount(3);

        // Check the output
        checkOutput(assembly, new byte[] { 0, 1, 2, 3, 3, 2, 1, 0, 0, 1, 2, 3 });

        // Check the output, step by step
        final List<AssemblyStep> steps = assembly.getSteps();
        checkOutput(steps.get(0), EMPTY_BYTE_ARRAY);
        checkOutput(steps.get(1), zeroOneTwoThree);
        checkOutput(steps.get(2), new byte[] { 3, 2, 1, 0 });
        checkOutput(steps.get(3), zeroOneTwoThree);
        checkOutput(steps.get(4), EMPTY_BYTE_ARRAY);
        checkOutput(steps.get(5), zeroOneTwoThree);
    }

    /**
     * Asserts that a symbol reference that cannot be resolved causes the assembly to end in error with an
     * {@link UnresolvedSymbolReferenceErrorMessage}.
     */
    @Test
    public void unresolvedSymbolReference() {
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbolAndExpectsNoSymbol("foo");
        final Assembly assembly = createAssembly(nodeThatReferencesTheFooSymbol);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(),
                contains(new EquivalentAssemblyMessage(new UnresolvedSymbolReferenceErrorMessage("foo"))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        nodeThatReferencesTheFooSymbol.assertAssembleCount(1);
    }

    /**
     * Asserts that a symbol reference that cannot be resolved causes the assembly to end in error with an
     * {@link UnresolvedSymbolReferenceErrorMessage}.
     */
    @Test
    public void unresolvedSymbolReferenceWithResolvedSymbolReference() {
        final TestSourceNode nodeThatReferencesTheFooSymbol = createNodeThatReferencesASymbolAndExpectsNoSymbol("foo");
        final TestSourceNode nodeThatDefinesTheBarSymbol = createNodeThatDefinesASymbol("bar", SymbolType.CONSTANT, FORTY_TWO);
        final TestSourceNode nodeThatReferencesTheBarSymbol = createNodeThatReferencesASymbolAndExpectsAUserSymbol("bar", "bar",
                SymbolType.CONSTANT, FORTY_TWO);
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatReferencesTheFooSymbol,
                nodeThatDefinesTheBarSymbol, nodeThatReferencesTheBarSymbol));
        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(),
                contains(new EquivalentAssemblyMessage(new UnresolvedSymbolReferenceErrorMessage("foo"))));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));
        nodeThatReferencesTheFooSymbol.assertAssembleCount(1);
        nodeThatDefinesTheBarSymbol.assertAssembleCount(1);
        nodeThatReferencesTheBarSymbol.assertAssembleCount(1);
    }

    /**
     * Asserts that calling {@link Assembly#addMessage(AssemblyMessage, AssemblyStep)} with an {@link AssemblyWarningMessage} sets
     * the assembly's {@linkplain Assembly#getGravity() gravity} to {@link MessageGravity#WARNING}, and that calling
     * {@link Assembly#addMessage(AssemblyMessage, AssemblyStep)} afterwards with an {@link AssemblyErrorMessage} sets the
     * assembly's {@linkplain Assembly#getGravity() gravity} to {@link MessageGravity#ERROR}.
     */
    @Test
    public void warningMessageThenErrorMessage() {
        final TestSourceNode nodeThatAddsAWarning = createNodeThatAddsAWarning();
        final TestSourceNode nodeThatAddsAnError = createNodeThatAddsAnError();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatAddsAWarning, nodeThatAddsAnError));
        final Assembly assembly = createAssembly(rootNode);

        // Root node
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getMessages(), is(empty()));
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));

        // Warning node
        step(assembly, AssemblyCompletionStatus.PENDING);
        assertThat(assembly.getMessages(), contains(A_WARNING));
        assertThat(assembly.getGravity(), is(MessageGravity.WARNING));

        // Error node
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getMessages(), contains(ImmutableList.<Matcher<? super AssemblyMessage>> of(A_WARNING, AN_ERROR)));
        assertThat(assembly.getGravity(), is(MessageGravity.ERROR));

        nodeThatAddsAWarning.assertAssembleCount(1);
        nodeThatAddsAnError.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link Assembly#writeAssembledDataTo(OutputStream)} throws a {@link NullPointerException} when the
     * <code>out</code> argument is {@link NullPointerException}.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test(expected = NullPointerException.class)
    public void writeAssembledDataToNull() throws IOException {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, EMPTY_SOURCE_FILE, NullArchitecture.DEFAULT));
        assembly.writeAssembledDataTo(null);
    }

}
