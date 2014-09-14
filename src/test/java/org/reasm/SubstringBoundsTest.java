package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link SubstringBounds}.
 *
 * @author Francis Gagné
 */
public class SubstringBoundsTest extends ObjectHashCodeEqualsContract {

    /**
     * Initializes a new SubstringBoundsTest.
     */
    public SubstringBoundsTest() {
        super(new SubstringBounds(123, 456), new SubstringBounds(123, 456), new SubstringBounds(123, 456),
                new SubstringBounds(0, 0), new SubstringBounds(0, 456), new SubstringBounds(123, 567), new Object());
    }

    /**
     * Asserts that {@link SubstringBounds#SubstringBounds(int, int)} correctly initializes a {@link SubstringBounds}.
     */
    @Test
    public void substringBounds() {
        final SubstringBounds bounds = new SubstringBounds(123, 456);
        assertThat(bounds.getStart(), is(123));
        assertThat(bounds.getEnd(), is(456));
    }

    /**
     * Asserts that {@link SubstringBounds#toString()} returns a string representation of the {@link SubstringBounds}.
     */
    @Test
    public void testToString() {
        assertThat(new SubstringBounds(123, 456).toString(), is("SubstringBounds [start=123, end=456]"));
    }

}
