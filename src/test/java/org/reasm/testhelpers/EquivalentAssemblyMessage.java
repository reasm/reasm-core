package org.reasm.testhelpers;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.reasm.AssemblyMessage;
import org.reasm.messages.InternalAssemblerErrorMessage;

/**
 * A matcher that checks whether an {@link AssemblyMessage} is of the same class and has the same {@link AssemblyMessage#getText()
 * text} as an expected value.
 *
 * @author Francis Gagné
 */
@Immutable
public final class EquivalentAssemblyMessage extends TypeSafeDiagnosingMatcher<AssemblyMessage> {

    @Nonnull
    private final AssemblyMessage expectedValue;

    /**
     * Initializes a new EquivalentAssemblyMessage.
     *
     * @param expectedValue
     *            the expected value
     */
    public EquivalentAssemblyMessage(@Nonnull AssemblyMessage expectedValue) {
        if (expectedValue == null) {
            throw new NullPointerException("expectedValue");
        }

        this.expectedValue = expectedValue;
    }

    @Override
    public void describeTo(@Nonnull Description description) {
        description.appendText("an assembly message of type ").appendText(this.expectedValue.getClass().getName())
                .appendText(" with text ").appendText(this.expectedValue.getText());
        if (this.expectedValue instanceof InternalAssemblerErrorMessage) {
            description.appendText(" and throwable ").appendValue(
                    ((InternalAssemblerErrorMessage) this.expectedValue).getThrowable());
        }
    }

    @Override
    protected boolean matchesSafely(@Nonnull AssemblyMessage item, @Nonnull Description mismatchDescription) {
        if (!Objects.equals(item.getClass(), this.expectedValue.getClass())) {
            mismatchDescription.appendText("has type ").appendValue(item.getClass().getName());
            return false;
        }

        if (!Objects.equals(item.getText(), this.expectedValue.getText())) {
            mismatchDescription.appendText("has text ").appendValue(item.getText());
            return false;
        }

        if (this.expectedValue instanceof InternalAssemblerErrorMessage) {
            if (!Objects.equals(((InternalAssemblerErrorMessage) item).getThrowable(),
                    ((InternalAssemblerErrorMessage) this.expectedValue).getThrowable())) {
                mismatchDescription.appendText("has throwable ").appendValue(((InternalAssemblerErrorMessage) item).getThrowable());
                return false;
            }
        }

        return true;
    }

}
