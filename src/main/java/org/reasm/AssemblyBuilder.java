package org.reasm;

import java.io.IOException;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.source.AbstractSourceFile;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNode;

import com.google.common.collect.ImmutableList;

/**
 * Contains methods to build an assembly.
 * <p>
 * The {@link AssemblyBuilder} class exposes methods that allow building an assembly, while the {@link Assembly} class exposes
 * methods that allow consuming an assembly.
 * <p>
 * Instances of the {@link AssemblyBuilder} class are not thread-safe; i.e., it is not safe to call the mutator methods of this
 * class on the same instance concurrently in different threads. However, it is safe to call those methods on different instances in
 * different threads.
 *
 * @author Francis Gagné
 */
public final class AssemblyBuilder {

    // assembly and step can both be null. However, I'm not putting a @CheckForNull annotation on them because FindBugs warns
    // me even though the fields are validated in checkState().

    //@CheckForNull
    private Assembly assembly;
    //@CheckForNull
    private AssemblyStep step;

    /**
     * Initializes a new AssemblyBuilder.
     *
     * @param assembly
     *            the assembly to manage
     */
    AssemblyBuilder(Assembly assembly) {
        this.assembly = assembly;
    }

    /**
     * Adds a message to the assembly.
     * <p>
     * Error messages cause the assembly to stop at the end of the current pass. Fatal error messages cause the assembly to stop
     * after the current step.
     *
     * @param message
     *            the message to add
     */
    public final void addMessage(@Nonnull AssemblyMessage message) {
        if (message == null) {
            throw new NullPointerException("message");
        }

        this.checkState();
        this.assembly.addMessage(message, this.step);
    }

    /**
     * Adds a message tentatively to the assembly.
     * <p>
     * Tentative messages are messages that are only processed at the end of the current pass if no additional pass is necessary. If
     * a new pass is required, the tentative messages are removed. Otherwise, they are added to the assembly before completing the
     * assembly.
     *
     * @param message
     *            the message to add
     */
    public final void addTentativeMessage(@Nonnull AssemblyMessage message) {
        if (message == null) {
            throw new NullPointerException("message");
        }

        this.checkState();
        this.assembly.addTentativeMessage(message, this.step);
    }

    /**
     * Appends a byte to the assembled representation of the current step of the assembly that this AssemblyBuilder manages.
     *
     * @param b
     *            byte to append to the assembled representation of the step
     * @throws IOException
     *             an I/O exception occurred while performing the operation
     */
    public final void appendAssembledData(byte b) throws IOException {
        this.checkState();
        this.step.appendAssembledData(b);
    }

    /**
     * Appends bytes to the assembled representation of the current step of the assembly that this AssemblyBuilder manages.
     *
     * @param data
     *            bytes to append to the assembled representation of the step
     * @throws IOException
     *             an I/O exception occurred while performing the operation
     */
    public final void appendAssembledData(@Nonnull byte[] data) throws IOException {
        if (data == null) {
            throw new NullPointerException("data");
        }

        this.checkState();
        this.step.appendAssembledData(data);
    }

    /**
     * Appends bytes to the assembled representation of the current step of the assembly that this AssemblyBuilder manages.
     *
     * @param data
     *            array of bytes containing the data to append to the assembled representation of the step
     * @param offset
     *            the starting offset in the data array
     * @param length
     *            the length of the data in the data array to append to this assembly step
     * @throws IOException
     *             an I/O exception occurred while performing the operation
     */
    public final void appendAssembledData(@Nonnull byte[] data, int offset, int length) throws IOException {
        if (data == null) {
            throw new NullPointerException("data");
        }

        if (offset < 0 || offset > data.length) {
            throw new IndexOutOfBoundsException("offset: " + offset);
        }

        if (length < 0 || offset + length > data.length) {
            throw new IndexOutOfBoundsException("length: " + length);
        }

        this.checkState();
        this.step.appendAssembledData(data, offset, length);
    }

