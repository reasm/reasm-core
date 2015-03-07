package org.reasm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.messages.AssemblyInterruptedErrorMessage;
import org.reasm.messages.ExitingNamespaceWithoutNamespaceErrorMessage;
import org.reasm.messages.IOErrorMessage;
import org.reasm.messages.IllegalSymbolNameErrorMessage;
import org.reasm.messages.InternalAssemblerErrorMessage;
import org.reasm.messages.NoOpenTransformationBlockErrorMessage;
import org.reasm.messages.OutOfMemoryErrorMessage;
import org.reasm.messages.SymbolDefinedWithUndeterminedValueErrorMessage;
import org.reasm.messages.UnresolvedSymbolReferenceErrorMessage;
import org.reasm.source.AbstractSourceFile;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNode;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * An assembly.
 *
 * @author Francis Gagné
 */
public final class Assembly {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Builds the fully qualified name of a symbol name in a namespace.
     *
     * @param namespace
     *            the namespace
     * @param symbolName
     *            the base symbol name
     * @return the fully qualified symbol name
     */
    @Nonnull
    static String buildNamespacedSymbolName(@CheckForNull Namespace namespace, @Nonnull String symbolName) {
        if (namespace == null) { // optimization
            return symbolName;
        }

        final StringBuilder sb = new StringBuilder(symbolName);

        for (; namespace != null; namespace = namespace.getParent()) {
            sb.insert(0, '.');
            sb.insert(0, namespace.getName());
        }

        return sb.toString();
    }

    static boolean isAnonymousSymbolReference(@Nonnull String name) {
        if (name.length() <= 0) {
            return false;
        }

        char firstChar = name.charAt(0);
        if (firstChar != '+' && firstChar != '-') {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            if (name.charAt(i) != firstChar) {
                return false;
            }
        }

        return true;
    }

    static boolean isSuffixSymbolName(@Nonnull String name) {
        return name.length() >= 1 && name.charAt(0) == '.';
    }

    /**
     * Notifies the custom assembly data objects that the assembly process has completed.
     *
     * @param iterator
     *            an iterator on the collection of custom assembly data objects
     * @param t
     *            a Throwable currently being handled, or <code>null</code> if nothing was thrown
     */
    private static void notifyCustomAssemblyDataCompleted(@Nonnull Iterator<CustomAssemblyData> iterator, Throwable t) {
        while (iterator.hasNext()) {
            CustomAssemblyData customAssemblyData = iterator.next();
            try {
                customAssemblyData.completed();
            } catch (Throwable t2) {
                if (t == null) {
                    // Notify the rest of the custom assembly data objects, then re-throw.
                    notifyCustomAssemblyDataCompleted(iterator, t2);

                    // Re-throwing a Throwable doesn't require us to declare `throws Throwable` on the method,
                    // whereas throwing a Throwable from a local variable would.
                    throw t2;
                }

                // Add this exception as a suppressed exception and continue the loop.
                t.addSuppressed(t2);
            }
        }
    }

    @CheckForNull
    private AssemblyBuilder builder = new AssemblyBuilder(this);
    @Nonnull
    private final Configuration configuration;
    @Nonnull
    private final AtomicBoolean stepping = new AtomicBoolean();
    private int currentPass;
    @Nonnull
    private final SymbolTable symbolTable = new SymbolTable();
    @Nonnull
    private final HashMap<AssemblyStepLocation, Scope> scopeTable = new HashMap<>();
    @Nonnull
    private final Iterable<UserSymbol> allSymbols;
    @Nonnull
    private MessageGravity gravity = MessageGravity.NONE;
    @Nonnull
    private final ArrayList<AssemblyMessage> messages = new ArrayList<>();
    @Nonnull
    private final ArrayList<AssemblyMessage> tentativeMessages = new ArrayList<>();
    @Nonnull
    private final TreeMap<String, Namespace> namespaces = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    @Nonnull
    private final AssemblyMessage outOfMemoryError = new OutOfMemoryErrorMessage();
    @Nonnull
    private final Map<String, byte[]> binaryFileCache = new HashMap<>();
    private final Map<String, SourceFile> sourceFileCache = new HashMap<>();

    // The following fields apply to the current pass only. They will be reset when a new pass starts.
    private final OutputImpl output;
    private long programCounter;
    private int forwCounter;
    private int backCounter;
    @CheckForNull
    private AssemblyStepLocation currentScopeKey;
    @CheckForNull
    private UserSymbol lastNonSuffixSymbol;
    @Nonnull
    private final ArrayList<SymbolReference> symbolReferences = new ArrayList<>();
    @Nonnull
    private final ArrayList<Block> blockStack = new ArrayList<>();
    @Nonnull
    private final ArrayList<AssemblyStep> steps = new ArrayList<>(1000);
    @Nonnull
    private final List<AssemblyStep> unmodifiableSteps = Collections.unmodifiableList(this.steps);
    @CheckForNull
    private Namespace currentNamespace;
    @Nonnull
    private final ArrayList<TransformationBlock> transformationBlockStack = new ArrayList<>();
    @Nonnull
    private Charset currentEncoding = UTF_8;

