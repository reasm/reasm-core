package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when an overflow occurs when computing the value of a literal.
 *
 * @author Francis Gagné
 */
public class OverflowInLiteralWarningMessage extends AssemblyWarningMessage {

    /**
     * Initializes a new OverflowInLiteralWarningMessage.
     *
     * @param literalText
     *            the text of the literal
     */
    public OverflowInLiteralWarningMessage(@Nonnull String literalText) {
        super("Overflow in literal: " + Objects.requireNonNull(literalText, "literalText"));
    }

}