    /**
     * Defines a symbol in this assembly.
     *
     * @param context
     *            the context in which the symbol is defined
     * @param symbolName
     *            the name of the symbol
     * @param isLocalSymbol
     *            <code>true</code> if the symbol is a local symbol; otherwise, <code>false</code>
     * @param symbolType
     *            the type of symbol
     * @param value
     *            the value of the symbol. The value may be <code>null</code>, which designates an <em>undetermined</em> value.
     *            Undetermined values should be used when the symbol's value cannot be determined, for example because its value
     *            depends on a symbol that has not been defined yet. If some symbols remain defined with an undetermined value at
     *            the end of the assembly, an error is raised for each of them.
     * @see SymbolReference
     */
    public final <TValue> void defineSymbol(@Nonnull SymbolContext<TValue> context, @Nonnull String symbolName,
            boolean isLocalSymbol, @Nonnull SymbolType symbolType, @CheckForNull TValue value) {
        if (context == null) {
            throw new NullPointerException("context");
        }

        if (symbolName == null) {
            throw new NullPointerException("symbolName");
        }

        if (symbolType == null) {
            throw new NullPointerException("symbolType");
        }

        this.checkState();
        this.assembly.defineSymbol(context, symbolName, isLocalSymbol, this.step, symbolType, value);
    }

    /**
     * Signals that the current pass should end after the current node. This is generally caused by a directive that marks the end
     * of the program. Calling this methods causes the rest of the source to be ignored and, if needed, starts a new pass.
     */
    public final void endPass() {
        this.checkState();
        this.assembly.endPass(this.step);
    }

    /**
     * Saves the current location, then uses the specified {@link Iterable} to generate the next source locations to assemble. When
     * the end of the iterator obtained from that iterable is reached, the specified {@link AssemblyStepIterationController} is used
     * to determine whether to retrieve a new iterator from the iterable to assemble them again. <code>iterationController</code>
     * may be <code>null</code> if no repetition is desired.
     *
     * @param sourceLocations
     *            an {@link Iterable} of {@link SourceLocation} objects
     * @param iterationController
     *            a {@link AssemblyStepIterationController} that determines whether the same source locations should be assembled
     *            again, or <code>null</code> if no repetition is desired
     * @param transparentParent
     *            <code>true</code> if the parent should not be represented in the full path of the {@link AssemblyStepLocation}
     *            objects to be created, or <code>false</code> if it should
     */
    public final void enterChildContext(@Nonnull Iterable<SourceLocation> sourceLocations,
            @CheckForNull AssemblyStepIterationController iterationController, boolean transparentParent) {
        if (sourceLocations == null) {
            throw new NullPointerException("sourceLocations");
        }

        this.checkState();
        this.assembly.enterChildContext(sourceLocations, iterationController, this.step, transparentParent);
    }

    /**
     * Saves the current location, then sets the current location in this assembly to the first source node in the specified file.
     * When the end of that file is reached, the previous location is restored and assembly goes on from the following source
     * location.
     *
     * @param file
     *            the child file
     * @param architecture
     *            the architecture under which the source file will be parsed and assembled. Specify <code>null</code> to use the
     *            architecture of the current file.
     */
    public final void enterChildFile(@Nonnull AbstractSourceFile<?> file, @CheckForNull Architecture architecture) {
        if (file == null) {
            throw new NullPointerException("file");
        }

        this.checkState();
        this.assembly.enterChildFile(file, architecture, this.step);
    }

    /**
     * Saves the current location, then sets the current location in this assembly to the first child source node of the
     * {@link CompositeSourceNode} at the current location. When the end of that source node's children is reached, the previous
     * location is restored and assembly goes on from the following source location.
     *
     * @param transparentParent
     *            <code>true</code> if the parent should not be represented in the full path of the {@link AssemblyStepLocation}
     *            objects to be created, or <code>false</code> if it should
     */
    public final void enterComposite(boolean transparentParent) {
        this.checkState();
        this.assembly.enterComposite(this.step, transparentParent);
    }

    /**
     * Enters a namespace with the specified name.
     *
     * @param name
     *            the name of the namespace
     */
    public final void enterNamespace(@Nonnull String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        this.checkState();
        this.assembly.enterNamespace(this.step, name);
    }

    /**
     * Enters a transformation block. Data written to the following assembly steps (until {@link #exitTransformationBlock()} is
     * called) will be transformed using the specified output transformation.
     *
     * @param transformation
     *            an object that will transform the data in the transformation block
     */
    public final void enterTransformationBlock(@Nonnull OutputTransformation transformation) {
        if (transformation == null) {
            throw new NullPointerException("transformation");
        }

        this.checkState();
        this.assembly.enterTransformationBlock(this.step, transformation);
    }

