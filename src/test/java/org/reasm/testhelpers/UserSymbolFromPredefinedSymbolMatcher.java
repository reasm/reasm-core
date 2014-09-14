package org.reasm.testhelpers;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.reasm.PredefinedSymbol;
import org.reasm.UserSymbol;

/**
 * A matcher that compares the properties of a {@link UserSymbol} with the properties of a {@link PredefinedSymbol}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class UserSymbolFromPredefinedSymbolMatcher extends TypeSafeDiagnosingMatcher<UserSymbol> {

    @Nonnull
    private final PredefinedSymbol predefinedSymbol;

    /**
     * Initializes a new UserSymbolFromPredefinedSymbolMatcher.
     *
     * @param predefinedSymbol
     *            the predefined symbol to compare UserSymbols to
     */
    public UserSymbolFromPredefinedSymbolMatcher(@Nonnull PredefinedSymbol predefinedSymbol) {
        if (predefinedSymbol == null) {
            throw new NullPointerException("predefinedSymbol");
        }

        this.predefinedSymbol = predefinedSymbol;
    }

    @Override
    public void describeTo(@Nonnull Description description) {
        description.appendText("a user symbol with context ").appendValue(this.predefinedSymbol.getContext()).appendText(", name ")
                .appendValue(this.predefinedSymbol.getName()).appendText(", type ").appendValue(this.predefinedSymbol.getType())
                .appendText(" and value ").appendValue(this.predefinedSymbol.getValue());
    }

    @Override
    protected boolean matchesSafely(@Nonnull UserSymbol item, @Nonnull Description mismatchDescription) {
        if (this.predefinedSymbol.getContext() != item.getContext()) {
            mismatchDescription.appendText("has context ").appendValue(item.getContext());
            return false;
        }

        if (!Objects.equals(this.predefinedSymbol.getName(), item.getName())) {
            mismatchDescription.appendText("has name ").appendValue(item.getName());
            return false;
        }

        if (this.predefinedSymbol.getType() != item.getType()) {
            mismatchDescription.appendText("has type ").appendValue(item.getType());
            return false;
        }

        if (!Objects.equals(this.predefinedSymbol.getValue(), item.getValue())) {
            mismatchDescription.appendText("has value ").appendValue(item.getValue());
            return false;
        }

        return true;
    }

}