    @Nonnull
    private final Map<Object, CustomAssemblyData> customAssemblyData = new HashMap<>();

    // Publicly exposed unmodifiable wrappers around internally modifiable collections
    @Nonnull
    private final IterableProxy<UserSymbol> symbolsProxy = new IterableProxy<>(this.symbolTable);
    @Nonnull
    private final Map<AssemblyStepLocation, Scope> unmodifiableScopeTable = Collections.unmodifiableMap(this.scopeTable);
    @Nonnull
    private final List<AssemblyMessage> unmodifiableMessages = Collections.unmodifiableList(this.messages);
    @Nonnull
    private final Map<String, Namespace> unmodifiableNamespaces = Collections.unmodifiableMap(this.namespaces);

    /**
     * Initializes a new assembly.
     *
     * @param configuration
     *            the configuration the assembly is based on; must not be <code>null</code>
     */
    public Assembly(@Nonnull Configuration configuration) {
        if (configuration == null) {
            throw new NullPointerException("configuration");
        }

        final Iterable<Iterable<UserSymbol>> localSymbols = Iterables.transform(this.scopeTable.values(),
                new Function<Scope, Iterable<UserSymbol>>() {
                    @Override
                    public Iterable<UserSymbol> apply(Scope input) {
                        return input.getLocalSymbolTable();
                    }
                });
        this.allSymbols = Iterables.concat(this.symbolTable, Iterables.concat(localSymbols));

        this.configuration = configuration;
        this.output = new OutputImpl(configuration.getEnvironment().getOutputMemorySize());
        this.startPass();

        // Add the predefined symbols specified in the configuration to the assembly.
        for (PredefinedSymbol predefinedSymbol : configuration.getPredefinedSymbols().symbols()) {
            this.symbolTable.addSymbol(UserSymbol.fromPredefinedSymbol(predefinedSymbol));
        }
    }

    /**
     * Fetches the raw contents of a file.
     *
     * @param filePath
     *            the path to a file
     * @return the file contents
     * @throws IOException
     *             an I/O exception occurred
     */
    @Nonnull
    public final byte[] fetchBinaryFile(@Nonnull String filePath) throws IOException {
        if (this.binaryFileCache.containsKey(filePath)) {
            return this.binaryFileCache.get(filePath);
        }

        final FileFetcher fileFetcher = this.configuration.getFileFetcher();
        if (fileFetcher == null) {
            throw new FileNotFoundException("no file fetcher was configured");
        }

        final byte[] fileContents = fileFetcher.fetchBinaryFile(filePath);
        if (fileContents == null) {
            throw new FileNotFoundException(filePath);
        }

        this.binaryFileCache.put(filePath, fileContents);
        return fileContents;
    }

    /**
     * Fetches a source file.
     *
     * @param filePath
     *            the path to a file
     * @return the {@link SourceFile}
     * @throws IOException
     *             an I/O exception occurred
     */
    @Nonnull
    public final SourceFile fetchSourceFile(@Nonnull String filePath) throws IOException {
        if (this.sourceFileCache.containsKey(filePath)) {
            return this.sourceFileCache.get(filePath);
        }

        final FileFetcher fileFetcher = this.configuration.getFileFetcher();
        if (fileFetcher == null) {
            throw new FileNotFoundException("no file fetcher was configured");
        }

        final SourceFile sourceFile = fileFetcher.fetchSourceFile(filePath);
        if (sourceFile == null) {
            throw new FileNotFoundException(filePath);
        }

        this.sourceFileCache.put(filePath, sourceFile);
        return sourceFile;
    }

    /**
     * Gets the configuration this assembly is based on.
     *
     * @return the configuration
     */
    @Nonnull
    public final Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Gets the current encoding to use when encoding strings to bytes. The default encoding is UTF-8.
     *
     * @return the {@link Charset} that represents the current encoding
     */
    @Nonnull
    public final Charset getCurrentEncoding() {
        return this.currentEncoding;
    }

    /**
     * Gets the current namespace for symbol lookup and symbol definition.
     *
     * @return the current namespace
     */
    @CheckForNull
    public final Namespace getCurrentNamespace() {
        return this.currentNamespace;
    }

    /**
     * Gets the current pass in this assembly.
     *
     * @return the current pass
     */
    public final int getCurrentPass() {
        return this.currentPass;
    }

