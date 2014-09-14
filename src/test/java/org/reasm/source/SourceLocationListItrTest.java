package org.reasm.source;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.reasm.source.SourceLocationListTest.EMPTY_SOURCE_LOCATION_LIST;
import static org.reasm.source.SourceLocationListTest.SOURCE_LOCATION_LIST;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * Test class for {@link SourceLocationList.Itr}.
 *
 * @author Francis Gagné
 */
public class SourceLocationListItrTest {

    /**
     * Asserts that {@link SourceLocationList.Itr#add(SourceLocation)} throws an {@link UnsupportedOperationException}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void add() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator();
        iterator.add(SOURCE_LOCATION_LIST.get(1));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#hasNext()} returns <code>true</code> when there are still elements in the
     * iteration.
     */
    @Test
    public void hasNext() {
        final Iterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.iterator();
        assertThat(iterator.hasNext(), is(true));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#hasNext()} returns <code>false</code> when there are no elements in the list.
     */
    @Test
    public void hasNextEmpty() {
        final Iterator<SourceLocation> iterator = EMPTY_SOURCE_LOCATION_LIST.iterator();
        assertThat(iterator.hasNext(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#hasNext()} returns <code>false</code> when there are no elements in the sublist.
     */
    @Test
    public void hasNextEmptySubList() {
        final Iterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.subList(1, 1).iterator();
        assertThat(iterator.hasNext(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#hasNext()} returns <code>false</code> when there are no more elements in the
     * iteration.
     */
    @Test
    public void hasNextPositionedAtEnd() {
        final Iterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator(4);
        assertThat(iterator.hasNext(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#hasPrevious()} returns <code>false</code> when there are no more elements in the
     * iteration (in reverse order).
     */
    @Test
    public void hasPrevious() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator();
        assertThat(iterator.hasPrevious(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#hasPrevious()} returns <code>false</code> when there are no elements in the list.
     */
    @Test
    public void hasPreviousEmpty() {
        final ListIterator<SourceLocation> iterator = EMPTY_SOURCE_LOCATION_LIST.listIterator();
        assertThat(iterator.hasPrevious(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#hasPrevious()} returns <code>false</code> when there are no elements in the
     * sublist.
     */
    @Test
    public void hasPreviousEmptySubList() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.subList(1, 1).listIterator();
        assertThat(iterator.hasPrevious(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#hasPrevious()} returns <code>true</code> when there are still elements in the
     * iteration (in reverse order).
     */
    @Test
    public void hasPreviousPositionedAtEnd() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator(4);
        assertThat(iterator.hasPrevious(), is(true));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#next()} returns the next element in the iteration and advances the iterator's
     * position.
     */
    @Test
    public void next() {
        final Iterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.iterator();
        assertThat(iterator.next(), is(SOURCE_LOCATION_LIST.get(0)));
        assertThat(iterator.next(), is(SOURCE_LOCATION_LIST.get(1)));
        assertThat(iterator.next(), is(SOURCE_LOCATION_LIST.get(2)));
        assertThat(iterator.next(), is(SOURCE_LOCATION_LIST.get(3)));
        assertThat(iterator.hasNext(), is(false));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#nextIndex()} returns the index of the next element in the iteration.
     */
    @Test
    public void nextIndex() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator();
        assertThat(iterator.nextIndex(), is(0));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#nextIndex()} returns 0 when there are no elements in the list.
     */
    @Test
    public void nextIndexEmpty() {
        final ListIterator<SourceLocation> iterator = EMPTY_SOURCE_LOCATION_LIST.listIterator();
        assertThat(iterator.nextIndex(), is(0));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#nextIndex()} returns 0 when there are no elements in the sublist.
     */
    @Test
    public void nextIndexEmptySubList() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.subList(1, 1).listIterator();
        assertThat(iterator.nextIndex(), is(0));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#nextIndex()} returns the size of the list when there are no more elements in the
     * iteration.
     */
    @Test
    public void nextIndexPositionedAtEnd() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator(4);
        assertThat(iterator.nextIndex(), is(4));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#next()} throws a {@link NoSuchElementException} when the end of the iteration has
     * been reached.
     */
    @Test(expected = NoSuchElementException.class)
    public void nextPositionedAtEnd() {
        final Iterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator(4);
        iterator.next();
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#previous()} throws a {@link NoSuchElementException} when the end of the iteration
     * (in reverse order) has been reached.
     */
    @Test(expected = NoSuchElementException.class)
    public void previous() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator();
        iterator.previous();
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#previousIndex()} returns -1 when there are no more elements in the iteration (in
     * reverse order).
     */
    @Test
    public void previousIndex() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator();
        assertThat(iterator.previousIndex(), is(-1));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#previousIndex()} returns -1 when there are no elements in the list.
     */
    @Test
    public void previousIndexEmpty() {
        final ListIterator<SourceLocation> iterator = EMPTY_SOURCE_LOCATION_LIST.listIterator();
        assertThat(iterator.previousIndex(), is(-1));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#previousIndex()} returns -1 when there are no elements in the sublist.
     */
    @Test
    public void previousIndexEmptySubList() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.subList(1, 1).listIterator();
        assertThat(iterator.previousIndex(), is(-1));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#previousIndex()} returns the index of the previous element in the iteration.
     */
    @Test
    public void previousIndexPositionedAtEnd() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator(4);
        assertThat(iterator.previousIndex(), is(3));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#previous()} returns the previous element in the iteration and rewinds the
     * iterator's position.
     */
    @Test
    public void previousPositionedAtEnd() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator(4);
        assertThat(iterator.previous(), is(SOURCE_LOCATION_LIST.get(3)));
        assertThat(iterator.previous(), is(SOURCE_LOCATION_LIST.get(2)));
        assertThat(iterator.previous(), is(SOURCE_LOCATION_LIST.get(1)));
        assertThat(iterator.previous(), is(SOURCE_LOCATION_LIST.get(0)));
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#remove()} throws an {@link UnsupportedOperationException}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator();
        iterator.remove();
    }

    /**
     * Asserts that {@link SourceLocationList.Itr#set(SourceLocation)} throws an {@link UnsupportedOperationException}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void set() {
        final ListIterator<SourceLocation> iterator = SOURCE_LOCATION_LIST.listIterator();
        iterator.set(SOURCE_LOCATION_LIST.get(1));
    }

}
