package org.reasm.source;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.reasm.testhelpers.HexArchitecture;

/**
 * Test class for {@link SourceLocationList}.
 *
 * @author Francis Gagné
 */
public class SourceLocationListTest {

    static final SourceLocationList SOURCE_LOCATION_LIST = (SourceLocationList) new SourceFile("00010203", null)
            .getSourceLocations(HexArchitecture.INSTANCE).get(0).getChildSourceLocations();
    static final SourceLocationList EMPTY_SOURCE_LOCATION_LIST = (SourceLocationList) new SourceFile("", null)
            .getSourceLocations(HexArchitecture.INSTANCE).get(0).getChildSourceLocations();
    private static final Object FOREIGN_OBJECT = new Object();

    /**
     * Asserts that {@link SourceLocationList#containsAll(Collection)} return <code>false</code> when some of the members of the
     * specified collection are not members of the list.
     */
    @Test
    public void containsAllFalse() {
        assertThat(SOURCE_LOCATION_LIST.containsAll(Arrays.asList(SOURCE_LOCATION_LIST.get(2), FOREIGN_OBJECT)), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList#containsAll(Collection)} returns <code>true</code> when all of the members of the
     * specified collection are members of the list.
     */
    @Test
    public void containsAllTrue() {
        assertThat(SOURCE_LOCATION_LIST.containsAll(Arrays.asList(SOURCE_LOCATION_LIST.get(2), SOURCE_LOCATION_LIST.get(0),
                SOURCE_LOCATION_LIST.get(1))), is(true));
    }

    /**
     * Asserts that {@link SourceLocationList#contains(Object)} returns <code>false</code> for an object that is not an element of
     * the list.
     */
    @Test
    public void containsFalse() {
        assertThat(SOURCE_LOCATION_LIST.contains(FOREIGN_OBJECT), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList#contains(Object)} returns <code>true</code> for an object that is an element of the
     * list.
     */
    @Test
    public void containsTrue() {
        assertThat(SOURCE_LOCATION_LIST.contains(SOURCE_LOCATION_LIST.get(3)), is(true));
    }

    /**
     * Asserts that {@link SourceLocationList#get(int)} throws an {@link IndexOutOfBoundsException} when the <code>index</code>
     * argument is greater than or equal to the list's size.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void getIndexTooHigh() {
        SOURCE_LOCATION_LIST.get(4);
    }

    /**
     * Asserts that {@link SourceLocationList#get(int)} throws an {@link IndexOutOfBoundsException} when the <code>index</code>
     * argument is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void getIndexTooLow() {
        SOURCE_LOCATION_LIST.get(-1);
    }

    /**
     * Asserts that {@link SourceLocationList#get(int)} returs the element at the specified index in the list.
     */
    @Test
    public void getIndexValid() {
        final SourceLocation sourceLocation = SOURCE_LOCATION_LIST.get(1);
        assertThat(sourceLocation.getTextPosition(), is(2));
    }

    /**
     * Asserts that {@link SourceLocationList#indexOf(Object)} returns -1 when the specified object is not an element of the list.
     */
    @Test
    public void indexOfObjectAbsent() {
        assertThat(SOURCE_LOCATION_LIST.indexOf(FOREIGN_OBJECT), is(-1));
    }

    /**
     * Asserts that {@link SourceLocationList#indexOf(Object)} returns the index of the first occurrence of the specified object in
     * the list.
     */
    @Test
    public void indexOfObjectPresent() {
        final SourceLocation sourceLocation = SOURCE_LOCATION_LIST.get(2);
        assertThat(SOURCE_LOCATION_LIST.indexOf(sourceLocation), is(2));
    }

    /**
     * Asserts that {@link SourceLocationList#isEmpty()} returns <code>false</code> for a non-empty list.
     */
    @Test
    public void isEmptyFalse() {
        assertThat(SOURCE_LOCATION_LIST.isEmpty(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList#isEmpty()} returns <code>true</code> for an empty list.
     */
    @Test
    public void isEmptyTrue() {
        assertThat(EMPTY_SOURCE_LOCATION_LIST.isEmpty(), is(true));
    }

    /**
     * Asserts that {@link SourceLocationList#lastIndexOf(Object)} returns -1 when the specified object is not an element of the
     * list.
     */
    @Test
    public void lastIndexOfObjectAbsent() {
        assertThat(SOURCE_LOCATION_LIST.lastIndexOf(FOREIGN_OBJECT), is(-1));
    }

    /**
     * Asserts that {@link SourceLocationList#lastIndexOf(Object)} returns the index of the last occurrence of the specified object
     * in the list.
     */
    @Test
    public void lastIndexOfObjectPresent() {
        final SourceLocation sourceLocation = SOURCE_LOCATION_LIST.get(2);
        assertThat(SOURCE_LOCATION_LIST.lastIndexOf(sourceLocation), is(2));
    }

    /**
     * Asserts that {@link SourceLocationList#listIterator(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>index</code> argument is greater than the list's size.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void listIteratorIntIndexTooHigh() {
        SOURCE_LOCATION_LIST.listIterator(5);
    }

    /**
     * Asserts that {@link SourceLocationList#listIterator(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>index</code> argument is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void listIteratorIntIndexTooLow() {
        SOURCE_LOCATION_LIST.listIterator(-1);
    }

    /**
     * Asserts that {@link SourceLocationList#size()} returns the list's size.
     */
    @Test
    public void size() {
        assertThat(SOURCE_LOCATION_LIST.size(), is(4));
    }

    /**
     * Asserts that {@link SourceLocationList#size()} returns 0 for an empty list.
     */
    @Test
    public void sizeEmpty() {
        assertThat(EMPTY_SOURCE_LOCATION_LIST.size(), is(0));
    }

    /**
     * Asserts that {@link SourceLocationList#subList(int, int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>fromIndex</code> argument is greater than the list's size.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void subListFromIndexTooHigh() {
        SOURCE_LOCATION_LIST.subList(5, 5);
    }

    /**
     * Asserts that {@link SourceLocationList#subList(int, int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>fromIndex</code> argument is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void subListFromIndexTooLow() {
        SOURCE_LOCATION_LIST.subList(-1, 4);
    }

    /**
     * Asserts that {@link SourceLocationList#subList(int, int)} returns the original list when the <code>fromIndex</code> argument
     * is 0 and the <code>toIndex</code> argument is equal to the list's size.
     */
    @Test
    public void subListIdentity() {
        assertThat(SOURCE_LOCATION_LIST.subList(0, 4), is(sameInstance((List<SourceLocation>) SOURCE_LOCATION_LIST)));
    }

    /**
     * Asserts that {@link SourceLocationList#subList(int, int)} returns a new list when the <code>fromIndex</code> and the
     * <code>toIndex</code> arguments don't designate the whole list.
     */
    @Test
    public void subListNotIdentity() {
        assertThat(SOURCE_LOCATION_LIST.subList(0, 3), is(not(sameInstance((List<SourceLocation>) SOURCE_LOCATION_LIST))));
    }

    /**
     * Asserts that {@link SourceLocationList#subList(int, int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>toIndex</code> argument is greater than the list's size.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void subListToIndexTooHigh() {
        SOURCE_LOCATION_LIST.subList(3, 5);
    }

    /**
     * Asserts that {@link SourceLocationList#subList(int, int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>toIndex</code> argument is less than the <code>fromIndex</code> argument.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void subListToIndexTooLow() {
        SOURCE_LOCATION_LIST.subList(3, 2);
    }

    /**
     * Asserts that {@link SourceLocationList#toArray()} returns an array that contains the list's elements.
     */
    @Test
    public void toArray() {
        final Object[] array = SOURCE_LOCATION_LIST.toArray();
        assertThat(array, is(arrayWithSize(4)));

        for (int i = 0; i < array.length; i++) {
            assertThat(array[i], is(sameInstance((Object) SOURCE_LOCATION_LIST.get(i))));
        }
    }

    /**
     * Asserts that {@link SourceLocationList#toArray(Object[])} returns the specified array filled with the list's elements when
     * the specified array's length is equal to the list's size.
     */
    @Test
    public void toArrayTArrayLargeEnough() {
        final SourceLocation[] arrayArgument = new SourceLocation[4];
        final SourceLocation[] array = SOURCE_LOCATION_LIST.toArray(arrayArgument);
        assertThat(array, is(sameInstance(arrayArgument)));

        for (int i = 0; i < array.length; i++) {
            assertThat(array[i], is(sameInstance(SOURCE_LOCATION_LIST.get(i))));
        }
    }

    /**
     * Asserts that {@link SourceLocationList#toArray(Object[])} returns the specified array filled with the list's elements,
     * followed by a <code>null</code> element, when the specified array's length is greater than the list's size.
     */
    @Test
    public void toArrayTArrayLarger() {
        final SourceLocation[] arrayArgument = new SourceLocation[5];
        arrayArgument[4] = SOURCE_LOCATION_LIST.get(0); // set this array item to test that null is written to it
        final SourceLocation[] array = SOURCE_LOCATION_LIST.toArray(arrayArgument);
        assertThat(array, is(sameInstance(arrayArgument)));

        for (int i = 0; i < 4; i++) {
            assertThat(array[i], is(sameInstance(SOURCE_LOCATION_LIST.get(i))));
        }

        assertThat(array[4], is(nullValue()));
    }

    /**
     * Asserts that {@link SourceLocationList#toArray(Object[])} throws a {@link NullPointerException} when the <code>array</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void toArrayTArrayNullArray() {
        SOURCE_LOCATION_LIST.toArray(null);
    }

    /**
     * Asserts that {@link SourceLocationList#toArray(Object[])} returns a new array filled with the list's elements when the
     * specified array's length is less that the list's size.
     */
    @Test
    public void toArrayTArrayTooSmall() {
        final SourceLocation[] array = SOURCE_LOCATION_LIST.toArray(new SourceLocation[3]);
        assertThat(array, is(arrayWithSize(4)));
        assertThat(array.getClass().getComponentType(), is((Object) SourceLocation.class));

        for (int i = 0; i < array.length; i++) {
            assertThat(array[i], is(sameInstance(SOURCE_LOCATION_LIST.get(i))));
        }
    }

}