    /**
     * Exits the current namespace. If there is no current namespace, an error message is added to the assembly.
     */
    public final void exitNamespace() {
        this.checkState();
        this.assembly.exitNamespace(this.step);
    }

    /**
     * Exits the current transformation block. If there is no current transformation block, an error message is added to the
     * assembly.
     *
     * @throws IOException
     *             an I/O exception occurred while transforming the contents of the transformation block
     */
    public final void exitTransformationBlock() throws IOException {
        this.checkState();
        this.assembly.exitTransformationBlock(this.step);
    }

    /**
     * Gets the assembly that this AssemblyBuilder manages.
     *
     * @return the assembly
     */
    public final Assembly getAssembly() {
        return this.assembly;
    }

    /**
     * Gets the custom assembly data object associated with the specified key, or <code>null</code> if no such object exists.
     *
     * @param key
     *            the key
     * @return the custom assembly data object, or <code>null</code> if no such object exists
     */
    public final CustomAssemblyData getCustomAssemblyData(@Nonnull Object key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        this.checkState();
        return this.assembly.getCustomAssemblyData(key);
    }

    /**
     * Gets the current step in the assembly that this AssemblyBuilder manages.
     *
     * @return the step
     */
    public final AssemblyStep getStep() {
        return this.step;
    }

    /**
     * Adds an assembly fatal error message for an {@link IOException}.
     *
     * @param exception
     *            the {@link IOException}
     */
    public final void processIOException(@Nonnull IOException exception) {
        if (exception == null) {
            throw new NullPointerException("exception");
        }

        this.checkState();
        this.assembly.processIOException(exception, this.step);
    }

