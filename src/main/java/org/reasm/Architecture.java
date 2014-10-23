package org.reasm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.source.AbstractSourceFile;
import org.reasm.source.SourceNode;

import ca.fragag.Consumer;
import ca.fragag.text.Document;

/**
 * An instruction set architecture (ISA). This abstract class specifies the names that can be used in source code to select an
 * architecture and provides methods used during assembly.
 * <p>
 * Implementations should be immutable classes; that is, they should not have methods that alter the behavior of the object
 * globally. To provide modifiable behavior, use properties in a {@link Configuration}.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class Architecture implements Environment.ObjectWithNames {

    @Nonnull
    private final Set<String> names;

    /**
     * Initializes a new architecture with the specified names.
     *
     * @param names
     *            the names of this architecture. If your architecture has only one name, you can use
     *            {@link Collections#singleton(Object)}. If your architecture has no name, you may pass either {@code null} or an
     *            empty collection.
     */
    protected Architecture(@CheckForNull Collection<String> names) {
        this.names = names == null ? Collections.<String> emptySet() : Collections.unmodifiableSet(new HashSet<>(names));
    }

    /**
     * Evaluates the specified expression.
     *
     * @param expression
     *            a CharSequence containing the expression to evaluate
     * @param assembly
     *            an assembly providing context for the evaluation. May be <code>null</code>.
     * @param symbolReferenceConsumer
     *            a {@link Consumer} that will be notified of each symbol reference that is found while evaluating the expression.
     *            May be <code>null</code>.
     * @param assemblyMessageConsumer
     *            a {@link Consumer} that will be notified of each {@link AssemblyMessage} that the evaluation produces. May be
     *            <code>null</code>.
     * @return a {@link Value} representing the value of the expression, or <code>null</code> if the expression is invalid. The
     *         default implementation always returns <code>null</code>.
     */
    @CheckForNull
    public Value evaluateExpression(@Nonnull CharSequence expression, @Nonnull Assembly assembly,
            @CheckForNull Consumer<SymbolReference> symbolReferenceConsumer,
            @CheckForNull Consumer<AssemblyMessage> assemblyMessageConsumer) {
        return null;
    }

    /**
     * Gets the names of this architecture. The returned set is {@linkplain Collections#unmodifiableSet(Set) unmodifiable}.
     *
     * @return the names of this architecture
     */
    @Override
    @Nonnull
    public final Set<String> getNames() {
        return this.names;
    }

    /**
     * Parses the contents of a source file.
     *
     * @param text
     *            the contents of a source file
     * @return a {@link SourceNode} that is the root of the source file's abstract syntax tree, according to this architecture's
     *         assembler syntax. The result must not be <code>null</code>. The source node's length must be equal to
     *         <code>text</code>'s length.
     */
    @Nonnull
    public abstract SourceNode parse(@Nonnull Document text);

    /**
     * Re-parses the contents of a source file after it has been altered.
     * <p>
     * This method receives the old source file, the new contents of the source file, the offset at which the replace occurred, the
     * length of text that was removed and the length of text that was inserted at that location. You may use this information to
     * reuse existing {@link SourceNode}s from the old source file in the parts that didn't change.
     * <p>
     * The default implementation simply calls {@link #parse(Document)}. It is strongly encouraged to override this method in
     * subclasses to supply a better implementation that reuses {@link SourceNode}s from the old source file.
     *
     * @param text
     *            the new contents of the source file
     * @param oldSourceFile
     *            the old source file
     * @param replaceOffset
     *            the offset at which the replace occurred
     * @param lengthToRemove
     *            the length of text from the old source file that was removed
     * @param lengthToInsert
     *            the length of text from the new source file that was inserted
     * @return a {@link SourceNode} that is the root of the source file's abstract syntax tree, according to this architecture's
     *         assembler syntax. The source node's length must be equal to <code>text</code>'s length.
     */
    @Nonnull
    public SourceNode reparse(@Nonnull Document text, @Nonnull AbstractSourceFile<?> oldSourceFile, int replaceOffset,
            int lengthToRemove, int lengthToInsert) {
        return this.parse(text);
    }

}
