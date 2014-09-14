package org.reasm.testhelpers;

import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Tests if a {@link Map} is empty.
 *
 * @author Francis Gagné
 * @param <K>
 *            the type of the keys in the map
 * @param <V>
 *            the type of the values in the map
 */
public class IsEmptyMap<K, V> extends TypeSafeMatcher<Map<? extends K, ? extends V>> {

    private static final IsEmptyMap<Object, Object> INSTANCE = new IsEmptyMap<>();

    /**
     * Creates a matcher for {@link Map}s matching examined maps whose <code>isEmpty</code> method returns <code>true</code>.
     *
     * @return a {@link Matcher}
     */
    @Factory
    @SuppressWarnings("unchecked")
    public static <K, V> Matcher<Map<? extends K, ? extends V>> empty() {
        return (IsEmptyMap<K, V>) INSTANCE;
    }

    private IsEmptyMap() {
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an empty map");
    }

    @Override
    protected boolean matchesSafely(Map<? extends K, ? extends V> item) {
        return item.isEmpty();
    }

}