    /**
     * Resolves a reference to a symbol and records the reference so as to be able to start a new pass when necessary.
     * <p>
     * This method must <strong>only</strong> be used to resolve symbol references that appear in the source of this assembly; use
     * {@link Assembly#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)} if you
     * just want to inspect the assembly.
     *
     * @param contexts
     *            a list of contexts of the symbol reference. The symbol will be looked up in each of these contexts in order, until
     *            a symbol is found.
     * @param name
     *            the name of the symbol to look up
     * @param local
     *            <code>true</code> to look up a local symbol; otherwise, <code>false</code>
     * @param bypassNamespaceResolution
     *            <code>true</code> to bypass namespace resolution, or <code>false</code> to perform namespace resolution. If the
     *            assembly is currently in a namespace, the namespace's name will be prepended, followed by a '.', to the specified
     *            symbol name, and this will be repeated for each parent namespace. Specify <code>true</code> if the symbol name is
     *            already fully qualified.
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
            boolean local, boolean bypassNamespaceResolution, @CheckForNull SymbolLookupContext lookupContext,
            @CheckForNull SymbolResolutionFallback symbolResolutionFallback) {
        if (contexts == null) {
            throw new NullPointerException("contexts");
        }

        if (name == null) {
            throw new NullPointerException("name");
        }

        this.checkState();

        final SymbolReference symbolReference = new SymbolReference(ImmutableList.copyOf(contexts), name, local,
                bypassNamespaceResolution, false, this.assembly.checkLookupContext(lookupContext), this.step,
                symbolResolutionFallback);
        this.assembly.addSymbolReference(symbolReference);
        return symbolReference;
    }

    /**
     * Resolves a reference to a symbol and records the reference so as to be able to start a new pass when necessary.
     * <p>
     * This method must <strong>only</strong> be used to resolve symbol references that appear in the source of this assembly; use
     * {@link Assembly#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * if you just want to inspect the assembly.
     *
     * @param context
     *            the context of the symbol reference
     * @param name
     *            the name of the symbol to look up
     * @param local
     *            <code>true</code> to look up a local symbol; otherwise, <code>false</code>
     * @param bypassNamespaceResolution
     *            <code>true</code> to bypass namespace resolution, or <code>false</code> to perform namespace resolution. If the
     *            assembly is currently in a namespace, the namespace's name will be prepended, followed by a '.', to the specified
     *            symbol name, and this will be repeated for each parent namespace. Specify <code>true</code> if the symbol name is
     *            already fully qualified.
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
            boolean bypassNamespaceResolution, @CheckForNull SymbolLookupContext lookupContext,
            @CheckForNull SymbolResolutionFallback symbolResolutionFallback) {
        if (context == null) {
            throw new NullPointerException("context");
        }

        if (name == null) {
            throw new NullPointerException("name");
        }

        this.checkState();

        final SymbolReference symbolReference = new SymbolReference(SymbolReference.cachedContextSingleton(context), name, local,
                bypassNamespaceResolution, false, this.assembly.checkLookupContext(lookupContext), this.step,
                symbolResolutionFallback);
        this.assembly.addSymbolReference(symbolReference);
        return symbolReference;
    }

    /**
     * Resolves a reference to a symbol and records the reference so as to be able to start a new pass when necessary.
     * <p>
     * This method must <strong>only</strong> be used to resolve symbol references that appear in the source of this assembly; use
     * {@link Assembly#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)}
     * if you just want to inspect the assembly.
     *
     * @param contexts
     *            an array of contexts of the symbol reference. The symbol will be looked up in each of these contexts in order,
     *            until a symbol is found.
     * @param name
     *            the name of the symbol to look up
     * @param local
     *            <code>true</code> to look up a local symbol; otherwise, <code>false</code>
     * @param bypassNamespaceResolution
     *            <code>true</code> to bypass namespace resolution, or <code>false</code> to perform namespace resolution. If the
     *            assembly is currently in a namespace, the namespace's name will be prepended, followed by a '.', to the specified
     *            symbol name, and this will be repeated for each parent namespace. Specify <code>true</code> if the symbol name is
     *            already fully qualified.
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
            boolean bypassNamespaceResolution, @CheckForNull SymbolLookupContext lookupContext,
            @CheckForNull SymbolResolutionFallback symbolResolutionFallback) {
        if (contexts == null) {
            throw new NullPointerException("contexts");
        }

        if (name == null) {
            throw new NullPointerException("name");
        }

        this.checkState();

        final SymbolReference symbolReference = new SymbolReference(ImmutableList.copyOf(contexts), name, local,
                bypassNamespaceResolution, false, this.assembly.checkLookupContext(lookupContext), this.step,
                symbolResolutionFallback);
        this.assembly.addSymbolReference(symbolReference);
        return symbolReference;
    }

    /**
     * Indicates that the current step of the assembly that this AssemblyBuilder manages has a side effect, i.e. it changes the
     * assembler's state beyond outputting data and defining symbols. This should be called when it is necessary to call
     * {@link SourceNode#assembleCore(AssemblyBuilder)} on all passes. Once this has been called for a step, it is not possible to
     * undo it.
     */
    public final void setCurrentStepHasSideEffects() {
        this.checkState();
        this.step.setHasSideEffects();
    }

    /**
     * Sets the custom assembly data object associated with the specified key.
     * <p>
     * Architectures may use custom assembly data to associate additional data with a specific assembly.
     *
     * @param key
     *            the key
     * @param customAssemblyData
     *            the custom data object
     */
    public final void setCustomAssemblyData(@Nonnull Object key, @Nonnull CustomAssemblyData customAssemblyData) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        if (customAssemblyData == null) {
            throw new NullPointerException("customAssemblyData");
        }

        this.checkState();
        this.assembly.setCustomAssemblyData(key, customAssemblyData);
    }

    /**
     * Sets the current program counter in this assembly.
     *
     * @param programCounter
     *            the new program counter
     */
    public final void setProgramCounter(long programCounter) {
        this.checkState();
        this.assembly.setProgramCounter(this.step, programCounter);
    }

    /**
     * Clears this builder's reference to the assembly.
     *
     * @see Assembly#completeAssembly()
     */
    final void completeAssembly() {
        this.assembly = null;
        this.step = null;
    }

    final void setStep(@CheckForNull AssemblyStep step) {
        this.step = step;
    }

    /**
     * Throws an {@link IllegalStateException} if a mutator method is called after the assembly process has completed or if no step
     * is running in the assembly.
     */
    private final void checkState() {
        if (this.assembly == null) {
            throw new IllegalStateException("The operation cannot be performed because the assembly process is complete.");
        }

        if (this.step == null) {
            throw new IllegalStateException("The operation cannot be performed because the assembly is not performing a step.");
        }
    }

}
