package org.reasm.source;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.reasm.source.SourceLocationListTest.SOURCE_LOCATION_LIST;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

/**
 * Test class for {@link SourceLocationList.SubList}.
 *
 * @author Francis Gagné
 */
public class SourceLocationListSubListTest {

    private static final List<SourceLocation> SUB_LIST_1_1 = SOURCE_LOCATION_LIST.subList(1, 1);
    private static final List<SourceLocation> SUB_LIST_1_3 = SOURCE_LOCATION_LIST.subList(1, 3);

    /**
     * Asserts that {@link SourceLocationList.SubList#containsAll(Collection)} returns <code>false</code> when the sublist doesn't
     * contain all of the supplied collection's elements.
     */
    @Test
    public void containsAllFalse() {
        assertThat(SUB_LIST_1_3.containsAll(Arrays.asList(SOURCE_LOCATION_LIST.get(1), SOURCE_LOCATION_LIST.get(0))), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#containsAll(Collection)} returns <code>true</code> when the sublist contains
     * all of the supplied collection's elements.
     */
    @Test
    public void containsAllTrue() {
        assertThat(SUB_LIST_1_3.containsAll(Arrays.asList(SOURCE_LOCATION_LIST.get(1), SOURCE_LOCATION_LIST.get(2))), is(true));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#contains(Object)} returns <code>false</code> when the sublist doesn't contain
     * the specified element.
     */
    @Test
    public void containsFalse() {
        assertThat(SUB_LIST_1_3.contains(SOURCE_LOCATION_LIST.get(0)), is(false));
        assertThat(SUB_LIST_1_3.contains(SOURCE_LOCATION_LIST.get(3)), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#contains(Object)} returns <code>true</code> when the sublist contains the
     * specified element.
     */
    @Test
    public void containsTrue() {
        assertThat(SUB_LIST_1_3.contains(SOURCE_LOCATION_LIST.get(1)), is(true));
        assertThat(SUB_LIST_1_3.contains(SOURCE_LOCATION_LIST.get(2)), is(true));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#get(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>index</code> argument is greater than or equal to the sublist's size.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void getIndexTooHigh() {
        SUB_LIST_1_3.get(2);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#get(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>index</code> argument is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void getIndexTooLow() {
        SUB_LIST_1_3.get(-1);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#get(int)} returns the element at the specified index.
     */
    @Test
    public void getIndexValid() {
        assertThat(SUB_LIST_1_3.get(0), is(sameInstance(SOURCE_LOCATION_LIST.get(1))));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#indexOf(Object)} returns -1 for an object that is not an element of the
     * sublist.
     */
    @Test
    public void indexOfAbsent() {
        assertThat(SUB_LIST_1_3.indexOf(SOURCE_LOCATION_LIST.get(3)), is(-1));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#indexOf(Object)} returns the index of the first occurrence of the specified
     * element in the sublist.
     */
    @Test
    public void indexOfPresent() {
        assertThat(SUB_LIST_1_3.indexOf(SOURCE_LOCATION_LIST.get(2)), is(1));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#isEmpty()} returns <code>false</code> for a non-empty sublist.
     */
    @Test
    public void isEmptyFalse() {
        assertThat(SUB_LIST_1_3.isEmpty(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#isEmpty()} returns <code>true</code> for an empty sublist.
     */
    @Test
    public void isEmptyTrue() {
        assertThat(SUB_LIST_1_1.isEmpty(), is(true));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#lastIndexOf(Object)} returns -1 for an object that is not an element of the
     * sublist.
     */
    @Test
    public void lastIndexOfAbsent() {
        assertThat(SUB_LIST_1_3.lastIndexOf(SOURCE_LOCATION_LIST.get(3)), is(-1));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#lastIndexOf(Object)} returns the index of the last occurrence of the specified
     * element in the sublist.
     */
    @Test
    public void lastIndexOfPresent() {
        assertThat(SUB_LIST_1_3.lastIndexOf(SOURCE_LOCATION_LIST.get(2)), is(1));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#listIterator(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>index</code> argument is greater than or equal to the sublist's size.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void listIteratorIntIndexTooHigh() {
        SUB_LIST_1_3.listIterator(3);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#listIterator(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>index</code> argument is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void listIteratorIntIndexTooLow() {
        SUB_LIST_1_3.listIterator(-1);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#size()} returns the sublist's size.
     */
    @Test
    public void size() {
        assertThat(SUB_LIST_1_3.size(), is(2));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#size()} returns 0 for an empty sublist.
     */
    @Test
    public void sizeEmpty() {
        assertThat(SUB_LIST_1_1.size(), is(0));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#subList(int, int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>fromIndex</code> argument is greater than the sublist's size.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void subListFromIndexTooHigh() {
        SUB_LIST_1_3.subList(3, 3);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#subList(int, int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>fromIndex</code> argument is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void subListFromIndexTooLow() {
        SUB_LIST_1_3.subList(-1, 2);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#subList(int, int)} returns the original sublist when the
     * <code>fromIndex</code> argument is 0 and the <code>toIndex</code> argument is equal to the sublist's size.
     */
    @Test
    public void subListIdentity() {
        assertThat(SUB_LIST_1_3.subList(0, 2), is(sameInstance(SUB_LIST_1_3)));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#subList(int, int)} returns a new sublist when the arguments don't designate
     * the whole sublist.
     */
    @Test
    public void subListNotIdentity() {
        assertThat(SUB_LIST_1_3.subList(0, 1), is(not(sameInstance(SUB_LIST_1_3))));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#subList(int, int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>toIndex</code> argument is greater than the sublist's size.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void subListToIndexTooHigh() {
        SUB_LIST_1_3.subList(1, 3);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#subList(int, int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>fromIndex</code> argument is less than the <code>toIndex</code> argument.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void subListToIndexTooLow() {
        SUB_LIST_1_3.subList(1, 0);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#toArray()} returns an array that contains the sublist's elements.
     */
    @Test
    public void toArray() {
        final Object[] array = SUB_LIST_1_3.toArray();
        assertThat(array, is(arrayWithSize(3 - 1)));

        for (int i = 0; i < array.length; i++) {
            assertThat(array[i], is(sameInstance((Object) SOURCE_LOCATION_LIST.get(1 + i))));
        }
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#toArray()} returns an empty array for an empty sublist.
     */
    @Test
    public void toArrayEmpty() {
        final Object[] array = SUB_LIST_1_1.toArray();
        assertThat(array, is(arrayWithSize(0)));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#toArray(Object[])} returns the specified array filled with the sublist's
     * elements when the specified array has the same size as the sublist.
     */
    @Test
    public void toArrayObjectArrayLargeEnough() {
        final SourceLocation[] arrayArgument = new SourceLocation[2];
        final SourceLocation[] array = SUB_LIST_1_3.toArray(arrayArgument);
        assertThat(array, is(sameInstance(arrayArgument)));

        for (int i = 0; i < array.length; i++) {
            assertThat(array[i], is(sameInstance(SOURCE_LOCATION_LIST.get(1 + i))));
        }
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#toArray(Object[])} returns the specified array filled with the sublist's
     * elements, followed by a <code>null</code> elements when the specified array's length is greater than the sublist's size.
     */
    @Test
    public void toArrayObjectArrayLarger() {
        final SourceLocation[] arrayArgument = new SourceLocation[3];
        arrayArgument[2] = SOURCE_LOCATION_LIST.get(0); // set this array item to test that null is written to it
        final SourceLocation[] array = SUB_LIST_1_3.toArray(arrayArgument);
        assertThat(array, is(sameInstance(arrayArgument)));

        for (int i = 0; i < 2; i++) {
            assertThat(array[i], is(sameInstance(SOURCE_LOCATION_LIST.get(1 + i))));
        }

        assertThat(array[2], is(nullValue()));
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#toArray(Object[])} throws a {@link NullPointerException} when the
     * <code>array</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void toArrayObjectArrayNullArray() {
        SUB_LIST_1_3.toArray(null);
    }

    /**
     * Asserts that {@link SourceLocationList.SubList#toArray(Object[])} returns a new array filled with the sublist's elements when
     * the specified array's length is less than the list's size.
     */
    @Test
    public void toArrayObjectArrayTooSmall() {
        final SourceLocation[] array = SUB_LIST_1_3.toArray(new SourceLocation[1]);
        assertThat(array, is(arrayWithSize(2)));
        assertThat(array.getClass().getComponentType(), is((Object) SourceLocation.class));

        for (int i = 0; i < array.length; i++) {
            assertThat(array[i], is(sameInstance(SOURCE_LOCATION_LIST.get(1 + i))));
        }
    }

}
