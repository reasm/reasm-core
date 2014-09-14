package org.reasm.testhelpers;

import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/**
 * Matches if map size satisfies a nested matcher.
 *
 * @param <K>
 *            the type of the keys in the map
 * @param <V>
 *            the type of the values in the map
 * @author Francis Gagné
 */
@Immutable
public final class IsMapWithSize<K, V> extends FeatureMatcher<Map<? extends K, ? extends V>, Integer> {

    /**
     * Creates a matcher for {@link Map}s that matches when the <code>size()</code> method returns a value equal to the specified
     * <code>size</code>.
     *
     * @param size
     *            the expected size of an examined {@link Map}
     * @return the matcher
     */
    @Factory
    @Nonnull
    public static <K, V> Matcher<Map<? extends K, ? extends V>> hasSize(int size) {
        Matcher<? super Integer> matcher = equalTo(size);
        return IsMapWithSize.<K, V> hasSize(matcher);
    }

    /**
     * Creates a matcher for {@link Map}s that matches when the <code>size()</code> method returns a value that satisfies the
     * specified matcher.
     *
     * @param sizeMatcher
     *            a matcher for the size of an examined {@link Map}
     * @return the matcher
     */
    @Factory
    @Nonnull
    public static <K, V> Matcher<Map<? extends K, ? extends V>> hasSize(@Nonnull Matcher<? super Integer> sizeMatcher) {
        return new IsMapWithSize<>(sizeMatcher);
    }

    /**
     * Initializes a new IsMapWithSize.
     *
     * @param sizeMatcher
     *            a matcher for the size of an examined {@link Map}
     */
    public IsMapWithSize(@Nonnull Matcher<? super Integer> sizeMatcher) {
        super(sizeMatcher, "a map with size", "map size");
    }

    @Nonnull
    @Override
    protected Integer featureValueOf(@Nonnull Map<? extends K, ? extends V> actual) {
        return actual.size();
    }

}
