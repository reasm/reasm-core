package org.reasm.testhelpers;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import org.reasm.AssemblyMessage;

import ca.fragag.Consumer;

/**
 * An implementation of {@link Consumer} that adds all received {@link AssemblyMessage} objects to an {@link ArrayList}.
 *
 * @author Francis Gagné
 */
public final class AssemblyMessageCollector implements Consumer<AssemblyMessage> {

    @Nonnull
    private final ArrayList<AssemblyMessage> messages;

    /**
     * Initializes a new AssemblyMessageCollector.
     *
     * @param messages
     *            the {@link ArrayList} to feed with the received {@link AssemblyMessage} objects
     */
    public AssemblyMessageCollector(@Nonnull ArrayList<AssemblyMessage> messages) {
        this.messages = messages;
    }

    @Override
    public void accept(AssemblyMessage message) {
        this.messages.add(message);
    }

}
