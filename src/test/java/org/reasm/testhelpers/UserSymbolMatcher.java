package org.reasm.testhelpers;

import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.reasm.SymbolContext;
import org.reasm.SymbolType;
import org.reasm.UserSymbol;

/**
 * Matches {@link UserSymbol} objects with specific properties.
 *
 * @param <TValue>
 *            the type of the symbol's value
 *
 * @author Francis Gagné
 */
public class UserSymbolMatcher<TValue> extends TypeSafeDiagnosingMatcher<UserSymbol> {

    private final SymbolContext<TValue> context;
    private final String name;
    private final SymbolType type;
    private final TValue value;

    /**
     * Initializes a new UserSymbolMatcher.
     *
     * @param context
     *            the symbol's expected context
     * @param name
     *            the symbol's expected name
     * @param type
     *            the symbol's expected type
     * @param value
     *            the symbol's expected value
     */
    public UserSymbolMatcher(SymbolContext<TValue> context, String name, SymbolType type, TValue value) {
        this.context = context;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a symbol with context ").appendValue(this.context).appendText(", name ").appendValue(this.name)
                .appendText(", type ").appendValue(this.type).appendText(" and value ").appendValue(this.value);
    }

    @Override
    protected boolean matchesSafely(UserSymbol item, Description mismatchDescription) {
        if (!Objects.equals(item.getContext(), this.context)) {
            mismatchDescription.appendText("has context ").appendValue(item.getContext());
            return false;
        }

        if (!Objects.equals(item.getName(), this.name)) {
            mismatchDescription.appendText("has name ").appendValue(item.getName());
            return false;
        }

        if (!Objects.equals(item.getType(), this.type)) {
            mismatchDescription.appendText("has type ").appendValue(item.getType());
            return false;
        }

        if (!Objects.equals(item.getValue(), this.value)) {
            mismatchDescription.appendText("has value ").appendValue(item.getValue());
            return false;
        }

        return true;
    }

}