    /**
     * Gets a {@link SymbolLookupContext} with some attributes from the current state of this assembly.
     *
     * @return a {@link SymbolLookupContext}
     */
    @Nonnull
    public final SymbolLookupContext getCurrentSymbolLookupContext() {
        return new SymbolLookupContext(this, this.currentNamespace, this.currentScopeKey, this.lastNonSuffixSymbol,
                this.forwCounter, this.backCounter);
    }

    /**
     * Gets the highest message gravity.
     *
     * @return the highest message gravity.
     */
    @Nonnull
    public final MessageGravity getGravity() {
        return this.gravity;
    }

    /**
     * Gets the messages produced during the assembly.
     *
     * @return the {@link List} of messages produced during the assembly
     */
    @Nonnull
    public final List<AssemblyMessage> getMessages() {
        return this.unmodifiableMessages;
    }

    /**
     * Gets a {@link Map} of the root namespaces that exist in this assembly.
     *
     * @return an unmodifiable {@link Map} of the root namespaces that exist in this assembly
     */
    public final Map<String, Namespace> getNamespaces() {
        return this.unmodifiableNamespaces;
    }

    /**
     * Gets the output of this assembly.
     *
     * @return an {@link Output} that gives access to the assembly's output
     */
    public final Output getOutput() {
        return this.output;
    }

    /**
     * Gets the current program counter in this assembly.
     *
     * @return the program counter
     */
    public final long getProgramCounter() {
        return this.programCounter;
    }

    /**
     * Gets the scopes for local symbols in this assembly.
     *
     * @return the map of {@link AssemblyStepLocation AssemblyStepLocations} to {@link Scope Scopes}
     */
    public final Map<AssemblyStepLocation, Scope> getScopes() {
        return this.unmodifiableScopeTable;
    }

    /**
     * Gets a list of the steps performed in this assembly. This list is unmodifiable, but it will contain the new steps created by
     * further calls to {@link #step()}, so it is not necessary to retrieve the list again.
     *
     * @return the list of steps
     */
    public final List<AssemblyStep> getSteps() {
        return this.unmodifiableSteps;
    }

    /**
     * Gets the non-local symbols declared or defined in this assembly.
     *
     * @return an {@link Iterable} of the non-local symbols declared or defined in this assembly
     */
    @Nonnull
    public final Iterable<UserSymbol> getSymbols() {
        return this.symbolsProxy;
    }

    /**
     * Resolves a reference to a symbol.
     * <p>
     * This method must <strong>not</strong> be used to resolve symbol references that appear in the source of this assembly; use
     * {@link AssemblyBuilder#resolveSymbolReference(List, String, boolean, SymbolLookupContext, SymbolResolutionFallback)} instead.
     * This method may be used to access the symbols from this assembly without recording it so that it isn't considered when
     * determining if a new pass is necessary.
     *
     * @param contexts
     *            a list of contexts of the symbol reference. The symbol will be looked up in each of these contexts in order, until
     *            a symbol is found.
     * @param name
     *            the name of the symbol to look up
     * @param local
     *            <code>true</code> to look up a local symbol; otherwise, <code>false</code>
     * @param lookupContext
     *            the context in which to perform the symbol lookups, or <code>null</code> to use the current context
     * @param symbolResolutionFallback
     *            a {@link SymbolResolutionFallback} object that provides a means of returning a symbol when no existing symbol is
     *            found
     * @return a new {@link SymbolReference} that stores the parameters of the symbol reference as well as the resolved
     *         {@linkplain SymbolReference#getSymbol() symbol} and the {@linkplain SymbolReference#getValue() value} it had at the
     *         time the symbol reference was resolved
     */
    public final SymbolReference resolveSymbolReference(@Nonnull List<? extends SymbolContext<?>> contexts, @Nonnull String name,
            boolean local, @CheckForNull SymbolLookupContext lookupContext,
            @CheckForNull SymbolResolutionFallback symbolResolutionFallback) {
        if (contexts == null) {
            throw new NullPointerException("contexts");
        }

        if (name == null) {
            throw new NullPointerException("name");
        }

        return new SymbolReference(ImmutableList.copyOf(contexts), name, local, false, this.checkLookupContext(lookupContext),
                null, symbolResolutionFallback);
    }

