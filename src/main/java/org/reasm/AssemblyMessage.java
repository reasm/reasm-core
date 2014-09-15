package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A message that is generated during an assembly.
 * <p>
 * To add a message to an assembly, call {@link AssemblyBuilder#addMessage(AssemblyMessage)} or
 * {@link AssemblyBuilder#addTentativeMessage(AssemblyMessage)}.
 *
 * @author Francis Gagné
 */
public abstract class AssemblyMessage {

    @Nonnull
    private final MessageGravity gravity;
    @Nonnull
    private final String text;
    @CheckForNull
    private final AssemblyMessage parent;
    @CheckForNull
    private AssemblyStep step;

    /**
     * Initializes a new assembly message.
     *
     * @param gravity
     *            the gravity of the message
     * @param text
     *            the text of the message, to be displayed to the end-user
     */
    // This constructor is default-visible to prevent external packages from subclassing this class directly.
    AssemblyMessage(@Nonnull MessageGravity gravity, @Nonnull String text, @CheckForNull AssemblyMessage parent) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        this.gravity = gravity;
        this.text = text;
        this.parent = parent;
    }

    /**
     * Gets the gravity of the message.
     *
     * @return the {@link MessageGravity} of the message
     */
    @Nonnull
    public final MessageGravity getGravity() {
        return this.gravity;
    }

    /**
     * Gets the parent message, i.e. the message that caused this message to be generated.
     *
     * @return the parent message
     */
    @CheckForNull
    public final AssemblyMessage getParent() {
        return this.parent;
    }

    /**
     * Gets the step in which the message was generated.
     * <p>
     * This method will return <code>null</code> until the message is added to an assembly.
     *
     * @return the step in which the message was generated
     * @see Assembly#addMessage(AssemblyMessage, AssemblyStep)
     */
    @CheckForNull
    public final AssemblyStep getStep() {
        return this.step;
    }

    /**
     * Gets the text of the message, to be displayed to the end-user.
     *
     * @return the text of the message
     */
    @Nonnull
    public final String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": " + this.text;
    }

    /**
     * Adds this message to an assembly.
     *
     * @param step
     *            the assembly step that caused this message
     */
    final void addToAssembly(@CheckForNull AssemblyStep step) {
        if (this.step != null) {
            throw new IllegalStateException("The assembly message has already been added to an assembly.");
        }

        this.step = step;
    }

}
