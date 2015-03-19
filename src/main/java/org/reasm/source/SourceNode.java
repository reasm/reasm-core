package org.reasm.source;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;
import org.reasm.AssemblyBuilder;
import org.reasm.messages.ParseErrorMessage;

import ca.fragag.text.Document;

/**
 * A node in the abstract syntax tree (AST) of a source file, as parsed by a particular {@link Architecture}.
 * <p>
 * {@link SourceNode} is designed to be immutable and to be sharable &ndash; that is, a node may appear more than once in a tree and
 * may appear in different trees (especially after re-parsing).
 * <p>
 * A {@link SourceNode} only stores a length and a parse error. It has no information about where it is within an AST; in fact, it
 * may be in many places in an AST, or in several ASTs. {@link SourceLocation} stores contextual information about a source node.
 * <p>
 * The abstract {@link #assembleCore(AssemblyBuilder)} method is called during assembly to perform a step. The method is called on
 * the source node referred to by {@link AssemblyBuilder#getStep()}.
 *
 * @author Francis Gagné
 * @see CompositeSourceNode
 * @see SourceLocation
 * @see Architecture#parse(Document)
 * @see Architecture#reparse(Document, AbstractSourceFile, int, int, int)
 */
@Immutable
public abstract class SourceNode {

    private final int length;
    @CheckForNull
    private final ParseError parseError;

    /**
     * Initializes a new SourceNode.
     *
     * @param length
     *            the number of characters in the source node
     * @param parseError
     *            the parse error on the source node, or <code>null</code> if no parse error occurred
     */
    public SourceNode(int length, @CheckForNull ParseError parseError) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be positive or zero");
        }

        this.length = length;
        this.parseError = parseError;
    }

    /**
     * Assembles a part of code.
     * <p>
     * This method performs a few validations, and if they pass, calls {@link #assembleCore(AssemblyBuilder)}.
     *
     * @param builder
     *            the assembly builder
     * @throws IOException
     *             an I/O exception occurred while assembling the source node
     */
    public final void assemble(@Nonnull AssemblyBuilder builder) throws IOException {
        if (builder == null) {
            throw new NullPointerException("builder");
        }

        final SourceNode sourceNode = builder.getStep().getLocation().getSourceLocation().getSourceNode();

        if (this != sourceNode) {
            throw new IllegalArgumentException(
                    "builder's current assembly step's source node must be the source node on which SourceNode.assemble() is called");
        }

        // If there's a parse error on the source node, add an error message about that and return.
        final ParseError parseError = this.parseError;
        if (parseError != null) {
            builder.addMessage(new ParseErrorMessage(parseError));
            return;
        }

        this.assembleCore(builder);
    }

    /**
     * Gets the length, in characters, of this source node.
     *
     * @return the length of the source node
     */
    public final int getLength() {
        return this.length;
    }

    /**
     * Gets the parse error that occurred on this source node.
     *
     * @return the parse error, or <code>null</code> if no parse error occurred
     */
    @CheckForNull
    public final ParseError getParseError() {
        return this.parseError;
    }

    /**
     * Assembles a part of code.
     *
     * @param builder
     *            the assembly builder
     * @throws IOException
     *             an I/O exception occurred while assembling the source node
     */
    protected abstract void assembleCore(@Nonnull AssemblyBuilder builder) throws IOException;

}