    /**
     * Resolves a reference to a symbol.
     * <p>
     * This method must <strong>not</strong> be used to resolve symbol references that appear in the source of this assembly; use
     * {@link AssemblyBuilder#resolveSymbolReference(SymbolContext, String, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * instead. This method may be used to access the symbols from this assembly without recording it so that it isn't considered
     * when determining if a new pass is necessary.
     *
     * @param context
     *            the context of the symbol reference
     * @param name
     *            the name of the symbol to look up
     * @param local
     *            <code>true</code> to look up a local symbol; otherwise, <code>false</code>
     * @param lookupContext
     *            the context in which to perform the symbol lookups, or <code>null</code> to use the current context
     * @param symbolResolutionFallback
     *            a {@link SymbolResolutionFallback} object that provides a means of returning a symbol when no existing symbol is
     *            found
     * @return a new {@link SymbolReference} that stores the parameters of the symbol reference as well as the resolved
     *         {@linkplain SymbolReference#getSymbol() symbol} and the {@linkplain SymbolReference#getValue() value} it had at the
     *         time the symbol reference was resolved
     */
    public final SymbolReference resolveSymbolReference(@Nonnull SymbolContext<?> context, @Nonnull String name, boolean local,
            @CheckForNull SymbolLookupContext lookupContext, @CheckForNull SymbolResolutionFallback symbolResolutionFallback) {
        if (context == null) {
            throw new NullPointerException("context");
        }

        if (name == null) {
            throw new NullPointerException("name");
        }

        return new SymbolReference(SymbolReference.cachedContextSingleton(context), name, local, false,
                this.checkLookupContext(lookupContext), null, symbolResolutionFallback);
    }

    /**
     * Resolves a reference to a symbol.
     * <p>
     * This method must <strong>not</strong> be used to resolve symbol references that appear in the source of this assembly; use
     * {@link AssemblyBuilder#resolveSymbolReference(SymbolContext[], String, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * instead. This method may be used to access the symbols from this assembly without recording it so that it isn't considered
     * when determining if a new pass is necessary.
     *
     * @param contexts
     *            an array of contexts of the symbol reference. The symbol will be looked up in each of these contexts in order,
     *            until a symbol is found.
     * @param name
     *            the name of the symbol to look up
     * @param local
     *            <code>true</code> to look up a local symbol; otherwise, <code>false</code>
     * @param lookupContext
     *            the context in which to perform the symbol lookups, or <code>null</code> to use the current context
     * @param symbolResolutionFallback
     *            a {@link SymbolResolutionFallback} object that provides a means of returning a symbol when no existing symbol is
     *            found
     * @return a new {@link SymbolReference} that stores the parameters of the symbol reference as well as the resolved
     *         {@linkplain SymbolReference#getSymbol() symbol} and the {@linkplain SymbolReference#getValue() value} it had at the
     *         time the symbol reference was resolved
     */
    public final SymbolReference resolveSymbolReference(@Nonnull SymbolContext<?>[] contexts, @Nonnull String name, boolean local,
            @CheckForNull SymbolLookupContext lookupContext, @CheckForNull SymbolResolutionFallback symbolResolutionFallback) {
        if (contexts == null) {
            throw new NullPointerException("contexts");
        }

        if (name == null) {
            throw new NullPointerException("name");
        }

        return new SymbolReference(ImmutableList.copyOf(contexts), name, local, false, this.checkLookupContext(lookupContext),
                null, symbolResolutionFallback);
    }

