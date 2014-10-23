package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.Assembly;
import org.reasm.AssemblyMessage;

import ca.fragag.Consumer;

/**
 * A set of information and handlers to use during evaluation of an {@link Expression}.
 *
 * @author Francis Gagné
 */
public final class EvaluationContext {

    /** A dummy evaluation context. */
    public static final EvaluationContext DUMMY = new EvaluationContext(null, 0, null);

    private static final Consumer<AssemblyMessage> DUMMY_ASSEMBLY_MESSAGE_CONSUMER = new Consumer<AssemblyMessage>() {
        @Override
        public void accept(AssemblyMessage object) {
        }
    };

    @CheckForNull
    private final Assembly assembly;
    private final long programCounter;
    @Nonnull
    private final Consumer<AssemblyMessage> assemblyMessageConsumer;

    /**
     * Initializes a new EvaluationContext.
     *
     * @param assembly
     *            the assembly that contains the step in which the expression appears
     * @param programCounter
     *            the program counter at the step in which the expression appears
     * @param assemblyMessageConsumer
     *            a {@link Consumer} that will receive the assembly messages that were raised while evaluating the expression
     */
    public EvaluationContext(@CheckForNull Assembly assembly, long programCounter,
            @CheckForNull Consumer<AssemblyMessage> assemblyMessageConsumer) {
        this.assembly = assembly;
        this.programCounter = programCounter;
        this.assemblyMessageConsumer = assemblyMessageConsumer != null ? assemblyMessageConsumer : DUMMY_ASSEMBLY_MESSAGE_CONSUMER;
    }

    /**
     * Gets the assembly in this evaluation context.
     *
     * @return the assembly
     */
    @CheckForNull
    public final Assembly getAssembly() {
        return this.assembly;
    }

    /**
     * Gets the assembly message consumer in this evaluation context.
     *
     * @return the assembly message consumer
     */
    @Nonnull
    public final Consumer<AssemblyMessage> getAssemblyMessageConsumer() {
        return this.assemblyMessageConsumer;
    }

    /**
     * Gets the program counter in this evaluation context.
     *
     * @return the program counter
     */
    public final long getProgramCounter() {
        return this.programCounter;
    }

}
