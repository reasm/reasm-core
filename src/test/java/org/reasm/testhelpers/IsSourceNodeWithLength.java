package org.reasm.testhelpers;

import static org.hamcrest.core.IsEqual.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.reasm.source.SourceNode;

/**
 * Matches if the length of a source node satisfies a nested matcher.
 *
 * @author Francis Gagné
 */
@Immutable
public final class IsSourceNodeWithLength extends FeatureMatcher<SourceNode, Integer> {

    /**
     * Creates a matcher for {@link SourceNode}s that matches when the {@link SourceNode#getLength()} method returns a value equal
     * to the specified <code>length</code>.
     *
     * @param length
     *            the expected length of an examined {@link SourceNode}
     * @return the matcher
     */
    @Factory
    @Nonnull
    public static Matcher<SourceNode> hasLength(int length) {
        return hasLength(equalTo(length));
    }

    /**
     * Creates a matcher for {@link SourceNode}s that matches when the {@link SourceNode#getLength()} method returns a value that
     * satisfies the specified matcher.
     *
     * @param lengthMatcher
     *            a matcher for the length of an examined {@link SourceNode}
     * @return the matcher
     */
    @Factory
    @Nonnull
    public static Matcher<SourceNode> hasLength(@Nonnull Matcher<Integer> lengthMatcher) {
        return new IsSourceNodeWithLength(lengthMatcher);
    }

    /**
     * Initializes a new IsSourceNodeWithLength.
     *
     * @param lengthMatcher
     *            a matcher for the length of an examined {@link SourceNode}
     */
    public IsSourceNodeWithLength(@Nonnull Matcher<? super Integer> lengthMatcher) {
        super(lengthMatcher, "a source node with length", "source node length");
    }

    @Nonnull
    @Override
    protected Integer featureValueOf(@Nonnull SourceNode actual) {
        return actual.getLength();
    }

}