    /**
     * Performs a step in assembling the source.
     *
     * @return A member of the {@link AssemblyCompletionStatus} enum.
     *         <ul>
     *         <li>COMPLETE: the assembly is complete. Once this method has returned COMPLETE for a given assembly, calling this
     *         method again will do nothing and return COMPLETE.</li>
     *         <li>PENDING: the assembly is not complete and has additional steps to perform. Call this method again to perform the
     *         next step.</li>
     *         <li>STARTED_NEW_PASS: the assembly is not complete and just started a new pass. This is returned only between passes;
     *         it is not returned before the first pass or after the last pass. Therefore, if only one pass is done, this value will
     *         never be returned.</li>
     *         </ul>
     */
    @Nonnull
    public final AssemblyCompletionStatus step() {
        if (!this.stepping.compareAndSet(false, true)) {
            throw new IllegalStateException("A step is already in progress in this assembly.");
        }

        try {
            final AssemblyBuilder builder = this.builder;
            if (builder == null) {
                return AssemblyCompletionStatus.COMPLETE;
            }

            try {
                // Create the assembly step.
                final TransformationBlock activeTransformationBlock = this.getActiveTransformationBlock();
                final AssemblyStep step = new AssemblyStep(this.blockStack.get(this.blockStack.size() - 1).nextLocation(),
                        this.programCounter, activeTransformationBlock != null ? activeTransformationBlock.getOutput()
                                : this.output);

                this.steps.add(step);

                builder.setStep(step);
                try {
                    final SourceNode sourceNode = step.getLocation().getSourceLocation().getSourceNode();

                    // Assemble the source node.
                    sourceNode.assemble(builder);

                    // Pop blocks whose end has been reached off the stack.
                    while (!this.blockStack.isEmpty() && !this.blockStack.get(this.blockStack.size() - 1).hasNextLocation()) {
                        this.popBlock();
                    }

                    // Increase the program counter.
                    this.programCounter += step.getAssembledDataLength();
                } finally {
                    builder.setStep(null);
                }

                // If a fatal error occurred, stop immediately.
                if (this.gravity == MessageGravity.FATAL_ERROR) {
                    this.completeAssembly(null);
                    return AssemblyCompletionStatus.COMPLETE;
                }

                if (this.blockStack.isEmpty()) {
                    boolean assemblyRequiresNewPass = false;
                    int numberOfUnresolvedSymbolReferences = 0;
                    for (SymbolReference symbolReference : this.symbolReferences) {
                        if (symbolReference.isStale()) {
                            assemblyRequiresNewPass = true;
                            break;
                        }

                        if (symbolReference.getSymbol() == null) {
                            numberOfUnresolvedSymbolReferences++;
                        }
                    }

                    ArrayList<UserSymbol> symbolsDefinedWithUndefinedValue = null;
                    for (UserSymbol symbol : this.allSymbols) {
                        if (assemblyRequiresNewPass) {
                            symbol.prepareForNewPass();
                        } else if (symbol.getValue() == null) {
                            if (symbolsDefinedWithUndefinedValue == null) {
                                symbolsDefinedWithUndefinedValue = new ArrayList<>();
                            }

                            symbolsDefinedWithUndefinedValue.add(symbol);
                        }
                    }

                    if (!assemblyRequiresNewPass) {
                        // Reserve space for all tentative messages, all unresolved symbol references
                        // and for this.outOfMemoryError eventually.
                        this.messages.ensureCapacity(this.messages.size() + this.tentativeMessages.size()
                                + numberOfUnresolvedSymbolReferences + 1);

                        for (AssemblyMessage message : this.tentativeMessages) {
                            this.appendMessage(message);
                        }

                        if (numberOfUnresolvedSymbolReferences != 0) {
                            for (SymbolReference symbolReference : this.symbolReferences) {
                                if (symbolReference.getSymbol() == null) {
                                    final AssemblyMessage message = new UnresolvedSymbolReferenceErrorMessage(symbolReference);
                                    message.addToAssembly(symbolReference.getStep());
                                    this.appendMessage(message);
                                }
                            }
                        }
                    }

                    this.tentativeMessages.clear();

                    if (symbolsDefinedWithUndefinedValue != null && this.gravity.compareTo(MessageGravity.ERROR) < 0) {
                        // Raise errors for symbols defined with an undefined value. This may happen, for example, if a
                        // symbol references itself, directly or indirectly, in its definition.
                        // Only do this if there are no other errors, because other errors (e.g. undefined symbols) might cause
                        // this and we probably don't need to confuse the programmer with these messages.
                        for (UserSymbol symbol : symbolsDefinedWithUndefinedValue) {
                            this.addMessage(new SymbolDefinedWithUndeterminedValueErrorMessage(symbol),
                                    symbol.getDefinition() != null ? symbol.getDefinition() : step);
                        }
                    }

                    // Decide whether we do another pass or not.
                    if (this.gravity.compareTo(MessageGravity.ERROR) >= 0 || !assemblyRequiresNewPass) {
                        this.completeAssembly(null);
                        return AssemblyCompletionStatus.COMPLETE;
                    }

                    this.startPass();
                    this.output.clear();
                    return AssemblyCompletionStatus.STARTED_NEW_PASS;
                }

                return AssemblyCompletionStatus.PENDING;
            } catch (OutOfMemoryError e) {
                this.outOfMemory(e, this.getLastStep());
                return AssemblyCompletionStatus.COMPLETE;
            } catch (ThreadDeath td) {
                try {
                    this.addMessage(new AssemblyInterruptedErrorMessage(), this.getLastStep());
                    this.completeAssembly(td);
                } catch (Throwable t) {
                    td.addSuppressed(t);
                }

                throw td;
            } catch (IOException e) {
                this.processIOException(e, this.getLastStep());
                return AssemblyCompletionStatus.COMPLETE;
            } catch (Throwable t) {
                this.internalAssemblerError(t, this.getLastStep());
                return AssemblyCompletionStatus.COMPLETE;
            }
        } finally {
            this.stepping.set(false);
        }
    }

    /**
     * Writes the assembled data to an {@link OutputStream}.
     *
     * @param out
     *            the output stream to write the data to
     * @throws IOException
     *             an I/O exception occurred while writing to the stream
     */
    public final void writeAssembledDataTo(@Nonnull OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException("out");
        }

        this.output.writeTo(out);
    }

    /** @see AssemblyBuilder#addMessage(AssemblyMessage) */
    final void addMessage(@Nonnull AssemblyMessage message, @CheckForNull AssemblyStep step) {
        // Reserve space for this message and for this.outOfMemoryError eventually.
        this.messages.ensureCapacity(this.messages.size() + 2);

        message.addToAssembly(step); // may throw
        this.appendMessage(message);
    }

    /**
     * Adds the specified symbol to the assembly.
     *
     * @param symbol
     *            the symbol to add to the assembly
     * @param scope
     *            the scope in which the local symbol is defined, or <code>null</code> if the symbol is not a local symbol
     */
    final void addSymbol(@Nonnull UserSymbol symbol, Scope scope) {
        SymbolTable symbolTable = this.symbolTable;
        if (scope != null) {
            symbolTable = scope.getLocalSymbolTable();
        }

        symbolTable.addSymbol(symbol);
    }

    final void addSymbolReference(@Nonnull SymbolReference symbolReference) {
        this.symbolReferences.add(symbolReference);
    }

    /** @see AssemblyBuilder#addTentativeMessage(AssemblyMessage) */
    final void addTentativeMessage(@Nonnull AssemblyMessage message, @Nonnull AssemblyStep step) {
        message.addToAssembly(step); // may throw
        this.tentativeMessages.add(message);
    }

    @Nonnull
    final SymbolLookupContext checkLookupContext(@CheckForNull SymbolLookupContext lookupContext) {
        if (lookupContext == null) {
            lookupContext = this.getCurrentSymbolLookupContext();
        } else if (lookupContext.getAssembly() != this) {
            throw new IllegalArgumentException("SymbolLookupContext comes from a different assembly");
        }

        return lookupContext;
    }

    /** @see AssemblyBuilder#defineSymbol(SymbolContext, String, boolean, SymbolType, Object) */
    final <TValue> void defineSymbol(@Nonnull SymbolContext<TValue> context, @Nonnull String symbolName, boolean isLocalSymbol,
            @Nonnull AssemblyStep definition, @Nonnull SymbolType symbolType, @CheckForNull TValue value) {
        if (symbolName.length() == 1 && symbolName.charAt(0) == '+') {
            // Anonymous symbol, forward reference.
            this.defineAnonymousSymbol(context, false, definition, symbolType, value);
        } else if (symbolName.length() == 1 && symbolName.charAt(0) == '-') {
            // Anonymous symbol, backward reference.
            this.defineAnonymousSymbol(context, true, definition, symbolType, value);
        } else if (symbolName.length() == 1 && symbolName.charAt(0) == '/') {
            // Anonymous symbol, both forward and backward reference: define 2 symbols at once.
            this.defineAnonymousSymbol(context, false, definition, symbolType, value);
            this.defineAnonymousSymbol(context, true, definition, symbolType, value);
        } else if (isAnonymousSymbolReference(symbolName)) {
            // The name is illegal, because it's impossible to reference it.
            this.addMessage(new IllegalSymbolNameErrorMessage(symbolName), definition);
        } else {
            this.defineSymbolFinal(context, symbolName, isLocalSymbol, definition, symbolType, value);
        }
    }

    /** @see AssemblyBuilder#endPass() */
    final void endPass(@Nonnull AssemblyStep step) throws IOException {
        while (!this.blockStack.isEmpty()) {
            this.popBlock();
        }

        step.setHasSideEffects();
    }

    /** @see AssemblyBuilder#enterBlock(Iterable, AssemblyStepIterationController, boolean, BlockEvents) */
    final void enterBlock(@Nonnull Iterable<SourceLocation> sourceLocations,
            @CheckForNull AssemblyStepIterationController iterationController, @Nonnull AssemblyStep step,
            boolean transparentParent, @CheckForNull BlockEvents events) {
        this.blockStack.add(new Block(new AssemblyStepLocationGenerator(sourceLocations, iterationController, step.getLocation(),
                transparentParent), events));

        step.setHasSideEffects();
    }

    /** @see AssemblyBuilder#enterComposite(boolean, BlockEvents) */
    final void enterComposite(@Nonnull AssemblyStep step, boolean transparentParent, @CheckForNull BlockEvents events) {
        this.enterBlock(step.getLocation().getSourceLocation().getChildSourceLocations(), null, step, transparentParent, events);
    }

    /** @see AssemblyBuilder#enterFile(AbstractSourceFile, Architecture) */
    final void enterFile(@Nonnull AbstractSourceFile<?> file, @CheckForNull Architecture architecture, @Nonnull AssemblyStep step) {
        final Architecture fileArchitecture = architecture == null ? step.getLocation().getSourceLocation().getArchitecture()
                : architecture;
        this.enterBlock(file.getSourceLocations(fileArchitecture), null, step, false, null);
    }

    /** @see AssemblyBuilder#enterNamespace(String) */
    final void enterNamespace(@Nonnull AssemblyStep step, @Nonnull String name) {
        Map<String, Namespace> namespaces;
        if (this.currentNamespace == null) {
            namespaces = this.namespaces;
        } else {
            namespaces = this.currentNamespace.internalGetInnerNamespaces();
        }

        Namespace ns = namespaces.get(name);
        if (ns == null) {
            ns = new Namespace(name, this.currentNamespace);
            namespaces.put(name, ns);
        }

        this.currentNamespace = ns;
        step.setHasSideEffects();
    }

    /** @see AssemblyBuilder#enterTransformationBlock(OutputTransformation) */
    final void enterTransformationBlock(@Nonnull AssemblyStep step, @Nonnull OutputTransformation transformation) {
        this.transformationBlockStack.add(new TransformationBlock(transformation, step, this.configuration.getEnvironment()));
        step.setHasSideEffects();
    }

    /** @see AssemblyBuilder#exitNamespace() */
    final void exitNamespace(@Nonnull AssemblyStep step) {
        if (this.currentNamespace == null) {
            this.addMessage(new ExitingNamespaceWithoutNamespaceErrorMessage(), step);
        } else {
            this.currentNamespace = this.currentNamespace.getParent();
        }

        step.setHasSideEffects();
    }

    /** @see AssemblyBuilder#exitTransformationBlock() */
    final void exitTransformationBlock(@Nonnull AssemblyStep step) throws IOException {
        if (this.transformationBlockStack.isEmpty()) {
            this.addMessage(new NoOpenTransformationBlockErrorMessage(), step);
        } else {
            final TransformationBlock transformationBlock = this.transformationBlockStack.remove(this.transformationBlockStack
                    .size() - 1);
            final AssemblyStep start = transformationBlock.getStart();

            // Reset the assembly step to the start of the block during the transformation
            // so that the transformed data gets written there.
            final AssemblyBuilder builder = this.builder;
            assert builder != null;
            builder.setStep(start);
            try {
                transformationBlock.transform(builder);
            } finally {
                builder.setStep(step);
            }

            this.programCounter = start.getProgramCounter() + start.getAssembledDataLength();
        }

        step.setHasSideEffects();
    }

    /** @see AssemblyBuilder#getCurrentBlock() */
    final Block getCurrentBlock() {
        if (this.blockStack.isEmpty()) {
            throw new IllegalStateException("No current block");
        }

        return this.blockStack.get(this.blockStack.size() - 1);
    }

    /** @see AssemblyBuilder#getCustomAssemblyData(Object) */
    @CheckForNull
    final CustomAssemblyData getCustomAssemblyData(@Nonnull Object key) {
        return this.customAssemblyData.get(key);
    }

    /**
     * Gets the scope for the specified key, creating it if it doesn't exist.
     *
     * @param scopeKey
     *            the {@link AssemblyStepLocation} where the scope starts
     * @return the scope
     */
    @Nonnull
    final Scope getScope(@CheckForNull AssemblyStepLocation scopeKey) {
        Scope scope = this.scopeTable.get(scopeKey);

        if (scope == null) {
            scope = new Scope();
            this.scopeTable.put(scopeKey, scope);
        }

        return scope;
    }

    @Nonnull
    final SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    /** @see AssemblyBuilder#processIOException(IOException) */
    final void processIOException(@Nonnull IOException exception, @CheckForNull AssemblyStep step) {
        this.addMessage(new IOErrorMessage(exception), step);
        this.completeAssembly(exception);
    }

    /** @see AssemblyBuilder#setCurrentEncoding(Charset) */
    final void setCurrentEncoding(@Nonnull Charset encoding) {
        this.currentEncoding = encoding;
    }

    /** @see AssemblyBuilder#setCustomAssemblyData(Object, CustomAssemblyData) */
    final void setCustomAssemblyData(@Nonnull Object key, @Nonnull CustomAssemblyData customAssemblyData) {
        this.customAssemblyData.put(key, customAssemblyData);
    }

    /** @see AssemblyBuilder#setProgramCounter(long) */
    final void setProgramCounter(@Nonnull AssemblyStep step, long programCounter) {
        this.programCounter = programCounter;
        step.setHasSideEffects();
    }

    private final void appendMessage(AssemblyMessage message) {
        this.messages.add(message);
        if (this.gravity.compareTo(message.getGravity()) < 0) {
            this.gravity = message.getGravity();
        }
    }

    /**
     * Completes the assembly process.
     *
     * @param t
     *            the {@link Throwable} that caused the assembly to end, or <code>null</code> if the assembly completed normally
     */
    private final void completeAssembly(Throwable t) {
        final AssemblyBuilder builder = this.builder;

        // Avoid running this more than once.
        if (builder == null) {
            return;
        }

        this.builder = null;

        try {
            notifyCustomAssemblyDataCompleted(this.customAssemblyData.values().iterator(), t);
        } finally {
            builder.completeAssembly();
        }
    }

    /**
     * Defines an anonymous symbol in this assembly.
     *
     * @param context
     *            the context in which the symbol is defined
     * @param backward
     *            <code>true</code> for a backward anonymous symbol, <code>false</code> for a forward anonymous symbol
     * @param definition
     *            the assembly step in which the symbol is defined
     * @param symbolType
     *            the type of symbol
     * @param value
     *            the value of the symbol
     */
    private final <TValue> void defineAnonymousSymbol(@Nonnull SymbolContext<TValue> context, boolean backward,
            @Nonnull AssemblyStep definition, @Nonnull SymbolType symbolType, @CheckForNull TValue value) {
        String symbolName;
        if (backward) {
            symbolName = String.format("__back%d", ++this.backCounter);
        } else {
            symbolName = String.format("__forw%d", ++this.forwCounter);
        }

        this.defineSymbolFinal(context, symbolName, false, definition, symbolType, value);
    }

    /**
     * Defines a symbol in this assembly without altering the symbol name.
     *
     * @param context
     *            the context in which the symbol is defined
     * @param symbolName
     *            the name of the symbol
     * @param isLocalSymbol
     *            <code>true</code> if the symbol is a local symbol; otherwise, <code>false</code>
     * @param definition
     *            the assembly step in which the symbol is defined
     * @param symbolType
     *            the type of symbol
     * @param value
     *            the value of the symbol
     */
    private final <TValue> void defineSymbolFinal(@Nonnull SymbolContext<TValue> context, @Nonnull String symbolName,
            boolean isLocalSymbol, @Nonnull AssemblyStep definition, @Nonnull SymbolType symbolType, @CheckForNull TValue value) {
        // Try to find an existing symbol with that name, or fall back to creating the symbol.
        final SymbolReference symbolReference = new SymbolReference(SymbolReference.cachedContextSingleton(context), symbolName,
                isLocalSymbol, true, this.getCurrentSymbolLookupContext(), definition,
                SymbolDefinitionResolutionFallback.getInstance(symbolType));
        this.addSymbolReference(symbolReference);
        final UserSymbol symbol = (UserSymbol) symbolReference.getSymbol();
        assert symbol != null;

        if (symbol.define(this, definition, symbolType, value)) {
            // Update the symbol reference's value, because we just changed it.
            symbolReference.setValue(value);
        }

        if (!isLocalSymbol) {
            this.currentScopeKey = definition.getLocation();

            if (!Assembly.isSuffixSymbolName(symbolName)) {
                this.lastNonSuffixSymbol = symbol;
            }
        }
    }

    @CheckForNull
    private final TransformationBlock getActiveTransformationBlock() {
        if (this.transformationBlockStack.isEmpty()) {
            return null;
        }

        return this.transformationBlockStack.get(this.transformationBlockStack.size() - 1);
    }

    private final AssemblyStep getLastStep() {
        if (this.steps.isEmpty()) {
            return null;
        }

        return this.steps.get(this.steps.size() - 1);
    }

    /**
     * Adds an "internal assembler error" fatal error message to the assembly.
     *
     * @param t
     *            the throwable
     * @param step
     *            the assembly step in which the message is being generated
     */
    private final void internalAssemblerError(@Nonnull Throwable t, @CheckForNull AssemblyStep step) {
        this.addMessage(new InternalAssemblerErrorMessage(t), step);
        this.completeAssembly(t);
    }

    /**
     * Adds an "out of memory" fatal error message to the assembly.
     *
     * @param e
     *            the {@link OutOfMemoryError} that was thrown
     *
     * @param step
     *            the assembly step in which the message is being generated
     */
    private final void outOfMemory(OutOfMemoryError e, @CheckForNull AssemblyStep step) {
        try {
            this.completeAssembly(e);
        } finally {
            // Add the message manually to the assembly. addMessage() ensures that the ArrayList has enough room to add another
            // message without having to allocate a new array.
            this.outOfMemoryError.addToAssembly(step);
            this.messages.add(this.outOfMemoryError);
            this.gravity = MessageGravity.FATAL_ERROR;
        }
    }

    private final void popBlock() throws IOException {
        this.blockStack.get(this.blockStack.size() - 1).exitBlock();
        this.blockStack.remove(this.blockStack.size() - 1);
    }

    /**
     * Starts a new pass.
     */
    private final void startPass() {
        this.steps.clear();
        this.programCounter = 0;
        this.forwCounter = 0;
        this.backCounter = 0;
        this.currentScopeKey = null;
        this.lastNonSuffixSymbol = null;
        this.symbolReferences.clear();
        SourceFile mainSourceFile = this.configuration.getMainSourceFile();
        Architecture initialArchitecture = this.configuration.getInitialArchitecture();
        this.blockStack.add(new Block(new AssemblyStepLocationGenerator(mainSourceFile.getSourceLocations(initialArchitecture),
                null, null, false), null));
        this.currentEncoding = UTF_8;
        this.currentNamespace = null;
        ++this.currentPass;

        for (CustomAssemblyData customAssemblyData : this.customAssemblyData.values()) {
            customAssemblyData.startedNewPass();
        }
    }
}
